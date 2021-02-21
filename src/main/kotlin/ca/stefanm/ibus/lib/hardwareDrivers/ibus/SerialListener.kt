package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.IBusMessageListenerService
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SerialListenerService @Inject constructor(
    private val logger: Logger,
    private val serialPortReader: SerialPortReader,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        serialPortReader.readMessages().collect {
//            logger.d("SerialListenerService", "Broadcasting received message: $it")

            for (service in mailboxes) {
                service.incomingIBusMessageMailbox.send(it)
            }
        }
    }

    private val mailboxes = mutableListOf<IBusMessageListenerService>()

    fun addMailbox(serviceWithMailbox : IBusMessageListenerService) {
        mailboxes.add(serviceWithMailbox)
    }

    fun removeMailbox(serviceWithMailbox: IBusMessageListenerService) {
        mailboxes.remove(serviceWithMailbox)
    }
}
