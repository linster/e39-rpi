package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal object QwertyKeyboard {


    @Composable
    internal fun QwertyKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        Column(Modifier
            .aspectRatio(2.35F)
            .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            val enteredText = remember { mutableStateOf("")}
            val cursorPosition = remember { mutableStateOf(-1) }

            Keyboard.CursorTextBoxViewer(enteredText.value, cursorPosition.value)

            val isCapsLocked = remember { mutableStateOf(false) }
            val isModifierCapitalized = remember { mutableStateOf(false) }

            fun String.appendToState() {
                enteredText.value = enteredText.value + this
                cursorPosition.value = cursorPosition.value + this.length
            }

            fun backSpace() {

            }

            Box(Modifier.wrapContentSize().align(Alignment.CenterHorizontally)) {
                Column(
                    Modifier
                        .wrapContentWidth(unbounded = false)
                        .align(Alignment.Center)
                        .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    qwertyKeyboardByRow.forEachIndexed { index, row ->
                        val isLast = index == qwertyKeyboardByRow.lastIndex
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { key ->
                                Column(
                                    Modifier.wrapContentHeight(),
                                    verticalArrangement = Arrangement.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (key.specialTag != null) {
                                        key.toView(
                                            isUpperCase = isModifierCapitalized.value,
                                            onSelectedEmitString = when (key.specialTag) {
                                                SpecialTags.Tab -> {
                                                    { "    ".appendToState() }
                                                }
                                                SpecialTags.CapsLock -> {
                                                    {
                                                        isCapsLocked.value = !isCapsLocked.value
                                                        isModifierCapitalized.value = !isModifierCapitalized.value
                                                    }
                                                }
                                                SpecialTags.Spacebar -> {
                                                    { " ".appendToState() }
                                                }
                                                SpecialTags.LeftArrow -> {
                                                    { cursorPosition.value = cursorPosition.value - 1 }
                                                }
                                                SpecialTags.RightArrow -> {
                                                    { cursorPosition.value = cursorPosition.value + 1 }
                                                }
                                                SpecialTags.Cancel -> {
                                                    { closeWithoutEntry() }
                                                }
                                                SpecialTags.Return -> {
                                                    { onTextEntered(enteredText.value) }
                                                }
                                                SpecialTags.Shift -> {
                                                    { isModifierCapitalized.value = !isModifierCapitalized.value }
                                                }
                                                SpecialTags.BackSpace -> {
                                                    { backSpace() }
                                                }
                                            }
                                        )
                                    } else {
                                        key.toView(
                                            isUpperCase = isModifierCapitalized.value,
                                            onSelectedEmitString = { it.appendToState() })
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

}