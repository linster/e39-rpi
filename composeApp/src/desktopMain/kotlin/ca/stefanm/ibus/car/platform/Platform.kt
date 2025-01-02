package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.hardwareDrivers.CoolingFanController
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPublisherService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.stefane39.TelephoneButtonVideoSwitcherService
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

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

@ApplicationScope
class ForegroundPlatform @Inject constructor(
    @Named(ApplicationModule.INITIAL_CONFIGURATION) private val deviceConfiguration: CarPlatformConfiguration,
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

@ApplicationScope
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