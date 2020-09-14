package ca.stefanm.ibus.lib.bluetooth

import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import javax.inject.Inject

//TODO For now we'll just hard-code which phone we're paired to.
//TODO Later, we'll wait here, present a UI, and allow the user
//TODO to choose which device to pair to (and unpair from).
class BluetoothOnScreenSetupManager @Inject constructor(
    private val pairedPhone: DeviceConfiguration.PairedPhone
) {

    suspend fun isPhonePaired() = true
    suspend fun getPairedPhone() = pairedPhone

    suspend fun requestBluetoothSetup() {
        //TODO here's where we'll use the ScreenPainter to ask the user
        //TODO to pair, wait for pairing to complete.
        //TODO this function will return once the pairing step is complete,
        //TODO and the user has connected the device to the rpi.
    }
}