package ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

/**
 * Where the browsing map is centred: where the user put it, or, while
 * following the GPS, on the car. Following, the map only pans when the GPS
 * point gets within [EDGE_MARGIN_DP] of the map's edge (or leaves it): then it
 * re-centres on the point, so the dot crosses most of the map between pans.
 *
 * The map widget (MapViewer) shows 256 dp Web Mercator tiles at the
 * MapScale's zoom level and reports its size ([onMapViewportChanged]), so
 * where a point lands on it is plain arithmetic.
 */
@ApplicationScope
class GpsMapCenterProvider internal constructor(
    private val gpsPositions: StateFlow<LatLng?>,
    coroutineScope: CoroutineScope,
    initialCenter: LatLng,
    initialZoom: MapScale,
    initialFollow: Boolean,
    private val logger: Logger
) {

    @Inject constructor(
        guidanceService: GuidanceService,
        configurationStorage: ConfigurationStorage,
        coroutineScope: CoroutineScope,
        logger: Logger
    ) : this(
        guidanceService.position,
        coroutineScope,
        configurationStorage.config[E39Config.MapConfig.defaultMapCenter].let { LatLng(it.first, it.second) },
        MapScale.KILOMETERS_1_6,
        configurationStorage.config[E39Config.MapConfig.followGpsByDefault],
        logger
    )

    companion object {
        const val TAG = "GpsMapCenterProvider"
        /**
         * MapViewer's size until it reports its own ([onMapViewportChanged]):
         * a pixel-doubled theme's, the window's 800 x 468 less the bottom bar.
         * It depends on the theme (the bottom bar's height) and on whether the
         * screen hides the bottom bar.
         */
        const val DEFAULT_WIDTH_DP = 800.0
        const val DEFAULT_HEIGHT_DP = 427.0
        /** Following, a GPS point this close to an edge re-centres the map on it. */
        const val EDGE_MARGIN_DP = 40.0
        /** A reported centre within this of [center] isn't a pan (rounding). */
        private const val SAME_CENTER_DP = 1.0
        private const val TILE_DP = 256.0

        /** [point]'s offset from [center] on the map at [zoom], in dp: (right, down). */
        //TODO this should be moved to one of the calculator classes. latlngtopixels??
        fun offsetDp(center: LatLng, point: LatLng, zoom: MapScale): Pair<Double, Double> {
            val worldDp = TILE_DP * 2.0.pow(zoom.mapZoomLevel)
            fun x(p: LatLng) = (p.longitude + 180.0) / 360.0 * worldDp
            fun y(p: LatLng): Double {
                val lat = Math.toRadians(p.latitude)
                return (1.0 - ln(tan(lat) + 1.0 / cos(lat)) / PI) / 2.0 * worldDp
            }
            return (x(point) - x(center)) to (y(point) - y(center))
        }

        /**
         * Whether [point] is within [EDGE_MARGIN_DP] of the edge of a
         * [widthDp] x [heightDp] map at [center], or off it.
         */
        fun nearEdge(center: LatLng, point: LatLng, zoom: MapScale, widthDp: Double, heightDp: Double): Boolean {
            val (dx, dy) = offsetDp(center, point, zoom)
            return abs(dx) > widthDp / 2 - EDGE_MARGIN_DP || abs(dy) > heightDp / 2 - EDGE_MARGIN_DP
        }
    }

    private val _center = MutableStateFlow(initialCenter)
    /** Where the map should be centred. */
    val center: StateFlow<LatLng> = _center.asStateFlow()

    private val _followGps = MutableStateFlow(initialFollow)
    /** Whether [center] follows the GPS. */
    val followGps: StateFlow<Boolean> = _followGps.asStateFlow()

    private var zoom: MapScale = initialZoom
    private var widthDp = DEFAULT_WIDTH_DP
    private var heightDp = DEFAULT_HEIGHT_DP
    /** Following without having centred on a fix yet (from [initialFollow]): the first fix centres the map. */
    private var awaitingFix = initialFollow
    private val lock = Any()

    init {
        coroutineScope.launch {
            gpsPositions.collect { position -> if (position != null) onGpsPosition(position) }
        }
    }

    /**
     * The user moved the map to [userPos] or zoomed to [zoom] (the map's pan
     * and zoom controls). A pan stops following the GPS; a zoom alone
     * doesn't (and may bring the GPS point near the edge: then it re-centres).
     */
    fun onUserMapCenterChanged(userPos: LatLng, zoom: MapScale) = synchronized(lock) {
        this.zoom = zoom
        val (dx, dy) = offsetDp(_center.value, userPos, zoom)
        if (abs(dx) > SAME_CENTER_DP || abs(dy) > SAME_CENTER_DP) {
            _followGps.value = false
            _center.value = userPos
            return@synchronized
        }
        if (_followGps.value) gpsPositions.value?.let { recentreIfNearEdge(it) }
    }

    /**
     * The map's size on screen, in dp (MapViewer's viewport: it depends on
     * the theme and the bottom bar). Following, a smaller map may put the GPS
     * point near the edge: then it re-centres.
     */
    fun onMapViewportChanged(widthDp: Double, heightDp: Double) = synchronized(lock) {
        this.widthDp = widthDp
        this.heightDp = heightDp
        if (_followGps.value) gpsPositions.value?.let { recentreIfNearEdge(it) }
    }

    /**
     * [follow]: centre on the GPS point now and follow it; false (and no
     * change) without a GPS position yet. Not [follow]: stop following.
     */
    fun followGps(follow: Boolean): Boolean = synchronized(lock) {
        logger.d(TAG, "Setting followGps($follow")
        if (!follow) {
            _followGps.value = false
            return@synchronized true
        }
        val position = gpsPositions.value ?: return@synchronized false
        _center.value = position
        _followGps.value = true
        awaitingFix = false
        true
    }

    private fun onGpsPosition(position: LatLng) = synchronized(lock) {
        if (!_followGps.value) return@synchronized
        if (awaitingFix) {
            awaitingFix = false
            _center.value = position
        } else {
            recentreIfNearEdge(position)
        }
    }

    private fun recentreIfNearEdge(position: LatLng) {
        if (nearEdge(_center.value, position, zoom, widthDp, heightDp)) _center.value = position
    }
}
