package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.reduceUpdateOnClick
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private open class SnapshotTriple<A, B, C>(
        val first : A,
        val second : B,
        val third : C
    ) : StateObject {
        val backingList = mutableStateListOf(first, second, third)
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }

        operator fun component1() : A = first
        operator fun component2() : B = second
        operator fun component3() : C = third
        override fun toString() =
            "SnapshotTriple(first = $first, second = $second, third = $third)"
    }

    private class ConjoinedListRecord<I, P>(
        val item : I,
        val sourcePlacementEnum : P, //LEFT or RIGHT, or a quadrant
        val originalItemPosition : Int //Original index in placement
    ) : SnapshotTriple<I, P, Int>(item, sourcePlacementEnum, originalItemPosition)

    @Composable
    fun OneColumn(
        items : List<MenuItem>,
        alignment: Alignment.Horizontal
    ) {

        val columnContents : @Composable ColumnScope.() -> Unit = {

                val colItems = items.reduceUpdateOnClick { it() ; println("items") }.let {
                    MenuWindow.MenuWindowKnobListener.current.listenForKnob(
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


        val circularList = mutableListOf<ConjoinedListRecord<MenuItem, TwoColumnListSource>>()

        if (leftItems.isNotEmpty()) {
            circularList.add(ConjoinedListRecord(leftItems[0], TwoColumnListSource.LEFT, 0))
        }

        circularList.addAll(
            rightItems.mapIndexed { index, menuItem -> ConjoinedListRecord(menuItem,TwoColumnListSource.RIGHT,index) }
        )

        circularList.addAll(
            leftItems.drop(1).reversed()
                .map { ConjoinedListRecord(it, TwoColumnListSource.LEFT, leftItems.indexOf(it)) }
        )


        val selectionOrderConjoinedList = remember { circularList }


        val knobListenerService = MenuWindow.MenuWindowKnobListener.current

        val currentlySelectedItem = remember { mutableStateOf<MenuItem?>(null) }

        val conjoinedList = knobListenerService.listenForKnob(
            listData = selectionOrderConjoinedList,

            onItemClickAdapter = {
                it.item.onClicked()
            },
            onSelectAdapter = { item, isNowSelected ->
                currentlySelectedItem.value = item.item
                println("WAT Currently selected: $item, $isNowSelected")
                ConjoinedListRecord(item.first.copyAndSetIsSelected(isNowSelected), item.second, item.third)
            },
            isSelectableAdapter = {
                it.item.isSelectable
            }
        )

//        println("ST Conjoined list: ${
//            conjoinedList.value.map {
//                val title = ((it.first as? TextMenuItem)?.title ?: (it.first as? CheckBoxMenuItem)?.title)
//                val source = it.second
//                val originalPosition = it.third
//                "title : ${title} ; isSelected : ${it.first.isSelected}"
//            }
//        }")


        Box (
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    if (leftItems.isNotEmpty()) {
                        key(currentlySelectedItem) {
                            conjoinedList.value
                                .filter { it.sourcePlacementEnum == TwoColumnListSource.LEFT }
                                .sortedBy { it.originalItemPosition }
                                .map { it.item }
                                .forEachIndexed { index, menuItem ->
                                    menuItem.toView(
                                        chipOrientation = if (!menuItem.isSelectable) {
                                            ItemChipOrientation.NONE
                                        } else {
                                            when (index) {
                                                0 -> ItemChipOrientation.NW
                                                leftItems.lastIndex -> ItemChipOrientation.SW
                                                else -> ItemChipOrientation.W
                                            }
                                        }
                                    )()
                                }
                        }
                    }
                }
                Column(Modifier.weight(0.5f, true)) {
                    if (rightItems.isNotEmpty()) {
                        key(currentlySelectedItem) {
                            conjoinedList.value
                                .filter { it.sourcePlacementEnum == TwoColumnListSource.RIGHT }
                                .sortedBy { it.originalItemPosition }
                                .map { it.item }
                                .forEachIndexed { index, menuItem ->
                                    menuItem.toView(
                                        chipOrientation = if (!menuItem.isSelectable) {
                                            ItemChipOrientation.NONE
                                        } else {
                                            when (index) {
                                                0 -> ItemChipOrientation.NE
                                                rightItems.lastIndex -> ItemChipOrientation.SE
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
}