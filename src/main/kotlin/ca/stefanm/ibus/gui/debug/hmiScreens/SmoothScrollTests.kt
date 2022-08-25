package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class SmoothScrollTest @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SmoothScrollTest::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

//        val items = listOf(
//            TextMenuItem("Go Back", onClicked = {navigationNodeTraverser.goBack() })
//        ) + (1..50).map {
//            TextMenuItem(
//                title = "Item $it",
//                onClicked = {}
//            )
//        }
//
//        ScrollMenu.SmoothOneColumnScroll(items)

    }
}