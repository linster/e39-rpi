package ca.stefanm.ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.annotations.services.PlatformServiceGroup

@PlatformServiceGroup(
    name = "Bluetooth",
    description = "Monolithic Bluetooth Service"
)
annotation class BluetoothServiceGroup

@PlatformServiceGroup(
    name = "CliPrinters",
    description = "Services that print debug info to stdout."
)
annotation class CliPrinterServiceGroup

@PlatformServiceGroup(
    name = "SerialInterface",
    description = "Services that interact with the (USB Modbmw) LIN transceiver"
)
annotation class SerialInterfaceServiceGroup

@PlatformServiceGroup(
    name = "Peripherals",
    description = "Various peripherals for RPI Case",
)
annotation class PeripheralsServiceGroup

@PlatformServiceGroup(
    name = "PeripheralsGen2",
    description = "Peripherals for 2nd gen hardware"
)
annotation class Peripherals2ServiceGroup