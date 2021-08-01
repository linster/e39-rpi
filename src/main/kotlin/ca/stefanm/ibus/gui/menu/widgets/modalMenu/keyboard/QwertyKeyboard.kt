package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal object QwertyKeyboard {


    @Composable
    internal fun QwertyKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        Column(Modifier.aspectRatio(3F).background(Color.Magenta).fillMaxSize(), verticalArrangement = Arrangement.Top) {

            Keyboard.CursorTextBoxViewer("Wat Foo bar", 1)
//                for (i in -1..7) {
//                    CursorTextBoxViewer("Wat Foo bar$i", i)
//                }

        }
    }

}