package ca.stefanm.ibus.lib.platform

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.wiringpi.Gpio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Inject

class RpiGpioPlatformResetManager @Inject constructor(
    private val deviceConfiguration: DeviceConfiguration,
    coroutineScope: CoroutineScope
) : LongRunningService(coroutineScope, Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {

    override suspend fun doWork() {
        if (!deviceConfiguration.isPi) {
            return
        }

        //TODO
    }

    fun restartApp() {
        //Depend on systemD to restart us.
        Runtime.getRuntime().exit(2)
    }


}