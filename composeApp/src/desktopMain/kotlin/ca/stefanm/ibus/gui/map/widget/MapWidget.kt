package ca.stefanm.ibus.gui.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import org.jxmapviewer.viewer.GeoPosition
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.MapScaleWidget
import ca.stefanm.ibus.gui.map.widget.tile.TileView
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import ca.stefanm.ibus.gui.map.widget.tile.LocalMapTileServices
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.places.FoundMapPoi
import ca.stefanm.ibus.gui.map.places.MapPoi
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlin.math.tan
import kotlin.math.sinh
import kotlin.math.ln
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.atan
import kotlin.math.PI
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
@Stable
class Extents(
    center : LatLng,
    mapScale: MapScale
) {
    var centerState : LatLng by mutableStateOf(center)
    var mapScaleState : MapScale by mutableStateOf(mapScale)
}

//This is the driving route we want to draw on the map.
data class Route(
    val path : List<LatLng>,

    val color : Color,
    val stroke: Stroke
    //Stroke??
) {
    companion object {
        val DefaultStroke = Stroke(width = 8F)


        fun findCenter(path : List<LatLng>) : LatLng {
            return LatLng(
                (path.minOf { it.latitude } + path.maxOf { it.latitude }) / 2.0,
                (path.minOf { it.longitude } + path.maxOf { it.longitude }) / 2.0
            )
        }

        fun findMapScaleForOverview(path : List<LatLng>) : MapScale {
            val topLeft = LatLng(path.minOf { it.latitude },  path.minOf { it.longitude })
            val bottomRight = LatLng(path.maxOf { it.latitude }, path.maxOf { it.longitude })

            //Hypotenuse.
            val distance = LatLngTool.distance(topLeft, bottomRight, LengthUnit.METER)

            //Match a close-ish "osm distance".
            return MapScale.values().minByOrNull { abs(distance - it.meters) } ?: MapScale.METERS_50
        }
    }
}

