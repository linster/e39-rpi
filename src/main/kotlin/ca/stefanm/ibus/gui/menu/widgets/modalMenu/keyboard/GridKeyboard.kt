package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.Composable

internal object GridKeyboard {


    @Composable
    internal fun NumericKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    internal fun TelephoneKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

    }

    @Composable
    private fun GridKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit,
        rows : Int,
        columns : Int,
        aspectRatio : Float,
        keys : List<QwertyKeyDefinition> //Populated from top-left, across, then down.
    ) {
        //Hold the preview window AND the buttons here
    }


    @Composable
    fun GridKeyboardButtons(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit,
        rows : Int,
        columns : Int,
        aspectRatio : Float,
        keys : List<QwertyKeyDefinition> //Populated from top-left, across, then down.
    ) {
        
    }
}