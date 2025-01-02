package ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import ca.stefanm.ca.stefanm.ibus.car.platform.SerialInterfaceServiceGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Named


//                PlatformService(
//                    baseService = serialListenerService,
//                    logger = logger
//                ),

@PlatformServiceInfo(
    name = "SerialListenerService",
    description = "Listens for serial mesages from the IBus dongle.",
)
@SerialInterfaceServiceGroup
@ConfiguredCarScope
class SerialListenerService @Inject constructor(

    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    private val logger: Logger,
    private val serialPortReader: SerialPortReader,

    private val serialListenerDebugService: SerialListenerDebugService,

    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        serialPortReader.readMessages().collect {
            incomingMessages.emit(it)
            serialListenerDebugService.logMessage(it)
        }
    }
}
