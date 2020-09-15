package ca.stefanm.ibus.lib.cli.debugPrinters

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class IbusInputEventCliPrinter @Inject constructor(
    private val iBusInputMessageParser: IBusInputMessageParser,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService, IBusInputEventListenerService {

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