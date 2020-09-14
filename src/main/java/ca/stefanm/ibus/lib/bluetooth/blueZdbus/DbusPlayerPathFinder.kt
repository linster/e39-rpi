package ca.stefanm.ibus.lib.bluetooth.blueZdbus

import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import javax.inject.Inject

class DbusPlayerPathFinder @Inject constructor(
    private val deviceConfiguration: DeviceConfiguration
) {

    fun getPlayerPath() : String {
        return ""
    }
}