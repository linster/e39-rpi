package ca.stefanm.ibus.lib.hardwareDrivers.gps

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.javadocmd.simplelatlng.LatLng
import java.time.Instant

/** Where gpsd says the car is: a TPV with a 2D or 3D fix. */
data class GpsFix(
    val latLng: LatLng,
    val mode: Mode,
    /** Over the ground, m/s. */
    val speedMps: Double?,
    /** Course over the ground, degrees from true north. */
    val trackDeg: Double?,
    /** The fix's time (the receiver's clock, UTC). */
    val time: Instant?,
    /** Horizontal error estimate, metres (gpsd's eph, else the larger of epx and epy). */
    val accuracyM: Double?,
) {
    enum class Mode { TWO_D, THREE_D }
}

/** What GpsdService knows about the position, for a reception indicator. */
enum class GpsState {
    /** gpsd isn't there (not running, or not reachable). */
    NO_GPSD,
    /** gpsd is there, without a receiver. */
    NO_RECEIVER,
    /** A receiver, but no fix yet. */
    SEARCHING,
    /** A current fix. */
    FIX,
    /** A fix was lost, or is older than the stale time: the last known position. */
    STALE,
}

/** Satellites from gpsd's last SKY. */
data class GpsSatellites(val used: Int, val visible: Int)

/**
 * The gpsd JSON messages GpsdService uses (https://gpsd.io/gpsd_json.html).
 * Fields can be missing at any mode, so everything is optional.
 */
sealed interface GpsdMessage {
    data class Version(val release: String?) : GpsdMessage

    /** The receivers gpsd has: all of them (replaces what was known). */
    data class Devices(val paths: List<String>) : GpsdMessage

    /** One receiver came or went (`activated` 0 when it's closed, e.g. unplugged). */
    data class Device(val path: String, val active: Boolean) : GpsdMessage

    data class Tpv(
        val device: String?,
        /** 0 unknown, 1 no fix, 2 2D, 3 3D. */
        val mode: Int,
        val lat: Double?,
        val lon: Double?,
        val speed: Double?,
        val track: Double?,
        val time: Instant?,
        val eph: Double?,
        val epx: Double?,
        val epy: Double?,
    ) : GpsdMessage {
        /** The fix, if this TPV has one (mode 2 or 3, with a position). */
        fun toFix(): GpsFix? {
            if (mode < 2 || lat == null || lon == null) return null
            if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null
            return GpsFix(
                latLng = LatLng(lat, lon),
                mode = if (mode >= 3) GpsFix.Mode.THREE_D else GpsFix.Mode.TWO_D,
                speedMps = speed,
                trackDeg = track,
                time = time,
                accuracyM = eph ?: if (epx != null && epy != null) maxOf(epx, epy) else null,
            )
        }
    }

    data class Sky(val satellites: GpsSatellites) : GpsdMessage

    /** Anything else (WATCH, PPS, ERROR, ...). */
    data class Other(val messageClass: String?) : GpsdMessage

    companion object {
        /** Turns on JSON reports: what a client sends after connecting. */
        const val WATCH = "?WATCH={\"enable\":true,\"json\":true};\n"

        /** One line from gpsd; null if it isn't a JSON object. */
        fun parse(line: String): GpsdMessage? {
            val json = try {
                JsonParser.parseString(line)
            } catch (e: Exception) {
                return null
            }
            if (!json.isJsonObject) return null
            val o = json.asJsonObject
            return when (val cls = o.string("class")) {
                "VERSION" -> Version(o.string("release"))
                "DEVICES" -> Devices(
                    o.array("devices").mapNotNull { d ->
                        d.takeIf { it.isJsonObject }?.asJsonObject?.string("path")
                    }
                )
                "DEVICE" -> o.string("path")?.let { Device(it, isActivated(o.get("activated"))) }
                    ?: Other(cls)
                "TPV" -> Tpv(
                    device = o.string("device"),
                    mode = o.double("mode")?.toInt() ?: 0,
                    lat = o.double("lat"),
                    lon = o.double("lon"),
                    speed = o.double("speed"),
                    track = o.double("track"),
                    time = o.string("time")?.let { t ->
                        try { Instant.parse(t) } catch (e: Exception) { null }
                    },
                    eph = o.double("eph"),
                    epx = o.double("epx"),
                    epy = o.double("epy"),
                )
                "SKY" -> {
                    val sats = o.array("satellites").filter { it.isJsonObject }.map { it.asJsonObject }
                    val visible = o.double("nSat")?.toInt() ?: sats.size
                    val used = o.double("uSat")?.toInt() ?: sats.count { it.boolean("used") == true }
                    // A SKY without satellites (newer gpsd sends some with only DOPs) says nothing about them.
                    if (o.has("nSat") || o.has("uSat") || o.has("satellites")) {
                        Sky(GpsSatellites(used = used, visible = visible))
                    } else {
                        Other(cls)
                    }
                }
                else -> Other(cls)
            }
        }

        /** `activated` is the time it was activated (a string, or a number in old gpsd); 0 or missing: not active. */
        private fun isActivated(e: JsonElement?): Boolean {
            if (e == null || e.isJsonNull || !e.isJsonPrimitive) return false
            val p = e.asJsonPrimitive
            return when {
                p.isNumber -> p.asDouble != 0.0
                p.isString -> p.asString.isNotBlank() && p.asString != "0"
                else -> false
            }
        }

        private fun JsonObject.string(name: String): String? =
            get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString

        private fun JsonObject.double(name: String): Double? =
            get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asDouble?.takeIf { it.isFinite() }

        private fun JsonObject.boolean(name: String): Boolean? =
            get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }?.asBoolean

        private fun JsonObject.array(name: String): List<JsonElement> =
            get(name)?.takeIf { it.isJsonArray }?.asJsonArray?.toList() ?: emptyList()
    }
}
