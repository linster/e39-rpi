package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.map.MapDebug
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject


class DebugLaunchpad @Inject constructor(

    private val windowManager: WindowManager,

    private val mapDebug: MapDebug,
    private val menuDebug: MenuDebug,
    private val keyEventSimulator: KeyEventSimulator,
    private val paneManagerDebug: PaneManagerDebug,
    private val serviceStatusViewer: ServiceStatusViewer,
    private val pairingDebug: PairingDebug,
    private val notificationSpammerDebug: NotificationSpammerDebug,
    private val hmiNavigatorDebugWindow: HmiNavigatorDebugWindow
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Debug Launchpad"
    override val size = WindowSize(300.dp, 500.dp)
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
            Button(
                onClick = {
                    windowManager.openDebugWindow(keyEventSimulator)
                }
            ) {
                Text("Key Event Simulator")
            }
            Button(onClick = { windowManager.openDebugWindow(mapDebug) }) {
                Text("Map Debug")
            }
            Button(onClick = { windowManager.openDebugWindow(menuDebug)}) {
                Text("Menu Debug")
            }
            Button(onClick = { windowManager.openDebugWindow(paneManagerDebug) }) {
                Text("Pane Manager Debug")
            }
            Button(onClick = {windowManager.openDebugWindow(serviceStatusViewer) }) {
                Text("Service Status Viewer")
            }
            Button(onClick = { pairingDebug.show()}) {
                Text("Pairing Debug")
            }
            Button(onClick = { windowManager.openDebugWindow(notificationSpammerDebug)}) {
                Text("Notification Spammer Debug")
            }
            Button(onClick = { windowManager.openDebugWindow(hmiNavigatorDebugWindow) }) {
                Text("HMI Navigator Debug")
            }
        }
    }
}