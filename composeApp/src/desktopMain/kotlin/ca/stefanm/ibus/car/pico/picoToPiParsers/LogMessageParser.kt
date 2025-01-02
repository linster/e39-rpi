package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass.PicoToPi
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
    name = "LogMessageParser",
    description = "Forwards Pico log messages to the Pi logger"
)
@PicoToPiParserGroup
class LogMessageParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages: MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel: MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        incomingMessages
            .filter { it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI }
            .collect { raw ->

                val message = try {
                    PicoToPi.parser().parseFrom(raw.data.toByteArray())
                } catch (e: Throwable) {
                    logger.e("LogMessageParser", "Could not parse protobuf", e)
                    return@collect
                }

                if (message.messageType == PicoToPi.MessageType.LogStatement) {
                    onLogMessage(raw, message)
                }
            }
    }

    suspend fun onLogMessage(raw: IBusMessage, parsed: PicoToPi) {
        commsDebugChannel.emit(
            IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
                Instant.now(),
                raw,
                parsed
            )
        )
        logger.i("PICO", parsed.loggerStatement)
    }
}