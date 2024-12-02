package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.reduceUpdateOnClick
import com.ginsberg.cirkle.circular
import java.awt.Menu
import kotlin.math.ceil
import kotlin.math.max

object ScrollMenu {

    interface OneColumnScrollListScope {
        val items : List<MenuItem>
    }

    @Composable
    fun OneColumnScroll(
        items : List<MenuItem>,
        displayOptions: ScrollListOptions,
        onScrollListExitSelected : () -> Unit,
        presentation : (@Composable OneColumnScrollListScope.() -> Unit)? = null
    ) {
        ConjoinedScrollList(
            sourceList = items.mapIndexed { index, menuItem ->
                ConjoinedListRecord(menuItem, 0, index)
            },
            displayOptions = displayOptions,
            conjoinedItemSpecialToken = -1,
            onScrollListExitSelected = onScrollListExitSelected,
        ) { visibleListSlice ->
            if (presentation == null) { //The default is full-screen menus unless the client wants to do something weird.
                FullScreenMenu.OneColumn(
                    items = visibleListSlice.map { it.item }
                )
            } else {
                presentation.invoke(object : OneColumnScrollListScope {
                    override val items: List<MenuItem>
                        get() = visibleListSlice.map { it.item }
                })
            }
        }
    }

    interface TwoColumnScrollListScope {
        val leftItems : List<MenuItem>
        val rightItems : List<MenuItem>
    }

    /** This is a two-column scroll list.
     * It's up to the caller to decide on the presentation
     * (whether it's bottom-half, or top-half scroll)
     */
    @Composable
    fun TwoColumnScroll(
        items: List<MenuItem>,
        fillFrom : Arrangement.Vertical = Arrangement.Top,
        fillScreen : Boolean = false,
        maxLines : Int = 6,
        displayOptions: ScrollListOptions,
        onScrollListExitSelected : () -> Unit,
        presentation : (@Composable TwoColumnScrollListScope.() -> Unit)? = null
    ) {

        val SPECIAL = -1
        val SOURCE = 0

        ConjoinedScrollList(
            sourceList = items.mapIndexed { index, menuItem -> ConjoinedListRecord(menuItem, SOURCE, index) },
            displayOptions = displayOptions,
            conjoinedItemSpecialToken = SPECIAL,
            onScrollListExitSelected = onScrollListExitSelected
        ) { visibleListSlice ->

            val numberSpecial = visibleListSlice.count { it.sourcePlacementEnum == SPECIAL }
            val indicesLeft = 0..(maxLines - numberSpecial)

            val indicesRight = (indicesLeft.last)..(visibleListSlice.lastIndex)

            val left = visibleListSlice
                .slice(indicesLeft)

            val right = visibleListSlice
                .slice(indicesRight)

            if (presentation != null) {
                presentation.invoke(object : TwoColumnScrollListScope {
                    override val leftItems = left.map { it.item }
                    override val rightItems = right.map { it.item }
                })
            } else {
                if (fillScreen) {
                    FullScreenMenu.TwoColumnFillFromTop(
                        leftItems = left.map { it.item },
                        rightItems = right.map { it.item }
                    )
                } else {
                    if (fillFrom == Arrangement.Top) {
                        HalfScreenMenu.TopHalfTwoColumn(
                            leftItems = left.map { it.item },
                            rightItems = right.map { it.item }
                        )
                    } else {
                        HalfScreenMenu.BottomHalfTwoColumn(
                            leftItems = left.map { it.item },
                            rightItems = right.map { it.item }
                        )
                    }
                }
            }
        }
    }

    //An options object to configure how the
    //scrolllist will be displayed
    data class ScrollListOptions(
        val itemsPerPage : Int,

        /** This label is shown on the menu item that
         *  exits the list selection
         */
        val exitListItemLabel : String = "Exit Scroll",

        /** Set to null so that there isn't a previous page
         *  list item and so the scrolling past the first
         *  item in the page also changes the page
         */
        val previousPageItemLabel : String = "Prev Page",

        /** Set to null so that there isn't a next page list item
         * and so that scrolling past the item sets the next page
         */
        val nextPageItemLabel : String = "Next Page",

        /** If true, put a selectable item on every page
         * that exits the scroll list.
         */
        val isExitItemOnEveryPage : Boolean = false,
        /** If true, put a non-selectable item on each page
         * showing how many pages (current/total) the scroll
         * list has.
         */
        val isPageCountItemVisible : Boolean = false,

        val showSpacerRow : Boolean = true
    )



