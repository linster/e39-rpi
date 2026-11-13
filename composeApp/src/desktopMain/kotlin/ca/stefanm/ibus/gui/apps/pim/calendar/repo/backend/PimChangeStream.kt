package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.ChangeNotice
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The PIM bridge's change notices (CalendarService.WatchChanges): one
 * stream shared by every repository flow, open while any is collected.
 * Reconnects (to the current settings) after errors; each connection starts
 * with `ready` (changes may have been missed: query again).
 */
@ApplicationScope
class PimChangeStream @Inject constructor(
    client: CalendarGrpcClient,
    backendChannel: PimBackendChannel,
    settingsRepository: PimBackendSettingsRepository,
    coroutineScope: CoroutineScope,
    logger: Logger,
) {
    companion object {
        const val TAG = "PimChangeStream"
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val changes: SharedFlow<ChangeNotice> = settingsRepository.settings
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
}
