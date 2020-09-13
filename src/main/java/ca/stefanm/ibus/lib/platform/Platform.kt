package ca.stefanm.ibus.lib.platform

import ca.stefanm.ibus.lib.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.lib.cli.debugPrinters.PlatformMetronomeLogger
import ca.stefanm.ibus.lib.hardwareDrivers.CoolingFanController
import ca.stefanm.ibus.lib.cli.debugPrinters.IncomingIbusMessageCliPrinter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPublisherService
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.*
import javax.inject.Inject


interface Service {
    fun onCreate()
    fun onShutdown()
}

interface JoinableService : Service {
   var jobToJoin : Job?
}

abstract class LongRunningService constructor(
    private val coroutineScope: CoroutineScope,
    private val parsingDispatcher: CoroutineDispatcher
) : JoinableService {

    override var jobToJoin : Job? = null

    override fun onCreate() {
        jobToJoin = coroutineScope.launch(parsingDispatcher) {
            while (true) {
                doWork()
                yield()
            }
        }
    }

    override fun onShutdown() {
        jobToJoin?.cancel(cause = Platform.PlatformShutdownCancellationException())
    }

    abstract suspend fun doWork()
}

interface CliPrinterService

class PlatformServiceRunner @Inject constructor(
    private val coroutineScope: CoroutineScope,
    iBusInputMessageParser: IBusInputMessageParser,
    coolingFanController: CoolingFanController,
    platformMetronomeLogger: PlatformMetronomeLogger,
    incomingIbusMessageCliPrinter: IncomingIbusMessageCliPrinter,
    serialPublisherService: SerialPublisherService,
    serialListenerService: SerialListenerService
) : Service {

    private val services = listOf<Service>(
        coolingFanController,
        iBusInputMessageParser,
        incomingIbusMessageCliPrinter,
        platformMetronomeLogger,
        serialPublisherService,
        serialListenerService
    )

    override fun onCreate() {
        runBlocking(coroutineScope.coroutineContext) {
            val jobsToJoin = mutableListOf<Job>()
            services.forEach {
                it.onCreate()
                if (it is JoinableService) {
                    it.jobToJoin?.let { job -> jobsToJoin.add(job) }
                }
            }

            jobsToJoin.joinAll()
        }
    }

    override fun onShutdown() {
        services.forEach {
            if (it is JoinableService) {
                it.jobToJoin?.cancel(Platform.PlatformShutdownCancellationException())
            }
            it.onShutdown()
        }
    }
}

class Platform @Inject constructor(
    private val platformServiceRunner: PlatformServiceRunner,
    private val logger: Logger
) {

    class PlatformShutdownCancellationException : CancellationException()

    fun run() {
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.i("Platform", "Shutting down platform.")
            platformServiceRunner.onShutdown()
        })

        platformServiceRunner.onCreate()
    }
}