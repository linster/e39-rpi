package ca.stefanm.ibus.gui.map

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.SwingPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component

class MapWidget @Inject constructor(){


    //Width and height passed in?

    //https://blog.jetbrains.com/cross-post/jetpack-compose-for-desktop-milestone-2-released/
    //TODO if I can't make the SkiaLayer transparent over the map, I can add ComposePanels to the map layer,
    //TODO and pass in composables here!

    @Composable
    fun widget(width: Int, height: Int) {
        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.Companion.horizontalGradient(
                        colors = listOf(Color(255, 0, 0, 0), Color(0, 255, 0, 255)),
                        startX = 0.0f,
                        endX = Float.POSITIVE_INFINITY,
                        tileMode = TileMode.Clamp
                    )
                )
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
    }

    //https://github.com/JetBrains/compose-jb/commit/455b634fdd4d0f02a86b3b28b87f2dc33ee2bb25

    fun mapViewer() : JXMapViewer = JXMapViewer().also { mapViewer ->

        //https://github.com/msteiger/jxmapviewer2/blob/master/examples/src/sample1_basics/Sample1.java

        val info: TileFactoryInfo = OSMTileFactoryInfo()
        val tileFactory = DefaultTileFactory(info)
        mapViewer.tileFactory = tileFactory

        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8)

        // Set the focus
        val frankfurt = GeoPosition(50.11, 8.68)

        mapViewer.zoom = 7
        mapViewer.addressLocation = frankfurt
    }

    //https://github.com/Dynamium/OKSM-Desktop/blob/master/src/main/kotlin/org/dynamium/oksm/ui/components/editor/Editor.kt#L87

}
