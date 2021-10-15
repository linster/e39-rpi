package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject


@AutoDiscover
class HmiLogViewerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = HmiLogViewerScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {
            HalfScreenMenu.OneColumn(items = listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                )
            ))


        }
    }
}