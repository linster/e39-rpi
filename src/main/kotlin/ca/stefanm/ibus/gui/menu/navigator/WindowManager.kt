package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.MenuWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Provider


@ApplicationScope
class WindowManager @Inject constructor(
    private val loadingWindow: Provider<LoadingWindow>,
    private val hmiWindow : MenuWindow
) {

    //Debugging options
    private val HMI_SHIFT_RIGHT = true //Don't overlap the HMI window fully

    companion object {
        val DEFAULT_SIZE = WindowSize(800.dp, 468.dp)
    }

    interface E39Window {
        val title : String
        val size : WindowSize

        val tag : Any

        val defaultPosition : DefaultPosition

        enum class DefaultPosition {
            OVER_MAIN,
            ANYWHERE,
            CENTER
        }

        fun content() : @Composable WindowScope.() -> Unit
    }

    data class HmiWindowState(
        val isHmiWindowOpen : Boolean = false
    )

    data class SystemWindowState(
        val openDebugWindows : SnapshotStateList<E39Window> = mutableStateListOf()
    ) {

        fun findWindowByTag(tag : Any) : E39Window? {
            return openDebugWindows.find { it.tag == tag }
        }

        fun closeWindow(e39Window: E39Window) {
            openDebugWindows.remove(e39Window)
        }

        fun openWindow(e39Window: E39Window) {
            with (openDebugWindows) {
                if (!contains(e39Window)) {
                    add(e39Window)
                }
            }
        }
    }

    private val hmiWindowState = MutableStateFlow(HmiWindowState())
    private val windowManagerState = MutableStateFlow(SystemWindowState())

    //Invoke from Main
    fun runApplication() = application {
        // Currently we use Swing's menu under the hood, so we need to set this property to change the look and feel of the menu on Windows/Linux
        System.setProperty("skiko.rendering.laf.global", "true")

        val systemWindowState = produceState(SystemWindowState()) {
            windowManagerState.collect { value = it }
        }

        val hmiWindowState = produceState(HmiWindowState()) {
            hmiWindowState.collect { value = it }
        }


        val mainWindowState = rememberWindowState(
            isOpen = true,
            size = DEFAULT_SIZE
        )

        Window(
            title = "BMW E39 Nav Loading",
            state = mainWindowState
        ) {
            loadingWindow.get().contents()()
        }


        if (hmiWindowState.value.isHmiWindowOpen) {
            Window(
                state = rememberWindowState(
                    position = mainWindowState.position.let {
                        if (HMI_SHIFT_RIGHT) {
                            WindowPosition(it.x + 900.dp, it.y)
                        } else {
                            it
                        }
                    },
                    size = DEFAULT_SIZE
                ),
                title = "E39 Menu",
                undecorated = false,
                resizable = false,
                alwaysOnTop = true,
                enabled = true,
                onCloseRequest = {
                    closeHmiMainWindow()
                }
            ) {
                    hmiWindow.content()()
            }
        }

        for (window in systemWindowState.value.openDebugWindows) {
            key(window) {
                Window(
                    title = window.title,
                    state = rememberWindowState(
                        size = window.size
                    ),
                    onCloseRequest = {
                        systemWindowState.value.closeWindow(window)
                    }
                ) {
                    window.content().invoke(this)
                }
            }
        }
    }

    //TODO LOLOL because the debug window opening updates the WindowManagerState.value
    //TODO under the hood there's a recomposition that causes the kob listener to break.
    fun openHmiMainWindow() {
        hmiWindowState.value = hmiWindowState.value.copy(isHmiWindowOpen = true)
    }

    fun closeHmiMainWindow() {
        hmiWindowState.value = hmiWindowState.value.copy(isHmiWindowOpen = false)
    }

    fun openDebugWindow(debugWindow: E39Window) {
        windowManagerState.value.openWindow(debugWindow)
    }
}