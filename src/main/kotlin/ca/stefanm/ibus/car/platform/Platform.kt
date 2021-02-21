package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.lib.hardwareDrivers.CoolingFanController
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPublisherService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.logging.cli.CliPrinterService
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.stefane39.TelephoneButtonVideoSwitcherService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.Executors
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
            doWork()
        }
    }

    override fun onShutdown() {
        jobToJoin?.cancel(cause = ForegroundPlatform.PlatformShutdownCancellationException())
    }

    abstract suspend fun doWork()
}

interface IBusMessageListenerService : Service {
    val incomingIBusMessageMailbox : Channel<IBusMessage>
}

interface IBusInputEventListenerService : Service {
    val incomingIbusInputEvents : Channel<InputEvent>
}


abstract class LongRunningLoopingService constructor(
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
        jobToJoin?.cancel(cause = ForegroundPlatform.PlatformShutdownCancellationException())
    }

    abstract suspend fun doWork()
}

class PlatformServiceRunner @Inject constructor(
    private val coroutineScope: CoroutineScope,
    iBusInputMessageParser: IBusInputMessageParser,
    coolingFanController: CoolingFanController,
    cliPrinterService: CliPrinterService,
    serialPublisherService: SerialPublisherService,
    serialListenerService: SerialListenerService,
    bluetoothService: BluetoothService,
    telephoneButtonVideoSwitcherService: TelephoneButtonVideoSwitcherService
) : Service {

    private val services = listOf<Service>(
        coolingFanController,
        iBusInputMessageParser,
        cliPrinterService,
        serialPublisherService,
        serialListenerService,
        bluetoothService,
        telephoneButtonVideoSwitcherService
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
                it.jobToJoin?.cancel(ForegroundPlatform.PlatformShutdownCancellationException())
            }
            it.onShutdown()
        }
    }
}

class ForegroundPlatform @Inject constructor(
    private val deviceConfiguration: DeviceConfiguration,
    private val platformServiceRunner: PlatformServiceRunner,
    private val logger: Logger
) {

    class PlatformShutdownCancellationException : CancellationException()

    fun run() {
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })

        println("Starting platform with device configuration: $deviceConfiguration")

        platformServiceRunner.onCreate()
    }

    fun stop() {
        logger.i("Platform", "Shutting down platform.")
        platformServiceRunner.onShutdown()
    }
}

class BackgroundPlatform @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val deviceConfiguration: DeviceConfiguration,
    private val platformServiceRunner: PlatformServiceRunner,
    private val logger: Logger
) {
    private var platformJob : Job? = null

    fun run(dispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
        println("Starting platform with device configuration: $deviceConfiguration")
        platformJob = coroutineScope.launch(dispatcher) {
            platformServiceRunner.onCreate()
        }

    }

    fun stop() {
        logger.i("BackgroundPlatform", "Shutting down platform.")
        platformServiceRunner.onShutdown()
        platformJob?.cancel()
    }
}