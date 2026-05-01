package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class SmoothGridScrollTest @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SmoothGridScrollTest::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Smooth Grid Scroll Test")

            val items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> =
                (1 .. 50).map { listIndex ->
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier.fillMaxSize(),
                            label = "WAT $listIndex $allocatedIndex",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                logger.d("WAT", "Clicked allocatedIndex $allocatedIndex")
                                navigationNodeTraverser.goBack()
                            }
                        )
                    }
                }




            SmoothScroll.GridScroll(
                modifier = Modifier,
                knobListenerService,
                "Smooth Grid Scroll Test",
                logger = logger,
                items = items,
                prependGoBackEntry = false,
                navigationNodeTraverser = navigationNodeTraverser,
                rowHeightFraction = 0.4F,
                desiredItemAspectRatio = 1F
            )
        }

    }
}