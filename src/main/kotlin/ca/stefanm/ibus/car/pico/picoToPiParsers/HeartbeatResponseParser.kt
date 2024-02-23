package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "PicoHeartbeatResponseParser",
    description = "Listens from heartbeat responses from the pico and prints to a debug log"
)
@PicoToPiParserGroup
class HeartbeatResponseParser  @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    override suspend fun doWork() {
        incomingMessages.filter {
            it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
        }.collect {

            val message = try {
                PicoToPiOuterClass.PicoToPi.parser().parseFrom(it.data.toByteArray())
            } catch (e : Throwable) {
                logger.e("HeartbeatResponseParser", "Could not parse protobuf", e)
                return@collect
            }

            if (message.messageType == PicoToPiOuterClass.PicoToPi.MessageType.PiSoftPowerRestartPi) {
                commsDebugChannel.emit(
                    IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
                    Instant.now(),
                    it,
                    message
                ))
                logger.w("HeartbeatResponseParser", "We got a heartbeat response")
            }
        }
    }


}