package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

class PicoCommsDebugWindow @Inject constructor(
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,

    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) val outgoingMessages : Channel<IBusMessage>,

    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

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

    override val size = DpSize(1680.dp, 1024.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(0.6f)) {
                PicoLogViewerWindow()
            }
            Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.Start) {
                PlatformControls(configurablePlatform)
                Row {
                    MessageSendCard()
                    SimulatePicoMessagesCard()
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
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.HeartbeatRequest.name
                            ) { heartbeatRequest() }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.HeartbeatResponse -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.HeartbeatResponse.name
                            ) { heartbeatResponse() }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.ConfigStatusRequest -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.ConfigStatusRequest.name
                            ) { configStatusRequest() }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestUpstream -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoVideoRequestUpstream.name
                            ) { videoSourceRequest(
                                PiToPicoMessageFactory.PicoVideoRequestSource.Upstream
                            ) }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestPico -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoVideoRequestPico.name
                            ) { videoSourceRequest(
                                PiToPicoMessageFactory.PicoVideoRequestSource.Pico
                            ) }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestRpi -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoVideoRequestRpi.name
                            ) { videoSourceRequest(
                                PiToPicoMessageFactory.PicoVideoRequestSource.Rpi
                            ) }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.PicoVideoRequestRVC -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoVideoRequestRVC.name
                            ) { videoSourceRequest(
                                PiToPicoMessageFactory.PicoVideoRequestSource.RVC
                            ) }
                        }
                    }
                    PiToPicoNoArgCannedMessageType.PicoPowerRequestOn -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoPowerRequestOn.name
                            ) { piHardPowerSwitch(true) } }
                    }
                    PiToPicoNoArgCannedMessageType.PicoPowerRequestOff -> {
                        scope.launch {
                            sendMessage(
                                PiToPicoNoArgCannedMessageType.PicoPowerRequestOff.name
                            ) { piHardPowerSwitch(false) } }
                    }
                }
            })
        }
    }

    suspend fun sendMessage(name: String = "", block : PiToPicoMessageFactory.() -> IBusMessage) {
        val rawMessage = with (piToPicoMessageFactory) { block() }

        outgoingMessages.send(rawMessage)

        val piToPicoMessage = try {
            PiToPicoOuterClass.PiToPico.parseFrom(rawMessage.data.toByteArray())
        } catch (e: Throwable) {
            logger.e("PicoCommsDebugWindow", "SendMessage could not parse message", e)
            null
        }

        if (piToPicoMessage != null) {
            logger.d("PicoCommsWindow", "Sending message: $name " +
                    "bytes: ${rawMessage.toWireBytes().toUByteArray().map {
                        it.toUInt().toString(radix = 16)
                    }}")
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
            val dryRun = remember { mutableStateOf(false) }
            CheckBoxWithLabel(
                isChecked = dryRun.value,
                onCheckChanged = { dryRun.value = it },
                label = "Dry Run"
            )

            val scope = rememberCoroutineScope()

            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi HeartbeatRequest"
                ) { heartbeatRequest() } } }) {
                Text("Heartbeat Request")
            }

            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi HeartbeatResponse"
                ) { heartbeatResponse() } } }) {
                Text("Heartbeat Response")
            }

            val logStatement1 = "Some short log statement from the raspberry pi pico board."
            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi LogStatement"
                ) { logStatement(logStatement1) } } }) {
                Text("Log Statement 1 (Len: ${logStatement1.length}) ")
            }


            val logStatement2 = "Some longer log statement from the raspberry pi pico board that is sketchily long and" +
                    "shouldn't really fit into one ibus packet, yet here we are."
            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi LogStatement"
                ) { logStatement(logStatement2) } } }) {
                Text("Log Statement 2 (Len: ${logStatement2.length} )")
            }


            val logStatement3 = "Some longer log statement from the raspberry pi pico board that is sketchily long and" +
                    "shouldn't really fit into one ibus packet, yet here we are. And we're going to keep going and going" +
                    "until we are over the character limit and lets just keep on going and going for days and days and then" +
                    "what we will do is ramble on and on until the software explodes in our faces."
            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi LogStatement"
                ) { logStatement(logStatement3) } } }) {
                Text("Log Statement 3 (Len: ${logStatement3.length} )")
            }

            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi RestartX"
                ) { softRestartX() } } }) {
                Text("Request restart X")
            }


            Button(onClick = { scope.launch {
                simulatePicoToPiMessage(
                    dryRun = dryRun.value,
                    name = "PicoToPi RestartPi"
                ) { softRestartPi() } } }) {
                Text("Request restart Pi")
            }

        }
    }

    suspend fun simulatePicoToPiMessage(
        dryRun : Boolean, //If true, only register a simulated event, don't also send it to incoming parsers
        name : String = "",
        block : PicoToPiMessageFactory.() -> IBusMessage
    ) {

        val ibusMessage = with (picoToPiMessageFactory) { block() }


        val picoToPiMessage = try {
            PicoToPiOuterClass.PicoToPi.parseFrom(ibusMessage.data.toByteArray())
        } catch (e: Throwable) {
            logger.e("PicoCommsDebugWindow", "simulatePicoToPiMessage could not parse message", e)
            null
        }

        if (picoToPiMessage != null) {
            logger.d("PicoCommsWindow", "Sending message: $name " +
                    "bytes: ${ibusMessage.toWireBytes().toUByteArray().map {
                        it.toUInt().toString(radix = 16)
                    }}")
            commsDebugChannel.emit(
                IbusCommsDebugMessage.IncomingMessage.SyntheticPicoToPiMessage(
                    rawMessage = ibusMessage,
                    recievedAt = Instant.now(),
                    picoToPiMessage = picoToPiMessage
                )
            )

            if (!dryRun) {
                incomingMessages.emit(ibusMessage)
            }
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