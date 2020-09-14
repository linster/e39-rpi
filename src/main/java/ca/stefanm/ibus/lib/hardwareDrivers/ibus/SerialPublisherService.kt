package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject
import javax.inject.Named

class SerialPublisherService @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val logger: Logger,
    private val serialPortWriter: SerialPortWriter,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        messagesOut.consumeAsFlow().collect {
            logger.d("SerialPublisherService", "Writing message to serial port: $it")
            serialPortWriter.writeRawBytes(it.toWireBytes())
        }
    }
}

