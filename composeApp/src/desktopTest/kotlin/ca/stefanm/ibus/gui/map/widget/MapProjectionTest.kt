package ca.stefanm.ibus.gui.map.widget

import ca.stefanm.ibus.gui.map.Extents
import ca.stefanm.ibus.gui.map.latLngToPixels
import ca.stefanm.ibus.gui.map.latLngToTile
import ca.stefanm.ibus.gui.map.pixelsToLatLng
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MapProjectionTest {

    private val parliament = LatLng(45.4236, -75.7009)
    private val extents = Extents(parliament, MapScale.KILOMETERS_1_6)

    @Test
    fun `tile corners land on 256 px boundaries`() {
        val zoom = 13
        val (tx, ty) = ExtentCalculator.getTileNumber(parliament.latitude, parliament.longitude, zoom)
        val startX = tx - 2
        val startY = ty - 2
        for (scaling in listOf(1F, 2F)) {
            // The top-left corner of the tile two right and three down.
            val corner = LatLng(ExtentCalculator.tile2lat(startY + 3, zoom), ExtentCalculator.tile2lon(startX + 2, zoom))
            val p = latLngToPixels(corner, extents, startX, startX + 4, startY, startY + 4, zoom, scaling)
            // LatLng keeps 6 decimals: up to ~0.006 px at zoom 13.
            assertEquals(2 * 256.0 * scaling, p.fromLeft, 0.02)
            assertEquals(3 * 256.0 * scaling, p.fromTop, 0.02)
        }
    }

    @Test
    fun `pixels and positions convert both ways`() {
        val zoom = 15
        val (tx, ty) = ExtentCalculator.getTileNumber(parliament.latitude, parliament.longitude, zoom)
        val p = latLngToPixels(parliament, extents, tx - 3, tx + 3, ty - 2, ty + 2, zoom, 1F)
        val back = pixelsToLatLng(p.fromLeft, p.fromTop, tx - 3, ty - 2, zoom, 1F)
        assertEquals(parliament.latitude, back.latitude, 1e-6)
        assertEquals(parliament.longitude, back.longitude, 1e-6)
    }

    @Test
    fun `the fractional tile agrees with the tile number`() {
        for (zoom in listOf(6, 13, 16)) {
            val (x, y) = latLngToTile(parliament, zoom)
            assertEquals(ExtentCalculator.getTileNumber(parliament.latitude, parliament.longitude, zoom),
                Pair(x.toInt(), y.toInt()))
        }
    }
}
