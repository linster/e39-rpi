package ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ConjoinedListRecord

@Immutable
internal interface KeyboardStateManagerScope {
    val enteredText : State<String>
    val cursorPosition : State<Int>
    val isCapsLocked : State<Boolean>
    val isModifierCapitalized : State<Boolean>
    fun QwertyKeyDefinition.onSelected()
    fun String.appendToState()
}

@Composable
internal fun StateManagedKeyboard(
    preFilled : String = "",
    onTextEntered: (entered: String) -> Unit,
    closeWithoutEntry: () -> Unit,
    drawContents : @Composable KeyboardStateManagerScope.() -> Unit
) {

    val enteredText = remember { mutableStateOf(preFilled) }
    val cursorPosition = remember { mutableStateOf(preFilled.length) }

    val isCapsLocked = remember { mutableStateOf(false) }
    val isModifierCapitalized = remember { mutableStateOf(false) }

    LaunchedEffect(enteredText.value, cursorPosition.value) {
        println(enteredText.value to cursorPosition.value)
    }

    fun String.appendToState() {
        if (cursorPosition.value == enteredText.value.length) {
            enteredText.value = enteredText.value + this
            cursorPosition.value = cursorPosition.value + this.length
        } else {
            //We're appending in the middle of the string.
            enteredText.value =
                enteredText.value.slice(0 until cursorPosition.value) +
                        this +
                        enteredText.value.slice(cursorPosition.value .. enteredText.value.lastIndex)

            cursorPosition.value = cursorPosition.value + 1
        }
    }

    fun QwertyKeyDefinition.onSelected() {
        val key = this
        if (key.specialTag !in SpecialTags.values()) {
            if (isModifierCapitalized.value) {
                if (isUpperCaseSelectable) {
                    key.upperCaseLabel.appendToState()
                }
            } else {
                if (isLowerCaseSelectable) {
                    key.lowerCaseLabel.appendToState()
                }
            }
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

    drawContents.invoke(object :KeyboardStateManagerScope {
        override val enteredText = enteredText
        override val cursorPosition = cursorPosition
        override val isCapsLocked = isCapsLocked
        override val isModifierCapitalized = isModifierCapitalized
        override fun QwertyKeyDefinition.onSelected() = onSelected()
        override fun String.appendToState() = appendToState()
    })
}



@Composable
internal fun KeyboardStateManagerScope.RowBasedKeyboard(
    knobListenerService: KnobListenerService,
    keysByRow : List<List<QwertyKeyDefinition>>,
    contents : @Composable RowBasedKeyboardScope.() -> Unit
) {
    //All the keyboard keys are arranged in rows of equal height.

    //Conjoin the list for scrolling between rows.
    val conjoinedQwertyRowList : List<ConjoinedListRecord<QwertyKeyDefinition, Int>> =
        keysByRow.flatMapIndexed { index: Int, rowList: List<QwertyKeyDefinition> ->
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

    contents(object : RowBasedKeyboardScope {
        override val scrollableKeysPartitionedByRow: List<List<QwertyKeyDefinition>>
            get() = knobQwertyKeyboardViewsByRow.rePartitionByRow()
    })

}

@Immutable
internal interface RowBasedKeyboardScope {
    val scrollableKeysPartitionedByRow : List<List<QwertyKeyDefinition>>

}