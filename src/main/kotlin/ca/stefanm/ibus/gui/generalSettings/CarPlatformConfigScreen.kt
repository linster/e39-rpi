package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ca.stefanm.ibus.gui.docs.CarPlatformScreenDocPartition
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.HmiLogViewerScreen
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




@ScreenDoc(
    screenName = "CarPlatformConfigScreen",
    description = "Configures the Car Platform, the part of the e39-Rpi Hmi that interfaces with the car."
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.OpensSubScreen("serialPortPrompt")
@ScreenDoc.OpensSubScreen("restartCarPlatformPrompt")
@ScreenDoc.NavigateTo(
    CarServiceConfigScreen::class,
    linkDescription = "",
    targetDescription = "Screen to enable, disable, restart car services while running."
)
@ScreenDoc.NavigateTo(HmiLogViewerScreen::class)
@ScreenDoc.NavigateTo(RelayToggleScreen::class)
@CarPlatformScreenDocPartition
@AutoDiscover
class CarPlatformConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage,
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform
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
                    "Restart Car Platform",
                        onClicked = { restartCarPlatformPrompt() }
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
                ),
                TextMenuItem(
                    "Log Viewer",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            HmiLogViewerScreen::class.java
                        )
                    }
                ),
                TextMenuItem(
                    "Relay Toggle",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            RelayToggleScreen::class.java
                        )
                    }
                )
            )
            )
        }
    }

    @ScreenDoc.SubScreen(
        screenName = "serialPortPrompt",
        paneDescription = "Prompts which serial port the Rpi uses (TtyACM0, TtyUSB0, etc..)"
    )
    private fun serialPortPrompt() {

        val availablePorts = SerialPort.getCommPorts().map { it.systemPortName }

        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                "Serial Port",
                text = {
                    SidePanelMenu.InfoLabel("Set TTY for the LIN transceiver", FontWeight.Bold)
                    SidePanelMenu.InfoLabel(configurationStorage.config[E39Config.CarPlatformConfigSpec._iBusInterfaceUri], FontWeight.Light)
                    //SidePanelMenu.InfoLabel("")
                    //SidePanelMenu.InfoLabel("Be careful when setting this option. Car buttons and user input won't work if E39-RPi can't communicate with the car.")
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

    @ScreenDoc.SubScreen(
        screenName = "restartCarPlatformPrompt",
        paneDescription = "Prompts the user to restart the car platform."
    )
    private fun restartCarPlatformPrompt() {
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                "Restart Car Platform?",
                text = @Composable {
                    SidePanelMenu.InfoLabel("Warning", FontWeight.Bold)
                    SidePanelMenu.InfoLabel("")
                    SidePanelMenu.InfoLabel("Restarting the Car Platform with invalid settings while driving" +
                            " might make it un-usable in a vehicle environment. Make sure you have access to SSH first.")
                    SidePanelMenu.InfoLabel("")
                    SidePanelMenu.InfoLabel("Do you wish to continue?")
                },
                buttons = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    ),
                    TextMenuItem(
                        title = "Reboot Car Platform",
                        onClicked = {
                            configurablePlatform.stop()
                            configurablePlatform.run()
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    )
                )
            )
        }
    }
}