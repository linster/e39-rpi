package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.annotations.services.PlatformServiceGroup
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.platform.Service
import kotlin.reflect.KClass

data class DiscoveredPlatformServiceGroup(
    val name: String,
    val description: String
)

data class DiscoveredServiceInfo(
    val name: String,
    val description: String,
    val implementingClass: KClass<*>,
    val accessor: ConfiguredCarComponent.() -> Service
)

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

@PlatformServiceGroup(
    name = "TelephoneControlSimulation",
    description = "Simulate BMW Telephone Control Unit? May conflict with pre-installed " +
            "telephone computer."
)
annotation class TelephoneControlSimulationServiceGroup

@PlatformServiceGroup(
    name = "Empty Group",
    description = "Enable this group and disable all others if for some reason you don't want any services to run."
)
annotation class EmptyServiceGroup