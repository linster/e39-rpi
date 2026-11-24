package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GpsStatusTextTest {

    private val gpsd = GpsdSettings("localhost", 2947)

    @Test
    fun nothingRunning() {
        assertEquals(
            listOf(
                "Source: none",
                "Status: No location service running",
                "Position: none",
                "Moving: unknown",
                "Last fix: none",
                "Satellites: unknown",
                "gpsd: localhost:2947",
            ),
            GpsStatusText.lines(null, GpsState.NO_GPSD, null, null, gpsd)
        )
    }

    @Test
    fun fullFix() {
        val fix = GpsFix(
            latLng = LatLng(45.347, -75.919),
            mode = GpsFix.Mode.THREE_D,
            speedMps = 13.9,
            trackDeg = 271.6,
            time = Instant.parse("2026-11-23T20:01:02Z"),
            accuracyM = 6.3,
        )
        assertEquals(
            listOf(
                "Source: GpsdService",
                "Status: Fix (3D)",
                "Position: 45.34700, -75.91900 ±6 m",
                "Moving: 50 km/h, heading 272°",
                "Last fix: 20:01:02 UTC",
                "Satellites: 6 used, 9 in view",
                "gpsd: gps.local:3000",
            ),
            GpsStatusText.lines("GpsdService", GpsState.FIX, fix, GpsSatellites(6, 9), GpsdSettings("gps.local", 3000))
        )
    }

    @Test
    fun fakeFixWithoutExtras() {
        val fix = GpsFix(LatLng(45.0, -75.0), GpsFix.Mode.TWO_D, null, null, null, null)
        assertEquals(
            listOf(
                "Source: FakeLocationService",
                "Status: Fix (2D)",
                "Position: 45.00000, -75.00000",
                "Moving: unknown",
                "Last fix: none",
                "Satellites: unknown",
                "gpsd: localhost:2947",
            ),
            GpsStatusText.lines("FakeLocationService", GpsState.FIX, fix, null, gpsd)
        )
    }

    @Test
    fun movingPartsAlone() {
        val speedOnly = GpsFix(LatLng(45.0, -75.0), GpsFix.Mode.THREE_D, 10.0, null, null, null)
        val headingOnly = GpsFix(LatLng(45.0, -75.0), GpsFix.Mode.THREE_D, null, 359.7, null, null)
        assertEquals("Moving: 36 km/h", GpsStatusText.lines("A", GpsState.FIX, speedOnly, null, gpsd)[3])
        assertEquals("Moving: heading 0°", GpsStatusText.lines("A", GpsState.FIX, headingOnly, null, gpsd)[3])
    }

    @Test
    fun states() {
        assertEquals("gpsd isn't reachable", GpsStatusText.status("GpsdService", GpsState.NO_GPSD, null))
        assertEquals("No GPS receiver", GpsStatusText.status("GpsdService", GpsState.NO_RECEIVER, null))
        assertEquals("Searching for satellites", GpsStatusText.status("GpsdService", GpsState.SEARCHING, null))
        assertEquals("Stale: last known position", GpsStatusText.status("GpsdService", GpsState.STALE, null))
        assertEquals("Fix", GpsStatusText.status("GpsdService", GpsState.FIX, null))
    }
}
