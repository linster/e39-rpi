package ca.stefanm.ibus.gui.menu.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BmwFullScreenBottomBar


@Composable
fun BmwSingleLineHeader(
    text : String = "Menu"
) {
    CenterGradientWithEdgeHighlight {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text.toUpperCase(),
                fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                fontWeight = FontWeight.Bold,
                color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
            )
        }
    }
}

@Composable
fun BmwFullScreenTrackInfoHeader(
    t0: String = "",
    t1: String = "",
    t2: String = "",
    t3: String = "",
    t4: String = "",
    t5: String = "",
    t6 : String = ""
) {
    CenterGradientWithEdgeHighlight {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(0.5f, true)) {
                TrackInfoText(t0, size = 48)
            }
            Column(modifier = Modifier.weight(0.5f, true).align(Alignment.CenterVertically)) {
                Row {
                    Column(modifier = Modifier.weight(0.3f, true)) { TrackInfoText(t4) }
                    Column(modifier = Modifier.weight(0.5f, true)) { TrackInfoText(t5) }
                    Column(modifier = Modifier.weight(0.2f, true)) { TrackInfoText(t2) }
                }

                Row {
                    Column(modifier = Modifier.weight(0.3f, true)) { TrackInfoText(t3) }
                    Column(modifier = Modifier.weight(0.5f, true)) { TrackInfoText(t6) }
                    Column(modifier = Modifier.weight(0.2f, true)) { TrackInfoText(t1) }
                }
            }
        }
    }
}

@Composable
fun TrackInfoText(text: String, size : Int = 18) {
    Text(
        text = text.toUpperCase(),
        fontSize = size.sp,
        fontWeight = FontWeight.Bold,
        color = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
    )
}




@Composable
fun CenterGradientWithEdgeHighlight(
    highlightAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable () -> Unit
) {
    val hp = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.headerPadding

    Box {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.backgroundGradientColorList
                    )
                )
                .padding(start = hp.start, end = hp.end, top = hp.top, bottom = hp.bottom)
        ) {
            content()
        }
        Spacer(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.edgeHighlightColor)
                .height(
                    ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.edgeHighlightHeight
                )
                .align(highlightAlignment)
                .fillMaxWidth()
        )
    }
}

//@Composable
//fun BmwFullScreenMenuItem(
//    label: String,
//    isSelected: Boolean = true,
//    onClick: () -> Unit = {}
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .background(Color.Cyan)
//            .then(if (isSelected) { Modifier.border(5.dp, Color.Red) } else { Modifier })
//    ) {
//        Box(Modifier.padding(15.dp)) {
//            Text(label)
//        }
//    }
//}