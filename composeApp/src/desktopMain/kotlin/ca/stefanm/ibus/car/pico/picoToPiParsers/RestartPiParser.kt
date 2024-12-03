package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "RestartPiParser",
    description = "Soft power request from Pico to do an Init 6"
)
@PicoToPiParserGroup
class RestartPiParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    override suspend fun doWork() {
        incomingMessages.filter {
            it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
        }.collect {

            val message = try {
                PicoToPiOuterClass.PicoToPi.parser().parseFrom(it.data.toByteArray())
            } catch (e : Throwable) {
                logger.e("LogMessageParser", "Could not parse protobuf", e)
                return@collect
            }

            if (message.messageType == PicoToPiOuterClass.PicoToPi.MessageType.PiSoftPowerRestartPi) {
                commsDebugChannel.emit(IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
                    Instant.now(),
                    it,
                    message
                ))
                restartPi()
            }
        }
    }

    private fun restartPi() {
        logger.w("RestartPiParser", "TODO restart pi")
    }

}