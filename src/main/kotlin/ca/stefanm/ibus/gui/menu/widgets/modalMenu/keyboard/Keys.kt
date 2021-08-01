package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

object SpecialTags {
    object Tab
    object CapsLock
    object Spacebar
}

const val TabSpacerLabel = "Tab"
const val BackSpaceLabel = "Backspace"
const val CapslockLabel = "Caps Lock"
const val ReturnLabel = "Return"
const val ShiftLabel = "Shift"

const val LeftArrowLabel = "<-"
const val RightArrowLabel = "->"

const val CancelLabel = "Cancel"
data class QwertyKeyDefinition(
    val lowerCaseLabel : String,
    val upperCaseLabel : String = lowerCaseLabel.uppercase(),
    val shiftModifierLabel : String = upperCaseLabel,
    val keySize : KeySize = KeySize.NORMAL,
    val specialTag : Any? = null //For keys with empty labels, so we can do an equality check on a tab vs caps lock.
) {
    enum class KeySize(val scale : Double) {
        NORMAL(1.0),
        TAB(1.5),
        CAPSLOCK(1.75),
        LEFT_SHIFT(2.0),
        SPACE(5.0),
        FLEX(-1.0) //Allow the key size to fit whatever space remains.
    }
}

@Composable
internal fun QwertyKeyDefinition.toView(
    isSelectableLowerCase : Boolean = true,
    isSelectableUpperCase : Boolean = true,
    isSelected : Boolean = false,
    hideLabelIfNotCurrentlySelectable : Boolean = true, //If we are in SHIFT modifier, and there's keys that aren't selectable, keep the key but hide the label.
    onSelectedEmitString : ((char : String) -> Unit)? = null,
    onSelected : (isModifierUpperCase : Boolean) -> Unit = { onSelectedEmitString?.invoke(if (it) upperCaseLabel else lowerCaseLabel)},
) {

    //TODO use the KeySize to make an aspect ratio for a button.
}

internal val qwertyKeyboardByRow : List<List<QwertyKeyDefinition>> = listOf(
    listOf(
        QwertyKeyDefinition("1","!"),
        QwertyKeyDefinition("2","@"),
        QwertyKeyDefinition("3","#"),
        QwertyKeyDefinition("4","$"),
        QwertyKeyDefinition("5","%"),
        QwertyKeyDefinition("6","^"),
        QwertyKeyDefinition("7","&"),
        QwertyKeyDefinition("8","*"),
        QwertyKeyDefinition("9","("),
        QwertyKeyDefinition("0",")"),
        QwertyKeyDefinition("-","_"),
        QwertyKeyDefinition("=","+"),
        QwertyKeyDefinition(BackSpaceLabel, BackSpaceLabel, keySize = QwertyKeyDefinition.KeySize.FLEX)
    ),
    listOf(
        QwertyKeyDefinition(TabSpacerLabel, TabSpacerLabel, keySize = QwertyKeyDefinition.KeySize.TAB),
        QwertyKeyDefinition("q"),
        QwertyKeyDefinition("w"),
        QwertyKeyDefinition("e"),
        QwertyKeyDefinition("r"),
        QwertyKeyDefinition("t"),
        QwertyKeyDefinition("y"),
        QwertyKeyDefinition("u"),
        QwertyKeyDefinition("i"),
        QwertyKeyDefinition("o"),
        QwertyKeyDefinition("p"),
        QwertyKeyDefinition("[", "{"),
        QwertyKeyDefinition("]", "}"),
        QwertyKeyDefinition("\\", "|")
    ),
    listOf(
        QwertyKeyDefinition(CapslockLabel, CapslockLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.CapsLock),
        QwertyKeyDefinition("a"),
        QwertyKeyDefinition("s"),
        QwertyKeyDefinition("d"),
        QwertyKeyDefinition("f"),
        QwertyKeyDefinition("g"),
        QwertyKeyDefinition("h"),
        QwertyKeyDefinition("j"),
        QwertyKeyDefinition("k"),
        QwertyKeyDefinition("l"),
        QwertyKeyDefinition(";", ":"),
        QwertyKeyDefinition("'", "\""),
        QwertyKeyDefinition(ReturnLabel, ReturnLabel, keySize = QwertyKeyDefinition.KeySize.FLEX)
    ),
    listOf(
        QwertyKeyDefinition(ShiftLabel, ShiftLabel, keySize = QwertyKeyDefinition.KeySize.LEFT_SHIFT),
        QwertyKeyDefinition("z"),
        QwertyKeyDefinition("x"),
        QwertyKeyDefinition("c"),
        QwertyKeyDefinition("v"),
        QwertyKeyDefinition("b"),
        QwertyKeyDefinition("n"),
        QwertyKeyDefinition("m"),
        QwertyKeyDefinition(",", "<"),
        QwertyKeyDefinition(".", ">"),
        QwertyKeyDefinition("/", "?"),
        QwertyKeyDefinition(ShiftLabel, ShiftLabel, keySize = QwertyKeyDefinition.KeySize.FLEX)
    ),
    listOf(
        //TODO when laying this out, put some spaces in here with weights.
        QwertyKeyDefinition("", "", keySize = QwertyKeyDefinition.KeySize.SPACE, specialTag = SpecialTags.Spacebar),
        QwertyKeyDefinition(LeftArrowLabel, LeftArrowLabel),
        QwertyKeyDefinition(RightArrowLabel, RightArrowLabel),
        QwertyKeyDefinition(CancelLabel, CancelLabel)
    )

)

