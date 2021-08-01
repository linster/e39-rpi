package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.Composable

const val BackSpaceLabel = "Backspace"
const val ReturnLabel = "Return"
const val ShiftLabel = "Shift"

const val LeftArrowLabel = "<-"
const val RightArrowLabel = "->"

data class QwertyKeyDefinition(
    val lowerCaseLabel : String,
    val upperCaseLabel : String = lowerCaseLabel.uppercase(),
    val shiftModifierLabel : String = upperCaseLabel,
    val keySize : KeySize = KeySize.NORMAL
) {
    enum class KeySize(val scale : Double) {
        NORMAL(1.0),
        TAB(1.5),
        CAPSLOCK(1.75),
        LEFT_SHIFT(2.0),
        SPACE(5.0)
    }
}

internal fun QwertyKeyDefinition.toView(
    isSelectableLowerCase : Boolean = true,
    isSelectableUpperCase : Boolean = true,
    isSelected : Boolean = false,
    hideLabelIfNotCurrentlySelectable : Boolean = true, //If we are in SHIFT modifier, and there's keys that aren't selectable, keep the key but hide the label.
    onSelectedEmitString : ((char : String) -> Unit)? = null,
    onSelected : (isModifierUpperCase : Boolean) -> Unit = { onSelectedEmitString?.invoke(if (it) upperCaseLabel else lowerCaseLabel)},
) : @Composable () -> Unit = {

    //TODO use the KeySize to make an aspect ratio for a button.
}

val qwertyKeyboardByRow : List<List<QwertyKeyDefinition>> = listOf(
    listOf(),
    listOf(),
    listOf(),
    listOf()
)



