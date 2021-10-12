package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.MapScreen
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.tile.TileFetcher
import ca.stefanm.ibus.gui.map.widget.tile.TileServerImageCache
import ca.stefanm.ibus.gui.map.widget.tile.TileServerImageCacheClearer
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FileUtils.byteCountToDisplaySize
import java.util.*
import javax.inject.Inject

@AutoDiscover
class MapTileDownloaderScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val tileServerImageCache: TileServerImageCache,
    private val tileFetcher: TileFetcher
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapTileDownloaderScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val downloadLocation = if (it?.resultFrom == MapScreen::class.java && it.result is MapScreen.MapScreenResult) {
            (it.result as? MapScreen.MapScreenResult.PointSelectedResult)?.point
        } else { null }

        val downloadRadius = remember { mutableStateOf(MapScale.KILOMETERS_50) }
        val _downloadRadius = remember { MutableStateFlow(downloadRadius.value) }
        rememberCoroutineScope().launch {
            _downloadRadius.collect { downloadRadius.value = it }
        }

        val isDownloading = remember { mutableStateOf(false) }
        val _isDownloading = remember { MutableStateFlow(isDownloading.value) }
        rememberCoroutineScope().launch {
            _isDownloading.collect { isDownloading.value = it }
        }
        LaunchedEffect(isDownloading, downloadRadius.value) {
            if (downloadLocation != null) {
                tileFetcher.downloadTiles(downloadLocation, downloadRadius.value)
            }
        }

        val downloadingTotal = remember { mutableStateOf(0) }
        val downloadedCurrent = remember { mutableStateOf(0) }

        val cacheSizeBytes = remember { mutableStateOf(0L) }
        val _cacheSizeBytes = remember { MutableStateFlow(cacheSizeBytes.value) }
        rememberCoroutineScope().launch {
            _cacheSizeBytes.collect { cacheSizeBytes.value = it }
        }

        Column {
            BmwSingleLineHeader("Download Map Tiles")


            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(
                    TextMenuItem(
                        title = if (downloadLocation != null) {
                            "Download center: ${downloadLocation.latitude}, {${downloadLocation.longitude}"
                        } else {
                            ""
                        },
                        isSelectable = false,
                        onClicked = {}
                    ),

                    TextMenuItem(
                        title = if (isDownloading.value) {
                            "Tile ${downloadedCurrent.value} / ${downloadingTotal.value}"
                        } else {
                            ""
                        },
                        isSelectable = false,
                        onClicked = {}
                    )
                ),
                ne = listOf(
                    TextMenuItem(
                        title = "Cache Size: ${cacheSizeBytes.value.bytesToHumanReadable()}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = "Radius: ${downloadRadius.value.displayString}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                ),
                sw = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.goBack()
                        }
                    ),
                    TextMenuItem(
                        title = "Pick Download Center...",
                        onClicked = {
                            MapScreen.openForUserLocationSelection(navigationNodeTraverser)
                        }
                    ),
                    TextMenuItem(
                        title = if (!isDownloading.value) "Download Tiles" else "Cancel Download",
                        onClicked = {
                            _isDownloading.value = !_isDownloading.value
                        }
                    )
                ),
                se = listOf(
                    TextMenuItem(
                        title = "Set Fetch Radius",
                        onClicked = {
                            val current = MapScale.values().indexOf(downloadRadius.value)
                            val next = MapScale.values().toList().circular()[current + 1]
                            _downloadRadius.value = next
                        }
                    ),
                    TextMenuItem(
                        title = "Clear Tile Cache",
                        onClicked = {
                            tileServerImageCache.clearCache()
                        }
                    ),
                    TextMenuItem(
                        title = "Sizeof Tile Cache?",
                        onClicked = {
                            _cacheSizeBytes.value = tileServerImageCache.calculateCacheSize()
                        }
                    )
                )
            )

        }
    }

    private fun Long.bytesToHumanReadable() : String {
        return byteCountToDisplaySize(this)
    }
}