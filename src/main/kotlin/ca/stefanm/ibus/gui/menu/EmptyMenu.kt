package ca.stefanm.ibus.gui.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.stefanm.ibus.gui.map.poi.CreateOrEditPoiScreen
import ca.stefanm.ibus.gui.map.poi.PoiManagerScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiKeyboard
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import javax.inject.Inject

//This is an empty menu that depends on a knob scroll listener so
//that the root menu that needs one works well.
@AutoDiscover
class EmptyMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = EmptyMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        FullScreenMenu.TwoColumnFillFromTop(
            leftItems = listOf(MenuItem.SPACER, MenuItem.SPACER),
            rightItems = listOf(MenuItem.SPACER, MenuItem.SPACER)
        )

        LaunchedEffect(true) {
            navigationNodeTraverser.navigateToNode(BMWMainMenu::class.java)
//            navigationNodeTraverser.navigateToNode(PoiManagerScreen::class.java)
        }
    }
}