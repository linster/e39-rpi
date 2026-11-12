package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.MapConfig.TileSource
import ca.stefanm.ibus.gui.map.widget.tile.OSMTileServerInfo
import ca.stefanm.ibus.gui.map.widget.tile.TileServerImageCache
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import javax.inject.Inject

/**
 * Where the map's tiles come from: OpenStreetMap's tile server directly, or
 * the maps backend (e39-rpi-maps /tiles: its own cache, which works offline,
 * filling misses from OpenStreetMap). The backend's host is the one in Maps
 * Backend settings; its web port is E39Config.MapConfig.Backend.httpPort.
 */
class MapTileSourceScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage,
    private val tileServerInfo: OSMTileServerInfo,
    private val tileServerImageCache: TileServerImageCache,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapTileSourceScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val server = remember { mutableStateOf(tileServerInfo.current()) }
        val cacheMb = remember { mutableStateOf(tileServerImageCache.calculateCacheSize() / 1_000_000) }

        fun choose(source: TileSource) {
            configurationStorage.config[E39Config.MapConfig.tileSource] = source
            server.value = tileServerInfo.current()
            cacheMb.value = tileServerImageCache.calculateCacheSize() / 1_000_000
        }

        fun label(source: TileSource, text: String) =
            (if (server.value.source == source) "\u2713 " else "   ") + text

        Column {
            BmwSingleLineHeader("Map Tiles")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                listOf(
                    "From: " + server.value.urlTemplate.substringBefore("/{z}"),
                    "Cached here: ${cacheMb.value} MB",
                ).forEach { line ->
                    TextMenuItem(
                        title = line,
                        isSelectable = false,
                        onClicked = {}
                    ).toView(Modifier.fillMaxWidth(), ItemChipOrientation.NONE)()
                }

                Column(Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    HalfScreenMenu.OneColumn(
                        fullWidth = true,
                        items = listOf(
                            TextMenuItem(
                                title = "Go Back",
                                onClicked = { navigationNodeTraverser.goBack() }
                            ),
                            TextMenuItem(
                                title = label(TileSource.OPENSTREETMAP, "OpenStreetMap"),
                                onClicked = { choose(TileSource.OPENSTREETMAP) }
                            ),
                            TextMenuItem(
                                title = label(TileSource.MAPS_BACKEND, "Maps backend (works offline)"),
                                onClicked = { choose(TileSource.MAPS_BACKEND) }
                            ),
                            TextMenuItem(
                                title = "Clear tile cache",
                                onClicked = {
                                    tileServerImageCache.clearCache()
                                    cacheMb.value = 0
                                }
                            ),
                        )
                    )
                }
            }
        }
    }
}
