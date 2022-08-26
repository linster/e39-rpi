package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@AutoDiscover
class MainBtMenu @Inject constructor(
    private val pairingManager: PairingManager,
    private val configurablePlatform: ConfigurablePlatform
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MainBtMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { incomingResult ->

        if (incomingResult != null && incomingResult.result is UiResult) {
            pairingManager.onUiResult(incomingResult.result)
        }

        val currentConfig = remember { mutableStateOf(configurablePlatform.currentConfiguration) }

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
                        "Current BMBT Device",
                        isSelectable = false,
                        onClicked = {}
                    ),
//                    TextMenuItem(
//                        "Refresh",
//                        onClicked = {
//                            currentConfig.value = configurablePlatform.currentConfiguration
//                        }
//                    ),
                    TextMenuItem(
                        "Name: ${currentConfig.value?.pairedPhone?.friendlyName ?: "None"}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "MAC: ${
                            currentConfig.value?.pairedPhone
                                ?.macAddress
                                ?.fold("") { acc: String, i: Int -> acc + i.toString(16) }
                                ?.windowed(2, 2, true) { "$it:" }
                                ?.fold("") { acc: String, s: String -> acc + s }
                                ?.removeSuffix(":")
                                ?: "None"
                        }",
                        isSelectable = false,
                        onClicked = {}
                    )
                ),
                se = listOf(
                    TextMenuItem(
                        title = "Clear BMBT Device",
                        onClicked = {
                            pairingManager.clearCarPlatformBtDevice()
                            pairingManager.onGoToBtMainMenu()
                        }
                    )
                ),
                sw = listOf(
                    TextMenuItem(
                        title = "View Connected Devices",
                        onClicked = {
                            pairingManager.requestCurrentConnectedDevicesViewer()
                        }
                    ),
                    TextMenuItem(
                        title = "Pair New Device",
                        onClicked = {
                            pairingManager.requestPairNewDevice()
                        }
                    )
                )
            )
        }
    }
}