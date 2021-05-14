package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.Keyboard
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import javax.inject.Inject

class MenuWindow @Inject constructor() {

    fun show(x : Int, y : Int) {
        Window(
            title = "BMW E39 Nav Menu",
            size = IntSize(800, 468),
            undecorated = true,
            centered = false,
            location = IntOffset(x, y+30)
        ) {
            Image(
                bitmap = imageFromResource("bmw_navigation.png"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                    .background(Color.Cyan)
                    .clickable { AppManager.focusedWindow?.close() }
            )
        }
    }
}