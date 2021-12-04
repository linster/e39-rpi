package ca.stefanm.ibus

import ca.stefanm.ibus.car.platform.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.LaptopDeviceConfiguration
import javax.inject.Inject

class CliArgParser @Inject constructor() {
    fun argsToDeviceConfiguration(args : Array<String>) : CarPlatformConfiguration {
        return LaptopDeviceConfiguration()
    }
}