package ca.stefanm.ibus.gui.map.guidance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import ca.stefanm.ibus.gui.map.guidance.notifications.ui.GuidanceSidePanel
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository.ManeuverKind
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import com.javadocmd.simplelatlng.LatLng
import org.jetbrains.skia.EncodedImageFormat
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Renders the guidance side panel to PNG files, to look at its layout
 * without a car or a running HMI:
 *
 *   E39_RENDER_DIR=/tmp/panels ./gradlew :composeApp:desktopTest --tests '*GuidanceSidePanelRender*'
 *
 * Skipped without E39_RENDER_DIR: the theme (ThemeWrapper) reads the HMI's
 * settings in /var/lib/e39, so run it where that's a scratch folder.
 */
internal class GuidanceSidePanelRender {

    private fun instruction(kind: ManeuverKind, text: String, streets: List<String>, exit: Int = 0) =
        GuidanceInstruction(4, 12, kind, text, "", streets, LatLng(45.0, -75.0), 200.0, 500.0, 60.0, exit)

    @Test
    fun render() {
        val dir = System.getenv("E39_RENDER_DIR")
        assumeTrue("E39_RENDER_DIR not set", dir != null)
        val out = File(dir!!).apply { mkdirs() }
        val cases = listOf(
            "right" to (instruction(ManeuverKind.RIGHT, "Turn right onto Bank Street.", listOf("Bank Street")) to 350.0),
            "roundabout" to (instruction(ManeuverKind.ROUNDABOUT_ENTER, "Enter the roundabout and take the 2nd exit onto Terry Fox Drive.",
                listOf("Terry Fox Drive"), exit = 2) to 120.0),
            "exit-long" to (instruction(ManeuverKind.EXIT_RIGHT, "Take exit 138 on the right toward Eagleson Road/Kanata South.",
                listOf("Highway 417", "Queensway"), exit = 0) to 1500.0),
            "now" to (instruction(ManeuverKind.SLIGHT_LEFT, "Bear left onto March Road.", listOf("March Road")) to 20.0),
            "destination" to (instruction(ManeuverKind.DESTINATION_RIGHT, "Your destination is on the right.", emptyList()) to 80.0),
        )
        for ((theme, w, h, suffix) in listOf(
            Quad(Themes.BmwBlueNormalSize, 160, 200, "normal"),
            Quad(Themes.BmwBlueDoubledPixels, 320, 400, "doubled"),
        )) {
            for ((name, case) in cases) {
                ImageComposeScene(w, h, Density(1F)) {
                    CompositionLocalProvider(ThemeWrapper.ThemeHandle provides theme) {
                        Box(Modifier.fillMaxSize()) { GuidanceSidePanel.Panel(case.first, case.second) }
                    }
                }.use { scene ->
                    // Two frames: images (painterResource) load after the first.
                    scene.render(0)
                    val image = scene.render(1_000_000_000)
                    File(out, "$suffix-$name.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
                }
            }
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
