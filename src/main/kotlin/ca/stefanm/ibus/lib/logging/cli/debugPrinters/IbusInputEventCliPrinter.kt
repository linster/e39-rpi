package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.lib.logging.cli.CliPrinterService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class IbusInputEventCliPrinter @Inject constructor(
    private val iBusInputMessageParser: IBusInputMessageParser,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService.CliPrinter, IBusInputEventListenerService {

    override val name = "IbusInputEventCliPrinter"
    override fun onPrinterEnabled() = onCreate()
    override fun onPrinterDisabled() = onShutdown()

    override fun onCreate() {
        iBusInputMessageParser.addMailbox(this)
        super.onCreate()
    }

    override fun onShutdown() {
        super.onShutdown()
        iBusInputMessageParser.removeMailbox(this)
    }

    override val incomingIbusInputEvents: Channel<InputEvent> = Channel(capacity = Channel.UNLIMITED)

    override suspend fun doWork() {
        incomingIbusInputEvents.consumeEach {
            logger.d("INPUT", "Got Input event: $it")
        }
    }
}