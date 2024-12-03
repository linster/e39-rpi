package ca.stefanm.ca.stefanm.ibus.car.debug

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ca.stefanm.ibus.car.platform.CliPrinterServiceGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
// TODO the IbusMessage UByteArray.toIbusMessage() : IBusMessage? doesn't handle
// TODO it well when it's a device we don't know about.
//@ExperimentalCoroutinesApi
//@ConfiguredCarScope
//@PlatformServiceInfo(
//    name = "UnknownIbusMessageLogger",
//    description = "Prints every IBusMessage that comes in to the logger."
//)
//@CliPrinterServiceGroup
//class UnknownIbusMessageLogger {
//}