    /** This is a scroll-list with all the items already split-up and
     *  re-conjoined.
     */
    @Composable
    internal fun <P> ConjoinedScrollList(

        sourceList : List<ConjoinedListRecord<MenuItem, P>>,

        displayOptions : ScrollListOptions,

        /** Called when the user wishes to exit the scroll list */
        onScrollListExitSelected : () -> Unit,

        //Value to set the Prev/Next/Exit special items to so the presentation
        //knows to draw them differently
        conjoinedItemSpecialToken : P,

        //TODO make this a scope, and have current page, and current selected item index be on it.
        presentation : @Composable (visibleListSlice : List<ConjoinedListRecord<MenuItem, P>>) -> Unit
    ) {

        //We need to split up the source list, and then add in the pieces we want.

        //We also need to keep track of the state for which page we're on,
        //and to mutate that state when our items


        val sourceItemsPerSlice = displayOptions.itemsPerPage + 1 -
                if (displayOptions.isExitItemOnEveryPage) 1 else 0 -
                        if (displayOptions.isPageCountItemVisible) 1 else 0 -
                                1 - //prev item
                                1 - //next item
                                if (displayOptions.showSpacerRow) 1 else 0   //spacer item

        val totalPages = ceil(sourceList.count().toDouble() / sourceItemsPerSlice.toDouble()).toInt()
        val currentPage = remember { mutableStateOf(0) }

        val validPageIndices = (0 until totalPages).toList()
        val exitItem = TextMenuItem(
            title = displayOptions.exitListItemLabel,
            onClicked = onScrollListExitSelected
        )

        val nextPageItem = TextMenuItem(
            title = displayOptions.nextPageItemLabel,
            onClicked = {
                if (currentPage.value + 1 in validPageIndices) {
                    currentPage.value = currentPage.value + 1
                }
            }
        )

        val previousPageItem = TextMenuItem(
            title = displayOptions.previousPageItemLabel,
            onClicked = {
                if (currentPage.value - 1 in validPageIndices) {
                    currentPage.value = currentPage.value - 1
                }
            }
        )

        val pageIndicatorItem = TextMenuItem(
            title = "Page: ${currentPage.value + 1} / $totalPages",
            isSelectable = false,
            onClicked = {}
        )

        val spacerItem = TextMenuItem(
            title = "",
            isSelectable = false,
            onClicked = {}
        )

        val pagePreamble = mutableListOf<MenuItem>().apply {
            add(nextPageItem)
            add(previousPageItem)
            if (currentPage.value == 0) {
                add(exitItem)
            } else {
                if (displayOptions.isExitItemOnEveryPage) {
                    add(exitItem)
                }
            }


            add(pageIndicatorItem)
            if (displayOptions.showSpacerRow) {
                add(spacerItem)
            }
        }.toList().mapIndexed { index, menuItem -> ConjoinedListRecord(menuItem, conjoinedItemSpecialToken, index) }


        val sourceItemsByPage = sourceList
            .windowed(
                size = sourceItemsPerSlice,
                step = sourceItemsPerSlice,
                partialWindows = true
            ).map { pageItems ->
                listOf(*pagePreamble.toTypedArray(), *pageItems.toTypedArray())
            }

        presentation.invoke((sourceItemsByPage.getOrNull(currentPage.value) ?: pagePreamble))
    }

    @Composable
    fun SmoothOneColumnScroll(
        itemsPerPage: Int = 5,
        items : List<MenuItem>,
        alignment: Alignment.Horizontal = Alignment.Start,
        fullWidth : Boolean = false
    ) {

        //TODO have a variable here that holds the current selected item,
        //TODO then a derived state of the index of that item
        //TODO then auto-update the page.

//        val columnContents : @Composable ColumnScope.() -> Unit = {
//
//            val colItems = items.reduceUpdateOnClick { it() ; println("items") }.let {
//                MenuWindow.MenuWindowKnobListener.current.listenForKnob(
//                    listData = items,
//
//                    onItemClickAdapter = { it.onClicked() },
//                    onSelectAdapter = { item, isNowSelected ->
//
//                        if (isNowSelected) {
//
//                        }
//
//                        when (item) {
//                            is TextMenuItem -> item.copy(isSelected = isNowSelected)
//                            is CheckBoxMenuItem -> item.copy(isSelected = isNowSelected)
//                            is ImageMenuItem -> item.copy(isSelected = isNowSelected)
//                            else -> error("Unsupported type")
//                        }
//                    },
//                    isSelectableAdapter = { it.isSelectable }
//                ).value
//            }
//            for (item in colItems) {
//                item.toView(
//                    chipOrientation = if (!item.isSelectable) {
//                        ItemChipOrientation.NONE
//                    } else {
//                        if (alignment == Alignment.Start) ItemChipOrientation.W else ItemChipOrientation.E
//                    },
//                )()
//            }
//        }
//
//        val sourceList : List<ConjoinedListRecord<MenuItem, P>>,
//
//
//        val displayOptions = ScrollListOptions(
//            itemsPerPage = itemsPerPage,
//            isExitItemOnEveryPage = false,
//            isPageCountItemVisible = true,
//            showSpacerRow = false
//        )
//
//
//        val sourceItemsPerSlice = displayOptions.itemsPerPage + 1 -
//                if (displayOptions.isExitItemOnEveryPage) 1 else 0 -
//                        if (displayOptions.isPageCountItemVisible) 1 else 0 -
//                                1 - //prev item
//                                1 - //next item
//                                if (displayOptions.showSpacerRow) 1 else 0   //spacer item
//
//        val totalPages = ceil(sourceList.count().toDouble() / sourceItemsPerSlice.toDouble()).toInt()
//
//        val sourceItemsByPage = sourceList
//            .windowed(
//                size = sourceItemsPerSlice,
//                step = sourceItemsPerSlice,
//                partialWindows = true
//            ).map { pageItems ->
//                listOf(*pageItems.toTypedArray())
//            }
//
//        val scrollState = rememberScrollState(0)
//
//
//        //We want to listen to each item become selected, and produce a state of
//        //the current selected item.
//
//        val foo = produceState(items.firstOrNull { it.isSelected } ?: 0) {
//
//        }
//
//        Box(
//            Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//        ) {
//            //Make the scrollbar mostly transparent in the BMW style.
//            VerticalScrollbar(rememberScrollbarAdapter(scrollState))
//            FullScreenMenu.OneColumn(items)
//        }
    }
}