package ca.stefanm.ibus.lib.system

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject

/** One time zone of tzdata's zone1970.tab: "America/Toronto", [CA, BS], "Eastern - ON & QC (most areas)". */
data class ZoneEntry(
    val id: String,
    val countryCodes: List<String>,
    val comment: String,
) {
    /** "Toronto", "St Johns", "Buenos Aires" (from America/Argentina/Buenos_Aires). */
    val city: String get() = id.substringAfterLast('/').replace('_', ' ')

    /** The first part of the id: "America", "Europe"; "Etc" for UTC and the like. */
    val region: String get() = if ('/' in id) id.substringBefore('/') else "Etc"
}

object ZoneTab {
    /** zone1970.tab / zone.tab: "codes<TAB>coordinates<TAB>TZ[<TAB>comments]"; # comments. */
    fun parse(text: String): List<ZoneEntry> = text.lineSequence()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .mapNotNull { line ->
            val columns = line.split('\t')
            if (columns.size < 3) null
            else ZoneEntry(columns[2], columns[0].split(','), columns.getOrElse(3) { "" })
        }
        .toList()
}

/**
 * The system time zone, for Settings -> Time Zone.
 *
 * On the e39-rpi image the HMI (user hmi) can't set it itself (no polkit):
 * it writes a request ("set <zone>" or "auto") to [REQUEST], a folder only
 * hmi may write to, and the image's e39rpi-timezone helper (root; e39-rpi-yocto
 * meta-e39rpi-base) carries it out and writes the outcome to [RESULT].
 * Automatic follows what the system names (the installed map's zone).
 */
class TimeZoneSettings @Inject constructor(
    private val logger: Logger,
) {
    companion object {
        const val TAG = "TimeZoneSettings"
        val REQUEST = File("/run/e39rpi-timezone/request")
        val STATE = File("/var/lib/e39rpi-timezone")
        val RESULT = File(STATE, "result")
        val MODE = File(STATE, "mode")
        private val LOCALTIME = File("/etc/localtime")
        private val ZONE_TABS = listOf(File("/usr/share/zoneinfo/zone1970.tab"), File("/usr/share/zoneinfo/zone.tab"))
        private const val RESULT_TIMEOUT_MS = 15_000L
    }

    enum class Mode { AUTOMATIC, MANUAL }

    /** Every zone tzdata lists (empty if it isn't installed). */
    fun zones(): List<ZoneEntry> =
        ZONE_TABS.firstOrNull { it.canRead() }?.let { ZoneTab.parse(it.readText()) } ?: emptyList()

    /** The zone the system uses now. */
    fun current(): ZoneId = ZoneId.systemDefault()

    /**
     * Follow a change of the system's zone made outside the HMI (the map
     * installer in automatic mode, later the car's position): the JVM reads
     * /etc/localtime once, at start-up. Cheap; called by the clock's tick.
     */
    fun followSystemZone() {
        val target = runCatching { java.nio.file.Files.readSymbolicLink(LOCALTIME.toPath()).toString() }.getOrNull() ?: return
        val id = target.substringAfter("zoneinfo/", "")
        if (id.isEmpty() || id == ZoneId.systemDefault().id) return
        runCatching { ZoneId.of(id) }.onSuccess {
            logger.i(TAG, "The system's time zone changed to $id; following it")
            TimeZone.setDefault(TimeZone.getTimeZone(it))
        }
    }

    /** The helper's mode; it starts in automatic. */
    fun mode(): Mode =
        if (MODE.canRead() && MODE.readText().trim() == "manual") Mode.MANUAL else Mode.AUTOMATIC

    /** Can this system set the zone for the HMI (is the image's helper there)? */
    fun canChange(): Boolean = REQUEST.parentFile.isDirectory

    /** Use [zone] (and stop following the map). */
    suspend fun set(zone: String): Result<ZoneId> = request("set $zone")

    /** Follow what the system names (the installed map's zone). */
    suspend fun automatic(): Result<ZoneId> = request("auto")

    private suspend fun request(line: String): Result<ZoneId> = withContext(Dispatchers.IO) {
        runCatching {
            check(canChange()) { "this system can't (no ${REQUEST.parent}: the image's e39rpi-timezone helper)" }
            val before = RESULT.lastModified()
            logger.i(TAG, "Time zone request: $line")
            REQUEST.writeText("$line\n")
            val waitedUntil = System.currentTimeMillis() + RESULT_TIMEOUT_MS
            while (RESULT.lastModified() == before) {
                check(System.currentTimeMillis() < waitedUntil) { "no answer from e39rpi-timezone" }
                delay(200)
            }
            val result = RESULT.readText().trim()
            check(result.startsWith("ok ")) { result.removePrefix("error ") }
            // The JVM read the zone at start-up: switch it too, so the
            // clock and everything else in the HMI follow.
            ZoneId.of(result.removePrefix("ok ")).also { TimeZone.setDefault(TimeZone.getTimeZone(it)) }
        }.onFailure { logger.w(TAG, "Time zone request '$line' failed: ${it.message}") }
    }
}
