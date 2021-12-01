package ca.stefanm.ibus

import ca.stefanm.ibus.car.conduit.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.car.conduit.configuration.LaptopDeviceConfiguration
import javax.inject.Inject

class CliArgParser @Inject constructor() {
    fun argsToDeviceConfiguration(args : Array<String>) : CarPlatformConfiguration {
        return LaptopDeviceConfiguration()
    }
}