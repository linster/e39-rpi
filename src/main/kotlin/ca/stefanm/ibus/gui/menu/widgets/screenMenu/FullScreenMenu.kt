package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import java.awt.Menu
import kotlin.math.E

//These are all non-scrollable full-screen menu types.
object FullScreenMenu {

    @Composable
    fun OneColumn(
        items : List<MenuItem>
    ) {
        Box(modifier = Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            HalfScreenMenu.OneColumn(items, fullWidth = true)
        }
    }

    @Composable
    fun TwoColumnFillFromTop(
        leftItems : List<MenuItem>,
        rightItems : List<MenuItem>
    ) {
        Box(modifier = Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            HalfScreenMenu.TwoColumn(leftItems, rightItems)
        }
    }

    private enum class QuadrantListItemSource {
        NW, NE, SW, SE
    }

    @Composable
    fun TwoColumnFillFromCorners(
        nw : List<MenuItem>,
        ne : List<MenuItem>,
        sw : List<MenuItem>,
        se : List<MenuItem>
    ) {

        //We combine each list into a super-list
        //that behaves as one, so that scrolling
        //spans all quadrants

        val circularList = mutableListOf<ConjoinedListRecord<MenuItem, QuadrantListItemSource>>().apply {
            if (nw.isNotEmpty()) {
                add(ConjoinedListRecord(nw[0], QuadrantListItemSource.NW, 0))
            }

            addAll(ne.mapIndexed { index, menuItem ->
                ConjoinedListRecord(menuItem, QuadrantListItemSource.NE, index)
            })

            addAll(se.mapIndexed { index, menuItem ->
                ConjoinedListRecord(menuItem, QuadrantListItemSource.SE, index)
            })

            addAll(sw.reversed().map { menuItem ->
                ConjoinedListRecord(menuItem, QuadrantListItemSource.SW, sw.indexOf(menuItem))
            })

            addAll(nw.drop(1).reversed().map { menuItem ->
                ConjoinedListRecord(menuItem, QuadrantListItemSource.NW, nw.indexOf(menuItem))
            })
        }

        val selectedItem = remember { mutableStateOf<ConjoinedListRecord<MenuItem, QuadrantListItemSource>?>(null)}

        //We're following along in the TwoColumn

        val selectionOrderConjoinedList = remember(MenuWindow.MenuWindowKnobListener.current) { mutableStateOf(circularList) }

        val conjoinedList = MenuWindow.MenuWindowKnobListener.current.listenForKnob(
            listData = selectionOrderConjoinedList.value,
            onItemClickAdapter = { it.item.onClicked() },
            onSelectAdapter = { item, isNowSelected ->
                if (isNowSelected) {
                    selectedItem.value = item
                }
                ConjoinedListRecord(item.first.copyAndSetIsSelected(isNowSelected), item.second, item.third)
            },
            isSelectableAdapter = { it.item.isSelectable }
        ).value

        fun conjoinedListSliceForQuadrant(quadrantListItemSource: QuadrantListItemSource) : List<MenuItem> =
            conjoinedList
                .filter { it.sourcePlacementEnum == quadrantListItemSource }
                .sortedBy { it.originalItemPosition }
                .map { it.item }

        fun List<MenuItem>.toViews(quadrantListItemSource: QuadrantListItemSource) : @Composable () -> Unit = {
            forEachIndexed { index, menuItem -> menuItem.toView(
                chipOrientation = if (!menuItem.isSelectable) {
                    ItemChipOrientation.NONE
                } else {
                    when (quadrantListItemSource) {
                        QuadrantListItemSource.NW -> if (index == 0) ItemChipOrientation.NW else ItemChipOrientation.W
                        QuadrantListItemSource.NE -> if (index == 0) ItemChipOrientation.NE else ItemChipOrientation.E
                        QuadrantListItemSource.SW -> if (index == lastIndex) ItemChipOrientation.SW else ItemChipOrientation.W
                        QuadrantListItemSource.SE -> if (index == lastIndex) ItemChipOrientation.SE else ItemChipOrientation.E
                    }
                })()
            }
        }

        Box(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {

                Row(Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.TopCenter)) {
                    Column(Modifier.weight(0.5F, true)) {
                        conjoinedListSliceForQuadrant(QuadrantListItemSource.NW).toViews(QuadrantListItemSource.NW)()
                    }
                    Column(Modifier.weight(0.5F, true)) {
                        conjoinedListSliceForQuadrant(QuadrantListItemSource.NE).toViews(QuadrantListItemSource.NE)()
                    }
                }

                Row(Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(Modifier.weight(0.5F, true)) {
                        conjoinedListSliceForQuadrant(QuadrantListItemSource.SW).toViews(QuadrantListItemSource.SW)()
                    }
                    Column(Modifier.weight(0.5F, true)) {
                        conjoinedListSliceForQuadrant(QuadrantListItemSource.SE).toViews(QuadrantListItemSource.SE)()
                    }
                }

        }


    }
}