package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.car.platform.CliPrinterServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@ConfiguredCarScope
@PlatformServiceInfo(
    name = "IbusInputEventCliPrinter",
    description = "Prints IBusInputEvents to stdout. " +
            "This is an abstraction over messages to indicate actions."
)
@CliPrinterServiceGroup
class IbusInputEventCliPrinter @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,
    private val logger : Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        inputEvents.collect {
            logger.d("INPUT", "Got Input event: $it")
        }
    }
}