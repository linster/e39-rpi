package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.car.platform.IBusMessageListenerService
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.lib.logging.cli.CliPrinterService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject

class IncomingIbusMessageCliPrinter @Inject constructor(
    private val serialListenerService: SerialListenerService,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService.CliPrinter, IBusMessageListenerService {

    override val name = "IncomingIbusMessageCliPrinter"
    override fun onPrinterEnabled() = onCreate()
    override fun onPrinterDisabled() = onShutdown()

    override fun onCreate() {
        super.onCreate()
        serialListenerService.addMailbox(this)
    }

    override fun onShutdown() {
        serialListenerService.removeMailbox(this)
        super.onShutdown()
    }

    override val incomingIBusMessageMailbox: Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)

    override suspend fun doWork() {
        incomingIBusMessageMailbox.consumeEach {
//            logger.d("INPUT", "Got IBusMessage: $it")
        }
    }
}