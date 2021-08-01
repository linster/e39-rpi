package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object Keyboard {

    enum class KeyboardType {
        FULL,
        NUMERIC,
        TELEPHONE
    }

    fun showKeyboard(
        type: KeyboardType,
        onTextEntered : (entered : String) -> Unit,
        closeWithoutEntry : () -> Unit
    ) : @Composable () -> Unit = {
        //This might actually be a good use-case for a CompositionLocal.

        KeyboardPane {
            LaunchedEffect(true) {
                delay(30_000)
                closeWithoutEntry()
            }
            when (type) {
                KeyboardType.FULL -> QwertyKeyboard(onTextEntered, closeWithoutEntry)
            }
        }
    }


    //This provides the big blue area popped in from the bottom that we're going to draw the keyboard in.
    @Composable
    private fun KeyboardPane(
        contents : @Composable () -> Unit
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier
                    .border(width = 2.dp, color = Color(48, 72, 107, 255))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(68, 128, 192, 255),
                                Color(61, 112, 176, 255),
                                Color(68, 128, 192, 255)
                            )
                        )
                    )
                    .fillMaxWidth()
                    .fillMaxHeight(0.6F),
                contentAlignment = Alignment.Center
            ) {
                contents()
            }
        }
    }

    @Composable
    private fun CursorTextBoxViewer(
        pendingText : String,
        cursorAfterIndex : Int //After which character 0-indexed is the cursor located
    ) {
        Box(Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .background(ChipItemColors.MenuBackground)
        ) {
            Row {

                @Composable fun displayChar(char : Char) {
                    Text(
                        text = " $char ",
                        color = ChipItemColors.TEXT_WHITE,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.background(ChipItemColors.TEXT_BLUE_LIGHT)
                    )
                }

                @Composable fun displayCursor() {
                    Text(
                        text = " ",
                        color = ChipItemColors.TEXT_WHITE,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.background(ChipItemColors.SelectedColor)
                    )
                }

                pendingText.forEachIndexed { index, c ->
                    if (cursorAfterIndex == -1) {
                        displayCursor()
                    }

                    displayChar(c)
                    if (index + 1 == cursorAfterIndex) {
                        displayCursor()
                    }
                }
            }
        }
    }

    @Composable
    private fun QwertyKeyboard(
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        Box(Modifier.aspectRatio(2F).background(Color.Magenta).fillMaxSize()) {
            Column {
                for (i in -1..7) {
                    CursorTextBoxViewer("Wat Foo bar", i)
                }
            }
        }
    }


    @Composable
    private fun drawKeyboard() {

    }

}