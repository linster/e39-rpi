package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.annotation.FloatRange
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlin.math.abs



object SmoothScroll {
//https://medium.com/@david.debre/my-experience-with-subcomposelayout-71406b079305
//https://medium.com/@olivervicente/subcomposition-in-jetpack-compose-how-to-use-measurement-phase-data-in-other-children-3965d700af8b
    @Composable
    fun SmoothScroll(
        modifier: Modifier,
        knobListenerService: KnobListenerService,
        tag : String? = null,
        logger: Logger,
        prependGoBackEntry : Boolean = false,
        navigationNodeTraverser: NavigationNodeTraverser? = null,
        items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>
    ) {

        val knobState = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerService,
            logger,
             "${(tag ?: "")} SmoothScroll"
        )

        val scrollState = rememberScrollState(0)
        val childIndexToPixelsFromTop = mutableMapOf<Int, Int>()

        val childIndexToPage = mutableMapOf<Int, Int>()
        var previousPage = 0

        BoxWithConstraints() {
            val viewPortHeight = maxHeight
            SubcomposeLayout(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .verticalScroll(
                        state = scrollState,
                        enabled = true
                    )
                    .then(modifier)
            ) { constraints ->
    //https://medium.com/@olivervicente/subcomposition-in-jetpack-compose-how-to-use-measurement-phase-data-in-other-children-3965d700af8b
                val measurables = items.let {
                    if (prependGoBackEntry) {
                        listOf<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>(
                            { allocatedIndex, currentIndex ->
                                MenuItem(
                                    label = "Go Back",
                                    chipOrientation = ItemChipOrientation.W,
                                    isSelected = allocatedIndex == currentIndex,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        navigationNodeTraverser?.goBack()
                                    }
                                )
                            }
                        ) + it
                    } else {
                        it
                    }
                }.flatMapIndexed { index, item ->
                    subcompose(slotId = index) {
                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            item(allocatedIndex, currentIndex)
                            LaunchedEffect(allocatedIndex, currentIndex) {
                                if (allocatedIndex == currentIndex) {
                                    if (
                                        childIndexToPage[currentIndex] != previousPage /* We've moved a page */
                                        && childIndexToPixelsFromTop.containsKey(currentIndex)
                                    ) {
                                        childIndexToPixelsFromTop[currentIndex]?.let {
                                            //Scrolls to the item that happens to be on the next page
                                            //scrollState.scrollTo(it)
                                            //What we actually want is to scroll to the page.
                                            scrollState.scrollTo(
                                                childIndexToPixelsFromTop[
                                                    childIndexToPage
                                                        .filterValues { it == childIndexToPage[currentIndex] ?: 0 }
                                                        .keys
                                                        .min()
                                                    ] ?: 0
                                            )
                                        }
                                        //Update the page we were on before
                                        previousPage = childIndexToPage[currentIndex] ?: -1
                                    }
                                }
                            }
                        }
                    }
                }
                val placeables = measurables.map {
                    it.measure(constraints)
                }

                val layoutWidth = placeables.maxOf { it.width }
                val layoutHeight = placeables.sumOf { it.height }

