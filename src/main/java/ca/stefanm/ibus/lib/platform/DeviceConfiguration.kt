package ca.stefanm.ibus.lib.platform


interface DeviceConfiguration {
    val isPi : Boolean
    val iBusInterfaceUri : String
    val displayDriver : DisplayDriver

    enum class DisplayDriver { TV_MODULE, MK4 }

    val pairedPhone : PairedPhone

    data class PairedPhone(
        val friendlyName : String,
        val macAddress : List<Int>
    )
}

class Pi3BPlusDeviceConfiguration() : DeviceConfiguration {
    override val isPi = true
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = DeviceConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )
}

class LaptopDeviceConfiguration : DeviceConfiguration {
    override val isPi = false
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = DeviceConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )
}