package ca.stefanm.ibus.configuration


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

    fun toDebugString() : String {
        return "DeviceConfiguration(" +
                "isPi: $isPi," +
                "iBusInterfaceUri: $iBusInterfaceUri," +
                "displayDriver: $displayDriver," +
                "pairedPhone : $pairedPhone"
    }

    enum class SerialPortReadMode { NON_BLOCKING, BLOCKING }
    enum class SerialPortWriteMode { NON_BLOCKING, BLOCKING }

    val serialPortReadMode : SerialPortReadMode
    val serialPortWriteMode : SerialPortWriteMode

    enum class TrackInfoPrinterType { BMBT, CLI } //Do we stub out track info printing to the cli?

    val trackInfoPrinter : TrackInfoPrinterType
}

class Pi3BPlusDeviceConfiguration() : DeviceConfiguration {
    override val isPi = true
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = DeviceConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )

    override fun toString() = toDebugString()

    override val serialPortReadMode = DeviceConfiguration.SerialPortReadMode.BLOCKING
    override val serialPortWriteMode = DeviceConfiguration.SerialPortWriteMode.NON_BLOCKING

    override val trackInfoPrinter = DeviceConfiguration.TrackInfoPrinterType.BMBT

}

class LaptopDeviceConfiguration : DeviceConfiguration {
    override val isPi = false
    override val iBusInterfaceUri = "/dev/ttyS5"
//    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = DeviceConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = DeviceConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )

    override fun toString() = toDebugString()


    override val serialPortReadMode = DeviceConfiguration.SerialPortReadMode.BLOCKING
    override val serialPortWriteMode = DeviceConfiguration.SerialPortWriteMode.NON_BLOCKING

    override val trackInfoPrinter = DeviceConfiguration.TrackInfoPrinterType.CLI

}