                layout(width = layoutWidth, height = layoutHeight) {
                    var y = 0
                    placeables.forEachIndexed { index, item ->
                        item.placeRelative(x = 0, y = y)
                        childIndexToPixelsFromTop[index] = y
                        childIndexToPage[index] = if (y == 0 || viewPortHeight.value.toInt() == 0) {
                            0
                        } else {
                            var intermediatePage = y.floorDiv(viewPortHeight.value.toInt())

                            // If < 25% of the item is visible, put it on the next page
                            if (abs((y).rem(viewPortHeight.value) - viewPortHeight.value) < (item.height *0.75)) {
                                intermediatePage += 1
                            }

                            intermediatePage
                        }
                        y += item.height
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopEnd)
                ,
                adapter = ScrollbarAdapter(scrollState),
                style = ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 16.dp,
                    shape = RoundedCornerShape(8.dp),
                    hoverDurationMillis = 300,
                    unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                    hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                )
            )
        }
    }

    @Composable
    fun GridScroll(
        modifier: Modifier,
        //TODO make a version of this where you can pass in a knob state
        knobListenerService: KnobListenerService,
        tag : String? = null,
        logger: Logger,
        prependGoBackEntry : Boolean = false,
        goBackEntryProvider : @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit = @Composable { _, _ -> },
        navigationNodeTraverser: NavigationNodeTraverser? = null,

        /** How tall should each row be as a fraction of viewport height?
         * Because the screen is small, we're not going to be fancy with having a responsive layout.
         * (For example, a 2-wide grid centered in the middle of the layout)
         * For example, 0.85 means that each row should be as high as 85% of the viewport height.
         * For having multiple
         */
        @FloatRange(0.0, 1.0)
        rowHeightFraction : Float,

        /** What's the aspect ratio each item should try to achieve?
         * 1.0F means square.
         */
        desiredItemAspectRatio : Float,

        items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>
    ) {

        val knobState = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerService,
            logger,
            "${(tag ?: "")} SmoothGrid Scroll"
        )

        GridScroll(
            modifier = modifier,
            knobState = knobState,
            tag = tag,
            logger = logger,
            prependGoBackEntry = prependGoBackEntry,
            goBackEntryProvider = goBackEntryProvider,
            navigationNodeTraverser = navigationNodeTraverser,
            rowHeightFraction = rowHeightFraction,
            desiredItemAspectRatio = desiredItemAspectRatio,
            items = items
        )
    }


    @Composable
    fun GridScroll(
        modifier: Modifier,
        knobState: KnobObserverBuilderState,
        tag : String? = null,
        logger: Logger,
        prependGoBackEntry : Boolean = false,
        goBackEntryProvider : @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit = @Composable { _, _ -> },
        navigationNodeTraverser: NavigationNodeTraverser? = null,

        /** How tall should each row be as a fraction of viewport height?
         * Because the screen is small, we're not going to be fancy with having a responsive layout.
         * (For example, a 2-wide grid centered in the middle of the layout)
         * For example, 0.85 means that each row should be as high as 85% of the viewport height.
         * For having multiple
         */
        @FloatRange(0.0, 1.0)
        rowHeightFraction : Float,

        /** What's the aspect ratio each item should try to achieve?
         * 1.0F means square.
         */
        desiredItemAspectRatio : Float,

        items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>
    ) {

        if (items.isEmpty()) {
            return
        }



        val scrollState = rememberScrollState(0)

        val childIndexToPixelsFromTop = mutableMapOf<Int, Int>()

        val childIndexToPage = mutableMapOf<Int, Int>()
        var previousPage = 0

        BoxWithConstraints {
            val viewPortHeight = maxHeight
            val viewPortWidth = maxWidth

            SubcomposeLayout(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .verticalScroll(
                        state = scrollState,
                        enabled = true
                    )
                    .then(modifier)
            ) { constraints ->

                val measurables = items.let {
                    if (prependGoBackEntry) {
                        listOf<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>(
                            { allocatedIndex, currentIndex ->
                                goBackEntryProvider(allocatedIndex, currentIndex)
                            }
                        ) + it
                    } else {
                        it
                    }
                }.flatMapIndexed { index, item ->
                    subcompose(slotId = index) {
                        //TODO maybe we want to put the height and aspect ratios as a modifier on a Box
                        //TODO surrounding this?
                        Box(
                            Modifier
                                .height((viewPortHeight.value * rowHeightFraction).dp)
                                .aspectRatio(desiredItemAspectRatio, true)
                        ) {
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                item(allocatedIndex, currentIndex)
                                LaunchedEffect(allocatedIndex, currentIndex) {
                                    if (allocatedIndex == currentIndex) {
                                        if (
                                            childIndexToPage[currentIndex] != previousPage /* We've moved a page */
                                            && childIndexToPixelsFromTop.containsKey(currentIndex)
                                        ) {
                                            childIndexToPixelsFromTop[currentIndex]?.let {
                                                //Scrolls to the item that happens to be on the next page
                                                //scrollState.scrollTo(it)
                                                //What we actually want is to scroll to the page.
                                                scrollState.scrollTo(
                                                    childIndexToPixelsFromTop[
                                                        childIndexToPage
                                                            .filterValues { it == childIndexToPage[currentIndex] ?: 0 }
                                                            .keys
                                                            .min()
                                                    ] ?: 0
                                                )
                                            }
                                            //Update the page we were on before
                                            previousPage = childIndexToPage[currentIndex] ?: -1
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //Now I think we can measure each measurable.
                //I placed them in a Box so that the constraints can be applied to their size.
                val placeables = measurables.map {
                    it.measure(constraints)
                }

                val placeablesByRow : MutableList<MutableList<Placeable>> = mutableListOf()
                placeablesByRow.add(mutableListOf())
                var rowIndex = 0

                val placablesCopy = placeables.toMutableList()
                while (placablesCopy.isNotEmpty()) {
                    val candidate = placablesCopy.removeFirst()

                    if ((placeablesByRow[rowIndex].sumOf { it.width } + candidate.width).dp <= viewPortWidth) {
                        //Place the candidate in this row
                    } else {
                        //Place the candidate in the next row
                        rowIndex += 1
                    }

                    if (placeablesByRow.indices.last < rowIndex) {
                        placeablesByRow.add(mutableListOf())
                    }
                    placeablesByRow[rowIndex].add(candidate)
                }

                val layoutHeight = placeablesByRow.sumOf { row ->
                    row.maxOf { sq -> sq.height }
                }

                layout(width = viewPortWidth.roundToPx(), height = layoutHeight) {

                    var x = 0
                    var y = 0

                    var slotId = 0
                    placeablesByRow.forEachIndexed { rowIndex, row ->
                        var rowHeight = 0
                        row.forEachIndexed { itemIndex, item ->
                            item.placeRelative(x = x, y = y)

                            childIndexToPixelsFromTop[slotId] = y
                            childIndexToPage[slotId] = if (y == 0) {
                                0
                            } else {
                                var intermediatePage = y.floorDiv(viewPortHeight.value.toInt())
                                if (abs((y).rem(viewPortHeight.value) - viewPortHeight.value) < (item.height *0.75)) {
                                    intermediatePage += 1
                                }
                                intermediatePage
                            }
                            x += item.width
                            rowHeight = maxOf(rowHeight, item.height)

                            slotId += 1
                        }
                        x = 0
                        y += rowHeight
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopEnd)
                ,
                adapter = ScrollbarAdapter(scrollState),
                style = ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 16.dp,
                    shape = RoundedCornerShape(8.dp),
                    hoverDurationMillis = 300,
                    unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                    hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                )
            )

        }
    }

}