package ca.stefanm.ibus.gui

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import ca.stefanm.ibus.gui.debug.DeviceConfigurationViewerWindow
import ca.stefanm.ibus.gui.debug.KeyEventSimulator
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.gui.debug.DebugLaunchpad
import ca.stefanm.ibus.gui.debug.PaneManagerDebug
import ca.stefanm.ibus.gui.menu.MenuWindow
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LoadingWindow @Inject constructor(
    private val deviceConfigurationViewerWindow: DeviceConfigurationViewerWindow,
    private val configurablePlatform: ConfigurablePlatform,
    private val keyEventSimulator: KeyEventSimulator,
    private val debugLaunchpad: DebugLaunchpad,
    private val paneManagerDebug: PaneManagerDebug,
    private val menuWindow: MenuWindow
) {

    fun show() = Window(
        title = "BMW E39 Nav Loading",
        size = IntSize(800, 468),
        menuBar = menuBar
    ) {
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

    private fun openMenuWindow() {
        menuWindow.show(
            AppManager.focusedWindow?.x ?: 0,
            AppManager.focusedWindow?.y ?: 0
        )
    }

    private val menuBar = MenuBar(
        Menu(
            name = "Quit",
            MenuItem(
                name = "Quit",
                onClick = { AppManager.exit() },
                shortcut = KeyStroke(Key.Q)
            )
        ),
        Menu(
            name = "Platform",
            MenuItem(
                name = "Start Platform",
                onClick = { configurablePlatform.run() },
                shortcut = KeyStroke(Key.S)
            ),
            MenuItem(
                name = "Stop Platform",
                onClick = { configurablePlatform.stop() },
                shortcut = KeyStroke(Key.E)
            ),
            MenuItem(
                name = "Restart Platform",
                onClick = { configurablePlatform.stop(); configurablePlatform.run() },
                shortcut = KeyStroke(Key.R)
            ),
        ),
        Menu(
            name = "Debug",
            MenuItem(
                name = "Key Event Simulator",
                onClick = { keyEventSimulator.show() },
                shortcut = KeyStroke(Key.K)
            ),
            MenuItem(
                name = "Debug Launchpad",
                onClick = { debugLaunchpad.show() },
                shortcut = KeyStroke(Key.D)
            ),
            MenuItem(
                name = "Pane Manager Debugger",
                onClick = { paneManagerDebug.showPalette() },
                shortcut = KeyStroke(Key.P)
            )
        ),
        Menu(
            name = "Configuration",
            MenuItem(
                name = "View Device Configuration",
                onClick = { deviceConfigurationViewerWindow.show(configurablePlatform.currentConfiguration) }
            )
        )
    )


}