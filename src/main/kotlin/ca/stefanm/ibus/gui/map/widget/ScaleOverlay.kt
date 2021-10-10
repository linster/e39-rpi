package ca.stefanm.ibus.gui.map.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ginsberg.cirkle.circular
import org.jxmapviewer.viewer.GeoPosition
import kotlin.math.cos
import kotlin.math.pow

enum class MapScale(
    val displayString : String,
    val meters : Int,
    val mapZoomLevel : Int)
{
    METERS_25(displayString = "25 m", meters = 25, mapZoomLevel = 19),
    METERS_50(displayString = "50 m", meters = 50, mapZoomLevel = 18),
    METERS_100(displayString = "100 m", meters = 100, mapZoomLevel = 17),
    METERS_200(displayString = "200 m", meters = 200, mapZoomLevel = 16),
    KILOMETERS_1_6(displayString = "1.6 km", meters = 1600, mapZoomLevel = 13),
    KILOMETERS_25(displayString = "25 km", meters = 25600, mapZoomLevel = 9),
    KILOMETERS_50(displayString = "50 km", meters = 51200, mapZoomLevel = 8),
    KILOMETERS_100(displayString = "100 km", meters = 102400, mapZoomLevel = 7),
}

//TODO we need a function to calculate a new map center given
//TODO an old mapcenter, the current zoom, and a direction.


@Composable
fun MapScaleWidget(
    scale : MapScale,
    mapCenter : GeoPosition,
    isSelected : Boolean = false //TODO when we do modal UI.
) {

    val osmZoom = scale.mapZoomLevel

    //How many meters wide is a tile?
    val tileWidthMeters = (40_075_016.686 * cos(Math.toRadians(mapCenter.latitude))) / (2.0.pow(osmZoom))
    val tilePixelsPerMeter = 256.0 / tileWidthMeters
    val scaleLength = scale.meters * tilePixelsPerMeter

    val debugScaleLength = false

    Box(modifier = Modifier
        .width(128.dp)
        .height(64.dp)
        .border(
            2.dp,
            Color.DarkGray
        )
        .background(Color.LightGray)
    ) {

        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Cyan),
                text = scale.displayString,
                textAlign = TextAlign.Center
            )
            if (debugScaleLength) {
                Text("${scaleLength}")
            } else {
//                    Canvas(
//                        Modifier.width((scaleLength + 3).dp).height(18.dp)
//                    ) {
//
//                        inset(
//                            top = 8F,
//                            bottom = 0F,
//                            left = 0F,
//                            right = 0F
//                        ) {
//
//                            val tickHeight = 16F
//                            val stroke = 4F
//                            drawLine(
//                                Color.Black,
//                                start = Offset(0F, 0F),
//                                end = Offset(0F, tickHeight),
//                                strokeWidth = stroke
//                            )
//
//                            drawLine(
//                                Color.Black,
//                                start = Offset(0F, tickHeight),
//                                end = Offset(scaleLength.toFloat(), tickHeight),
//                                strokeWidth = stroke
//                            )
//
//                            drawLine(
//                                Color.Black,
//                                start = Offset(scaleLength.toFloat(), 0F),
//                                end = Offset(scaleLength.toFloat(), tickHeight)
//                            )
//
//                        }
//                    }
            }
        }

    }

}