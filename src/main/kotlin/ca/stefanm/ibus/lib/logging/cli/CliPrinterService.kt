package ca.stefanm.ibus.lib.logging.cli

import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IbusInputEventCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IncomingIbusMessageCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.PlatformMetronomeLogger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CliPrinterService @Inject constructor(
    ibusInputEventCliPrinter: IbusInputEventCliPrinter,
    incomingIbusMessageCliPrinter: IncomingIbusMessageCliPrinter,
    platformMetronomeLogger: PlatformMetronomeLogger
) : Service {

    interface CliPrinter {
        val name : String
        fun onPrinterEnabled()
        fun onPrinterDisabled()
    }

    //TODO someday, make this be settable at run-time.
    private val printers = mapOf<CliPrinter, Boolean>(
        ibusInputEventCliPrinter to true,
        incomingIbusMessageCliPrinter to true,
        platformMetronomeLogger to false
    )

    override fun onCreate() {
        printers.filter { it.value }.forEach { it.key.onPrinterEnabled() }
    }

    override fun onShutdown() {
        printers.forEach { (printer, _) ->
            printer.onPrinterDisabled()
        }
    }
}