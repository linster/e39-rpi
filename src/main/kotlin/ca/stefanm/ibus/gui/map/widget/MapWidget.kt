package ca.stefanm.ibus.gui.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import org.jxmapviewer.viewer.GeoPosition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.*
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.MapScaleWidget
import ca.stefanm.ibus.gui.map.widget.tile.TileView
import ca.stefanm.ibus.lib.logging.StdOutLogger
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt


//Defines overlays on the map.
data class OverlayProperties(
    val centerCrossHairsVisible: Boolean,
    val mapScaleVisible : Boolean,
    val gpsReceptionIconVisible : Boolean,
    val route : Route? = null,
    val poiOverlay: PoiOverlay? = null
)

//Defines how much of the world we can see
data class Extents(
    val center : GeoPosition,
    val mapScale: MapScale
)

//This is the driving route we want to draw on the map.
data class Route(
    val path : List<LatLng>,

    val color : Color,
    val stroke: Stroke
    //Stroke??
)

data class PoiOverlay(
    val pois : List<PoiOverlayItem>
) {
    data class PoiOverlayItem(
        val label : String,
        val position: LatLng,
        val icon : @Composable () -> Unit = @Composable {}
    ) {
        companion object Icons {
            val ICON_BLUE_CIRCLE = @Composable {
                Canvas(Modifier.size(16.dp)){
                    drawCircle(
                        center = Offset(0F, 0F),
                        radius = 32F,
                        color = Color.Blue
                    )
                }
            }


            //We can't fully support free-form composable icons because
            //The Composable Lambda has to be a constant at run-time.
            //It cannot be passed along how we think it should be. We also
            //can't do composable function references.
            //So, we'll hack around it here, but hard-coding each lambda we'd ever need.

        }
    }
}

