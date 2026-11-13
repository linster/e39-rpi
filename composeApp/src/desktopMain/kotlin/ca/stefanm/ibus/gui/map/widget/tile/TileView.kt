package ca.stefanm.ibus.gui.map.widget.tile


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.MapConfig.TileSource
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * What the map's tiles need: the app's tile fetcher and settings. Provided
 * once per window (MenuWindow), through [LocalMapTileServices].
 *
 * Every TileView used to build its own DaggerApplicationComponent, three
 * times (debug flag, tile source, loading): ~11 ms each on a laptop, as
 * each new component re-reads and parses the settings files, twice per tile
 * during composition, on the UI thread. With 81 tiles that was most of the
 * map's 1.4 s first frame on a laptop, and 15+ s in QEMU.
 */
class MapTileServices(
    val tileFetcher: TileFetcher,
    val configurationStorage: ConfigurationStorage,
    val logger: Logger,
)

/** Windows that don't provide one (debug windows) share one component's. */
private val fallbackMapTileServices: MapTileServices by lazy {
    DaggerApplicationComponent.create().let {
        MapTileServices(it.tileFetcher(), it.configurationStorage(), it.logger())
    }
}

val LocalMapTileServices = staticCompositionLocalOf { fallbackMapTileServices }

@Composable
fun TileView(
    x : Int,
    y : Int,
    zoom : Int,
    debug : Boolean = false,
    /** null: read the setting (callers drawing many tiles pass it once). */
    source : TileSource? = null,
) {
    val services = LocalMapTileServices.current
    // The tile source (OpenStreetMap or the maps backend) is part of the key:
    // switching it redraws the tiles from the other server.
    val tileSource = source ?: services.configurationStorage.config[E39Config.MapConfig.tileSource]

    val image = remember(x, y, zoom, tileSource) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(x, y, zoom, tileSource) {
        try {
            // Cache lookup, download, file read and decoding off the UI thread.
            image.value = withContext(Dispatchers.IO) {
                val tileFile = services.tileFetcher.getTile(x, y, zoom)
                org.jetbrains.skia.Image.makeFromEncoded(tileFile.readBytes()).toComposeImageBitmap()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // No network, or the maps backend has no such tile offline: leave
            // the tile blank rather than failing the whole map.
            image.value = null
            services.logger.w("TileView", "Tile $zoom/$x/$y unavailable: ${e.message}")
        }
    }

    Box(
        modifier = Modifier.size(256.dp)
    ) {
        image.value?.let { bitmap ->
            Image(
                bitmap = bitmap,
                modifier = Modifier.size(256.dp),
                contentDescription = "x: $x, y: $y, zoom: $zoom"
            )
        }

        if (debug) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .background(Color(0, 0, 0, 128))
            ) {
                Text("x: $x")
                Text("y: $y")
                Text("zoom: $zoom")
            }
        }
    }

}
