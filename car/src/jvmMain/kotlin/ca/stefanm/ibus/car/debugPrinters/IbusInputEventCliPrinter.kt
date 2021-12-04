package ca.stefanm.ibus.car.debugPrinters

import ca.stefanm.ibus.car.data.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.di.QualifierNames
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.car.platform.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@ConfiguredCarScope
class IbusInputEventCliPrinter @Inject constructor(
    @Named(QualifierNames.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,
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