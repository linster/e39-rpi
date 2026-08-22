package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.settings.MapCenterSettingsScreen
import ca.stefanm.ibus.gui.map.settings.MapTileDownloaderScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class MapSettingsMainScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "MapSettingsMainScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapSettingsMainScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val context = SmoothScroll.MenuWindowSmoothScrollContext()

        with(context) {
            OneColumnSmoothScreen(
                header = "Map Settings",
                prependGoBackEntry = true,
                items = listOf(
                    "Download Map Tiles" to { navigateToNode(MapTileDownloaderScreen::class.java) },
                    "Set Default Center" to { navigateToNode(MapCenterSettingsScreen::class.java) }
                )
            )
        }
    }
}