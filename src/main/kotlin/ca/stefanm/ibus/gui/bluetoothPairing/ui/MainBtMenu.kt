package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class MainBtMenu @Inject constructor(
    private val pairingManager: PairingManager
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MainBtMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { incomingResult ->

        if (incomingResult != null && incomingResult.result is UiResult) {
            pairingManager.onUiResult(incomingResult.result)
        }

        Column {
            BmwSingleLineHeader("Bluetooth Main menu")
            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(
                    TextMenuItem(
                        title = "Main Menu",
                        onClicked = { pairingManager.onRequestNavigatorRoot() }
                    )
                ),
                ne = listOf(
                    TextMenuItem(
                        title = "View Connected Devices",
                        onClicked = {
                            pairingManager.requestCurrentConnectedDevicesViewer()
                        }
                    )
                ),
                se = listOf(
                    TextMenuItem(
                        title = "Pair New Device",
                        onClicked = {
                            pairingManager.requestPairNewDevice()
                        }
                    )
                ),
                sw = listOf()
            )
        }
    }
}