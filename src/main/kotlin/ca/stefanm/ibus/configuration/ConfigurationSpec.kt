package ca.stefanm.ibus.configuration

import com.javadocmd.simplelatlng.LatLng
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec


object HmiVersion : ConfigSpec() {
    val gitHash by optional("<no version set>",
        "gitHash", description = "Git Hash of the e39-rpi commit")
}

object E39Config : ConfigSpec() {

    object CarPlatformConfigSpec : ConfigSpec() {

        enum class InitialCarPlatformConfiguration {
            LAPTOP,
            RPI
        }

        val initialCarPlatformConfiguration by optional(
            InitialCarPlatformConfiguration.LAPTOP,
            "initialCarPlatformConfiguration",
            "If we start the platform without a configuration, do we start it as the laptop or the pi?"
        )

        fun toCarPlatformConfiguration(config: Config) : CarPlatformConfiguration {
            return object : CarPlatformConfiguration {
                override val isPi: Boolean get() = config[_isPi]
                override val iBusInterfaceUri: String get() = config[_iBusInterfaceUri]
                override val displayDriver get() = config[_displayDriver]
                override val pairedPhone get() = config[_pairedPhone]
                override val serialPortReadMode get() = config[_serialPortReadMode]
                override val serialPortWriteMode get() = config[_serialPortWriteMode]
                override val trackInfoPrinter get() = config[_trackInfoPrinter]
            }
        }


        val _isPi by optional(false,
            "isPi", description = "Is E39 running on the RPi or a laptop?")

        val _iBusInterfaceUri by optional("/dev/ttyUSB0",
            name = "iBusInterfaceUri", description = "Device file for the LIN transceiver"
        )
        val _displayDriver by optional(
            CarPlatformConfiguration.DisplayDriver.MK4,
            "DisplayDriver",
            "Which Nav Computer is installed in the car driving the stock display?")
        val _pairedPhone by optional(
            CarPlatformConfiguration.PairedPhone(
                friendlyName = "Stefan's Pixel 2",
                macAddress = listOf(0x40, 0x4E, 0x36, 0xB9, 0x47, 0x3E)
            ),
            "PairedPhone",
            "Which phone is being used for track changing and printing?")
        val _serialPortReadMode by optional(
            CarPlatformConfiguration.SerialPortReadMode.BLOCKING,
            "SerialPortReadMode",
        "Which impl of the IBus reader to use?")
        val _serialPortWriteMode by optional(
            CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING,
            "SerialPortWriteMode",
            "Which impl of the IBus writer to use?", )
        val _trackInfoPrinter by optional(
            CarPlatformConfiguration.TrackInfoPrinterType.BMBT,
            "TrackInfoPrinter",
            "Which trackInfoPrinter to use?"
        )
    }

    object WindowManagerConfig : ConfigSpec() {

        val mainWindowUndecorated by optional(
            false,
            "DEBUG_MAIN_DECORATION",
            "Open Main Window with titlebar chrome. False for debug."
        )

        val hmiShiftRight by optional(
            false,
            "DEBUG_HMI_SHIFT_RIGHT",
            "Open HMI window shifted to the right. True for debug."
        )
    }

    object LoadingWindowConfig : ConfigSpec() {

        val autoLaunchHmi by optional(
            false,
            "AutoLaunchHmi",
            "Should Open HMI automatically from loading window?"
        )

        val autoLaunchPlatformOnOpen by optional(
            true,
            "AutoLaunchCarPlatformOnOpen",
            "Do we start the platform on opening the loading window?"
        )

        //Auto-launch platform on open?
        //Hide Menu?
    }

    object MapConfig : ConfigSpec() {

        val showDebugInfoOnTiles by optional(
            false,
            "showDebugInfoOnTiles",
            "Should each mapTile have its osm x, y, zoom printed on it in the UI?"
        )

        val defaultMapCenter by optional(
            Pair(45.3154699,-75.9194058),
            "defaultMapCenter",
            "default LatLng to open all maps at. Default is in Kanata, ON, Canada."
        )

        object GuidanceService : ConfigSpec() {
            val hereMapsApiToken by optional(
                "",
                "HereMapsApiToken",
                "API Token for HERE Maps Guidance service."
            )
        }
    }
}