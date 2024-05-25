package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.docs.CarPlatformScreenDocPartition
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.hardwareDrivers.RelayReaderWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

@ScreenDoc(
    screenName = "RelayToggleScreen",
    description = "A screen to write to I2C to toggle an optional relay board."
)
@ScreenDoc.AllowsGoBack
@CarPlatformScreenDocPartition
@ApplicationScope
@AutoDiscover
class RelayToggleScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val configurablePlatform: ConfigurablePlatform
) : NavigationNode<Nothing> {

    private fun getRelayReaderWriter() : RelayReaderWriter? {
        return configurablePlatform.configuredCarComponent?.relayReaderWriter()
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RelayToggleScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ {

        val relayReaderWriter = getRelayReaderWriter()

        if (relayReaderWriter == null) {
            LaunchedEffect(true) {
                notificationHub.postNotification(Notification(
                    Notification.NotificationImage.ALERT_TRIANGLE,
                    "Platform Not Running",
                    "Platform needs to run to set relays.")
                )
                navigationNodeTraverser.goBack()
            }
            return@content
        }

        @Composable
        fun RelayReaderWriter.Relay.toPeriodicState() : State<Boolean?> = produceState(null as Boolean?) {
            flow {
                while (true) {
                    delay(1000)
                    emit(Unit)
                }
            }.map {
                relayReaderWriter.readRelayState(this@toPeriodicState)
            }.collect {
                value = it
            }
        }


        val relay1Status = RelayReaderWriter.Relay.RELAY_1.toPeriodicState()
        val relay2Status = RelayReaderWriter.Relay.RELAY_2.toPeriodicState()
        val relay3Status = RelayReaderWriter.Relay.RELAY_3.toPeriodicState()
        val relay4Status = RelayReaderWriter.Relay.RELAY_4.toPeriodicState()

        Menu(
            relay1Status.value,
            relay2Status.value,
            relay3Status.value,
            relay4Status.value
        ) { relay ->
            relayReaderWriter.writeRelayState(relay, !(relayReaderWriter.readRelayState(relay)))
        }
    }

    @Composable
    private fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 30.sp else 9.sp,
            fontWeight = weight
        )
    }

    @Composable
    private fun Menu(
        relay1Status : Boolean?,
        relay2Status: Boolean?,
        relay3Status : Boolean?,
        relay4Status : Boolean?,
        onRequestRelayStateToggle : (relay : RelayReaderWriter.Relay) -> Unit
    ) {
        fun Boolean?.toUiState() = if (this == null) "null" else if (this) "On" else "Off"

        Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            BmwSingleLineHeader("Relay Toggle")

            Column(Modifier.padding(horizontal = 120.dp.halveIfNotPixelDoubled(), vertical = 40.dp.halveIfNotPixelDoubled())) {
                Row(Modifier.wrapContentHeight().fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoLabel("Relay 1: ${relay1Status.toUiState()}")
                    InfoLabel("Relay 3: ${relay3Status.toUiState()}")
                }

                Row(Modifier.wrapContentHeight().fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoLabel("Relay 2: ${relay2Status.toUiState()}")
                    InfoLabel("Relay 4: ${relay4Status.toUiState()}")
                }
            }

            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(
                    TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                    TextMenuItem("Toggle 1", onClicked = {
                        onRequestRelayStateToggle(RelayReaderWriter.Relay.RELAY_1)
                    }),
                    TextMenuItem("Toggle 2", onClicked = {
                        onRequestRelayStateToggle(RelayReaderWriter.Relay.RELAY_2)
                    })
                ),
                rightItems = listOf(
                    TextMenuItem("Toggle 3", onClicked = {
                        onRequestRelayStateToggle(RelayReaderWriter.Relay.RELAY_3)
                    }),
                    TextMenuItem("Toggle 4", onClicked = {
                        onRequestRelayStateToggle(RelayReaderWriter.Relay.RELAY_4)
                    })
                )
            )
        }
    }
}