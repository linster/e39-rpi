package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named

class SerialListenerService @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INPUT_CHANNEL) private val inputChannel : Channel<IBusMessage>,
    private val logger: Logger,
    private val serialPortReader: SerialPortReader,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        serialPortReader.readMessages().collect {
            logger.d("SerialListenerService", "Broadcasting received message: $it")
            inputChannel.send(it)
        }
    }
}

//TODO box to connect, and provide a socket? or a channel of packets??

//TODO use INPA cable with https://www.cyberciti.biz/hardware/5-linux-unix-commands-for-connecting-to-the-serial-console/
//TODO see what is transmitted.


//We need to listen to a stream of bytes, use the len field, and know when to slice into a bytearray piece.
//TODO calculate the checksum on a message.

//TODO use OKIO to stream the bytes?

