package ca.stefanm.ibus.gui

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.window.v1.KeyStroke
import ca.stefanm.ibus.gui.debug.DeviceConfigurationViewerWindow
import ca.stefanm.ibus.gui.debug.KeyEventSimulator
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.gui.debug.DebugLaunchpad
import ca.stefanm.ibus.gui.debug.PaneManagerDebug
import ca.stefanm.ibus.gui.menu.MenuWindow
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalTime
class LoadingWindow @Inject constructor(
    private val deviceConfigurationViewerWindow: DeviceConfigurationViewerWindow,
    private val configurablePlatform: ConfigurablePlatform,
    private val keyEventSimulator: KeyEventSimulator,
    private val debugLaunchpad: DebugLaunchpad,
    private val paneManagerDebug: PaneManagerDebug,
    private val menuWindow: MenuWindow
) {

    fun show() = application {

        // Currently we use Swing's menu under the hood, so we need to set this property to change the look and feel of the menu on Windows/Linux
        System.setProperty("skiko.rendering.laf.global", "true")

        val windowState = rememberWindowState(
            size = WindowSize(800.dp, 468.dp)
        )
            Window(
                title = "BMW E39 Nav Loading",
                state = windowState
            ) {



                MenuBar {

                    Menu("Quit") {
                        Item("Quit",
                            onClick = { AppManager.exit() },
                            //shortcut = KeyStroke(Key.Q)
                        )
                    }

                    Menu("Platform") {
                        Item("Start Platform",
                            onClick = { configurablePlatform.run() },
                            //shortcut = KeyStroke(Key.S)
                        )
                        Item("Stop Platform",
                            onClick = { configurablePlatform.stop() },
                            //shortcut = KeyStroke(Key.E)
                        )
                        Item("Restart Platform",
                            onClick = { configurablePlatform.stop(); configurablePlatform.run() },
                            //shortcut = KeyStroke(Key.R)
                        )
                    }
                    Menu("Debug") {
                        Item("Key Event Simulator",
                            onClick = { keyEventSimulator.show() },
                            //shortcut = KeyStroke(Key.K)
                        )
                        Item("Debug Launchpad",
                            onClick = { debugLaunchpad.show() },
                            //shortcut = KeyStroke(Key.D)
                        )
                        Item("Pane Manager Debugger",
                            onClick = { paneManagerDebug.showPalette() },
                            //shortcut = KeyStroke(Key.P)
                        )
                    }
                    Menu("Configuration") {
                        Item("View Device Configuration",
                            onClick = { deviceConfigurationViewerWindow.show(configurablePlatform.currentConfiguration) }
                        )
                    }
                }

                Image(
                    bitmap = imageFromResource("bmw_navigation.png"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Black)
                        .clickable {
                            openMenuWindow()
                        }
                )
            }
        }

    private fun openMenuWindow() {
        menuWindow.openWindow(
            AppManager.focusedWindow?.x ?: 0,
            AppManager.focusedWindow?.y ?: 0
        )
    }



}