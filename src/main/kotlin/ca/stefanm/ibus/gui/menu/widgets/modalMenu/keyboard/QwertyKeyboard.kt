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

            //Conjoin the list for scrolling between rows.
            val conjoinedQwertyRowList : List<ConjoinedListRecord<QwertyKeyDefinition, Int>> =
                qwertyKeyboardByRow.flatMapIndexed { index: Int, rowList: List<QwertyKeyDefinition> ->
                    rowList.mapIndexed { indexInRow, qwertyKeyDefinition ->
                        ConjoinedListRecord(
                            item = qwertyKeyDefinition,
                            sourcePlacementEnum = index,
                            //Scrolling will always jump from right-edge to left edge of every row.
                            //If we want to have smooth scrolling (right, down-one + right, move left)
                            //then we need to reverse every second rowList, and then this flag needs to do
                            //and indexOf on the original list of lists. See the QuadrantTwoColumn for an example to follow.
                            //Though, if you do that, then on even rows, scrolling right will move the selector left.
                            //So it's better to leave it for consistency.
                            originalItemPosition = indexInRow
                        )
                    }
                }

            fun QwertyKeyDefinition.onSelected() {
                val key = this
                if (key.specialTag !in SpecialTags.values()) {
                    if (isModifierCapitalized.value) key.upperCaseLabel.appendToState() else key.lowerCaseLabel.appendToState()
                } else {
                    when (key.specialTag) {
                        SpecialTags.Tab -> { "    ".appendToState() }
                        SpecialTags.CapsLock -> {
                            isCapsLocked.value = !isCapsLocked.value
                            isModifierCapitalized.value = !isModifierCapitalized.value
                        }
                        SpecialTags.Spacebar -> { " ".appendToState() }
                        SpecialTags.LeftArrow -> {
                            if (cursorPosition.value > 0) {
                                cursorPosition.let { pos ->
                                    pos.value = (pos.value - 1).rem(enteredText.value.length)
                                }
                            }
                        }
                        SpecialTags.RightArrow -> {
                            cursorPosition.let { pos ->
                                if (pos.value != enteredText.value.length) {
                                    pos.value =
                                        (pos.value + 1).rem(enteredText.value.length + 1)
                                }
                            }
                        }
                        SpecialTags.Cancel -> { closeWithoutEntry() }
                        SpecialTags.Return -> { onTextEntered(enteredText.value) }
                        SpecialTags.Shift -> { isModifierCapitalized.value = !isModifierCapitalized.value }
                        SpecialTags.BackSpace -> {
                            if (cursorPosition.value > 0) {
                                enteredText.value = enteredText.value.filterIndexed { index, c -> index != cursorPosition.value - 1 }
                                cursorPosition.value = cursorPosition.value - 1
                            }
                        }
                    }
                }
            }

            val knobQwertyKeyboardViewsByRow = remember { knobListenerService }.listenForKnob(
                listData = remember { conjoinedQwertyRowList },
                onSelectAdapter = { item, isNowSelected ->
                    ConjoinedListRecord(item.item.copy(isSelected = isNowSelected), item.second, item.third) },
                isSelectableAdapter = {
                    if (isModifierCapitalized.value) {
                        it.item.isUpperCaseSelectable
                    } else {
                        it.item.isLowerCaseSelectable
                    }
                },
                onItemClickAdapter = {
                    it.item.onSelected()
                }
            ).value

            fun SnapshotStateList<ConjoinedListRecord<QwertyKeyDefinition, Int>>.rePartitionByRow() : List<List<QwertyKeyDefinition>> {
                val returned : List<MutableList<ConjoinedListRecord<QwertyKeyDefinition, Int>>> = (0..qwertyKeyboardByRow.lastIndex).map { mutableListOf() }

                for (record in this) {
                    returned[record.sourcePlacementEnum].add(record)
                }

                for (sublist in returned) {
                    sublist.sortBy { it.originalItemPosition }
                }

                return returned.map {
                    it.map { inner -> inner.item }
                }
            }

            Box(Modifier.wrapContentSize().align(Alignment.CenterHorizontally)) {
                Column(
                    Modifier
                        .wrapContentWidth(unbounded = false)
                        .align(Alignment.Center)
                        .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    knobQwertyKeyboardViewsByRow.rePartitionByRow().forEachIndexed { index, row ->
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
                                            onMouseClick = { key.onSelected() }
                                        )
                                    } else {
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