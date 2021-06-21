package ca.stefanm.ibus.gui.map

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.SwingPanel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactoryInfo
import javax.inject.Inject
import javax.swing.JPanel
import androidx.compose.runtime.snapshots.Snapshot.Companion.current
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component

enum class MapScale {
    METERS_50,
    METERS_200,
    METERS_500,
    KILOMETERS_1,
    KILOMETERS_2,
    KILOMETERS_5,
    KILOMETERS_25,
    KILOMETERS_50,
    KILOMETERS_100,
    KILOMETERS_250,
    KILOMETERS_500,
    KILOMETERS_1000,
    KILOMETERS_1500,
    KILOMETERS_2000,
    KILOMETERS_3000, //Canada, eh?
}

//TODO actually do the calculation based on how much we can
//TODO see in the viewport.
//https://wiki.openstreetmap.org/wiki/Zoom_levels
fun MapScale.toOsmZoomLevel() : Int {
    return 7
}

fun Int.toMapScale() : MapScale {
    return MapScale.values().toList().circular()[this + 3]
}

//TODO we need a function to calculate a new map center given
//TODO an old mapcenter, the current zoom, and a direction.


@Composable
fun MapScaleWidget(
    scale : MapScale
) {

}

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

            val stateVertical = rememberScrollState((height / 2).value.toInt())
            val stateHorizontal = rememberScrollState((width / 2).value.toInt())

            LaunchedEffect(stateVertical.value, stateHorizontal.value) {
                //TODO From the extents, take the scroll values, and then find where our new center actually is
                //TODO call onCenterPositionChanged()
            }

            val (initialX, initialY, initialZoom) = remember(extents) {
                derivedStateOf(extents) {

                }
            }

            Layout(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(stateVertical)
                    .horizontalScroll(stateHorizontal),
                content = {

                    //TODO put a list of 100 composables in here.
                    //TODO check the size of the earth and how many map tiles we have.
                    //TODO we might need a circular list / mod of the tile indexes.
                }
            ) { measurables, constraints ->

            }

        }

        if (overlayProperties.mapScaleVisible) {

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