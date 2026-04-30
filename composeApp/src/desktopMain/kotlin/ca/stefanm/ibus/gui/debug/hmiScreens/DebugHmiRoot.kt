package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.*
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
@Stable
@AutoDiscover
class DebugHmiRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(
            modifier = Modifier.background(
                ThemeWrapper.ThemeHandle.current.colors.menuBackground
            )
        ) {
            BmwSingleLineHeader("Debug HmiRoot")

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = null,
                logger = logger,
                prependGoBackEntry = true,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {navigationNodeTraverser.goBack()}
                    ),
                    TextMenuItem(
                        title = "DebugHmiMenuTests",
                        onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiMenuTest::class.java) }
                    ),
                    TextMenuItem(
                        title = "Smooth Scroll test",
                        onClicked = { navigationNodeTraverser.navigateToNode(SmoothScrollTest::class.java)}
                    ),
                    TextMenuItem(
                        title = "Smooth Grid Scroll test",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(SmoothGridScrollTest::class.java)
                        }
                    ),
                ).toDynamicLambdas()
            )
        }

    }
}