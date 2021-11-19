package ca.stefanm.ibus.gui.menu.widgets.bottombar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard

@Composable
fun BmwFullScreenBottomBar(
    date : String = "--/--/--",
    time : String = "00:00",
    centerContent : @Composable () -> Unit = { Text("") }
) {
    CenterGradientWithEdgeHighlight(highlightAlignment = Alignment.TopCenter) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = time,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(229, 255, 255, 255)
            )
            centerContent()
            Text(
                text = date,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(229, 255, 255, 255)
            )
        }
    }
}