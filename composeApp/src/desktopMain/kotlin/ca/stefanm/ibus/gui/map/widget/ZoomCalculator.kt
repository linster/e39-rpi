package ca.stefanm.ibus.gui.map.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import ca.stefanm.ibus.gui.map.Extents
import com.javadocmd.simplelatlng.LatLng
import kotlin.math.abs

object ZoomCalculator {

    /** A tile's size, as [MapViewer] draws it (RawTileGrid, latLngToPixels). */
    private const val TILE_SIZE_DP = 256

    /**
     * Whether [location] is on screen in a map of [viewportWidth] x
     * [viewportHeight] showing [extents] (centred on its centre, at its
     * zoom), edges included.
     *
     * Done as [MapViewer] places things: Web Mercator tile coordinates
     * ([latLngToTile]) at the scale's zoom level, and 256 dp tiles. The tiles
     * and the viewport are both in dp, so the window's density (pixels per
     * dp) would scale both sides alike: it doesn't change the answer.
     */
    fun isLatLngInZoom(
        location : LatLng,
        extents: Extents,
        viewportWidth : Dp,
        viewportHeight : Dp
    ) : Boolean {
        val zoom = extents.mapScaleState.mapZoomLevel

        val (locationX, locationY) = latLngToTile(location, zoom)
        val (centerX, centerY) = latLngToTile(extents.centerState, zoom)

        // How far the location is from the middle of the viewport, in dp.
        val fromCenterXDp = (locationX - centerX) * TILE_SIZE_DP
        val fromCenterYDp = (locationY - centerY) * TILE_SIZE_DP

        // Beyond Web Mercator's latitudes (the poles) there's no position for it.
        if (!fromCenterXDp.isFinite() || !fromCenterYDp.isFinite()) {
            return false
        }

        return abs(fromCenterXDp) <= viewportWidth.value / 2.0 &&
                abs(fromCenterYDp) <= viewportHeight.value / 2.0
    }
}
