package ca.stefanm.ibus.car.debugPrinters

import ca.stefanm.ibus.car.data.IBusMessage
import ca.stefanm.ibus.car.di.QualifierNames
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named

class IncomingIbusMessageCliPrinter @Inject constructor(
    @Named(QualifierNames.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        incomingMessages.collect {
            logger.d("INPUT", "Got IBusMessage: $it")
        }
    }
}