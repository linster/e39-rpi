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
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors

object KeyboardViews {
    //This provides the big blue area popped in from the bottom that we're going to draw the keyboard in.
    @Composable
    internal fun KeyboardPane(
        maxHeight : Float = 0.6F,
        contents: @Composable () -> Unit
    ) {
        val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier
                    .border(
                        width = if (isPixelDoubled) 2.dp else 1.dp,
                        color = ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .background(
                        Brush.horizontalGradient(
                            ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.backgroundGradientColorList
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
        val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .padding(if (isPixelDoubled) 4.dp else 2.dp)
        ) {
            Row {

                @Composable
                fun displayChar(char: Char) {
                    Text(
                        text = " $char ",
                        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                        fontSize = if (isPixelDoubled) 24.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                    )
                }

                @Composable
                fun displayCursor() {
                    Text(
                        text = " ",
                        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                        fontSize = if (isPixelDoubled) 24.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.background(ThemeWrapper.ThemeHandle.current.colors.selectedColor)
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