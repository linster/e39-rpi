package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialWriterDebugService
import ca.stefanm.ibus.car.platform.SerialInterfaceServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.car.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "SerialPublisherService",
    description = "Sends serial messages to the IBus dongle."
)
@SerialInterfaceServiceGroup
class SerialPublisherService @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val logger: Logger,
    private val serialPortWriter: SerialPortWriter,
    private val serialWriterDebugService: SerialWriterDebugService,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        messagesOut.consumeEach {
            logger.d("SerialPublisherService", "Writing message to serial port: $it")
            serialPortWriter.writeRawBytes(it.toWireBytes())

            serialWriterDebugService.logMessage(it)
        }
    }
}

