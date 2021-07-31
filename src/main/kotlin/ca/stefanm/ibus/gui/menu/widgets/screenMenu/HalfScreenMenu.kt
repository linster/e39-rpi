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
    internal fun TwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        //We combine each list into a super-list
        //that behaves as one, so that scrolling
        //spans both columns

        val selectionOrderConjoinedList = listOf(
            leftItems[0] to TwoColumnListSource.LEFT
        ) + rightItems.map { it to TwoColumnListSource.RIGHT
        } + leftItems.drop(1).reversed().map { it to TwoColumnListSource.LEFT }

        val knobListenerService = DaggerApplicationComponent.create().knobListenerService()

        val conjoinedList = knobListenerService.listenForKnob(
            listData = selectionOrderConjoinedList,

            onItemClickAdapter = {
                it.first.onClicked()
            },
            onSelectAdapter = { item, isNowSelected ->
                //We're going to do a side-effect on the onSelect.
                val updatedItem = if (item.second == TwoColumnListSource.LEFT) {
                    updatableLeft.set(updatableLeft.indexOf(item.first),
                        item.first.copyAndSetIsSelected(isNowSelected))
                } else {
                    updatableRight.set(updatableRight.indexOf(item.first),
                        item.first.copyAndSetIsSelected(isNowSelected))
                }

                Pair(updatedItem, item.second)
            },
            isSelectableAdapter = {
                it.first.isSelectable
            }
        )


        val updatableLeft = remember(conjoinedList.value) { derivedStateOf {
            conjoinedList.value
                .asSequence()
                .filter { it.second == TwoColumnListSource.LEFT }
                .map { it.first }
                .map { Pair(it, leftItems.indexOf(it)) }
                .sortedBy { it.second }
                .map { it.first }
                .toList()
        } }
        val updatableRight = mutableStateListOf(*rightItems.toTypedArray())


        Box (
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    if (updatableLeft.isNotEmpty()) {
                        updatableLeft.forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NW
                                        updatableLeft.lastIndex -> ItemChipOrientation.SW
                                        else -> ItemChipOrientation.W
                                    }
                                }
                            )()
                        }
                    }
                }
                Column(Modifier.weight(0.5f, true)) {
                    if (updatableRight.isNotEmpty()) {
                        updatableRight.forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NE
                                        updatableRight.lastIndex -> ItemChipOrientation.SE
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