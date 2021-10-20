package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.fazecast.jSerialComm.SerialPort
import javax.inject.Inject

@AutoDiscover
class CarPlatformConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CarPlatformConfigScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {
            BmwSingleLineHeader("Car Platform Config")
            FullScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    "Go Back",
                    onClicked = {navigationNodeTraverser.goBack()}
                ),
                TextMenuItem(
                    "Set Serial Port",
                    onClicked = { serialPortPrompt() }
                ),
                TextMenuItem(
                    "Service Config",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            CarServiceConfigScreen::class.java
                        )
                    }
                )
            )
            )
        }
    }

    private fun serialPortPrompt() {

        val availablePorts = SerialPort.getCommPorts().map { it.systemPortName }

        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                "Serial Port",
                text = {
                    SidePanelMenu.InfoLabel("Set the tty for the LIN transceiver", FontWeight.Bold)
                    SidePanelMenu.InfoLabel(configurationStorage.config[E39Config.CarPlatformConfigSpec._iBusInterfaceUri], FontWeight.Light)
                    SidePanelMenu.InfoLabel("")
                    SidePanelMenu.InfoLabel("Be careful when setting this option. Car buttons and user input won't work if E39-RPi can't communicate with the car.")
                },
                buttons = availablePorts.map {
                    TextMenuItem(
                        title = it,
                        onClicked = {
                            configurationStorage.config[E39Config.CarPlatformConfigSpec._iBusInterfaceUri] = "/dev/$it"
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    )
                } + listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    )
                )
            )
        }
    }
}