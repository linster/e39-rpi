package ca.stefanm.ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class SmoothScrollToolbarTest @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
) : NavigationNode<Nothing> {
    companion object {
        const val TAG = "SmoothScrollToolbarTest"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SmoothScrollToolbarTest::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

        val knobState = setupListener(
            knobListenerServiceMain,
            logger,
            "agendaScreen"
        )

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Smooth Scroll Toolbar Test")

            makeTopToolbar(
                knobState,
                10,
                onExitSelected = { navigationNodeTraverser.goBack() }
            )()

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobState = knobState,
                tag = TAG,
                logger = logger,
                items = (1..50).map { it.toScrollTestItem() }
            )

        }

    }

    fun makeTopToolbar(
        knobState : KnobObserverBuilderState,
        buttons : Int,
        onExitSelected : () -> Unit
    ) : @Composable () -> Unit = {
        //Make a toolbar with the right amount of buttons

        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
        ) {
            (0 .. buttons).forEach {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier.weight(1F, true),
                        label = "$it",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            logger.d(TAG, "Toolbar $it")
                        }
                    )
                }
            }
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(2F, true),
                    label = "Exit",
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onExitSelected()
                    }
                )
            }

        }
    }

    fun Int.toScrollTestItem(
    ) : @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit = @Composable { allocatedIndex, currentIndex ->
        MenuItem(
            label = "item:(${this@toScrollTestItem}) al: $allocatedIndex, cur: $currentIndex",
            chipOrientation = ItemChipOrientation.W,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                logger.d(TAG, "Click: $currentIndex")
            }
        )
    }
}