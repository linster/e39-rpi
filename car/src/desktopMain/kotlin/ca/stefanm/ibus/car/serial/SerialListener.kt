package ca.stefanm.ibus.car.serial

import ca.stefanm.ibus.car.conduit.CarConduitModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named

@ConfiguredCarScope
class SerialListenerService @Inject constructor(

    @Named(CarConduitModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    private val logger: Logger,
    private val serialPortReader: SerialPortReader,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        serialPortReader.readMessages().collect {
            incomingMessages.emit(it)
        }
    }
}
