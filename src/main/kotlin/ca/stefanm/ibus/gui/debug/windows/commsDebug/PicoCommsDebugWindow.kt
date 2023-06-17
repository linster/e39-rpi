package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PicoToPiMessageFactory
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PiToPicoOuterClass
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.ServiceStatusViewer
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

class PicoCommsDebugWindow @Inject constructor(
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,

    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) val outgoingMessages : Channel<IBusMessage>,

    private val piToPicoMessageFactory: PiToPicoMessageFactory,
    private val picoToPiMessageFactory: PicoToPiMessageFactory,

    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger,
    private val sunroofOpener: SunroofOpener
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Pico Comms Debug"
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(1280.dp, 1024.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(0.6f)) {
                PicoLogViewerWindow()
            }
            Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.Start) {
                PlatformControls(configurablePlatform)
                Row {
                    MessageSendCard()
                }
                SendTestMessages()
            }
        }
    }

    @Composable
    fun MessageSendCard() {
        NestingCard {
            NestingCardHeader("Send Pico Message")

            val scope = rememberCoroutineScope()
            CannedMessageTypes(modifier = Modifier, onMessageTypeSelected = {
                when (it) {
                    PiToPicoNoArgCannedMessageType.HeartbeatRequest -> {
                        scope.launch { sendMessage { heartbeatRequest() } }
                    }
                    PiToPicoNoArgCannedMessageType.HeartbeatResponse -> {
                        scope.launch { sendMessage { heartbeatResponse() } }
                    }
                    PiToPicoNoArgCannedMessageType.ConfigStatusRequest -> {
                        scope.launch { sendMessage { configStatusRequest() } }
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestUpstream -> {
                        scope.launch { sendMessage { videoSourceRequest(
                            PiToPicoMessageFactory.PicoVideoRequestSource.Upstream
                        ) }}
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestPico -> {
                        scope.launch { sendMessage { videoSourceRequest(
                            PiToPicoMessageFactory.PicoVideoRequestSource.Pico
                        ) }}
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestRpi -> {
                        scope.launch { sendMessage { videoSourceRequest(
                            PiToPicoMessageFactory.PicoVideoRequestSource.Rpi
                        ) }}
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestRVC -> {
                        scope.launch { sendMessage { videoSourceRequest(
                            PiToPicoMessageFactory.PicoVideoRequestSource.RVC
                        ) }}
                    }
                    PiToPicoNoArgCannedMessageType.PicoPowerRequestOn -> {
                        scope.launch { sendMessage { piHardPowerSwitch(true) } }
                    }
                    PiToPicoNoArgCannedMessageType.PicoPowerRequestOff -> {
                        scope.launch { sendMessage { piHardPowerSwitch(false) } }
                    }
                }
            })
        }
    }

    suspend fun sendMessage( block : PiToPicoMessageFactory.() -> IBusMessage) {
        val rawMessage = with (piToPicoMessageFactory) { block() }

        outgoingMessages.send(rawMessage)

        val piToPicoMessage = try {
            PiToPicoOuterClass.PiToPico.parseFrom(rawMessage.data.toByteArray())
        } catch (e: Throwable) {
            logger.e("PicoCommsDebugWindow", "SendMessage could not parse message", e)
            null
        }

        if (piToPicoMessage != null) {
            commsDebugChannel.emit(
                IbusCommsDebugMessage.OutgoingMessage.SyntheticPiToPicoMessage(
                    rawMessage,
                    sentAt = Instant.now(),
                    piToPicoMessage = piToPicoMessage
                )
            )
        }
    }

    @Composable
    fun SimulatePicoMessagesCard() {
        NestingCard {
            NestingCardHeader("Simulate Pico Messages")

            //TODO Checkbox here for "Real, or simulated"
        }
    }

    @Composable
    fun PicoLogViewerWindow() {
        HorizontalServiceControlStrip(modifier = Modifier, configurablePlatform)

        Text("commsDebugChannel: ${commsDebugChannel.hashCode()}")
        IbusMessageLogbackPane(commsDebugChannel)
    }

    @Composable
    fun SendTestMessages() {
        NestingCard {
            NestingCardHeader("Send Test Messages")

            val scope = rememberCoroutineScope()
            Button(onClick = { scope.launch { sunroofOpener.openSunroof() } }) {
                Text("Open Sunroof")
            }
        }
    }


}