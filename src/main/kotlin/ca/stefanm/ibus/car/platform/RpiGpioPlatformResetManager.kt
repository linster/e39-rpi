package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.configuration.DeviceConfiguration
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