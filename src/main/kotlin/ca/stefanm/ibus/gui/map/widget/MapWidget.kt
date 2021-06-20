package ca.stefanm.ibus.gui.map

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.SwingPanel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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

        Box(
            Modifier.fillMaxSize()
        ) {
            Text("Foo")
            SwingPanel(
                background = Color.White,
                modifier = Modifier.fillMaxSize(),
                factory = {
                    JPanel().apply {
                        layout = BorderLayout(0, 0)
                        add(mapViewer())
                    }
                }
            )
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


private fun mapViewer() : JXMapViewer = JXMapViewer().also { mapViewer ->

    //https://github.com/msteiger/jxmapviewer2/blob/master/examples/src/sample1_basics/Sample1.java

    val info: TileFactoryInfo = OSMTileFactoryInfo()
    val tileFactory = DefaultTileFactory(info)
    mapViewer.tileFactory = tileFactory

    // Use 8 threads in parallel to load the tiles
    tileFactory.setThreadPoolSize(8)

    // Set the focus
    val frankfurt = GeoPosition(50.11, 8.68)

    mapViewer.zoom = 7
    mapViewer.centerPosition = frankfurt
}
