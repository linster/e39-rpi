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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.DaggerApplicationComponent
import kotlinx.coroutines.CancellationException

@Composable
fun TileView(
    x : Int,
    y : Int,
    zoom : Int,
    debug : Boolean =
        DaggerApplicationComponent.create().configurationStorage()
            .config[E39Config.MapConfig.showDebugInfoOnTiles]
) {

    val image = remember { mutableStateOf<ImageBitmap?>(null) }

    // The tile source (OpenStreetMap or the maps backend) is part of the key:
    // switching it redraws the tiles from the other server.
    val source = DaggerApplicationComponent.create().configurationStorage()
        .config[E39Config.MapConfig.tileSource]

    LaunchedEffect(x, y, zoom, source) {
        val component = DaggerApplicationComponent.create()
        try {
            val tileFile = component.tileFetcher().getTile(x, y, zoom)
            tileFile.inputStream().buffered().use {
                image.value = org.jetbrains.skia.Image.makeFromEncoded(
                    it.readBytes()
                ).toComposeImageBitmap()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // No network, or the maps backend has no such tile offline: leave
            // the tile blank rather than failing the whole map.
            image.value = null
            component.logger().w("TileView", "Tile $zoom/$x/$y unavailable: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .size(256.dp)
            .then(
                if (debug) {
//                    Modifier.border(1.dp, Color.Red)
                    Modifier
                } else {
                    Modifier
                }
            )
    ) {
        if (image.value != null) {
            Image(
                bitmap = image.value!!,
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