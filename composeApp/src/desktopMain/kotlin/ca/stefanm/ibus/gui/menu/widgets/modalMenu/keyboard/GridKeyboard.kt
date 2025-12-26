package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService

internal object GridKeyboard {

    @Composable
    internal fun NumericKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

        val keysByRow = listOf(
            listOf(
                QwertyKeyDefinition("7"),
                QwertyKeyDefinition("8"),
                QwertyKeyDefinition("9"),
                QwertyKeyDefinition(CancelLabel, CancelLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Cancel)
            ),
            listOf(
                QwertyKeyDefinition("4"),
                QwertyKeyDefinition("5"),
                QwertyKeyDefinition("6"),
                QwertyKeyDefinition(LeftArrowLabel, LeftArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.LeftArrow)
            ),
            listOf(
                QwertyKeyDefinition("1"),
                QwertyKeyDefinition("2"),
                QwertyKeyDefinition("3"),
                QwertyKeyDefinition(RightArrowLabel, RightArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.RightArrow),
                QwertyKeyDefinition(BackSpaceLabel, BackSpaceLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.BackSpace),
                QwertyKeyDefinition(ReturnLabel, ReturnLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Return)
            )
        )

        GridKeyboard(
            prefilled, keysByRow,
            aspectRatio = 1.65F,
            knobListenerService, onTextEntered, closeWithoutEntry
        )
    }

    @Composable
    internal fun TelephoneKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {

        val keysByRow = listOf(
            listOf(
                QwertyKeyDefinition("1", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("2", "ABC", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("3", "DEF", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(CancelLabel, CancelLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Cancel)
            ),
            listOf(
                QwertyKeyDefinition("4", "GHI", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("5", "JKL", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("6", "MNO", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(LeftArrowLabel, LeftArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.LeftArrow)
            ),
            listOf(
                QwertyKeyDefinition("7", "PQRS", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("8", "TUV", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("9", "WXYZ", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(RightArrowLabel, RightArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.RightArrow)
            ),
            listOf(
                QwertyKeyDefinition("*", "", keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("0", "+", keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("#", "", keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(ReturnLabel, ReturnLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Return)
            ),
            listOf(
                QwertyKeyDefinition("Letters", "Letters", keySize = QwertyKeyDefinition.KeySize.LEFT_SHIFT, specialTag = SpecialTags.Shift)
            )
        )

        GridKeyboard(
            prefilled, keysByRow,
            aspectRatio = 1.85F,
            knobListenerService, onTextEntered, closeWithoutEntry
        )
    }

    @Composable
    internal fun TimePickerKeyboard(
        prefilled : String = "",
        knobListenerService: KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        val keysByRow = listOf(
            listOf(
                QwertyKeyDefinition("1", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("2", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("3", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(":00", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(CancelLabel, CancelLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Cancel),
                QwertyKeyDefinition(ClearLabel, ClearLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Clear),
            ),
            listOf(
                QwertyKeyDefinition("4", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("5", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("6", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(":15", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(LeftArrowLabel, LeftArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.LeftArrow),
                QwertyKeyDefinition(" AM", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
            ),
            listOf(
                QwertyKeyDefinition("7", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("8", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition("9", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(":30", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(RightArrowLabel, RightArrowLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.RightArrow),
                QwertyKeyDefinition(" PM", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
            ),
            listOf(
                QwertyKeyDefinition("0", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(":", ":", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(" ", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(":45", "", isUpperCaseSelectable = false, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK),
                QwertyKeyDefinition(ReturnLabel, ReturnLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.Return),
                QwertyKeyDefinition(BackSpaceLabel, BackSpaceLabel, keySize = QwertyKeyDefinition.KeySize.CAPSLOCK, specialTag = SpecialTags.BackSpace),
            )
        )

        GridKeyboard(
            prefilled, keysByRow,
            aspectRatio = 3F,
            knobListenerService, onTextEntered, closeWithoutEntry
        )

    }

    @Composable
    private fun GridKeyboard(
        prefilled: String,
        keysByRow : List<List<QwertyKeyDefinition>>,
        aspectRatio: Float,
        knobListenerService : KnobListenerService,
        onTextEntered: (entered: String) -> Unit,
        closeWithoutEntry: () -> Unit
    ) {
        StateManagedKeyboard(
            preFilled = prefilled,
            onTextEntered = onTextEntered,
            closeWithoutEntry = closeWithoutEntry
        ) {
            RowBasedKeyboard(
                knobListenerService,
                keysByRow = keysByRow
            ) {
                GridKeyboardView(
                    this@StateManagedKeyboard,
                    this@RowBasedKeyboard,
                    aspectRatio
                )
            }
        }

    }

    @Composable
    private fun GridKeyboardView(
        stateScope : KeyboardStateManagerScope,
        rowStateManagerScope: RowBasedKeyboardScope,
        aspectRatio : Float
    ) {
        Column(
            Modifier
            .aspectRatio(aspectRatio)
            .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            KeyboardViews.CursorTextBoxViewer(stateScope.enteredText.value, stateScope.cursorPosition.value)

            Box(Modifier.wrapContentSize().align(Alignment.CenterHorizontally)) {
                Column(
                    Modifier
                    .wrapContentWidth(unbounded = false)
                    .align(Alignment.Center)
                    .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rowStateManagerScope.scrollableKeysPartitionedByRow.forEachIndexed { index, row ->
                        val isLast = index == qwertyKeyboardByRow.lastIndex
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { key ->
                                Column(
                                    Modifier.wrapContentHeight(),
                                    verticalArrangement = Arrangement.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    key.toView(
                                        isUpperCase = stateScope.isModifierCapitalized.value,
                                        onMouseClick = {
                                            with(stateScope) {
                                                key.onSelected()
                                            }
                                        }
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