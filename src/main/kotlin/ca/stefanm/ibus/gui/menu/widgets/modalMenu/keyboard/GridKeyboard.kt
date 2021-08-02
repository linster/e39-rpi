package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService

internal object GridKeyboard {


    @Composable
    internal fun NumericKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    internal fun TelephoneKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    private fun GridKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit,
        rows : Int,
        columns : Int,
        aspectRatio : Float,
        keys : List<QwertyKeyDefinition> //Populated from top-left, across, then down.
    ) {
        //Hold the preview window AND the buttons here
    }
}