package ca.stefanm.ibus.gui.map.guidance.notifications.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


object TrafficArrows {

    object Intersections {

    }

    object Roundabouts {


        @Composable
        fun Roundabout(
            modifier: Modifier,
            numberOfExists : Int,
            selectedExitIndex : Int?,
            roadColor : Color,
            selectedColor : Color
        ) {

            Canvas(
                modifier = modifier
            ) {

            }
        }
    }
}