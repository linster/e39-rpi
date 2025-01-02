package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ConjoinedListRecord
import com.ginsberg.cirkle.circular

internal object QwertyKeyboard {

    @Composable
    internal fun QwertyKeyboard(
        preFilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        StateManagedKeyboard(
            preFilled = preFilled,
            onTextEntered = onTextEntered,
            closeWithoutEntry = closeWithoutEntry
        ) {
            RowBasedKeyboard(knobListenerService, qwertyKeyboardByRow) {
                Column(Modifier
                    .aspectRatio(2.35F)
                    .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    KeyboardViews.CursorTextBoxViewer(enteredText.value, cursorPosition.value)

                    Box(Modifier.wrapContentSize().align(Alignment.CenterHorizontally)) {
                        Column(Modifier
                            .wrapContentWidth(unbounded = false)
                            .align(Alignment.Center)
                            .background(Color.Transparent),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            scrollableKeysPartitionedByRow.forEachIndexed { index, row ->
                                val isLast = index == qwertyKeyboardByRow.lastIndex
                                Row(Modifier.fillMaxWidth()) {
                                    row.forEach { key ->
                                        Column(
                                            Modifier.wrapContentHeight(),
                                            verticalArrangement = Arrangement.Bottom,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            key.toView(
                                                isUpperCase = isModifierCapitalized.value,
                                                onMouseClick = { key.onSelected() }
                                            )
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

}