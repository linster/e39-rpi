package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors

object KeyboardViews {
    //This provides the big blue area popped in from the bottom that we're going to draw the keyboard in.
    @Composable
    internal fun KeyboardPane(
        maxHeight : Float = 0.6F,
        contents: @Composable () -> Unit
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
                    .fillMaxHeight(maxHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    contents()
                }
            }
        }
    }

    @Composable
    internal fun CursorTextBoxViewer(
        pendingText: String,
        cursorAfterIndex: Int //After which character 0-indexed is the cursor located
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(ChipItemColors.MenuBackground)
                .padding(4.dp)
        ) {
            Row {

                @Composable
                fun displayChar(char: Char) {
                    Text(
                        text = " $char ",
                        color = ChipItemColors.TEXT_WHITE,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                    )
                }

                @Composable
                fun displayCursor() {
                    Text(
                        text = " ",
                        color = ChipItemColors.TEXT_WHITE,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.background(ChipItemColors.SelectedColor)
                    )
                }

                if (pendingText.isEmpty() || cursorAfterIndex == 0) {
                    displayCursor()
                }

                pendingText.forEachIndexed { index, c ->
                    displayChar(c)
                    if (index + 1 == cursorAfterIndex) {
                        displayCursor()
                    }
                }
            }
        }
    }
}