@Composable
fun MapViewer(
    overlayProperties: OverlayProperties,
    extents: Extents,
    onCenterPositionUpdated : (newCenter : GeoPosition) -> Unit
) {

    Box(
        modifier = Modifier
            .width(800.dp)
            .height(468.dp)
    ) {

        BoxWithConstraints(
            Modifier.fillMaxSize()
        ) {

            val viewportHeight = this.maxHeight
            val viewportWidth = this.maxWidth


            //TODO have a big-ass box.
            //TODO load a 20*20 grid of tiles in the box for the zoom level
            //TODO use rememberScrollPosition for horizontal/vertical,
            //TODO and then change that to get the extent center in the center of the viewport.


            //TODO keep track of original extents, and then the panned extents.
            //TODO use a side-effect to change the original extents when the panned extents
            //TODO aren't fully contained in the original.
            //TODO this should look like a LaunchedEffect?? followed by some derivedStateOf

            val stateVertical = rememberScrollState(0)
            val stateHorizontal = rememberScrollState(0)

            val numPreLoadedTilesX = (maxWidth / 256.dp) * 6
            val numPreLoadedTilesY = (maxWidth / 256.dp) * 6 //We want this to be square so we a line diagonally across passes through the center.

            val centerContainingTile = ExtentCalculator.getTileNumber(
                extents.center.latitude,
                extents.center.longitude,
                extents.mapScale.mapZoomLevel
            )

            //These are the tiles in view.
            val startX = (centerContainingTile.first) - (numPreLoadedTilesX / 2).toInt()
            val endX = (centerContainingTile.first) + (numPreLoadedTilesX / 2).toInt()
            val startY = (centerContainingTile.second) - (numPreLoadedTilesY / 2).toInt()
            val endY = (centerContainingTile.second) + (numPreLoadedTilesY / 2).toInt()
            val zoom = extents.mapScale.mapZoomLevel


            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .horizontalScroll(stateHorizontal)
            ) {


                //Scroll so our center is in the middle of the screen.
                LaunchedEffect(extents.center, extents.mapScale) {
                    val canvasTilesTall = endY - startY
                    val canvasHeightMeters = LatLngTool.distance(
                        LatLng(ExtentCalculator.tile2lat(startY, zoom), extents.center.longitude),
                        LatLng(ExtentCalculator.tile2lat(endY + 1, zoom), extents.center.longitude),
                        LengthUnit.METER
                    )

                    val canvasHeightPixels = (canvasTilesTall) * 256
                    val actualMetersFromTop = LatLngTool.distance(
                        LatLng(ExtentCalculator.tile2lat(startY, zoom), extents.center.longitude),
                        LatLng(extents.center.latitude, extents.center.longitude),
                        LengthUnit.METER
                    )

//                    val scrollPixelsFromTop = (actualMetersFromTop / canvasHeightMeters) * canvasHeightPixels
                    val scrollPixelsFromTop_lp = (actualMetersFromTop / canvasHeightMeters) * stateVertical.maxValue
                    val scrollPixelsFromTop = (actualMetersFromTop / canvasHeightMeters) * stateVertical.maxValue

                    stateVertical.scrollTo(0)
                    stateVertical.dispatchRawDelta(scrollPixelsFromTop.toFloat())

                    val canvasWidthTiles = endX - startX
                    val canvasWidthMeters = LatLngTool.distance(
                        LatLng(extents.center.latitude, ExtentCalculator.tile2lon(startX, zoom)),
                        LatLng(extents.center.latitude, ExtentCalculator.tile2lon(endX + 1, zoom)),
                        LengthUnit.METER
                    )
                    val canvasWidthPixels = (canvasWidthTiles) * 256
                    val actualMetersFromLeft = LatLngTool.distance(
                        LatLng(extents.center.latitude, ExtentCalculator.tile2lon(startX, zoom)),
                        LatLng(extents.center.latitude, extents.center.longitude),
                        LengthUnit.METER
                    )

//                    val scrollPixelsFromLeft = (actualMetersFromLeft / canvasWidthMeters) * canvasWidthPixels
                    val scrollPixelsFromLeft = (actualMetersFromLeft / canvasWidthMeters) * stateHorizontal.maxValue

                    stateHorizontal.scrollTo(0)
                    stateHorizontal.dispatchRawDelta(scrollPixelsFromLeft.toFloat())
                }

                //TODO add an effect here to listen to the scroll state and use the canvas to compute
                //TODO the new center for the listener

                LaunchedEffect(stateHorizontal.value, stateVertical.value) {

                    val startPosition = LatLng(
                        ExtentCalculator.tile2lat(startY, zoom),
                        ExtentCalculator.tile2lon(startX, zoom)
                    )

                    val topRight = LatLng(
                        ExtentCalculator.tile2lat(startY, zoom),
                        ExtentCalculator.tile2lon(endX + 1, zoom)
                    )

                    val bottomLeft = LatLng(
                        ExtentCalculator.tile2lat(endY + 1, zoom),
                        ExtentCalculator.tile2lon(startX, zoom)
                    )

                    val fractionGoingEast = stateHorizontal.value / stateHorizontal.maxValue.toDouble()
                    val fractionGoingSouth = stateVertical.value / stateVertical.maxValue.toDouble()

                    val longFromLeft = LatLngTool.travel(
                        startPosition,
                        LatLngTool.Bearing.EAST,
                        LatLngTool.distance(
                            startPosition,
                            topRight,
                            LengthUnit.METER
                        ) * fractionGoingEast,
                        LengthUnit.METER
                    ).longitude

                    val latFromNorth = LatLngTool.travel(
                        startPosition,
                        LatLngTool.Bearing.SOUTH,
                        LatLngTool.distance(
                            startPosition,
                            bottomLeft,
                            LengthUnit.METER
                        ) * fractionGoingSouth,
                        LengthUnit.METER
                    ).latitude

                    onCenterPositionUpdated(
                        GeoPosition(latFromNorth, longFromLeft)
                    )
                }

                RawTileGrid(startX, endX, startY, endY,
                    zoom = extents.mapScale.mapZoomLevel
                )

                var scaling = 2F
                Canvas(Modifier.matchParentSize()) {
                    scaling = this.size.height.dp.toPx() / this.size.height.dp.value
                }

                if (overlayProperties.poiOverlay != null) {
                    PoiOverlayLayer(
                        poiOverlay = overlayProperties.poiOverlay,
                        extents = extents,
                        startX, endX, startY, endY, zoom, scaling
                    )
                }

                if (overlayProperties.route != null) {
                    RouteLineLayer(
                        overlayProperties.route,
                        extents, startX, endX, startY, endY, zoom, scaling
                    )
                }
            }
        }

        if (overlayProperties.mapScaleVisible) {
            Box(Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
            ) {
                MapScaleWidget(
                    extents.mapScale,
                    extents.center,
                    isSelected = false
                )
            }
        }

        if (overlayProperties.centerCrossHairsVisible) {
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                val strokeWidth = 8F
                drawLine(
                    color = Color.DarkGray,
                    strokeWidth = strokeWidth,
                    start = Offset(this.size.width / 2, 0F),
                    end = Offset(this.size.width / 2, this.size.height)
                )

                drawLine(
                    color = Color.DarkGray,
                    strokeWidth = strokeWidth,
                    start = Offset(0F, size.height / 2),
                    end = Offset(this.size.width, size.height / 2)
                )
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.RouteLineLayer(
    route: Route,
    extents: Extents,
    startX : Int,
    endX :  Int,
    startY : Int,
    endY : Int,
    zoom : Int,
    scaling : Float = 2F
) {

    val canvasWidthPixels = ((endX + 1) - startX) * 256 * scaling
    val canvasHeightPixels = ((endY + 1) - startX) * 256 * scaling


    Canvas(
        modifier = Modifier.matchParentSize()
    ) {
        route.path.windowed(2, 1, false).forEach {
            val from = it[0]
            val to = it[1]

            drawLine(
                color = route.color,
                strokeWidth = route.stroke.width,
                start = latLngToPixels(from, extents, startX, endX, startY, endY, zoom, scaling).let {
                    Offset(
                        x = it.fromLeft.toFloat().coerceIn(0F, canvasWidthPixels.toFloat()),
                        y = it.fromTop.toFloat().coerceIn(0F, canvasHeightPixels.toFloat())
                    )
                },
                end = latLngToPixels(to, extents, startX, endX, startY, endY, zoom, scaling).let {
                    Offset(
                        x = it.fromLeft.toFloat().coerceIn(0F, canvasWidthPixels.toFloat()),
                        y = it.fromTop.toFloat().coerceIn(0F, canvasHeightPixels.toFloat())
                    )
                }
            )
        }
    }



}

data class DoublePixelOffset(
    val fromLeft : Double,
    val fromTop : Double
)

fun latLngToPixels(
    latLng: LatLng,
    extents: Extents,
    startX : Int,
    endX :  Int,
    startY : Int,
    endY : Int,
    zoom : Int,
    scaling : Float = 2F
) : DoublePixelOffset {
    val canvasHeightMeters = LatLngTool.distance(
        LatLng(ExtentCalculator.tile2lat(startY, zoom), extents.center.longitude),
        LatLng(ExtentCalculator.tile2lat(endY + 1, zoom), extents.center.longitude),
        LengthUnit.METER
    )

    val actualMetersFromTop = LatLngTool.distance(
        LatLng(ExtentCalculator.tile2lat(startY, zoom), latLng.longitude),
        latLng,
        LengthUnit.METER
    )

    val poiPixelsFromTop =
        scaling * ((actualMetersFromTop / canvasHeightMeters) * (((endY + 1) - startY) * 256))

    val canvasWidthMeters = LatLngTool.distance(
        LatLng(extents.center.latitude, ExtentCalculator.tile2lon(startX, zoom)),
        LatLng(extents.center.latitude, ExtentCalculator.tile2lon(endX + 1, zoom)),
        LengthUnit.METER
    )

    val actualMetersFromLeft = LatLngTool.distance(
        LatLng(latLng.latitude, ExtentCalculator.tile2lon(startX, zoom)),
        latLng,
        LengthUnit.METER
    )

    val poiPixelsFromLeft =
        scaling * ((actualMetersFromLeft / canvasWidthMeters) * (((endX + 1) - startX) * 256))

    return DoublePixelOffset(fromLeft = poiPixelsFromLeft, fromTop = poiPixelsFromTop)
}

@Composable
fun BoxWithConstraintsScope.PoiOverlayLayer(
    poiOverlay: PoiOverlay,
    extents: Extents,
    startX : Int,
    endX :  Int,
    startY : Int,
    endY : Int,
    zoom : Int,
    scaling : Float = 2F
) {
    poiOverlay.pois.forEach { poi ->
        val (poiPixelsFromLeft, poiPixelsFromTop) = latLngToPixels(
            poi.position,
            extents, startX, endX, startY, endY, zoom, scaling
        )

        with (DaggerApplicationComponent.create().logger()) {
            d("POI", "$poi")
            d("POI", "pixels from top: $poiPixelsFromTop")
            d("POI", "pixels from left: $poiPixelsFromLeft")
            d("POI", "scaling: $scaling")
        }

        Layout(
            modifier = Modifier.matchParentSize(),
            content = {
                poi.icon()
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            layout(constraints.minWidth, constraints.minWidth) {
                if (placeables.isEmpty()) {
                    StdOutLogger().w("WAT WAT", "Placables is empty!")
                    return@layout
                }
                placeables[0].place(
                    IntOffset(
                        poiPixelsFromLeft.roundToInt(),
                        poiPixelsFromTop.roundToInt(),
                    )
                )
            }
        }
    }
}



@Composable
fun RawTileGrid(
    startX : Int,
    endX :  Int,
    startY : Int,
    endY : Int,
    zoom : Int,
) {
    val validXIndices = (0 .. (2.0.pow(zoom) - 1).toInt()).toList().circular()
    val validYIndices = (0 .. (2.0.pow(zoom) - 1).toInt()).toList().circular()

    val rowIterations =
        (min(startY, endY) .. kotlin.math.max(startY, endY))
            .map { validYIndices[it] }

    val columnIterations =
        (min(startX, endX) .. kotlin.math.max(startX, endX)).map { validXIndices[it] }

    Column {
        for (y in rowIterations) {
            Row(
                Modifier.height(256.dp)
            ) {
                for (x in columnIterations) {
                    Column(
                        Modifier.width(256.dp)
                    ) {
                        TileView(x, y, zoom)
                    }
                }
            }
        }
    }
}