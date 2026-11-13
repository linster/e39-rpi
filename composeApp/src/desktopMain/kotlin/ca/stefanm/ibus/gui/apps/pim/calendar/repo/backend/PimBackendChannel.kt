package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The gRPC connection to the PIM bridge (e39-rpi-pim), for the clients in
 * this package.
 *
 * One channel (HTTP/2 connection) for all services, rebuilt when
 * [PimBackendSettingsRepository.settings] changes. No TLS: the bridge
 * listens on localhost, on the Pi or on the laptop during development.
 */
@ApplicationScope
class PimBackendChannel @Inject constructor(
    private val settingsRepository: PimBackendSettingsRepository,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "PimBackendChannel"

        /**
         * Deadline for each call. The first query of a calendar loads it
         * from Akonadi (SQLite on the SD card), so allow more than the maps
         * backend's 5 s.
         */
        const val CALL_TIMEOUT_SECONDS = 15L
    }

    private var current: Pair<PimBackendSettings, ManagedChannel>? = null

    @Synchronized
    fun channel(): ManagedChannel {
        val settings = settingsRepository.settings.value
        current?.let { (forSettings, channel) ->
            if (forSettings == settings) return channel
            channel.shutdown()
        }
        logger.d(TAG, "Connecting to the PIM bridge at ${settings.target}")
        val channel = ManagedChannelBuilder.forAddress(settings.host, settings.port)
            .usePlaintext()
            // Change streams (WatchChanges) stay open; ping so a bridge that
            // went away is noticed.
            .keepAliveTime(1, TimeUnit.MINUTES)
            .build()
        current = settings to channel
        return channel
    }

    /**
     * After a failed connection gRPC waits (exponential backoff, up to 2
     * minutes) before trying again, failing calls fast meanwhile. Call this
     * before retrying so a bridge that came up is reached right away.
     */
    @Synchronized
    fun resetConnectBackoff() {
        current?.second?.resetConnectBackoff()
    }
}

/**
 * A failed call to the PIM bridge, by what the caller can do about it (see
 * e39pim.v1 common.proto for the codes).
 */
sealed class PimBackendException(message: String, cause: Throwable?) : Exception(message, cause) {
    /** The bridge isn't reachable (not running, wrong host/port), is too slow, or Akonadi isn't running yet. */
    class Unavailable(message: String, cause: Throwable?) : PimBackendException(message, cause)
    class NotFound(message: String, cause: Throwable?) : PimBackendException(message, cause)
    /** The bridge rejected the request; [message] says why. */
    class InvalidArgument(message: String, cause: Throwable?) : PimBackendException(message, cause)
    /** E.g. the calendar is read-only. */
    class FailedPrecondition(message: String, cause: Throwable?) : PimBackendException(message, cause)
    /** The event changed meanwhile (e.g. a sync came in): reload and try again. */
    class Conflict(message: String, cause: Throwable?) : PimBackendException(message, cause)
    /** The bridge doesn't do this yet. */
    class Unimplemented(message: String, cause: Throwable?) : PimBackendException(message, cause)
    class Failed(message: String, cause: Throwable?) : PimBackendException(message, cause)

    companion object {
        fun from(e: Throwable): PimBackendException {
            val status = when (e) {
                is StatusException -> e.status
                is StatusRuntimeException -> e.status
                else -> return e as? PimBackendException ?: Failed(e.message ?: e.toString(), e)
            }
            val message = status.description ?: status.code.name
            return when (status.code) {
                // For connection failures gRPC says "io exception"; the root
                // cause says what happened (e.g. "Connection refused").
                Status.Code.UNAVAILABLE -> Unavailable(rootCauseMessage(e) ?: message, e)
                Status.Code.DEADLINE_EXCEEDED -> Unavailable("No answer in time", e)
                Status.Code.NOT_FOUND -> NotFound(message, e)
                Status.Code.INVALID_ARGUMENT -> InvalidArgument(message, e)
                Status.Code.FAILED_PRECONDITION -> FailedPrecondition(message, e)
                Status.Code.ABORTED -> Conflict(message, e)
                Status.Code.UNIMPLEMENTED -> Unimplemented(message, e)
                else -> Failed("${status.code}: $message", e)
            }
        }

        private fun rootCauseMessage(e: Throwable): String? {
            var cause: Throwable = e
            while (cause.cause != null && cause.cause !== cause) cause = cause.cause!!
            return cause.message?.substringAfterLast(": ")?.takeIf { cause !== e }
        }
    }
}

/** Run a gRPC call, turning gRPC errors into [PimBackendException]. */
internal suspend fun <T> pimCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: StatusException) {
        throw PimBackendException.from(e)
    } catch (e: StatusRuntimeException) {
        throw PimBackendException.from(e)
    }
