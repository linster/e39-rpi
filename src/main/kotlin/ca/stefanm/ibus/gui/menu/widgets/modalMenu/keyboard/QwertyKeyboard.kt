package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ginsberg.cirkle.circular

internal object QwertyKeyboard {


    @Composable
    internal fun QwertyKeyboard(
        preFilled : String = "",
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        Column(Modifier
            .aspectRatio(2.35F)
            .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            val enteredText = remember { mutableStateOf(preFilled)}

            val cursorPosition = remember { mutableStateOf(preFilled.length) }

            Keyboard.CursorTextBoxViewer(enteredText.value, cursorPosition.value)

            val isCapsLocked = remember { mutableStateOf(false) }
            val isModifierCapitalized = remember { mutableStateOf(false) }

            fun String.appendToState() {
                enteredText.value = enteredText.value + this
                cursorPosition.value = cursorPosition.value + this.length
            }

            LaunchedEffect(enteredText.value, cursorPosition.value) {
                println("Text, Cursor: ${enteredText.value.toList()}, ${cursorPosition.value}")
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
                                                    {
                                                        if (cursorPosition.value > 0) {
                                                            cursorPosition.let { pos ->
                                                                pos.value = (pos.value - 1).rem(enteredText.value.length)
                                                            }
                                                        }
                                                    }
                                                }
                                                SpecialTags.RightArrow -> {
                                                    {
                                                        cursorPosition.let { pos ->
                                                            if (pos.value != enteredText.value.length) {
                                                                pos.value =
                                                                    (pos.value + 1).rem(enteredText.value.length + 1)
                                                            }
                                                        }
                                                    }
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
                                                    {
                                                        if (cursorPosition.value > 0) {
                                                            enteredText.value = enteredText.value.filterIndexed { index, c -> index != cursorPosition.value - 1 }
                                                            cursorPosition.value = cursorPosition.value - 1
                                                        }
                                                    }
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