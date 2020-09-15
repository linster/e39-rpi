package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bluetooth.BluetoothService
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.DbusConnector
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.lib.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.TelephoneLedManager
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Named

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
        trackInfoPrinter.onCreate()

        delay(5000)
        inputEventParser.debugSend(InputEvent.NextTrack)
        delay(2000)
        inputEventParser.debugSend(InputEvent.NextTrack)

        if (!sunRoofIsOpen) {
            sunroofOpener.openSunroof()


            trackInfoPrinter.onNewTrackInfo("123456789", "Dole", "Wat")

            sunRoofIsOpen = true
        }

    }

    var sunRoofIsOpen = false
}