package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.lib.bluetooth.BluetoothService
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.DbusConnector
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.TelephoneLedManager
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import javax.inject.Inject

class StefanE39Application @ExperimentalStdlibApi
@Inject constructor(
    private val telephoneLedManager: TelephoneLedManager,
    private val sunroofOpener: SunroofOpener,
    private val bluetoothService: BluetoothService,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
): LongRunningLoopingService(coroutineScope, parsingDispatcher) {

    @ExperimentalStdlibApi
    override suspend fun doWork() {

        bluetoothService.onCreate()

        telephoneLedManager.setTelephoneLeds(TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.BLINK, TelephoneLedManager.LedState.ON)
        delay(5 * 1000)

        telephoneLedManager.setTelephoneLeds(TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.OFF)

        delay(3 * 1000)

        if (!sunRoofIsOpen) {
            sunroofOpener.openSunroof()
            sunRoofIsOpen = true
        }
    }

    var sunRoofIsOpen = false
}