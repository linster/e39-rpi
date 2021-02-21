package ca.stefanm.ibus.gui

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import ca.stefanm.ibus.car.platform.BackgroundPlatform
import javax.inject.Inject

class LoadingWindow @Inject constructor(
    private val platform: BackgroundPlatform,
    private val deviceConfigurationViewerWindow: DeviceConfigurationViewerWindow,
    private val keyEventSimulator: KeyEventSimulator
) {
    fun show() = Window(
        title = "BMW E39 Nav",
        size = IntSize(800, 600),
//        undecorated = true,
        menuBar = MenuBar(
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
                    onClick = { platform.run() },
                    shortcut = KeyStroke(Key.S)
                ),
                MenuItem(
                    name = "Stop Platform",
                    onClick = { platform.stop() },
                    shortcut = KeyStroke(Key.E)
                ),
                MenuItem(
                    name = "Restart Platform",
                    onClick = { platform.stop(); platform.run() },
                    shortcut = KeyStroke(Key.R)
                ),
                MenuItem(
                    name = "Key Event Simulator",
                    onClick = { keyEventSimulator.show() },
                    shortcut = KeyStroke(Key.K)
                )
            ),
            Menu(
                name = "Configuration",
                MenuItem(
                    name = "View Device Configuration",
                    onClick = { deviceConfigurationViewerWindow.show() }
                )
            )
        )
    ) {
        Image(
            bitmap = imageFromResource("bmw_navigation.png"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
        )
    }


}