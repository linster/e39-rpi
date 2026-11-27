package ca.stefanm.ibus.gui.crash

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DefaultWindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Rectangle
import javax.inject.Inject
import javax.inject.Provider
import kotlin.system.exitProcess

/**
 * What happens when a window's content throws (#78).
 *
 * With the error screen on ([E39Config.WindowManagerConfig.errorScreen], or on
 * the Pi), a crash closes the HMI window and shows [crash] on the error screen
 * ([ErrorScreenWindow]) instead of Compose's error dialog, which the knob
 * can't close. After [countdownSeconds], [finish] goes to the root screen and
 * opens the HMI window again. A crash within [REPEAT_MS] of that reopening, or
 * a crash of the loading window, exits instead (code 1), so systemd starts a
 * fresh HMI (e39rpi-hmi.service: Restart=on-failure).
 *
 * With it off (a laptop), Compose's error dialog shows, as before.
 */
@ApplicationScope
class HmiCrashHandler internal constructor(
    private val navigator: Provider<Navigator>,
    private val logger: Logger,
    private val enabled: () -> Boolean,
    private val countdown: () -> Int,
    private val clock: () -> Long,
    private val exit: (Int) -> Unit,
) {
    @Inject constructor(
        navigator: Provider<Navigator>,
        logger: Logger,
        configurationStorage: ConfigurationStorage,
    ) : this(
        navigator, logger,
        enabled = {
            configurationStorage.config[E39Config.CarPlatformConfigSpec._isPi] ||
                    configurationStorage.config[E39Config.WindowManagerConfig.errorScreen]
        },
        countdown = { configurationStorage.config[E39Config.WindowManagerConfig.errorScreenSeconds] },
        clock = System::currentTimeMillis,
        exit = { exitProcess(it) },
    )

    companion object {
        const val TAG = "HmiCrashHandler"
        /** A crash this soon after the HMI window reopened exits instead. */
        const val REPEAT_MS = 120_000L
        const val EXIT_CODE = 1
    }

    enum class CrashedWindow { HMI, LOADING }

    enum class Then {
        /** Go to the root screen and open the HMI window again. */
        REOPEN_HMI,
        /** Exit, for systemd to start the HMI again. */
        EXIT,
    }

    data class Crash(
        val window: CrashedWindow,
        val throwable: Throwable,
        /** Where the crashed window was, in AWT (dp) units: the error screen goes there. */
        val bounds: Rectangle,
        val then: Then,
    )

    private val _crash = MutableStateFlow<Crash?>(null)
    /** The crash the error screen is showing, or null. */
    val crash: StateFlow<Crash?> = _crash.asStateFlow()

    private val _crashRequested = MutableStateFlow(false)
    /** The HMI Navigator debug window's "Crash the HMI": the HMI window throws while this is set. */
    val crashRequested: StateFlow<Boolean> = _crashRequested.asStateFlow()

    private var reopenedAt: Long? = null

    val countdownSeconds: Int get() = countdown().coerceAtLeast(1)

    /** For a window's LocalWindowExceptionHandlerFactory. */
    @OptIn(ExperimentalComposeUiApi::class)
    fun exceptionHandlerFactory(window: CrashedWindow): WindowExceptionHandlerFactory =
        if (!enabled()) {
            DefaultWindowExceptionHandlerFactory
        } else {
            object : WindowExceptionHandlerFactory {
                override fun exceptionHandler(awtWindow: java.awt.Window) = object : WindowExceptionHandler {
                    override fun onException(throwable: Throwable) = onCrash(window, throwable, awtWindow.bounds)
                }
            }
        }

    /** The error screen itself threw: there's nothing left to show, so exit. */
    @OptIn(ExperimentalComposeUiApi::class)
    val errorScreenExceptionHandlerFactory = object : WindowExceptionHandlerFactory {
        override fun exceptionHandler(window: java.awt.Window) = object : WindowExceptionHandler {
            override fun onException(throwable: Throwable) {
                logger.e(TAG, "The error screen crashed; exiting", throwable)
                exit(EXIT_CODE)
            }
        }
    }

    internal fun onCrash(window: CrashedWindow, throwable: Throwable, bounds: Rectangle) {
        val shown = _crash.value
        if (shown?.window == window) {
            // The same window throwing again before it's gone (its next frame).
            logger.d(TAG, "The $window window threw again: $throwable")
            return
        }
        val now = clock()
        val repeated = reopenedAt?.let { now - it < REPEAT_MS } == true
        val then = if (window == CrashedWindow.HMI && !repeated) Then.REOPEN_HMI else Then.EXIT
        logger.e(TAG, "The $window window crashed${if (repeated) " again" else ""}; then $then", throwable)

        _crash.value = if (shown == null) {
            Crash(window, throwable, bounds, then)
        } else {
            // The other window crashing too, while the error screen shows: start afresh.
            shown.copy(then = Then.EXIT)
        }
    }

    /** At the end of the error screen's countdown. */
    fun finish() {
        val crash = _crash.value ?: return
        when (crash.then) {
            Then.EXIT -> {
                logger.e(TAG, "Exiting ($EXIT_CODE) for the HMI to be started again")
                exit(EXIT_CODE)
            }
            Then.REOPEN_HMI -> {
                navigator.get().navigateToRoot()
                reopenedAt = clock()
                _crash.value = null
                logger.i(TAG, "Reopened the HMI window on the root screen")
            }
        }
    }

    fun requestCrash() {
        _crashRequested.value = true
    }

    /** Called by the HMI window's content: throws once after [requestCrash]. */
    fun throwIfRequested() {
        if (_crashRequested.value) {
            _crashRequested.value = false
            throw IllegalStateException("Crash the HMI (HMI Navigator Debug)")
        }
    }
}
