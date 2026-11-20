package ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider.Companion.EDGE_MARGIN_DP
import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider.Companion.MAP_HEIGHT_DP
import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider.Companion.MAP_WIDTH_DP
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.pow

class GpsMapCenterProviderTest {

    private val start = LatLng(45.4215, -75.6972)
    private val zoom = MapScale.KILOMETERS_1_6
    private val gps = MutableStateFlow<LatLng?>(null)

    // Inside the margin, and within it, from the middle of the map.
    private val halfW = MAP_WIDTH_DP / 2
    private val halfH = MAP_HEIGHT_DP / 2
    private val insideX = halfW - EDGE_MARGIN_DP - 10
    private val insideY = halfH - EDGE_MARGIN_DP - 10
    private val nearX = halfW - EDGE_MARGIN_DP + 5
    private val nearY = halfH - EDGE_MARGIN_DP + 5

    private val logger = object : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    private fun TestScope.provider(follow: Boolean = false) =
        GpsMapCenterProvider(gps, backgroundScope, start, zoom, follow, logger).also { runCurrent() }

    /** The point [dx] dp right and [dy] dp down of [from] on the map at [scale] (inverse Web Mercator). */
    private fun at(from: LatLng, dx: Double, dy: Double, scale: MapScale = zoom): LatLng {
        val worldDp = 256.0 * 2.0.pow(scale.mapZoomLevel)
        val x = (from.longitude + 180.0) / 360.0 * worldDp + dx
        val lat0 = Math.toRadians(from.latitude)
        val y = (1.0 - kotlin.math.ln(kotlin.math.tan(lat0) + 1.0 / kotlin.math.cos(lat0)) / PI) / 2.0 * worldDp + dy
        val lng = x / worldDp * 360.0 - 180.0
        val lat = Math.toDegrees(atan(kotlin.math.sinh(PI * (1.0 - 2.0 * y / worldDp))))
        return LatLng(lat, lng)
    }

    private fun TestScope.drive(to: LatLng) {
        gps.value = to
        runCurrent()
    }

    private fun assertSame(expected: LatLng, actual: LatLng) {
        val (dx, dy) = GpsMapCenterProvider.offsetDp(expected, actual, zoom)
        assertTrue("$actual is ($dx, $dy) dp from $expected", kotlin.math.abs(dx) < 0.5 && kotlin.math.abs(dy) < 0.5)
    }

    @Test
    fun offsetIsInTileDp() {
        // One tile east is 256 dp at the scale's zoom level.
        val oneTileEast = LatLng(start.latitude, start.longitude + 360.0 / 2.0.pow(zoom.mapZoomLevel))
        // LatLng keeps 6 decimals: 1e-6 degrees is about 0.006 dp at this zoom.
        val dp = 0.01
        val (dx, dy) = GpsMapCenterProvider.offsetDp(start, oneTileEast, zoom)
        assertEquals(256.0, dx, dp)
        assertEquals(0.0, dy, dp)
        // The test's inverse agrees.
        val (bx, by) = GpsMapCenterProvider.offsetDp(start, at(start, 120.0, -80.0), zoom)
        assertEquals(120.0, bx, dp)
        assertEquals(-80.0, by, dp)
    }

    @Test
    fun doesNotFollowUntilAsked() = runTest {
        val p = provider()
        assertFalse(p.followGps.value)
        drive(at(start, 2000.0, 0.0))
        assertSame(start, p.center.value)
    }

    @Test
    fun followingByDefaultCentresOnTheFirstFix() = runTest {
        val p = provider(follow = true)
        assertTrue(p.followGps.value)
        assertSame(start, p.center.value)
        // Inside the margin, but the map was never on the car: centre on it.
        val car = at(start, 50.0, 30.0)
        drive(car)
        assertTrue(p.followGps.value)
        assertSame(car, p.center.value)
        // Then as usual: small moves stay.
        drive(at(car, 50.0, 20.0))
        assertSame(car, p.center.value)
    }

    @Test
    fun followingByDefaultAPanBeforeTheFirstFixStopsIt() = runTest {
        val p = provider(follow = true)
        val panned = at(start, 0.0, -256.0)
        p.onUserMapCenterChanged(panned, zoom)
        assertFalse(p.followGps.value)
        drive(at(start, 50.0, 30.0))
        assertSame(panned, p.center.value)
    }

    @Test
    fun followWithoutPositionIsRefused() = runTest {
        val p = provider()
        assertFalse(p.followGps(true))
        assertFalse(p.followGps.value)
        assertSame(start, p.center.value)
    }

    @Test
    fun followCentresOnTheGpsPoint() = runTest {
        val p = provider()
        val car = at(start, 300.0, 100.0)
        drive(car)
        assertTrue(p.followGps(true))
        assertTrue(p.followGps.value)
        assertSame(car, p.center.value)
    }

    @Test
    fun insideTheMarginTheMapStays() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        for ((dx, dy) in listOf(insideX to 0.0, -insideX to 0.0, 0.0 to insideY, 0.0 to -insideY, insideX to insideY)) {
            drive(at(start, dx, dy))
            assertSame(start, p.center.value)
        }
    }

    @Test
    fun nearEachEdgeTheMapRecentresOnTheGpsPoint() = runTest {
        for ((dx, dy) in listOf(nearX to 0.0, -nearX to 0.0, 0.0 to nearY, 0.0 to -nearY)) {
            gps.value = null
            val p = provider()
            drive(start)
            p.followGps(true)
            val car = at(start, dx, dy)
            drive(car)
            assertSame(car, p.center.value)
        }
    }

    @Test
    fun aJumpOffTheMapRecentres() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        val far = at(start, 5000.0, -3000.0)
        drive(far)
        assertSame(far, p.center.value)
    }

    @Test
    fun afterRecentringSmallMovesStay() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        val car = at(start, nearX, 0.0)
        drive(car)
        drive(at(car, 50.0, 20.0))
        assertSame(car, p.center.value)
    }

    @Test
    fun zoomKeepsFollowingAndRecentresIfThePointIsNowNearTheEdge() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        val car = at(start, 150.0, 0.0)
        drive(car)
        assertSame(start, p.center.value)
        // Zooming out: still inside.
        p.onUserMapCenterChanged(p.center.value, MapScale.KILOMETERS_3_2)
        assertTrue(p.followGps.value)
        assertSame(start, p.center.value)
        // Zooming in twice: the point is 600 dp out at the old centre.
        p.onUserMapCenterChanged(p.center.value, MapScale.METERS_400)
        assertTrue(p.followGps.value)
        assertSame(car, p.center.value)
    }

    @Test
    fun aPanStopsFollowing() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        val panned = at(start, 0.0, -256.0)
        p.onUserMapCenterChanged(panned, zoom)
        assertFalse(p.followGps.value)
        assertSame(panned, p.center.value)
        drive(at(start, 2000.0, 0.0))
        assertSame(panned, p.center.value)
    }

    @Test
    fun followGpsFalseStopsFollowing() = runTest {
        val p = provider()
        drive(start)
        p.followGps(true)
        assertTrue(p.followGps(false))
        assertFalse(p.followGps.value)
        drive(at(start, 2000.0, 0.0))
        assertSame(start, p.center.value)
    }
}
