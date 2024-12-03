package ca.stefanm.ca.stefanm.ibus.gui.map.widget.tile


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
import ca.stefanm.ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.DaggerApplicationComponent

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

    LaunchedEffect(x, y, zoom) {
        val tileFetcher = DaggerApplicationComponent.create().tileFetcher()

        val tileFile = tileFetcher.getTile(x, y, zoom)

        tileFile.inputStream().buffered().use {
            image.value = org.jetbrains.skia.Image.makeFromEncoded(
                        it.readBytes()
                    ).toComposeImageBitmap()
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