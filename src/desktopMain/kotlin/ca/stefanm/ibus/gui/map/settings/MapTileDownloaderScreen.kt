package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.tile.TileFetcher
import ca.stefanm.ibus.gui.map.widget.tile.TileServerImageCache
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils.byteCountToDisplaySize
import javax.inject.Inject

@ApplicationScope
@AutoDiscover
class MapTileDownloaderScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val tileServerImageCache: TileServerImageCache,
    private val tileFetcher: TileFetcher,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapTileDownloaderScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val scope = rememberCoroutineScope()

        val downloadLocation = if (it?.resultFrom == MapScreen::class.java && it.result is MapScreenResult) {
            (it.result as? MapScreenResult.PointSelectedResult)?.point
        } else { null }

        val downloadRadius = remember { mutableStateOf(MapScale.KILOMETERS_50) }
        val downloadClosestZoom = remember { mutableStateOf(MapScale.METERS_800) }
        val isDownloading = remember { mutableStateOf(false) }

        val downloadingTotal = remember { mutableStateOf(0) }
        val downloadedCurrent = remember { mutableStateOf(0) }

        DisposableEffect(true) {
            onDispose {
                modalMenuService.closeModalMenu()
            }
        }

        Column(
            Modifier.fillMaxHeight().background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            BmwSingleLineHeader("Download Map Tiles")

            @Composable
            fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
                Text(
                    text = text,
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 20.sp else 11.sp,
                    fontWeight = weight
                )
            }

            Column(Modifier
                .fillMaxWidth()
            ) {
                InfoLabel(if (downloadLocation != null) {
                        val lat = downloadLocation.latitude.toString().slice(0..7)
                        val lng = downloadLocation.longitude.toString().slice(0..7)
                        "Download center: $lat, $lng"
                    } else {
                        "No Download Center Selected"
                    })
                InfoLabel(if (isDownloading.value) {
                        "Downloading tile ${downloadedCurrent.value} / ${downloadingTotal.value}"
                    } else {
                        "Download not running."
                    })

                InfoLabel("Download zooms: ${downloadClosestZoom.value.displayString} - ${downloadRadius.value.displayString}")
            }


            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(
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
                            scope.launch {
                                if (downloadLocation != null && !isDownloading.value) {
                                    isDownloading.value = true
                                    val updateFlow = tileFetcher.downloadTiles(downloadLocation, downloadRadius.value, downloadClosestZoom.value)
                                    updateFlow.collect { status ->
                                        downloadedCurrent.value = status.tilesDownloaded
                                        downloadingTotal.value = status.totalTilesToDownload
                                    }
                                    isDownloading.value = false
                                }
                            }
                        }
                    )
                ),
                rightItems = listOf(
                    TextMenuItem(
                        title = "Set Furthest Zoom",
                        onClicked = {
                            scope.launch {
                                val current = MapScale.values().indexOf(downloadRadius.value)
                                val next = MapScale.values().toList().circular()[current + 1]
                                downloadRadius.value = next
                            }
                        }
                    ),
                    TextMenuItem(
                        title = "Set Closest Zoom",
                        onClicked = {
                            scope.launch {
                                val current = MapScale.values().indexOf(downloadClosestZoom.value)
                                val next = MapScale.values().toList().circular()[current + 1]
                                downloadClosestZoom.value = next
                            }
                        }
                    ),
                    TextMenuItem(
                        title = "Clear Tile Cache",
                        onClicked = {
                            modalMenuService.showModalMenu(
                                dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                                    menuTopLeft = IntOffset(1200, 600),
                                    menuWidth = 384,
                                ).toNormalModalMenuDimensions(),
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.E,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            "Back",
                                            onClicked = {
                                                modalMenuService.closeModalMenu()
                                            }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            "Delete Tiles",
                                            onClicked = { tileServerImageCache.clearCache()}
                                        )
                                    )
                                )
                            )
                        }
                    ),
                    TextMenuItem(
                        title = "Sizeof Tile Cache?",
                        onClicked = {
                            scope.launch {
                                val size = tileServerImageCache.calculateCacheSize().bytesToHumanReadable()
                                notificationHub.postNotification(
                                    Notification(
                                        image = Notification.NotificationImage.ALERT_CIRCLE,
                                        topText = "Tile Cache Size",
                                        contentText = "Size on disk: $size"
                                    )
                                )
                            }
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