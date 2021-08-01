package ca.stefanm.ibus.gui.menu.widgets.modalMenu

const val BackSpaceLabel = "Backspace"
const val ReturnLabel = "Return"
const val ShiftLabel = "Shift"

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

val qwertyKeyboardByRow : List<List<QwertyKeyDefinition>> = listOf(
    listOf()
)