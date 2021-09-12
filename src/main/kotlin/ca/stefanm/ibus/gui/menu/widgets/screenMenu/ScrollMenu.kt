package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable

object ScrollMenu {

    interface OneColumnScrollListScope {
        val items : List<MenuItem>
    }

    @Composable
    fun OneColumnScroll(
        items : List<MenuItem>,
        fullWidth : Boolean = false,
        displayOptions: ScrollListOptions,
        onScrollListExitSelected : () -> Unit,
        onScrollListItemSelected : (item : MenuItem) -> Unit = { it.onClicked() },
        presentation : (@Composable OneColumnScrollListScope.() -> Unit)? = null
    ) {
        ConjoinedScrollList(
            sourceList = items.mapIndexed { index, menuItem ->
                ConjoinedListRecord(menuItem, 0, index)
            },
            displayOptions = displayOptions,
            onScrollListExitSelected = onScrollListExitSelected,
            onScrollListItemSelected = onScrollListItemSelected
        ) ConjoinedScope@ {
            if (presentation == null) {
                HalfScreenMenu.OneColumn(
                    items = this.visibleListSlice.map { it.item },
                    fullWidth = fullWidth
                )
            } else {
                presentation.invoke(object : OneColumnScrollListScope {
                    override val items: List<MenuItem>
                        get() = this@ConjoinedScope.visibleListSlice.map { it.item }

                })
            }
        }
    }

    @Composable
    fun QuadrantColumnScrollFromSingleList(
        items : List<MenuItem>,
        fullWidth : Boolean = false,
        displayOptions: ScrollListOptions,
        maxLines : Int = 7,
        onScrollListExitSelected : () -> Unit,
        onScrollListItemSelected : (item : MenuItem) -> Unit = { it.onClicked() },
        presentation : (@Composable OneColumnScrollListScope.() -> Unit)? = null
    ) {
        //Go back items at top left.
        //Next page at bottom right
        //Keep track of how many lines.

        //TODO we could use the quadrant list.
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
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>,
        fillFrom : Arrangement.Vertical = Arrangement.Top,
        fillScreen : Boolean = false,
        displayOptions: ScrollListOptions,
        onScrollListExitSelected : () -> Unit,
        onScrollListItemSelected : (item : MenuItem) -> Unit = { it.onClicked() },
        presentation : (@Composable TwoColumnScrollListScope.() -> Unit)? = null
    ) {

        val LEFT = 0
        val RIGHT = 1

        val conjoinedList = (leftItems.mapIndexed { index, menuItem -> ConjoinedListRecord(menuItem, LEFT, index) }) +
                (rightItems.mapIndexed { index, menuItem -> ConjoinedListRecord(menuItem, RIGHT, index) })


        ConjoinedScrollList(
            sourceList = conjoinedList,
            displayOptions = displayOptions,
            onScrollListExitSelected = onScrollListExitSelected,
            onScrollListItemSelected = onScrollListItemSelected
        ) presentationScope@ {

            val left = this.visibleListSlice
                .filter { it.sourcePlacementEnum == LEFT }
                .sortedBy { it.originalItemPosition }
                .map { it.item }

            val right = this.visibleListSlice
                .filter { it.sourcePlacementEnum == RIGHT }
                .sortedBy { it.originalItemPosition }
                .map { it.item }

            if (presentation != null) {
                presentation.invoke(object : TwoColumnScrollListScope {
                    override val leftItems = left
                    override val rightItems = rightItems
                })
            } else {
                if (fillScreen) {
                    FullScreenMenu.TwoColumnFillFromTop(
                        leftItems = left,
                        rightItems = right
                    )
                } else {
                    if (fillFrom == Arrangement.Top) {
                        HalfScreenMenu.TopHalfTwoColumn(
                            leftItems = left,
                            rightItems = right
                        )
                    } else {
                        HalfScreenMenu.BottomHalfTwoColumn(
                            leftItems = left,
                            rightItems = right
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun FullScreenQuadrantScroll(
        nw : List<MenuItem>,
        ne : List<MenuItem>,
        sw : List<MenuItem>,
        se : List<MenuItem>,
        maxLines: Int = 7,
        onScrollListExitSelected : () -> Unit,
        onScrollListItemSelected : (item : MenuItem) -> Unit = { it.onClicked() },
        displayOptions: ScrollListOptions
    ) {

        //TODO be careful about the ItemsPerPage in ScrollListOptions here

        TODO()

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
        val previousPageItemLabel : String? = "Prev Page",

        /** Set to null so that there isn't a next page list item
         * and so that scrolling past the item sets the next page
         */
        val nextPageItemLabel : String? = "Next Page",

        /** If true, put a selectable item on every page
         * that exits the scroll list.
         */
        val isExitItemOnEveryPage : Boolean = false,
        /** If true, put a non-selectable item on each page
         * showing how many pages (current/total) the scroll
         * list has.
         */
        val isPageCountItemVisible : Boolean = false,
    )

    internal interface ConjoinedColumnScrollListScope<P> {
        val visibleListSlice : List<ConjoinedListRecord<MenuItem, P>>
    }

    /** This is a scroll-list with all the items already split-up and
     *  re-conjoined.
     */
    @Composable
    internal fun <P> ConjoinedScrollList(

        sourceList : List<ConjoinedListRecord<MenuItem, P>>,

        displayOptions : ScrollListOptions,

        /** Called when the user wishes to exit the scroll list */
        onScrollListExitSelected : () -> Unit,

        onScrollListItemSelected : (item : MenuItem) -> Unit = { it.onClicked() },

        presentation : @Composable ConjoinedColumnScrollListScope<P>.() -> Unit
    ) {

        //We need to split up the source list, and then add in the pieces we want.

        //We also need to keep track of the state for which page we're on,
        //and to mutate that state when our items



    }
}