package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorMath
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole.*
import org.junit.Assert.assertEquals
import org.junit.Test

/** The built-in themes' colours and sizes, as they were when every theme was written out by hand. */
class BuiltInThemesTest {

    private val shared = mapOf(
        SELECTED_COLOR to "#F0BDB0FF",
        CHIP_HIGHLIGHTS to "#FFFFFFFF",
        TEXT_WHITE to "#FFFFFFFF",
        TEXT_BLUE_LIGHT to "#0000FFFF",
        TEXT_BLUE_DARK to "#0000FFFF",
        TEXT_RED to "#FF0000FF",
    )

    private val bmwBlue = shared + mapOf(
        MENU_BACKGROUND to "#30486BFF",
        TEXT_MENU_COLOR_ACCENT to "#E5FFFFFF",
        CHIP_COLOR to "#79B5DCFF",
        SIDE_MENU_BORDER to "#3D70B0FF",
        HEADER_FONT to "#E5FFFFFF",
        GRADIENT_EDGE to "#4480C0FF",
        GRADIENT_CENTER to "#3D70B0FF",
        EDGE_HIGHLIGHT to "#568BBFFF",
    )

    private val revolutionOrange = shared + mapOf(
        MENU_BACKGROUND to "#AD6500FF",
        TEXT_MENU_COLOR_ACCENT to "#FFFFE5FF",
        CHIP_COLOR to "#DA9A51FF",
        SIDE_MENU_BORDER to "#E98906FF",
        HEADER_FONT to "#E5FFFFFF",
        GRADIENT_EDGE to "#F9940BFF",
        GRADIENT_CENTER to "#E98906FF",
        EDGE_HIGHLIGHT to "#B17E43FF",
    )

    private val gooseGreen = shared + mapOf(
        MENU_BACKGROUND to "#005229FF",
        TEXT_MENU_COLOR_ACCENT to "#E5FFE5FF",
        CHIP_COLOR to "#009A51FF",
        SIDE_MENU_BORDER to "#006635FF",
        HEADER_FONT to "#E5FFE5FF",
        GRADIENT_EDGE to "#007B41FF",
        GRADIENT_CENTER to "#00703BFF",
        EDGE_HIGHLIGHT to "#00542CFF",
    )

    private fun assertColours(expected: Map<ThemeColorRole, String>, theme: Theme) {
        assertEquals(theme.configFileName, expected, ThemeColorRole.colorsOf(theme).mapValues { ThemeColorMath.toHex(it.value) })
        // The gradient is edge, center, edge.
        val gradient = theme.centerGradientWithEdgeHighlight.backgroundGradientColorList
        assertEquals(3, gradient.size)
        assertEquals(gradient[0], gradient[2])
    }

    private fun assertNormalSize(theme: Theme) {
        assertEquals(false, theme.isPixelDoubled)
        assertEquals(Themes.NormalWindowSize, theme.windowSize)
        assertEquals(Theme.MenuItemMeasurements(8F, 4F, 15.sp), theme.bigItem)
        assertEquals(Theme.MenuItemMeasurements(6F, 3F, 12.sp), theme.smallItem)
        assertEquals(9.sp, theme.hmiHeaderFooter.fontSize)
        assertEquals(Theme.HmiHeaderFooter.HeaderPadding(6.dp, 6.dp, 4.dp, 4.dp), theme.hmiHeaderFooter.headerPadding)
        assertEquals(2.dp, theme.centerGradientWithEdgeHighlight.edgeHighlightHeight)
    }

    private fun assertDoubledPixels(theme: Theme) {
        assertEquals(true, theme.isPixelDoubled)
        assertEquals(Themes.PixelDoubledWindowSize, theme.windowSize)
        assertEquals(Theme.MenuItemMeasurements(16F, 8F, 30.sp), theme.bigItem)
        assertEquals(Theme.MenuItemMeasurements(12F, 6F, 24.sp), theme.smallItem)
        assertEquals(18.sp, theme.hmiHeaderFooter.fontSize)
        assertEquals(Theme.HmiHeaderFooter.HeaderPadding(12.dp, 12.dp, 8.dp, 8.dp), theme.hmiHeaderFooter.headerPadding)
        assertEquals(4.dp, theme.centerGradientWithEdgeHighlight.edgeHighlightHeight)
    }

    @Test
    fun bmwBlue() {
        assertColours(bmwBlue, Themes.BmwBlueNormalSize)
        assertColours(bmwBlue, Themes.BmwBlueDoubledPixels)
        assertNormalSize(Themes.BmwBlueNormalSize)
        assertDoubledPixels(Themes.BmwBlueDoubledPixels)
        assertEquals("BmwBlueNormalSize" to "BMW Blue", Themes.BmwBlueNormalSize.let { it.configFileName to it.friendlyName })
        assertEquals("BmwBlueDoubledPixels" to "BMW Blue (2X)", Themes.BmwBlueDoubledPixels.let { it.configFileName to it.friendlyName })
    }

    @Test
    fun revolutionOrange() {
        assertColours(revolutionOrange, Themes.RevolutionOrangeNormalSize)
        assertColours(revolutionOrange, Themes.RevolutionOrangeDoubledPixels)
        assertNormalSize(Themes.RevolutionOrangeNormalSize)
        assertDoubledPixels(Themes.RevolutionOrangeDoubledPixels)
        assertEquals("RevolutionOrangeNormalSize" to "Revolution Orange", Themes.RevolutionOrangeNormalSize.let { it.configFileName to it.friendlyName })
        assertEquals("RevolutionOrangeDoubledPixels" to "Revolution Orange (2X)", Themes.RevolutionOrangeDoubledPixels.let { it.configFileName to it.friendlyName })
    }

    @Test
    fun gooseGreen() {
        assertColours(gooseGreen, Themes.GooseGreenNormalSize)
        assertColours(gooseGreen, Themes.GooseGreenDoubledPixels)
        assertNormalSize(Themes.GooseGreenNormalSize)
        assertDoubledPixels(Themes.GooseGreenDoubledPixels)
        assertEquals("GooseGreenNormalSize" to "Goose Green", Themes.GooseGreenNormalSize.let { it.configFileName to it.friendlyName })
        assertEquals("GooseGreenDoubledPixels" to "Goose Green (2X)", Themes.GooseGreenDoubledPixels.let { it.configFileName to it.friendlyName })
    }
}
