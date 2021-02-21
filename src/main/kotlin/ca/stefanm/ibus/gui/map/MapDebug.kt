package ca.stefanm.ibus.gui.map

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.IntSize
import javax.inject.Inject

class MapDebug @Inject constructor(
    private val mapWidget: MapWidget
) {

    fun show() {
        Window(
            title = "Map Debug",
            size = IntSize(600, 400),
        ) {
            mapWidget.widget(200, 200)
        }
    }
}