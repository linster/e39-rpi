package ca.stefanm.ibus.lib.cli.debugPrinters

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.CliPrinterService
import ca.stefanm.ibus.lib.platform.IBusMessageListenerService
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject
import javax.inject.Named

class IncomingIbusMessageCliPrinter @Inject constructor(
    private val serialListenerService: SerialListenerService,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), CliPrinterService, IBusMessageListenerService {

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