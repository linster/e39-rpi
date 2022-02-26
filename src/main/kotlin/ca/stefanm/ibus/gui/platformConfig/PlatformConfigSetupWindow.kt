package ca.stefanm.ca.stefanm.ibus.gui.platformConfig

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject


@ApplicationScope
class PlatformConfigSetupWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val configurablePlatform: ConfigurablePlatform
): WindowManager.E39Window {

    override val title = "Platform Config Setup"
    override val size: DpSize = WindowManager.DEFAULT_DEBUG_WINDOW_SIZE
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {

        Row(Modifier.fillMaxSize()) {
            val leftPaneScrollState = rememberScrollState()
            Box(Modifier.wrapContentWidth()) {
                this@Row.PlatformConfigPane()
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(leftPaneScrollState)
                )
            }
            ServiceListViewer()
        }
    }

    @Composable
    private fun RowScope.PlatformConfigPane() {
        Column(Modifier.weight(0.25f, true)) {
                NestingCardHeader("Platform Service Startup Config")
                Text(
                    "Set which platform services should be running " +
                            "when the car platform is started. Disable services " +
                            "that may conflict with other hardware fitted to your " +
                            "IBus car."
                )

                NestingCard {
                    Row {
                        Button(onClick = {
                            val config = E39Config.CarPlatformConfigSpec
                                .toCarPlatformConfiguration(config = configurationStorage.config)
                            configurablePlatform.run(config)
                        }) { Text("Start Platform") }
                        Button(onClick = {
                            configurablePlatform.stop()
                        }) { Text("Stop Platform") }
                    }
                }

                NestingCard(Modifier.fillMaxWidth()) {
                    NestingCardHeader("Profile Generation Options")

                    ProfileGenerationOptions()

                }
            }
    }

    @Composable
    private fun CardSpacer() {
        Spacer(Modifier.height(10.dp))
    }

    @Composable
    private fun ProfileGenerationOptions(

    ) {
        NestingCard {
            NestingCardHeader("Telephone Control Simulation")
            Text(
                "Simulate BMW Telephone Control Unit? May conflict with pre-installed " +
                        "telephone computer."
            )
            CheckBoxWithLabel(false, {}, "Simulate TCU")
            CardSpacer()
            Text("Illuminate Telephone LEDs as e39-rpi state? May conflict with pre-installed telephone computer.")
            CheckBoxWithLabel(false, {}, "Telephone LEDs as e39-rpi state")
            CardSpacer()
        }

        NestingCard {
            NestingCardHeader("Bluetooth Button Services")
            Text(
                "These services should be disabled if a BlueBus is installed in the car, " +
                        "as it will cause the same events to trigger multiple track changes."
            )
            CardSpacer()
            CheckBoxWithLabel(false, {}, "BT Steering wheel and BMBT button listeners")
            CardSpacer()
        }

        NestingCard {
            NestingCardHeader("Case peripherals")
            Text(
                "Services to control peripherals for the 1st prototype Pi Case only. " +
                        "Physical hardware might not exist on subsequent versions."
            )
            CardSpacer()
            CheckBoxWithLabel(
                false,
                {},
                "Cooling fan controller. Uses I2C Relay Board to turn on cooling fan"
            )
            CardSpacer()
            CheckBoxWithLabel(
                false,
                {},
                "I2C Relay Board HMI enable? If false, disable the I2C relay board page in settings."
            )
        }

        NestingCard {
            NestingCardHeader("Video Output Configuration")
            Text("Services which functon to code modules (TV, BMBT) for NTSC/PAL video output")
            CardSpacer()
            Text(
                "This system assumed all modules coded to NTSC due to video timing constraints. \n" +
                        "(The VGA output is calibrated for NTSC output only) \n" +
                        "These services can be enabled to ensure that coding."
            )
        }
    }

    @Composable
    private fun ServiceListViewer() {
        val scope = rememberCoroutineScope()
        val list = configurablePlatform.servicesRunning.collectAsState(scope.coroutineContext)

        Column {
            for (item in list.value) {
                Text("${item.name} ${item.description}")
            }
        }
    }
}