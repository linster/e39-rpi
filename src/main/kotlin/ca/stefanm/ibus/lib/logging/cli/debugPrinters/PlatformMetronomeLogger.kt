package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ca.stefanm.ibus.car.platform.CliPrinterServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

@PlatformServiceInfo(
    name = "PlatformMetronomeLogger",
    description = "Prints ticks to stdout at an interval to show the Car Platform is running."
)
@CliPrinterServiceGroup
class PlatformMetronomeLogger @Inject constructor(
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {

    var prevTime : Long = 0L

    override suspend fun doWork() {

        val currentTime = Date().toInstant().epochSecond
        val delta = currentTime - prevTime
        prevTime = currentTime

        logger.v("Metronome", "Tick. $delta")
        delay(10 * 1000)
    }
}