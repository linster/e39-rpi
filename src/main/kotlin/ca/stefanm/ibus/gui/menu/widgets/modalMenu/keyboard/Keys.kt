package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors

enum class SpecialTags {
    Tab,
    CapsLock,
    Spacebar,
    LeftArrow,
    RightArrow,
    Cancel,
    Return,
    Shift
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
    val specialTag : SpecialTags? = null //For keys with empty labels, so we can do an equality check on a tab vs caps lock.
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
    isUpperCase : Boolean = false,
    isSelected : Boolean = false,
    hideLabelIfNotCurrentlySelectable : Boolean = true, //If we are in SHIFT modifier, and there's keys that aren't selectable, keep the key but hide the label.
    onSelectedEmitString : ((char : String) -> Unit)? = null,
    onSelected : (isModifierUpperCase : Boolean) -> Unit = { onSelectedEmitString?.invoke(if (it) upperCaseLabel else lowerCaseLabel)},
) {

    Box(Modifier.border(
        width = 4.dp,
        color = if (isSelected) {
            ChipItemColors.SelectedColor
        } else {
            ChipItemColors.MenuBackground
        })
        .height(38.dp)
        .then(if (keySize == QwertyKeyDefinition.KeySize.FLEX) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.aspectRatio(keySize.scale.toFloat())
        })
        .clickable { onSelected(isUpperCase) }
        , contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (hideLabelIfNotCurrentlySelectable && isSelected) "" else {
                if (isUpperCase) {
                    upperCaseLabel
                } else {
                    lowerCaseLabel
                }
            },
            color = Color.White
        )
    }
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
        QwertyKeyDefinition(ReturnLabel, ReturnLabel, keySize = QwertyKeyDefinition.KeySize.FLEX, specialTag = SpecialTags.Return)
    ),
    listOf(
        QwertyKeyDefinition(ShiftLabel, ShiftLabel, keySize = QwertyKeyDefinition.KeySize.LEFT_SHIFT, specialTag = SpecialTags.Shift),
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
        QwertyKeyDefinition(ShiftLabel, ShiftLabel, keySize = QwertyKeyDefinition.KeySize.FLEX, specialTag = SpecialTags.Shift)
    ),
    listOf(
        //TODO when laying this out, put some spaces in here with weights.
        QwertyKeyDefinition("", "", keySize = QwertyKeyDefinition.KeySize.SPACE, specialTag = SpecialTags.Spacebar),
        QwertyKeyDefinition(LeftArrowLabel, LeftArrowLabel, specialTag = SpecialTags.LeftArrow),
        QwertyKeyDefinition(RightArrowLabel, RightArrowLabel, specialTag = SpecialTags.RightArrow),
        QwertyKeyDefinition(CancelLabel, CancelLabel, specialTag = SpecialTags.Cancel)
    )

)

