package ca.stefanm.ibus.gui.map.repo

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
 * The gRPC connection to the maps backend, for the clients in this package.
 *
 * One channel (HTTP/2 connection) for all services, rebuilt when
 * [MapsBackendSettingsRepository.settings] changes. The backend has no TLS:
 * it's on the Pi itself or on the laptop during development.
 */
@ApplicationScope
class MapsBackendChannel @Inject constructor(
    private val settingsRepository: MapsBackendSettingsRepository,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "MapsBackendChannel"

        /** Deadline for each call: answers come from SQLite on the same device. */
        const val CALL_TIMEOUT_SECONDS = 5L
    }

    private var current: Pair<MapsBackendSettings, ManagedChannel>? = null

    @Synchronized
    fun channel(): ManagedChannel {
        val settings = settingsRepository.settings.value
        current?.let { (forSettings, channel) ->
            if (forSettings == settings) return channel
            channel.shutdown()
        }
        logger.d(TAG, "Connecting to the maps backend at ${settings.target}")
        val channel = ManagedChannelBuilder.forAddress(settings.host, settings.port)
            .usePlaintext()
            // Drop the connection when unused; it's re-established on the next call.
            .idleTimeout(2, TimeUnit.MINUTES)
            .build()
        current = settings to channel
        return channel
    }

    /**
     * After a failed connection gRPC waits (exponential backoff, up to 2
     * minutes) before trying again, failing calls fast meanwhile. Call this
     * before retrying so a backend that came up is reached right away.
     */
    @Synchronized
    fun resetConnectBackoff() {
        current?.second?.resetConnectBackoff()
    }
}

/** A failed call to the maps backend, by what the caller can do about it. */
sealed class MapsBackendException(message: String, cause: Throwable?) : Exception(message, cause) {
    /** Not reachable (not running, wrong host/port) or too slow. */
    class Unavailable(message: String, cause: Throwable?) : MapsBackendException(message, cause)
    class NotFound(message: String, cause: Throwable?) : MapsBackendException(message, cause)
    /** The backend rejected the request; [message] says why (e.g. "a POI needs a name"). */
    class InvalidArgument(message: String, cause: Throwable?) : MapsBackendException(message, cause)
    /** The backend doesn't do this yet (e.g. routing before Valhalla). */
    class Unimplemented(message: String, cause: Throwable?) : MapsBackendException(message, cause)
    class Failed(message: String, cause: Throwable?) : MapsBackendException(message, cause)

    companion object {
        fun from(e: Throwable): MapsBackendException {
            val status = when (e) {
                is StatusException -> e.status
                is StatusRuntimeException -> e.status
                else -> return e as? MapsBackendException ?: Failed(e.message ?: e.toString(), e)
            }
            val message = status.description ?: status.code.name
            return when (status.code) {
                // gRPC says "io exception"; the root cause says what happened
                // (e.g. "Connection refused").
                Status.Code.UNAVAILABLE -> Unavailable(rootCauseMessage(e) ?: message, e)
                Status.Code.DEADLINE_EXCEEDED -> Unavailable("No answer in time", e)
                Status.Code.NOT_FOUND -> NotFound(message, e)
                Status.Code.INVALID_ARGUMENT -> InvalidArgument(message, e)
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

/** Run a gRPC call, turning gRPC errors into [MapsBackendException]. */
internal suspend fun <T> backendCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: StatusException) {
        throw MapsBackendException.from(e)
    } catch (e: StatusRuntimeException) {
        throw MapsBackendException.from(e)
    }