data class PoiOverlay(
    val pois : List<PoiOverlayItem>
) {
    data class PoiOverlayItem(
        val label : String,
        val position: LatLng,
        val icon : @Composable () -> Unit = @Composable {}
    ) {


        //TODO selectable POI overlay item with a hint string box


        companion object {

            fun SelectablePoi(
                poi : MapPoi,
                indexHint : String,
                showPoiNameBox : Boolean = false,
                isPreselected : Boolean = false,
                //Open a sidepane with all the POI info.
                onPoiClicked : (poi : MapPoi) -> Unit
            ) : @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit = @Composable { allocatedIndex: Int, currentIndex: Int ->

                //First calculate the size of the overlay.
                //The way it's layed out in the overlay layer is that the icon gets centered: the center of this box must
                //also be the location of the icon. We'll have to make a slightly larger fake box

                val boxWidth = 384F.halveIfNotPixelDoubled()
                val boxHeight = 96F.halveIfNotPixelDoubled()
                val circleRadius = 16F.halveIfNotPixelDoubled()

                val selectedColor = ThemeWrapper.ThemeHandle.current.colors.selectedColor
                val isSelected = allocatedIndex == currentIndex
                val circleColor = if (isSelected) {
                    selectedColor
                } else {
                    if (isPreselected) {
                        Color.Red
                    } else {
                        Color.Blue
                    }
                }
                Box(
                    Modifier
                        .width(boxWidth.dp)
                        .height(boxHeight.dp)
                        //.border(1.dp, Color.Red)
                        .clickable { onPoiClicked(poi) }
                ) {
                    val textMeasurer = rememberTextMeasurer()
                    val circleIndexStyle = TextStyle(
                        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                        fontSize = ThemeWrapper.ThemeHandle.current.smallItem.fontSize,
                    )
                    val circleIndexStyleSelected = TextStyle(
                        color = Color.Black,
                        fontSize = ThemeWrapper.ThemeHandle.current.smallItem.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                    //TODO if isPreselected, make the font red so that you
                    //TODO know which one you're looking for.
                    val circleIndexStylePreSelectedSelected = TextStyle(
                        color = Color.Red,
                        fontSize = ThemeWrapper.ThemeHandle.current.smallItem.fontSize,
                    )
                    val circleTextLayout = remember(indexHint, circleIndexStyle, circleIndexStyleSelected, isSelected) {
                        val style = if (isSelected) {
                            if (isPreselected) {
                                circleIndexStylePreSelectedSelected
                            } else {
                                circleIndexStyleSelected
                            }
                        } else {
                            circleIndexStyle
                        }
                        textMeasurer.measure(indexHint, style)
                    }
                    Canvas(Modifier
                        .size((circleRadius * 2).dp)
                        .absoluteOffset(
                            x = (boxWidth / 2).dp,
                            y = (boxHeight / 2).dp
                        )
                    ){
                        drawCircle(
                            center = Offset(size.width / 2, size.height / 2),
                            radius = circleRadius,
                            color = circleColor,
                        )
                        drawText(
                            textLayoutResult = circleTextLayout,
                            topLeft = Offset(
                                x = center.x - circleTextLayout.size.width / 2f,
                                y = center.y - circleTextLayout.size.height / 2f,
                            )
                        )
                    }

                    if (showPoiNameBox) {
                        Box(
                            Modifier
                                .height((boxHeight * 0.3).dp)
                                .width((boxWidth * 0.5).dp)
                                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)

                                .clickable { onPoiClicked(poi) }
                        ) {
                            Text(
                                text = poi.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding((0.6 * ThemeWrapper.ThemeHandle.current.smallItem.chipWidth).dp),
                                color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                                fontSize = TextUnit.Unspecified,
//                            fontSize = ThemeWrapper.ThemeHandle.current.smallItem.fontSize,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isSelected) {
                                //Draw the selection
                                val highlightWidth = ThemeWrapper.ThemeHandle.current.smallItem.highlightWidth
                                Canvas(
                                    Modifier.fillMaxSize()
                                ) {
                                    drawRect(
                                        color = selectedColor,
                                        style = Stroke(
                                            width = highlightWidth
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val ICON_BLUE_CIRCLE = @Composable {
                Canvas(Modifier.size(16.dp)){
                    drawCircle(
                        center = Offset(size.width / 2, size.height / 2),
                        radius = 32F,
                        color = Color.Blue
                    )
                }
            }
            //We can't fully support free-form composable icons because
            //The Composable Lambda has to be a constant at run-time.
            //It cannot be passed along how we think it should be. We also
            //can't do composable function references.

            //It's not a tax, it's a shift -- Stephane Dion
            //It's not a new function on the fly, it's calling the same one with a new parameter.
            val CIRCLE_COLOR : @Composable (color : Color) -> Unit = { color ->
                val circleRadius = 32F.halveIfNotPixelDoubled()
                Canvas(Modifier.size(16.dp)){
                    drawCircle(
                        center = Offset(size.width / 2, size.height / 2),
                        radius = circleRadius,
                        color = color
                    )
                }
            }

            val ICON_FILE : @Composable (iconFile : DrawableResource, tint : Color?) -> Unit = { iconFile, tint ->
                Image(
                    painter = painterResource(iconFile),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp.halveIfNotPixelDoubled()),
                    colorFilter = tint?.let { ColorFilter.tint(tint, BlendMode.SrcAtop) }
                )
            }

        }
    }
}

@Composable
fun MapViewer(
    overlayProperties: OverlayProperties,
    extents: Extents,
    onCenterPositionUpdated : (newCenter : LatLng) -> Unit,
    /** The map's size on screen, in dp: what's left of the window (theme, bottom bar). */
    onViewportSizeChanged : (widthDp : Double, heightDp : Double) -> Unit = { _, _ -> },
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
            LaunchedEffect(viewportWidth, viewportHeight) {
                onViewportSizeChanged(viewportWidth.value.toDouble(), viewportHeight.value.toDouble())
            }


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
            // Until the first scroll to the centre, the scroll position (0, 0)
            // isn't where the map is: its centre isn't reported.
            var scrolledToCenter by remember { mutableStateOf(false) }

            // The tiles: the viewport's, plus one tile on each side. (Was six
            // viewport widths each way, square: 81 tiles for a 400x234 map,
            // 361 at 800x468, where about 2x1 / 4x2 are on the screen.) Panning
            // moves the centre and rebuilds the grid around it.
            val zoom = extents.mapScaleState.mapZoomLevel
            val centerContainingTile = ExtentCalculator.getTileNumber(
                extents.centerState.latitude,
                extents.centerState.longitude,
                zoom
            )
            val tilesEachSideX = ceil(viewportWidth.value / 2 / 256).toInt() + 1
            val tilesEachSideY = ceil(viewportHeight.value / 2 / 256).toInt() + 1
            val startX = centerContainingTile.first - tilesEachSideX
            val endX = centerContainingTile.first + tilesEachSideX
            val startY = centerContainingTile.second - tilesEachSideY
            val endY = centerContainingTile.second + tilesEachSideY

            // Pixels per dp: the tiles are 256 dp each, the overlays are
            // placed in pixels. (Was a `var` set in a Canvas draw call,
            // which runs after composition: the overlays always got its
            // initial 2, so on a density-1 screen like QEMU's every POI
            // and route point was placed twice as far from the grid's
            // corner, off the screen.)
            val scaling = LocalDensity.current.density
            val viewportWidthPx = viewportWidth.value * scaling
            val viewportHeightPx = viewportHeight.value * scaling

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .horizontalScroll(stateHorizontal)
            ) {

                // Scroll so the centre is in the middle of the viewport: its
                // pixel on the grid, minus half the viewport. (Was a fraction
                // of the scroll range, exact only for a point in the middle
                // of a large grid.)
                LaunchedEffect(extents.centerState, extents.mapScaleState, viewportWidthPx, viewportHeightPx) {
                    withFrameNanos { }   // the grid is laid out: the scroll range is known
                    val center = latLngToPixels(extents.centerState, extents, startX, endX, startY, endY, zoom, scaling)
                    stateVertical.scrollTo((center.fromTop - viewportHeightPx / 2).roundToInt())
                    stateHorizontal.scrollTo((center.fromLeft - viewportWidthPx / 2).roundToInt())
                    scrolledToCenter = true
                }

                // The map's centre now (the middle of the viewport), for
                // "New POI on Center", Places Nearby, location selection.
                // Keyed on scrolledToCenter too: a centre already in the
                // middle doesn't scroll, but is reported once scrolled to.
                LaunchedEffect(stateHorizontal.value, stateVertical.value, scrolledToCenter) {
                    if (!scrolledToCenter) return@LaunchedEffect
                    onCenterPositionUpdated(
                        pixelsToLatLng(
                            stateHorizontal.value + viewportWidthPx / 2.0,
                            stateVertical.value + viewportHeightPx / 2.0,
                            startX, startY, zoom, scaling
                        )
                    )
                }

                RawTileGrid(startX, endX, startY, endY, zoom = zoom)


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
                    extents.mapScaleState,
                    extents.centerState,
                    isSelected = false
                )
            }
        }

        if (overlayProperties.centerCrossHairsVisible) {
            val strokeWidth = 8F.halveIfNotPixelDoubled()
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
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
    val canvasHeightPixels = ((endY + 1) - startY) * 256 * scaling


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

/** [latLng]'s position in tiles (x, y; fractional) at [zoom]: Web Mercator, as the tiles are drawn. */
fun latLngToTile(latLng: LatLng, zoom: Int): Pair<Double, Double> {
    val n = 2.0.pow(zoom)
    val lat = Math.toRadians(latLng.latitude)
    val x = (latLng.longitude + 180.0) / 360.0 * n
    val y = (1.0 - ln(tan(lat) + 1.0 / cos(lat)) / PI) / 2.0 * n
    return x to y
}

/**
 * [latLng]'s pixel on the tile grid whose top-left tile is ([startX],
 * [startY]): tiles are 256 dp, [scaling] pixels per dp. (Was proportional
 * to meters across the grid, which isn't how the Mercator tiles are drawn:
 * slightly off, more so on bigger grids.) [extents], [endX], [endY]: unused,
 * kept for the callers.
 */
@Suppress("UNUSED_PARAMETER")
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
    val (x, y) = latLngToTile(latLng, zoom)
    return DoublePixelOffset(
        fromLeft = (x - startX) * 256 * scaling,
        fromTop = (y - startY) * 256 * scaling,
    )
}

/** The inverse of [latLngToPixels]: the position at a pixel of the grid. */
fun pixelsToLatLng(fromLeft: Double, fromTop: Double, startX: Int, startY: Int, zoom: Int, scaling: Float): LatLng {
    val n = 2.0.pow(zoom)
    val x = startX + fromLeft / (256 * scaling)
    val y = startY + fromTop / (256 * scaling)
    val lon = x / n * 360.0 - 180.0
    val lat = Math.toDegrees(atan(sinh(PI * (1 - 2 * y / n))))
    return LatLng(lat, lon)
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

        Layout(
            modifier = Modifier.matchParentSize(),
            content = {
                poi.icon()
            }
        ) { measurables, constraints ->
            // The icon at its own size (not stretched to the layer), centred
            // on the POI.
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            layout(constraints.maxWidth, constraints.maxHeight) {
                val icon = placeables.firstOrNull() ?: return@layout
                icon.place(
                    IntOffset(
                        poiPixelsFromLeft.roundToInt() - icon.width / 2,
                        poiPixelsFromTop.roundToInt() - icon.height / 2,
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
    // Tile numbers wrap around the date line.
    val tilesPerSide = 1 shl zoom
    val rowIterations = (min(startY, endY)..kotlin.math.max(startY, endY)).map { Math.floorMod(it, tilesPerSide) }
    val columnIterations = (min(startX, endX)..kotlin.math.max(startX, endX)).map { Math.floorMod(it, tilesPerSide) }

    // Read once for all the tiles.
    val config = LocalMapTileServices.current.configurationStorage.config
    val debug = config[E39Config.MapConfig.showDebugInfoOnTiles]
    val source = config[E39Config.MapConfig.tileSource]

    Column {
        for (y in rowIterations) {
            Row(
                Modifier.height(256.dp)
            ) {
                for (x in columnIterations) {
                    Column(
                        Modifier.width(256.dp)
                    ) {
                        key(x, y) { TileView(x, y, zoom, debug, source) }
                    }
                }
            }
        }
    }
}