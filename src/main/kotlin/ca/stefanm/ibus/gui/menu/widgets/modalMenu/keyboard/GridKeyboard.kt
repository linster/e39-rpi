package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.Composable

internal object GridKeyboard {


    @Composable
    internal fun NumericKeyboard(
        prefilled : String = "",
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    internal fun TelephoneKeyboard(
        prefilled : String = "",
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    private fun GridKeyboard(
        prefilled : String = "",
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