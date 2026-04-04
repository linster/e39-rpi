package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.launch
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

        val childIndexOnPage = mutableMapOf<Int, Int>()
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
                                        childIndexOnPage[currentIndex] != previousPage /* We've moved a page */
                                        && childIndexToPixelsFromTop.containsKey(currentIndex)
                                    ) {
                                        childIndexToPixelsFromTop[currentIndex]?.let {
                                            //Scrolls to the item that happens to be on the next page
                                            //scrollState.scrollTo(it)
                                            //What we actually want is to scroll to the page.
                                            scrollState.scrollTo(
                                                childIndexToPixelsFromTop[
                                                    childIndexOnPage
                                                        .filterValues { it == childIndexOnPage[currentIndex] ?: 0 }
                                                        .keys
                                                        .min()
                                                    ] ?: 0
                                            )
                                        }
                                        //Update the page we were on before
                                        previousPage = childIndexOnPage[currentIndex] ?: -1
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
                        childIndexOnPage[index] = if (y == 0) {
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
}