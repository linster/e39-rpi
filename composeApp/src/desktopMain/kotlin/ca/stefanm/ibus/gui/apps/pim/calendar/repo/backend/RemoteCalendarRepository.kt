package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.AkonadiState
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.e39pim.v1.event
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarEventRef
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarInfo
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventScope
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventTimes
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
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
 * changed ([PimChangeStream], shared by the whole app), and retries
 * every [RETRY_INTERVAL] while the bridge is unreachable, keeping the last
 * list meanwhile. [connectionState] says how the last call went.
 */
@ApplicationScope
class RemoteCalendarRepository @Inject constructor(
    private val client: CalendarGrpcClient,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    private val changeStream: PimChangeStream,
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
     * Occurrences overlapping [[start], [end]), in the bridge's order (by
     * start). Emits whenever they may have changed; errors don't end the
     * flow (the last list stays, see [connectionState]).
     */
    fun events(start: Instant, end: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Flow<List<AgendaCalendarEventData>> =
        channelFlow {
            val reloads = Channel<Unit>(Channel.CONFLATED)
            launch { changeStream.changes.collect { if (it.affects(start, end)) reloads.send(Unit) } }
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

    /** All calendars (with events and/or to-dos), by account then name. */
    suspend fun calendars(): Result<List<CalendarInfo>> = result {
        client.listCalendars().map { it.toCalendarInfo() }
    }

    /**
     * Where new events go when the caller doesn't choose: the first writable
     * calendar with events. Failure (FailedPrecondition) if there's none.
     */
    suspend fun defaultEventCalendar(): Result<CalendarInfo> = calendars().mapCatching { list ->
        list.firstOrNull { it.hasEvents && !it.readOnly }
            ?: throw PimBackendException.FailedPrecondition("No writable calendar (add an account first)", null)
    }

    /** Creates an event in [calendarId] (null: [defaultEventCalendar]). */
    suspend fun createEvent(
        summary: String,
        times: EventTimes,
        location: String = "",
        calendarId: String? = null,
    ): Result<AgendaCalendarEventData> = result {
        val calendar = calendarId ?: defaultEventCalendar().getOrThrow().id
        val created = client.createEvent(calendar, event {
            this.summary = summary
            this.location = location
            setTimes(times)
        })
        logger.i(TAG, "Created event ${created.eventId} '${created.summary}'")
        created.toAgendaEvent(TimeZone.currentSystemDefault())
    }

    /**
     * Changes the given fields (null: unchanged) of the event [ref] shows.
     * [scope] matters for repeating events: THIS_OCCURRENCE changes only
     * [ref]'s occurrence; WHOLE_SERIES the series (then [times] can't be
     * given: an occurrence's times don't say where the series should be).
     * Failure with [PimBackendException.Conflict] if it changed meanwhile.
     */
    suspend fun updateEvent(
        ref: CalendarEventRef,
        scope: EventScope = EventScope.THIS_OCCURRENCE,
        summary: String? = null,
        location: String? = null,
        description: String? = null,
        times: EventTimes? = null,
    ): Result<AgendaCalendarEventData> = result {
        val wholeSeries = scope == EventScope.WHOLE_SERIES && ref.recurring
        require(!(wholeSeries && times != null)) { "Times can only be changed for one occurrence of a repeating event" }
        val paths = buildList {
            if (summary != null) add("summary")
            if (location != null) add("location")
            if (description != null) add("description")
            if (times != null) addAll(EVENT_TIME_PATHS)
        }
        require(paths.isNotEmpty()) { "Nothing to change" }
        val updated = client.updateEvent(event {
            eventId = ref.eventId
            if (!wholeSeries && ref.recurrenceId != null) recurrenceId = ref.recurrenceId.toTimestamp()
            revision = if (wholeSeries) seriesRevision(ref) else ref.revision
            if (summary != null) this.summary = summary
            if (location != null) this.location = location
            if (description != null) this.description = description
            if (times != null) setTimes(times)
        }, paths)
        logger.i(TAG, "Updated event ${ref.eventId} ($scope): $paths")
        updated.toAgendaEvent(TimeZone.currentSystemDefault())
    }

    /** Deletes [ref]'s occurrence (THIS_OCCURRENCE) or the whole event/series. */
    suspend fun deleteEvent(ref: CalendarEventRef, scope: EventScope = EventScope.THIS_OCCURRENCE): Result<Unit> = result {
        val wholeSeries = scope == EventScope.WHOLE_SERIES || !ref.recurring
        client.deleteEvent(
            eventId = ref.eventId,
            recurrenceId = if (wholeSeries) null else ref.recurrenceId,
            revision = if (wholeSeries && ref.recurring) seriesRevision(ref) else ref.revision,
        )
        logger.i(TAG, "Deleted event ${ref.eventId} ($scope)")
    }

    /**
     * The series' own revision: an occurrence carries its exception's when
     * it has one.
     */
    private suspend fun seriesRevision(ref: CalendarEventRef): Long =
        client.getEvent(ref.eventId, recurrenceId = null, zone = TimeZone.currentSystemDefault()).revision

    /** Runs a bridge call, recording the connection state; errors as [PimBackendException] failures. */
    private suspend fun <T> result(block: suspend () -> T): Result<T> {
        val target = settingsRepository.settings.value.target
        return try {
            val value = block()
            _connectionState.value = ConnectionState.Connected(target)
            Result.success(value)
        } catch (e: PimBackendException) {
            if (e is PimBackendException.Unavailable) {
                _connectionState.value = ConnectionState.Unavailable(target, e.message ?: e.toString())
            }
            logger.w(TAG, "PIM bridge call failed: ${e.message}")
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

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
