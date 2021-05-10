package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarModuleScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@ConfiguredCarModuleScope
class IbusInputEventCliPrinter @Inject constructor(
//    @Named(ApplicationModule.INPUT_EVENTS)
    private val inputEvents : SharedFlow<InputEvent>,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        inputEvents.collect {
            logger.d("INPUT", "Got Input event: $it")
        }
    }
}