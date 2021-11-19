package ca.stefanm.ibus.lib.logging.cli.debugPrinters

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

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