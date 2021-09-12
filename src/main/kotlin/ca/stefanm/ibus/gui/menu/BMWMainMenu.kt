package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class BMWMainMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = BMWMainMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Menu")

            val nwItems = listOf(
                TextMenuItem(
                    title = "GPS-Navigation",
                    onClicked = { navigationNodeTraverser.goBack() }
                )
            )
            val neItems = listOf(
                TextMenuItem(
                    title = "Telephone Dialer",
                    onClicked = {}
                ),
                TextMenuItem(
                    title = "Chat Notifications",
                    onClicked = {}
                ),
            )

            val swItems = listOf(
                TextMenuItem(
                    title = "Bluetooth Pairing",
                    onClicked = { navigationNodeTraverser.navigateToNode(BluetoothPairingMenu::class.java) }
                ),
                TextMenuItem(
                    title = "Settings",
                    onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiRoot::class.java) }
                ),
                TextMenuItem(
                    title = "Debug",
                    labelColor = Color.Red,
                    onClicked = {}
                )
            )
            val seItems = listOf(
                TextMenuItem(
                    title = "Back to BMW",
                    labelColor = Color.Red,
                    onClicked = {}
                )
            )

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromCorners(
                    nw = nwItems,
                    ne = neItems,
                    sw = swItems,
                    se = seItems
                )
            }
        }
    }
}