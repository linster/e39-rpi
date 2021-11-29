package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.hardwareDrivers.CoolingFanController
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPublisherService
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IbusInputEventCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IncomingIbusMessageCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.PlatformMetronomeLogger
import ca.stefanm.ibus.stefane39.TelephoneButtonVideoSwitcherService
import javax.inject.Inject


data class PlatformServiceGroup(
    val name : String,
    val description: String,
    val children : List<PlatformService>
)

@ConfiguredCarScope
class PlatformServiceList @Inject constructor(
    bluetoothService: BluetoothService,

    ibusInputEventCliPrinter: IbusInputEventCliPrinter,
    incomingIbusMessageCliPrinter: IncomingIbusMessageCliPrinter,
    platformMetronomeLogger: PlatformMetronomeLogger,

    serialPublisherService: SerialPublisherService,
    serialListenerService: SerialListenerService,

    iBusInputMessageParser: IBusInputMessageParser,

    coolingFanController: CoolingFanController,
    telephoneButtonVideoSwitcherService: TelephoneButtonVideoSwitcherService,

    logger: Logger

    ) {

    val list : List<PlatformServiceGroup> = listOf(
        PlatformServiceGroup(
            name = "Bluetooth",
            description = "Services that interface with DBus to interact with the AVRCP",
            children = listOf(
                PlatformService(
                    name = "BluetoothService",
                    description = "Monolithic bluetooth service. TODO Should be split up.",
                    baseService = bluetoothService,
                    logger = logger
                )
            )
        ),

        PlatformServiceGroup(
            name = "CliPrinters",
            description = "Services that print debug info to stdout.",
            children = listOf(
                PlatformService(
                    name = "PlatformMetronomeLogger",
                    description = "Prints ticks to stdout at an interval to show the Car Platform is running.",
                    baseService = platformMetronomeLogger,
                    logger = logger
                ),
                PlatformService(
                    name = "IncomingIbusMessageCliPrinter",
                    description = "Prints incoming IBusMessages to stdout",
                    baseService = incomingIbusMessageCliPrinter,
                    logger = logger
                ),
                PlatformService(
                    name = "IbusInputEventCliPrinter",
                    description = "Prints IBusInputEvents to stdout. " +
                            "This is an abstraction over messages to indicate actions.",
                    baseService = ibusInputEventCliPrinter,
                    logger = logger
                )
            )
        ),

        PlatformServiceGroup(
            name = "SerialInterface",
            description = "Services that interact with the LIN transceiver",
            children = listOf(
                PlatformService(
                    name = "SerialPublisherService",
                    description = "Sends serial messages to the IBus dongle.",
                    baseService = serialPublisherService,
                    logger = logger
                ),
                PlatformService(
                    name = "SerialListenerService",
                    description = "Listens for serial mesages from the IBus dongle.",
                    baseService = serialListenerService,
                    logger = logger
                ),
                PlatformService(
                    name = "IbusInputMessageParser",
                    description = "Parses IBus packets into IBusInputMessages",
                    baseService = iBusInputMessageParser,
                    logger = logger
                )
            )
        ),

        PlatformServiceGroup(
            name = "Peripherals",
            description = "Various peripherals",
            children = listOf(
                PlatformService(
                    name = "CoolingFanController",
                    description = "Turns on the cooling fan in the case.",
                    baseService = coolingFanController,
                    logger = logger
                ),
                PlatformService(
                    name = "TelephoneButtonVideoSwitcherService",
                    description = "Switches the TV Module video input on BMBT Telephone button press",
                    baseService = telephoneButtonVideoSwitcherService,
                    logger = logger
                )
            )
        )
    )
}