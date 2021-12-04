package ca.stefanm.ibus.car.serial

import ca.stefanm.ibus.car.conduit.CarConduitModule
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.car.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import javax.inject.Named

class SerialPublisherService @Inject constructor(
    @Named(CarConduitModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val logger: Logger,
    private val serialPortWriter: SerialPortWriter,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        messagesOut.consumeEach {
            logger.d("SerialPublisherService", "Writing message to serial port: $it")
            serialPortWriter.writeRawBytes(it.toWireBytes())
        }
    }
}

