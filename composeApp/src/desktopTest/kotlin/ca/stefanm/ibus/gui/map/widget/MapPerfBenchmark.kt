package ca.stefanm.ibus.gui.map.widget

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.map.Extents
import ca.stefanm.ibus.gui.map.OverlayProperties
import ca.stefanm.ibus.gui.map.MapViewer
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Where the map's time goes (opt-in: E39_MAP_BENCH=1; needs a scratch
 * /var/lib/e39 and a user.home with cached tiles in .e39/mapTiles, see
 * docs/map-performance.md).
 */
internal class MapPerfBenchmark {

    private fun timeMs(times: Int, block: () -> Unit): Double {
        repeat(5) { block() }   // warm up
        val start = System.nanoTime()
        repeat(times) { block() }
        return (System.nanoTime() - start) / 1e6 / times
    }

    @Test
    fun benchmark() {
        assumeTrue("E39_MAP_BENCH not set", System.getenv("E39_MAP_BENCH") != null)

        // What TileView does per tile, three times: a new component, its settings.
        val component = timeMs(50) {
            DaggerApplicationComponent.create().configurationStorage().config[E39Config.MapConfig.tileSource]
        }
        val fetcher = timeMs(50) { DaggerApplicationComponent.create().tileFetcher() }
        val app = DaggerApplicationComponent.create()
        val shared = timeMs(200) { app.configurationStorage().config[E39Config.MapConfig.tileSource] }

        val tiles = File(System.getProperty("user.home"), ".e39/mapTiles").walk().filter { it.extension == "png" }.toList()
        val decode = timeMs(tiles.size) {
            tiles.forEach { Image.makeFromEncoded(it.readBytes()).close() }
        } / tiles.size
        val cachedFetch = runBlocking {
            val f = app.tileFetcher()
            val (z, x, y) = tiles.first().let { t -> Triple(t.parentFile.parentFile.name.toInt(), t.parentFile.name.toInt(), t.nameWithoutExtension.toInt()) }
            timeMs(200) { runBlocking { f.getTile(x, y, z) } }
        }

        println("per tile today: new component + settings ${"%.2f".format(component)} ms (x3 per tile), " +
                "new component + tileFetcher ${"%.2f".format(fetcher)} ms; with one component: settings ${"%.4f".format(shared)} ms")
        println("cached tile lookup ${"%.3f".format(cachedFetch)} ms, PNG decode ${"%.2f".format(decode)} ms (${tiles.size} tiles)")

        for ((w, h) in listOf(400 to 234, 800 to 468)) {
            val start = System.nanoTime()
            var frames = 0
            ImageComposeScene(w, h, Density(1F)) {
                CompositionLocalProvider(ThemeWrapper.ThemeHandle provides Themes.BmwBlueNormalSize) {
                    MapViewer(
                        overlayProperties = OverlayProperties(false, false, false),
                        extents = Extents(LatLng(45.3154699, -75.9194058), MapScale.KILOMETERS_1_6),
                        onCenterPositionUpdated = {},
                    )
                }
            }.use { scene ->
                val first = System.nanoTime()
                scene.render(0); frames++
                val firstFrame = (System.nanoTime() - first) / 1e6
                // Let the tiles' LaunchedEffects load (frames 16 ms apart).
                var t = 0L
                val loopStart = System.nanoTime()
                while (t < 3_000_000_000L) { t += 16_000_000L; scene.render(t); frames++ }
                val loop = (System.nanoTime() - loopStart) / 1e6
                println("MapViewer ${w}x$h: first frame ${"%.0f".format(firstFrame)} ms, then $frames frames in " +
                        "${"%.0f".format(loop)} ms; total ${"%.0f".format((System.nanoTime() - start) / 1e6)} ms")
            }
        }
    }
}
