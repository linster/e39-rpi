package ca.stefanm.ibus.lib.cli.debugPrinters

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.CliPrinterService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named

class IncomingIbusMessageCliPrinter @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INPUT_CHANNEL) private val inputChannel : Channel<IBusMessage>,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService {

    override suspend fun doWork() {
        inputChannel.poll()?.let {
            logger.d("MESSAGE", "Got IBUS Message: $it")
        }
    }
}