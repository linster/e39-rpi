package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.di.UnConfiguredCarComponent
import ca.stefanm.ibus.car.di.UnConfiguredCarModule
import ca.stefanm.ibus.car.di.UnConfiguredCarScope
import ca.stefanm.ibus.car.hardware.CoolingFanController
import ca.stefanm.ibus.car.hardware.TelephoneButtonVideoSwitcherService
import ca.stefanm.ibus.car.serial.SerialListenerService
import ca.stefanm.ibus.car.serial.SerialPublisherService
import ca.stefanm.ibus.logger.Logger
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named

@ConfiguredCarScope
class PlatformServiceRunner @Inject constructor(
    private val coroutineScope: CoroutineScope,
    iBusInputMessageParser: IBusInputMessageParser,
    coolingFanController: CoolingFanController,
    serialPublisherService: SerialPublisherService,
    serialListenerService: SerialListenerService,
    bluetoothService: BluetoothService,
    telephoneButtonVideoSwitcherService: TelephoneButtonVideoSwitcherService
) : Service {

    private val services = listOf<Service>(
        coolingFanController,
        iBusInputMessageParser,
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

@UnConfiguredCarScope
class ForegroundPlatform @Inject constructor(
    @Named(UnConfiguredCarModule.INITIAL_CONFIGURATION) private val deviceConfiguration: CarPlatformConfiguration,
    private val logger: Logger
) {

    class PlatformShutdownCancellationException : CancellationException()

    private var platformServiceRunner : PlatformServiceRunner? = null

    fun run(initialConfiguration: CarPlatformConfiguration = LaptopDeviceConfiguration()) {
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })

        println("Starting platform with device configuration: $deviceConfiguration")


//        platformServiceRunner = configuredCarComponentProvider
//            .get()
//            .configuredCarModule(ConfiguredCarModule(initialConfiguration))
//            .build()
//            .legacyPlatformServiceRunner()

        platformServiceRunner!!.onCreate()
    }

    fun stop() {
        logger.i("Platform", "Shutting down platform.")
        platformServiceRunner!!.onShutdown()
    }
}

@UnConfiguredCarScope
class BackgroundPlatform @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val logger: Logger
) {
    private var platformJob : Job? = null

    private var platformServiceRunner : PlatformServiceRunner? = null


    fun run(initialConfiguration: CarPlatformConfiguration = LaptopDeviceConfiguration(),
            dispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
        println("Starting platform with device configuration: $initialConfiguration")
        platformJob = coroutineScope.launch(dispatcher) {
//            platformServiceRunner = configuredCarComponentProvider
//                .get()
//                .configuredCarModule(ConfiguredCarModule(initialConfiguration))
//                .build()
//                .legacyPlatformServiceRunner()

            platformServiceRunner!!.onCreate()
        }

    }

    fun stop() {
        logger.i("BackgroundPlatform", "Shutting down platform.")
        platformServiceRunner!!.onShutdown()
        platformJob?.cancel()
    }
}