package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass.PicoToPi
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "PicoHeartbeatRequestParser",
    description = "Listens from heartbeat requests from the pico and then sends a heartbeat response."
)
@PicoToPiParserGroup
class HeartbeatRequestParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) val outgoingMessages : Channel<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val piToPicoMessageFactory: PiToPicoMessageFactory,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){


    override suspend fun doWork() {

        incomingMessages
            .filter {
                it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
            }
            .map {
                try {
                    PicoToPi.parser().parseFrom(it.data.toByteArray())
                } catch (e : Throwable) {
                    logger.e("HeartbeatRequestParser", "Could not parse protobuf", e)
                    null
                }
            }
            .filter { it != null }
            .map { it as PicoToPi }
            .filter { it.messageType == PicoToPi.MessageType.HeartbeatRequest }
            .collect {
                logger.i("HeartbeatRequestParser", "Got HeartbeatRequest from Pico")
                logger.d("HeartbeatRequestParser", it.toString())
                sendHeartbeatResponse()
            }
    }

    suspend fun sendHeartbeatResponse() {
        outgoingMessages.send(piToPicoMessageFactory.heartbeatResponse())
    }
}
