package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.car.platform.CliPrinterServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "IncomingIbusMessageCliPrinter",
    description = "Prints incoming IBusMessages to stdout"
)
@CliPrinterServiceGroup
class IncomingIbusMessageCliPrinter @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    private val logger : Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        incomingMessages.collect {
            logger.d("INPUT", "Got IBusMessage: $it")
        }
    }
}