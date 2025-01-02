package ca.stefanm.ibus

import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import javax.inject.Inject

class CliArgParser @Inject constructor() {
    fun argsToDeviceConfiguration(args : Array<String>) : CarPlatformConfiguration {
        return LaptopDeviceConfiguration()
    }
}