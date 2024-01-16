package ca.stefanm.ibus.configuration


interface CarPlatformConfiguration {
    val isPi : Boolean
    val iBusInterfaceUri : String
    val displayDriver : DisplayDriver

    enum class DisplayDriver { TV_MODULE, MK4 }

    val pairedPhone : PairedPhone?

    data class PairedPhone(
        val friendlyName : String,
        val macAddress : List<Int>
    )

    fun toDebugString() : String {
        return "DeviceConfiguration(" +
                "isPi: $isPi," +
                "iBusInterfaceUri: $iBusInterfaceUri," +
                "baudRate: $serialPortBaudRate" +
                "displayDriver: $displayDriver," +
                "pairedPhone : $pairedPhone"
    }

    enum class SerialPortReadMode { NON_BLOCKING, BLOCKING }
    enum class SerialPortWriteMode { NON_BLOCKING, BLOCKING }

    val serialPortReadMode : SerialPortReadMode
    val serialPortWriteMode : SerialPortWriteMode

    //TODO expose 8N1, 8E1, and baudrate here.

    enum class PicoSerialBaudRate{
        SERIAL_115200_8N1,
        SERIAL_9600_8E1
    }
    val serialPortBaudRate : PicoSerialBaudRate

    enum class TrackInfoPrinterType { BMBT, CLI } //Do we stub out track info printing to the cli?

    val trackInfoPrinter : TrackInfoPrinterType

    /** Empty list means start all service groups */
    val listOfServiceGroupsOnStartup : List<String>
}

class Pi3BPlusDeviceConfiguration() : CarPlatformConfiguration {
    override val isPi = true
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = CarPlatformConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = CarPlatformConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )

    override fun toString() = toDebugString()

    override val serialPortReadMode = CarPlatformConfiguration.SerialPortReadMode.BLOCKING
    override val serialPortWriteMode = CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING

    override val serialPortBaudRate = CarPlatformConfiguration.PicoSerialBaudRate.SERIAL_115200_8N1

    override val trackInfoPrinter = CarPlatformConfiguration.TrackInfoPrinterType.BMBT

    override val listOfServiceGroupsOnStartup: List<String> = emptyList()
}

class LaptopDeviceConfiguration : CarPlatformConfiguration {
    override val isPi = false
//    override val iBusInterfaceUri = "/dev/ttyS5"
    override val iBusInterfaceUri = "/dev/ttyUSB0"
    override val displayDriver = CarPlatformConfiguration.DisplayDriver.TV_MODULE

    override val pairedPhone = CarPlatformConfiguration.PairedPhone(
        friendlyName = "Stefan's Pixel 2",
        macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
    )

    override fun toString() = toDebugString()


    override val serialPortReadMode = CarPlatformConfiguration.SerialPortReadMode.BLOCKING
    override val serialPortWriteMode = CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING

    override val serialPortBaudRate = CarPlatformConfiguration.PicoSerialBaudRate.SERIAL_115200_8N1

    override val trackInfoPrinter = CarPlatformConfiguration.TrackInfoPrinterType.CLI

    override val listOfServiceGroupsOnStartup: List<String> = emptyList()
}