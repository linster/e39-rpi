package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * What the GPS settings screen says about the position: always the same lines
 * (the screen updates them in place).
 */
object GpsStatusText {

    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

    fun lines(
        source: String?,
        state: GpsState,
        fix: GpsFix?,
        satellites: GpsSatellites?,
        gpsd: GpsdSettings,
    ): List<String> = buildList {
        add("Source: ${source ?: "none"}")
        add("Status: " + status(source, state, fix))
        add("Position: " + (fix?.let { position(it) } ?: "none"))
        add("Moving: " + (moving(fix) ?: "unknown"))
        add("Last fix: " + (fix?.time?.let { timeFormat.format(it) } ?: "none"))
        add("Satellites: " + (satellites?.let { "${it.used} used, ${it.visible} in view" } ?: "unknown"))
        add("gpsd: ${gpsd.target}")
    }

    fun status(source: String?, state: GpsState, fix: GpsFix?): String = when (state) {
        GpsState.NO_GPSD -> if (source == null) "No location service running" else "gpsd isn't reachable"
        GpsState.NO_RECEIVER -> "No GPS receiver"
        GpsState.SEARCHING -> "Searching for satellites"
        GpsState.FIX -> when (fix?.mode) {
            GpsFix.Mode.TWO_D -> "Fix (2D)"
            GpsFix.Mode.THREE_D -> "Fix (3D)"
            null -> "Fix"
        }
        GpsState.STALE -> "Stale: last known position"
    }

    private fun position(fix: GpsFix): String =
        String.format(Locale.ROOT, "%.5f, %.5f", fix.latLng.latitude, fix.latLng.longitude) +
                (fix.accuracyM?.let { " ±${it.roundToInt()} m" } ?: "")

    /** "50 km/h, heading 272°", either part alone, or null without either. */
    private fun moving(fix: GpsFix?): String? {
        if (fix == null) return null
        val parts = listOfNotNull(
            fix.speedMps?.let { "${(it * 3.6).roundToInt()} km/h" },
            fix.trackDeg?.let { "heading ${it.roundToInt() % 360}°" },
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }
}
