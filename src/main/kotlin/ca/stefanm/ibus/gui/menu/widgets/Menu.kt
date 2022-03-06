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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(229, 255, 255, 255)
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
        color = Color(229, 255, 255, 255)
    )
}




@Composable
fun CenterGradientWithEdgeHighlight(
    highlightAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable () -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(68, 128, 192, 255),
                            Color(61, 112, 176, 255),
                            Color(68, 128, 192, 255)
                        )
                    )
                )
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            content()
        }
        Spacer(
            modifier = Modifier
                .background(Color(86, 139, 191, 255))
                .height(4.dp)
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