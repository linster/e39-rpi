package ca.stefanm.ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ApplicationScope
@AutoDiscover
class ComposeDebugMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {
    override val thisClass = ComposeDebugMenu::class.java

    override fun provideMainContent(): @Composable (incoming : Navigator.IncomingResult?) -> Unit = {

            val enteredText = remember { mutableStateOf("Initial") }


            Column {
                Text("Hello Main Menu")
                Button(onClick = {
                    navigationNodeTraverser.navigateToNode(BluetoothPairingMenu::class.java)
                }) { Text("Go to bt")}

                Button(
                    onClick = {}
                ) { Text("Enter text")}
                Text(text = "Entered text: ${enteredText.value}")


                Button(onClick = {
                    modalMenuService.showModalMenu(
                        dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                            menuTopLeft = IntOffset(400, 100),
                            menuWidth = 280
                        ).toNormalModalMenuDimensions(),
                        menuData = ModalMenu(
                            chipOrientation = ItemChipOrientation.E,
                            onOpen = {},
                            onClose = {},
                            items = (0..4).map {
                                ModalMenu.ModalMenuItem(
                                    title = "Item: $it",
                                    isSelectable = it != 3,
                                    onClicked = {
                                        notificationHub.postNotificationBackground(
                                            Notification(
                                                Notification.NotificationImage.ALERT_CIRCLE,
                                                topText = "Modal click $it"
                                            )
                                        )
                                    }
                                )
                            }
                        )
                    )
                }) { Text("Show Modal")}

                Button(onClick = {
                    navigationNodeTraverser.navigateToNode(DebugHmiRoot::class.java)
                }) { Text("Debug 2 with passed in listener")}
            }
        }
}
