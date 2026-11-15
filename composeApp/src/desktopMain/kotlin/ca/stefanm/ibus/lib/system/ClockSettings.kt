package ca.stefanm.ibus.lib.system

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** What `timedatectl show` says about internet time. */
data class ClockStatus(
    /** Internet time (systemd-timesyncd) is on. */
    val internetTime: Boolean,
    /** The clock has been synchronized with a time server. */
    val synchronized: Boolean,
) {
    companion object {
        /** `timedatectl show -p NTP -p NTPSynchronized` (key=value lines). */
        fun parse(text: String): ClockStatus? {
            val values = text.lineSequence()
                .mapNotNull { line -> line.split('=', limit = 2).takeIf { it.size == 2 } }
                .associate { (key, value) -> key.trim() to value.trim() }
            val ntp = values["NTP"] ?: return null
            return ClockStatus(internetTime = ntp == "yes", synchronized = values["NTPSynchronized"] == "yes")
        }
    }
}

/**
 * The system clock for Settings -> Date & Time: internet time (NTP) on or
 * off, and the date and time set by hand. Reading is `timedatectl show`
 * (no privileges needed); changes go through the image's root helper
 * (e39rpi-timezone: "ntp on|off", "set-time YYYY-MM-DD HH:MM"), like the time
 * zone, as the HMI has no polkit for timedatectl. The JVM reads the system
 * clock directly, so the HMI's clock follows at once.
 */
class ClockSettings @Inject constructor(
    private val timeZoneSettings: TimeZoneSettings,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "ClockSettings"
        val SET_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        fun setTimeRequest(time: LocalDateTime) = "set-time ${time.format(SET_TIME_FORMAT)}"
    }

    fun canChange(): Boolean = timeZoneSettings.canChange()

    suspend fun status(): ClockStatus? = withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder("timedatectl", "show", "-p", "NTP", "-p", "NTPSynchronized")
                .redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            check(process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0) { output.trim().take(200) }
            ClockStatus.parse(output)
        }.onFailure { logger.w(TAG, "timedatectl show: ${it.message}") }.getOrNull()
    }

    suspend fun setInternetTime(on: Boolean): Result<Unit> =
        timeZoneSettings.helperRequest(if (on) "ntp on" else "ntp off").map { }

    /** Sets the clock (local time, to the minute); turns internet time off. */
    suspend fun setTime(time: LocalDateTime): Result<Unit> =
        timeZoneSettings.helperRequest(setTimeRequest(time)).map { }
}
