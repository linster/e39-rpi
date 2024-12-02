package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.lib.logging.StaticLogger
import ca.stefanm.ibus.lib.logging.StdOutLogger

enum class SpecialTags {
    Tab,
    CapsLock,
    Spacebar,
    LeftArrow,
    RightArrow,
    Cancel,
    Return,
    Shift,
    BackSpace
}

const val TabSpacerLabel = "Tab"
const val BackSpaceLabel = "Backspace"
const val CapslockLabel = "Caps"
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

    var isSelected: Boolean = false,

    val isLowerCaseSelectable : Boolean = true,
    val isUpperCaseSelectable : Boolean = isLowerCaseSelectable,

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
    onMouseClick : () -> Unit = {}
) {
    val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled

    fun Dp.halveIfNotPixelDoubled() : Dp = if (!isPixelDoubled) (this.value / 2F).dp else this

    Box(Modifier.border(
        width = 4.dp.halveIfNotPixelDoubled(),
        color = if (isSelected) {
            ThemeWrapper.ThemeHandle.current.colors.selectedColor
        } else {
            ThemeWrapper.ThemeHandle.current.colors.menuBackground
        })
        .height(38.dp.halveIfNotPixelDoubled())
        .then(if (keySize == QwertyKeyDefinition.KeySize.FLEX) {
            //This is probably a good place for Intrinsics?
            if (specialTag == SpecialTags.Return) {
                Modifier.width(85.dp.halveIfNotPixelDoubled())
            } else {
                Modifier.widthIn(38.dp.halveIfNotPixelDoubled(), (3 * 38).dp.halveIfNotPixelDoubled()).fillMaxWidth()
            }
        } else {
            Modifier.aspectRatio(keySize.scale.toFloat())
        })
        .clickable { onMouseClick() }
        , contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUpperCase) { upperCaseLabel } else { lowerCaseLabel },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (isPixelDoubled) 16.sp else 8.sp
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
        QwertyKeyDefinition(BackSpaceLabel, BackSpaceLabel, keySize = QwertyKeyDefinition.KeySize.FLEX, specialTag = SpecialTags.BackSpace)
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
        QwertyKeyDefinition("\\", "|", keySize = QwertyKeyDefinition.KeySize.TAB)
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
        QwertyKeyDefinition(CancelLabel, CancelLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Cancel)
    )

)

