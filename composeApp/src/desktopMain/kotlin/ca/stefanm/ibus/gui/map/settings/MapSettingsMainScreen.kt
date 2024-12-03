package ca.stefanm.ca.stefanm.ibus.gui.map.settings

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
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class MapSettingsMainScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapSettingsMainScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Map Settings")

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = {
                        navigationNodeTraverser.goBack()
                    }
                ),
                TextMenuItem(
                    title = "Download Map Tiles",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            MapTileDownloaderScreen::class.java
                        )
                    }
                ),
                TextMenuItem(
                    title = "Set Default Center",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            MapCenterSettingsScreen::class.java
                        )
                    }
                ),
            ))
        }
    }
}