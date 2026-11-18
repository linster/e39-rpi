package ca.stefanm.ibus.configuration


interface CarPlatformConfiguration {
    /**
     * Which product the HMI is built into (linster/e39-rpi-yocto#11), and so
     * which hardware and features it has. Not where it runs: a development
     * laptop runs any machine type (its windows: WindowManagerConfig.kioskWindows).
     */
    val machineType : MachineType

    enum class MachineType(
        /** The BMW side: IBus (through the Pico or a USB interface) and the BMBT screen. */
        val hasBmwIBus : Boolean,
        /** The PIM bridge: calendar, mail, contacts, accounts. */
        val hasPim : Boolean,
    ) {
        /** The BMW E39 head unit. */
        E39(hasBmwIBus = true, hasPim = true),
        /** A Volvo P2: Waveshare 4" DSI LCD, a GPIO knob, Bluetooth audio to a speaker. */
        VOLVO_P2(hasBmwIBus = false, hasPim = true),
        /** M5Stack CardputerZero: 512 MB, a keyboard, HDMI when plugged in. */
        CARDPUTER_ZERO(hasBmwIBus = false, hasPim = false);

        /**
         * Service groups (car/platform/ServiceGroups.kt) for hardware this
         * machine type doesn't have: never started, also when
         * ServiceGroupsOnStartup lists them or is empty ("all").
         */
        val unavailableServiceGroups : Set<String>
            get() = buildSet {
                if (!hasBmwIBus) {
                    addAll(listOf("SerialInterface", "SerialInterfaceDebug", "PicoToPiParsers",
                        "TelephoneControlSimulation", "TvModuleAnnounceSim"))
                }
                if (!hasPim) {
                    add("Pim")
                }
            }
    }

    /**
     * A Pico sits between the Pi and the car (and switches the BMBT's video
     * input): an E39 whose serial port goes to the Pico.
     */
    val hasPico : Boolean
        get() = machineType.hasBmwIBus && iBusConnection == IBusConnection.PICO

    /**
     * The HMI has its own web browser (for OAuth logins and Wi-Fi captive
     * portals). A machine may ship without it, and a laptop may use it
     * instead of the desktop's browser.
     */
    val hasBrowser : Boolean
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
                "machineType: $machineType," +
                "hasBrowser: $hasBrowser," +
                "iBusInterfaceUri: $iBusInterfaceUri," +
                "baudRate: $serialPortBaudRate, " +
                "iBusConnection: $iBusConnection, " +
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

    /**
     * What the serial port ([iBusInterfaceUri]) is connected to. Not derivable from the baud rate:
     * the Pico's CAR_WITH_PI firmware talks to the Pi at 9600 8E1, the same as the car bus.
     */
    enum class IBusConnection {
        /** The Pico's UART link (point to point): the Pico forwards to and from the car bus. */
        PICO,
        /** The car's I/K-bus itself, through a USB IBus interface: the Pi is one of several talkers. */
        DIRECT_BUS
    }
    val iBusConnection : IBusConnection get() = IBusConnection.PICO

    enum class TrackInfoPrinterType { BMBT, CLI } //Do we stub out track info printing to the cli?

    val trackInfoPrinter : TrackInfoPrinterType

    /** Empty list means start all service groups */
    val listOfServiceGroupsOnStartup : List<String>
}

class LaptopDeviceConfiguration : CarPlatformConfiguration {
    override val machineType = CarPlatformConfiguration.MachineType.E39
    override val hasBrowser = false
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