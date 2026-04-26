package ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper


//Background, goes into a box.
//Has the timeslot bars.
//Scroll bars too for up/down.

@Composable
fun TimeSlotBars(

) {

    Column(Modifier.fillMaxSize()) {

        (1..3).forEach { i ->
            SlotDivider()
            SlotLabel(Modifier, "0${i}:00")
        }

        CalendarEventBox(
            modifier = Modifier,
            header = "Plast",
            body = "7-8 pm",
            onClick = {},
            baseColor = Color.Cyan
        )
    }
}

@Composable
fun SlotDivider(
    modifier: Modifier = Modifier,
) {
    val theme = ThemeWrapper.ThemeHandle.current
    val strokeWidth = theme.bigItem.highlightWidth.let { it / 2F }

    Box(
        modifier = Modifier.height(strokeWidth.dp).fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                        0F to Color.Transparent,
                        0.15F to theme.colors.sideMenuBorder,
                        0.5F to theme.colors.sideMenuBorder,
                        0.85F to theme.colors.sideMenuBorder,
                        1F to Color.Transparent,
                    tileMode = TileMode.Clamp
                )
            ).then(modifier)
    )
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    colorOverride : Color? = null
) {
    val theme = ThemeWrapper.ThemeHandle.current
    val strokeWidth = theme.bigItem.highlightWidth.let { it / 2F }

    if (colorOverride != null) {
        Box(
            modifier = Modifier.width(strokeWidth.dp)
                .background(colorOverride).then(modifier)
        )
    } else {
        Box(
            modifier = Modifier.width(strokeWidth.dp)
                .background(
                    Brush.verticalGradient(
                        0F to Color.Transparent,
                        0.15F to theme.colors.sideMenuBorder,
                        0.5F to theme.colors.sideMenuBorder,
                        0.85F to theme.colors.sideMenuBorder,
                        1F to Color.Transparent,
                        tileMode = TileMode.Clamp
                    )
                ).then(modifier)
        )
    }
}

@Composable
fun SlotLabel(
    modifier: Modifier = Modifier,
    label : String
) {
    val theme = ThemeWrapper.ThemeHandle.current
    val strokeWidth = theme.bigItem.highlightWidth.let { it / 2F }

    Text(modifier = modifier, text = label, color = theme.colors.TEXT_WHITE, maxLines = 1, fontSize = theme.smallItem.fontSize, fontWeight = FontWeight.Light)
}