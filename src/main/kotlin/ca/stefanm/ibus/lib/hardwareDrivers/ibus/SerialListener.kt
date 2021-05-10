package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarModuleScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@ConfiguredCarModuleScope
class SerialListenerService @Inject constructor(

    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS)
    val incomingMessages : MutableSharedFlow<IBusMessage>,

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
