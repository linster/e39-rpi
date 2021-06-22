package ca.stefanm.ibus.gui.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import org.jxmapviewer.viewer.GeoPosition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.MapScaleWidget
import ca.stefanm.ibus.gui.map.widget.tile.TileView
import com.ginsberg.cirkle.circular
import kotlin.math.min
import kotlin.math.pow



//Defines overlays on the map.
data class OverlayProperties(
    val centerCrossHairsVisible: Boolean,
    val mapScaleVisible : Boolean,
    val gpsReceptionIconVisible : Boolean
)

//Defines how much of the world we can see
data class Extents(
    val center : GeoPosition,
    val mapScale: MapScale
)

//This is the driving route we want to draw on the map.
data class Route(
    val path : List<GeoPosition>
)

@Composable
fun MapViewer(
    overlayProperties: OverlayProperties,
    extents: Extents,
    onCenterPositionUpdated : (newCenter : GeoPosition) -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier
            .width(800.dp)
            .height(468.dp)
    ) {

        BoxWithConstraints(
            Modifier.fillMaxSize()
        ) {

            val height = this.maxHeight
            val width = this.maxWidth


            //TODO have a big-ass box.
            //TODO load a 20*20 grid of tiles in the box for the zoom level
            //TODO use rememberScrollPosition for horizontal/vertical,
            //TODO and then change that to get the extent center in the center of the viewport.


            //TODO keep track of original extents, and then the panned extents.
            //TODO use a side-effect to change the original extents when the panned extents
            //TODO aren't fully contained in the original.
            //TODO this should look like a LaunchedEffect?? followed by some derivedStateOf

            val stateVertical = rememberScrollState(0) //(height / 2).value.toInt())
            val stateHorizontal = rememberScrollState(0)

            val numPreLoadedTilesX = (maxWidth / 256.dp) * 5
            val numPreLoadedTilesY = (maxHeight / 256.dp) * 5

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

            //Initial launch effect to center the map on the extent
            LaunchedEffect(extents.center, extents.mapScale) {
                stateHorizontal.scroll {

                    //TODO
                    //TODO NONONO. Don't use tiles to get close to the answer.
                    //TODO Use the big canvas size and find the offset.
                    //TODO Find the TL corner of the canvas. Find the BR corner of the canvas.
                    //TODO find how many meters across it is. Find how many pixels wide it is.
                    //TODO find how many meters from the top-left we need to move right to get
                    //TODO to our center. Find what fraction of the pixel-width we need to move.
                    //TODO
                    val exb = endX
                    val tilePixelsFull = (centerContainingTile.first - startX) * 256
                    val partialTilePixels = ExtentCalculator.offsetInTile(
                        extents.center,
                        centerContainingTile.first,
                        centerContainingTile.second,
                        zoom
                    ).x
                    scrollBy((tilePixelsFull + partialTilePixels).toFloat() - (maxWidth.value))
                }

                stateVertical.scroll {
                    val tilePixelsFull = (centerContainingTile.second - startY) * 256
                    val partialTilePixels = ExtentCalculator.offsetInTile(
                        extents.center,
                        centerContainingTile.first,
                        centerContainingTile.second,
                        zoom
                    ).y
                    scrollBy((tilePixelsFull + partialTilePixels).toFloat() - (maxHeight.value))
                }
            }

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .horizontalScroll(stateHorizontal)
            ) {

                LaunchedEffect(extents.center, extents.mapScale) {

                    val canvasTilesTall = endY - startY

                    val canvasWidthTiles = endX - startX
                    val canvasWidthMeters = ExtentCalculator.tileWidthInMeters(startY, zoom) * canvasWidthTiles
                    val canvasWidthPixels = canvasWidthTiles * 256


                    stateHorizontal.scroll {
                    }

                    stateVertical.scroll {
//                        scrollBy((tilePixelsFull + partialTilePixels).toFloat() - (maxHeight.value))
                    }
                }



                //TODO change this to boxWithConstraints, and move the launchedEffect down here.
                //TODO then we'll know how wide the virtual canvas is.
                    RawTileGrid(startX, endX, startY, endY,
                        zoom = extents.mapScale.mapZoomLevel
                    )
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
fun RawTileGrid(
    startX : Int,
    endX :  Int,
    startY : Int,
    endY : Int,
    zoom : Int,
) {
    //We need to bounds check here.

    val tileSize = 256.dp

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