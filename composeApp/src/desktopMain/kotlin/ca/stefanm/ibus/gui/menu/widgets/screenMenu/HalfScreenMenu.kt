package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.reduceUpdateOnClick


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



    @Composable
    fun OneColumn(
        items : List<MenuItem>,
        alignment: Alignment.Horizontal = Alignment.Start,
        fullWidth : Boolean = false
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
                        chipOrientation = if (!item.isSelectable) {
                            ItemChipOrientation.NONE
                        } else {
                            if (alignment == Alignment.Start) ItemChipOrientation.W else ItemChipOrientation.E
                        },
                    )()
                }
            }


        Box (
            Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = if (alignment == Alignment.Start) Arrangement.Start else Arrangement.End
            ) {
                if (fullWidth) {
                    Column(Modifier.weight(1F, true)) { columnContents() }
                } else {
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
    }

    @Composable
    internal fun TwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        //We combine each list into a super-list
        //that behaves as one, so that scrolling
        //spans both columns

        val selectedItem = remember { mutableStateOf<ConjoinedListRecord<MenuItem, TwoColumnListSource>?>(null) }

        val circularList = mutableListOf<ConjoinedListRecord<MenuItem, TwoColumnListSource>>()

        if (leftItems.isNotEmpty()) {
            circularList.add(ConjoinedListRecord(leftItems[0], TwoColumnListSource.LEFT, 0))
        }

        circularList.addAll(
            rightItems.mapIndexed { index, menuItem ->
                ConjoinedListRecord(
                    menuItem,
                    TwoColumnListSource.RIGHT,
                    index
                )
            }
        )

        circularList.addAll(
            leftItems.drop(1).reversed()
                .map { ConjoinedListRecord(it, TwoColumnListSource.LEFT, leftItems.indexOf(it)) }
        )
        //We want this list to stay between compositions even though we mutate it.
        //If the remember isn't here, we infinitely recompose and I don't know why.
        val selectionOrderConjoinedList = remember (MenuWindow.MenuWindowKnobListener.current) { mutableStateOf(circularList) }

        val conjoinedList = MenuWindow.MenuWindowKnobListener.current.listenForKnob(
            listData = selectionOrderConjoinedList.value,

            onItemClickAdapter = {
                it.item.onClicked()
            },
            onSelectAdapter = { item, isNowSelected ->
                if (isNowSelected) {
                    selectedItem.value = item
                }
                println("WAT Currently selected: $item, $isNowSelected")
                ConjoinedListRecord(item.first.copyAndSetIsSelected(isNowSelected), item.second, item.third)
            },
            isSelectableAdapter = {
                it.item.isSelectable
            }
        ).value

        Box(
            Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    conjoinedList
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
                Column(Modifier.weight(0.5f, true)) {
                    conjoinedList
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

    data class GridMenuConjoinedLIst(
        val row : Int,
        val positionInRow : Int,
        val originalItem : MenuItem,
        val calculatedChipOrientation: ItemChipOrientation
    )

    @Composable
    fun GridMenu(
        items : List<List<MenuItem>>, //LTR, TTL (Left to right, top to bottom)
    ) {

       val conjoinedItems = items.mapIndexed { rowIndexInGrid, rowList ->
            rowList.mapIndexed { itemIndexInRow, menuItem ->

                val isFirstRow = rowIndexInGrid == 0
                val isLastRow = rowIndexInGrid == items.lastIndex

                val isFirstItemInRow = itemIndexInRow == 0
                val isLastItemInRow = itemIndexInRow == rowList.lastIndex

                val chipOrientation = when {
                    isFirstRow && isFirstItemInRow -> ItemChipOrientation.NW
                    isFirstRow && !isFirstItemInRow && !isLastItemInRow -> ItemChipOrientation.N
                    isFirstRow && isLastItemInRow -> ItemChipOrientation.NE

                    !isFirstRow && !isLastRow && isFirstItemInRow -> ItemChipOrientation.W
                    !isFirstRow && !isLastRow && isLastItemInRow -> ItemChipOrientation.E

                    isLastRow && isFirstItemInRow -> ItemChipOrientation.SW
                    isLastRow && !isFirstItemInRow &&!isLastItemInRow -> ItemChipOrientation.S
                    isLastRow && isLastItemInRow -> ItemChipOrientation.SE

                    else -> ItemChipOrientation.NONE
                }

                GridMenuConjoinedLIst(
                    row = rowIndexInGrid,
                    positionInRow = itemIndexInRow,
                    originalItem = menuItem,
                    calculatedChipOrientation = chipOrientation
                )
            }
       }.flatten()

        val observableConjoinedItems = MenuWindow.MenuWindowKnobListener.current.listenForKnob(
            listData = conjoinedItems,
            onItemClickAdapter = {item -> item.originalItem.onClicked() },
            onSelectAdapter = { item, isNowSelected ->  item.copy(originalItem = item.originalItem.copyAndSetIsSelected(isNowSelected)) },
            isSelectableAdapter = {item -> item.originalItem.isSelectable }
        ).value

        Column {
            observableConjoinedItems.groupBy { it.row }.forEach {
                Row(Modifier.wrapContentWidth(),horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    it.value.sortedBy { it.positionInRow }.forEach {
                        it.originalItem.toView(Modifier.wrapContentSize(), it.calculatedChipOrientation)()
                    }
                }
            }
        }


    }

}