package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import kotlinx.coroutines.delay


object Keyboard {

    enum class KeyboardType {
        FULL,
        NUMERIC,
        TELEPHONE
    }

    fun showKeyboard(
        type: KeyboardType,
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered : (entered : String) -> Unit,
        closeWithoutEntry : () -> Unit
    ) : @Composable () -> Unit = {
        //This might actually be a good use-case for a CompositionLocal.

        KeyboardViews.KeyboardPane {
            when (type) {
                KeyboardType.FULL -> QwertyKeyboard.QwertyKeyboard(prefilled, knobListenerService, onTextEntered, closeWithoutEntry)
                KeyboardType.NUMERIC -> GridKeyboard.NumericKeyboard(prefilled, knobListenerService, onTextEntered, closeWithoutEntry)
                KeyboardType.TELEPHONE -> GridKeyboard.TelephoneKeyboard(prefilled, knobListenerService, onTextEntered, closeWithoutEntry)
            }
        }
    }



}