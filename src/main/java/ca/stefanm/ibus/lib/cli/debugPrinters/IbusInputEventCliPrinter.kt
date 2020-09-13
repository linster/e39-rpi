package ca.stefanm.ibus.lib.cli.debugPrinters

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.platform.CliPrinterService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named

class IbusInputEventCliPrinter @Inject constructor(
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel : Channel<InputEvent>,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService {

    override suspend fun doWork() {
        inputEventChannel.poll()?.let {
            logger.d("INPUT", "Got Input event: $it")
        }
    }
}