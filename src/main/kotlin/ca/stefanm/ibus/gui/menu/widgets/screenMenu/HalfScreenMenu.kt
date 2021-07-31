package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.reduceUpdateOnClick
import org.intellij.lang.annotations.JdkConstants
import kotlin.math.E

object HalfScreenMenu {

    /**
     *      -----------------
     *      '(open to below)'
     *      '---------------'
     *      ' ~~         ~~ '
     *      ' ~~         ~~ '
     *      -----------------
     */
    @Composable
    fun BottomHalfTwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom)
        ) {
                TwoColumn(leftItems, rightItems)
        }
    }

    /**
     *      -----------------
     *      ' ~~         ~~ '
     *      ' ~~         ~~ '
     *      -----------------
     *      '(open to below)'
     *      -----------------
     */
    @Composable
    fun TopHalfTwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top)
        ) {
            TwoColumn(leftItems, rightItems)
        }
    }

    private enum class TwoColumnListSource { LEFT, RIGHT }

    private data class SnapshotPair<A, B>(
        val first : A,
        val second : B
    ) : StateObject {

        val backingList = mutableStateListOf(first, second)

        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }

    @Composable
    fun OneColumn(
        items : List<MenuItem>,
        alignment: Alignment.Horizontal
    ) {

        val columnContents : @Composable ColumnScope.() -> Unit = {

                val colItems = items.reduceUpdateOnClick { it() ; println("items") }.let {
                    DaggerApplicationComponent.create().knobListenerService().listenForKnob(
                        listData = items,

                        onItemClickAdapter = { it.onClicked() },
                        onSelectAdapter = { item, isNowSelected ->
                            when (item) {
                                is TextMenuItem -> item.copy(isSelected = isNowSelected)
                                is CheckBoxMenuItem -> item.copy(isSelected = isNowSelected)
                                is ImageMenuItem -> item.copy(isSelected = isNowSelected)
                                else -> error("Unsupported type")
                            }
                        },
                        isSelectableAdapter = { it.isSelectable }
                    ).value
                }
                for (item in colItems) {
                    item.toView(
                        chipOrientation = if (alignment == Alignment.Start) ItemChipOrientation.W else ItemChipOrientation.E,
                    )()
                }
            }


        Box (
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    if (alignment == Alignment.Start) {
                        columnContents()
                    } else {
                        MenuItem.SPACER
                    }
                }
                Column(Modifier.weight(0.5f, true)) {
                    if (alignment == Alignment.End) {
                        columnContents()
                    } else {
                        MenuItem.SPACER
                    }
                }
            }
        }
    }

    @Composable
    internal fun TwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        //We combine each list into a super-list
        //that behaves as one, so that scrolling
        //spans both columns

        val selectionOrderConjoinedList = mutableStateListOf(*(listOf(SnapshotPair(leftItems[0], TwoColumnListSource.LEFT)) +
                rightItems.map { SnapshotPair(it, TwoColumnListSource.RIGHT) } +
                leftItems.drop(1).asReversed()
                    .map { SnapshotPair(it, TwoColumnListSource.LEFT) }).toTypedArray())

        println("   Conjoined list: ${selectionOrderConjoinedList.map { 
            ((it.first as? TextMenuItem)?.title ?: (it.first as? CheckBoxMenuItem)?.title) to it.second 
        }}")

        val knobListenerService = DaggerApplicationComponent.create().knobListenerService()

        val conjoinedList = knobListenerService.listenForKnob(
            listData = selectionOrderConjoinedList,

            onItemClickAdapter = {
                it.first.onClicked()
            },
            onSelectAdapter = { item, isNowSelected ->
                SnapshotPair(item.first.copyAndSetIsSelected(isNowSelected), item.second)
            },
            isSelectableAdapter = {
                it.first.isSelectable
            }
        )

        println("ST Conjoined list: ${
            conjoinedList.value.map {
                val title = ((it.first as? TextMenuItem)?.title ?: (it.first as? CheckBoxMenuItem)?.title)
                val source = it.second
                "title : ${title} ; isSelected : ${it.first.isSelected}" 
            }
        }")


        val updatableLeft = remember(conjoinedList.value) { derivedStateOf {
            conjoinedList.value
                .filter { it.second == TwoColumnListSource.LEFT }
                .map { it.first }
                .map { Pair(it, leftItems.indexOf(it)) }
                .sortedBy { it.second }
                .map { it.first }
                .toList()
        } }
        val updatableRight = remember(conjoinedList.value) { derivedStateOf {
            conjoinedList.value
                .filter { it.second == TwoColumnListSource.RIGHT }
                .map { it.first }
                .map { Pair(it, rightItems.indexOf(it)) }
                .sortedBy { it.second }
                .map { it.first }
                .toList()
        } }

        Box (
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    if (leftItems.isNotEmpty()) {
                        conjoinedList.value
                            .filter { it.second == TwoColumnListSource.LEFT }
                            .map { it.first }
                            .map { Pair(it, leftItems.indexOf(it)) }
                            .sortedBy { it.second }
                            .map { it.first }
                            .toList()
                            .forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NW
                                        updatableLeft.value.lastIndex -> ItemChipOrientation.SW
                                        else -> ItemChipOrientation.W
                                    }
                                }
                            )()
                        }
                    }
                }
                Column(Modifier.weight(0.5f, true)) {
                    if (rightItems.isNotEmpty()) {
                        updatableRight.value.forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NE
                                        updatableRight.value.lastIndex -> ItemChipOrientation.SE
                                        else -> ItemChipOrientation.E
                                    }
                                }
                            )()
                        }
                    }
                }
            }
        }
    }
}