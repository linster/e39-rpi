package ca.stefanm.ibus.lib.hardwareDrivers

import ca.stefanm.ibus.car.platform.Peripherals2ServiceGroup
import ca.stefanm.ibus.car.platform.PeripheralsServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import com.pi4j.system.SystemInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs

@PlatformServiceInfo(
    name = "CoolingFanController",
    description = "Turns on the cooling fan in the case."
)
@PeripheralsServiceGroup
@Peripherals2ServiceGroup
class CoolingFanController @Inject constructor(
    private val relayReaderWriter: RelayReaderWriter,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {

    private fun getCpuTemp() : Float {
        return try {
            SystemInfo.getCpuTemperature()
        } catch (e : Exception) {
            //We're not on a pi
            60.0f //TODO
        }
    }

    var prevTemp = 0.0F

    override suspend fun doWork() {
        val currentTemp = getCpuTemp()
        if (abs(currentTemp - prevTemp) > 5.0) {
            logger.v("TEMP", "Current CPU Temp: $currentTemp")
        }
        prevTemp = currentTemp

        coolingFanOn = currentTemp > 30.0
        delay(8 * 1000)
    }

    private var coolingFanOn : Boolean
        get() = relayReaderWriter.readRelayState(RelayReaderWriter.Relay.RELAY_2)
        set(value) = relayReaderWriter.writeRelayState(RelayReaderWriter.Relay.RELAY_2, value)

}