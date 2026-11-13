package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.AkonadiState
import ca.stefanm.e39pim.v1.ChangeNotice
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Calendar events from the PIM bridge (e39-rpi-pim, Akonadi behind it),
 * reached through [CalendarGrpcClient]: the HMI keeps no calendar data of
 * its own.
 *
 * [events] re-queries whenever the bridge says something in its range
 * changed (one shared WatchChanges stream for the whole app), and retries
 * every [RETRY_INTERVAL] while the bridge is unreachable, keeping the last
 * list meanwhile. [connectionState] says how the last call went.
 */
@ApplicationScope
class RemoteCalendarRepository @Inject constructor(
    private val client: CalendarGrpcClient,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    coroutineScope: CoroutineScope,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "RemoteCalendarRepository"
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    sealed interface ConnectionState {
        data object Connecting : ConnectionState
        data class Connected(val target: String) : ConnectionState
        data class Unavailable(val target: String, val reason: String) : ConnectionState
    }

    /** What the bridge says about itself and its calendars, for the setup screen. */
    data class BridgeSummary(
        val akonadiState: String,
        val brokenReason: String,
        val bridgeVersion: String,
        val akonadiVersion: String,
        val calendars: List<String>,
    ) {
        val akonadiRunning: Boolean get() = akonadiState == AkonadiState.AKONADI_STATE_RUNNING.name
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /**
     * The bridge's change notices, from one stream shared by every [events]
     * flow, open while any is collected. Reconnects (to the current
     * settings) after errors; each connection starts with `ready`.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val changes: SharedFlow<ChangeNotice> = settingsRepository.settings
        .flatMapLatest {
            flow {
                while (true) {
                    try {
                        client.watchChanges().collect { emit(it) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.d(TAG, "Change stream ended: ${e.message}")
                    }
                    delay(RETRY_INTERVAL)
                    backendChannel.resetConnectBackoff()
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000))

    /**
     * Occurrences overlapping [[start], [end]), in the bridge's order (by
     * start). Emits whenever they may have changed; errors don't end the
     * flow (the last list stays, see [connectionState]).
     */
    fun events(start: Instant, end: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Flow<List<AgendaCalendarEventData>> =
        channelFlow {
            val reloads = Channel<Unit>(Channel.CONFLATED)
            launch { changes.collect { if (it.affects(start, end)) reloads.send(Unit) } }
            launch { settingsRepository.settings.drop(1).collect { reloads.send(Unit) } }
            reloads.send(Unit)

            var retry: Job? = null
            for (reload in reloads) {
                retry?.cancel()
                val target = settingsRepository.settings.value.target
                try {
                    send(client.listEvents(start, end, zone).map { it.toAgendaEvent(zone) })
                    _connectionState.value = ConnectionState.Connected(target)
                } catch (e: PimBackendException) {
                    logger.w(TAG, "Events $start..$end from $target: ${e.message}")
                    _connectionState.value = ConnectionState.Unavailable(target, e.message ?: e.toString())
                    retry = launch {
                        delay(RETRY_INTERVAL)
                        backendChannel.resetConnectBackoff()
                        reloads.send(Unit)
                    }
                }
            }
        }.flowOn(Dispatchers.IO)

    /** Asks the bridge for its status and calendars (setup screen, "Test Connection"). */
    suspend fun checkConnection(): Result<BridgeSummary> {
        val target = settingsRepository.settings.value.target
        backendChannel.resetConnectBackoff()
        return try {
            val status = client.getStatus()
            val calendars = if (status.akonadiState == AkonadiState.AKONADI_STATE_RUNNING) {
                client.listCalendars().map { "${it.name} (${it.accountName})" }
            } else {
                emptyList()
            }
            _connectionState.value = ConnectionState.Connected(target)
            Result.success(
                BridgeSummary(
                    akonadiState = status.akonadiState.name,
                    brokenReason = status.brokenReason,
                    bridgeVersion = status.bridgeVersion,
                    akonadiVersion = status.akonadiVersion,
                    calendars = calendars,
                )
            )
        } catch (e: PimBackendException) {
            _connectionState.value = ConnectionState.Unavailable(target, e.message ?: e.toString())
            Result.failure(e)
        }
    }
}
