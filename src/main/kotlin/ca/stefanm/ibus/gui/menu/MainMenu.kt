package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugScreen2
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.picker.TextEntry
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ApplicationScope
class MainMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val textEntry: TextEntry,
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val knobListenerService: KnobListenerService,
) : NavigationNode<Nothing> {
    override val thisClass = MainMenu::class.java

    override fun provideMainContent(): @Composable (incoming : Navigator.IncomingResult?) -> Unit = {

            val enteredText = remember { mutableStateOf("Initial") }
            val btResult = remember { mutableStateOf<BluetoothPairingMenu.PairingResult?>(
                (it?.result as? BluetoothPairingMenu.PairingResult?)
            )}

            Column {
                Text("Hello Main Menu")
                Button(onClick = {
                    navigationNodeTraverser.navigateToNode(BluetoothPairingMenu::class.java)
                }) { Text("Go to bt")}

                Text("Pairing Result: ${btResult.value}")

                Button(
                    onClick = {
                        textEntry.enterText(
                            listOf(),
                            onComplete = {
                                enteredText.value = it
                            },
                            onCancel = {}
                        )
                    }
                ) { Text("Enter text")}
                Text(text = "Entered text: ${enteredText.value}")


                Button(onClick = {
                    modalMenuService.showModalMenu(
                        menuTopLeft = IntOffset(400, 100),
                        menuWidth = 280,
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
                    navigationNodeTraverser.navigateToNodeWithParameters(DebugScreen2::class.java, knobListenerService)
                }) { Text("Debug 2 with passed in listener")}
            }
        }


}
