package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.TelephoneLedManager
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ConfiguredCarScope
class StefanE39Application @ExperimentalStdlibApi
@Inject constructor(
    private val telephoneLedManager: TelephoneLedManager,
    private val sunroofOpener: SunroofOpener,
    private val bluetoothService: BluetoothService,
    private val trackInfoPrinter: ScreenTrackInfoPrinter,
    private val inputEventParser: IBusInputMessageParser,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
): LongRunningLoopingService(coroutineScope, parsingDispatcher) {

    @ExperimentalStdlibApi
    override suspend fun doWork() {

//        bluetoothService.onCreate()
//
//        telephoneLedManager.setTelephoneLeds(TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.BLINK, TelephoneLedManager.LedState.ON)
//        delay(5 * 1000)
//
//        telephoneLedManager.setTelephoneLeds(TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.OFF, TelephoneLedManager.LedState.OFF)
//
//        delay(3 * 1000)
//

//        delay(5000)
//        inputEventParser.debugSend(InputEvent.NextTrack)
//        delay(2000)
//        inputEventParser.debugSend(InputEvent.NextTrack)

        if (!sunRoofIsOpen) {
//            sunroofOpener.openSunroof()

            //TODO DUMMY Don't call this more than once.
            trackInfoPrinter.onCreate()


//            trackInfoPrinter.onNewTrackInfo("1234567890123456789", "Dole", "Wat")



            sunRoofIsOpen = true
        }

    }

    var sunRoofIsOpen = false
}