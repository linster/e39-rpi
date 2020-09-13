package ca.stefanm.ibus.lib.platform


interface DeviceConfiguration {
    val isPi : Boolean
    val iBusInterfaceUri : String
    val displayDriver : DisplayDriver

    enum class DisplayDriver { TV_MODULE, MK4 }
}

class Pi3BPlusDeviceConfiguration() : DeviceConfiguration {
    override val isPi = true
    override val iBusInterfaceUri = "/dev/ttyACM0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE
}

class LaptopDeviceConfiguration : DeviceConfiguration {
    override val isPi = false
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE
}