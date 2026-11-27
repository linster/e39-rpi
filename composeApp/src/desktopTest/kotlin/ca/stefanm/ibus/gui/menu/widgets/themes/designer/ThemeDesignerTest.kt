package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ThemeDesignerTest {

    /** Within one step of 255 on each channel (HSL round trips aren't exact in floats). */
    private fun assertClose(expected: Color, actual: Color, what: String) {
        val channels = listOf(
            expected.red to actual.red, expected.green to actual.green,
            expected.blue to actual.blue, expected.alpha to actual.alpha,
        )
        assertTrue(
            "$what: expected ${ThemeColorMath.toHex(expected)}, got ${ThemeColorMath.toHex(actual)}",
            channels.all { (e, a) -> abs(e - a) <= 1.5F / 255F }
        )
    }

    // --- Colour maths ---------------------------------------------------

    @Test
    fun hexRoundTrip() {
        val purple = Color(82, 41, 100, 255)
        assertEquals("#522964FF", ThemeColorMath.toHex(purple))
        assertEquals(purple, ThemeColorMath.parseHex("#522964FF"))
        assertEquals(purple, ThemeColorMath.parseHex("522964"))
        assertEquals(Color(82, 41, 100, 128), ThemeColorMath.parseHex("#52296480"))
    }

    @Test
    fun badHexIsNull() {
        listOf("", "#", "#12345", "#1234567", "#GG0000", "red").forEach {
            assertNull(it, ThemeColorMath.parseHex(it))
        }
    }

    @Test
    fun hslRoundTripsEveryThemeColour() {
        Themes.builtInThemes.forEach { theme ->
            ThemeColorRole.colorsOf(theme).forEach { (role, color) ->
                assertClose(color, Hsl.of(color).toColor(), "${theme.configFileName} $role")
            }
        }
    }

    @Test
    fun hslOfKnownColours() {
        val bmwBlueBackground = Hsl.of(Color(48, 72, 107, 255))
        assertEquals(216F, bmwBlueBackground.h, 1F)
        assertEquals(0.38F, bmwBlueBackground.s, 0.01F)
        assertEquals(0.30F, bmwBlueBackground.l, 0.01F)
        assertEquals(Hsl(0F, 0F, 1F), Hsl.of(Color.White))
    }

    @Test
    fun contrast() {
        assertEquals(21.0, ThemeColorMath.contrast(Color.White, Color.Black), 0.01)
        assertEquals(1.0, ThemeColorMath.contrast(Color.Red, Color.Red), 0.01)
    }

    // --- Generator ------------------------------------------------------

    private fun assertGivesBack(theme: Theme, preset: ThemeGenerator.Preset) {
        val colors = ThemeColorRole.colorsOf(theme)
        val generated = ThemeGenerator.generate(colors.getValue(ThemeColorRole.MENU_BACKGROUND), preset)
        assertEquals(ThemeColorRole.entries.toSet(), generated.keys)
        colors.forEach { (role, color) -> assertClose(color, generated.getValue(role), "${preset.name} $role") }
    }

    @Test
    fun lightPresetGivesBackBmwBlue() = assertGivesBack(Themes.BmwBlueNormalSize, ThemeGenerator.LIGHT)

    @Test
    fun darkPresetGivesBackGooseGreen() = assertGivesBack(Themes.GooseGreenNormalSize, ThemeGenerator.DARK)

    @Test
    fun darkPresetIsDarkerThanLight() {
        val purple = Color(82, 41, 100, 255)
        val light = ThemeGenerator.generate(purple, ThemeGenerator.LIGHT)
        val dark = ThemeGenerator.generate(purple, ThemeGenerator.DARK)
        listOf(ThemeColorRole.GRADIENT_CENTER, ThemeColorRole.GRADIENT_EDGE, ThemeColorRole.CHIP_COLOR).forEach {
            assertTrue("$it", Hsl.of(dark.getValue(it)).l < Hsl.of(light.getValue(it)).l)
        }
    }

    @Test
    fun generatedColoursKeepTheBaseHue() {
        val purple = Color(82, 41, 100, 255)
        val generated = ThemeGenerator.generate(purple, ThemeGenerator.LIGHT)
        assertClose(purple, generated.getValue(ThemeColorRole.MENU_BACKGROUND), "background")
        val baseHue = Hsl.of(purple).h
        listOf(ThemeColorRole.GRADIENT_CENTER, ThemeColorRole.GRADIENT_EDGE, ThemeColorRole.SIDE_MENU_BORDER).forEach {
            assertEquals("$it", baseHue, Hsl.of(generated.getValue(it)).h, 5F)
        }
        // Shared by every theme, whatever the base.
        assertEquals(Themes.BmwBlueNormalSize.colors.selectedColor, generated.getValue(ThemeColorRole.SELECTED_COLOR))
        assertEquals(Color.White, generated.getValue(ThemeColorRole.CHIP_HIGHLIGHTS))
    }

    @Test
    fun lightnessShiftMovesEveryLevel() {
        val purple = Color(82, 41, 100, 255)
        val plain = ThemeGenerator.generate(purple, ThemeGenerator.LIGHT)
        val darker = ThemeGenerator.generate(purple, ThemeGenerator.LIGHT, lightnessShift = -0.1F)
        listOf(ThemeColorRole.MENU_BACKGROUND, ThemeColorRole.GRADIENT_CENTER, ThemeColorRole.CHIP_COLOR).forEach {
            assertEquals("$it", Hsl.of(plain.getValue(it)).l - 0.1F, Hsl.of(darker.getValue(it)).l, 0.01F)
        }
    }

    // --- The designer's state --------------------------------------------

    @Test
    fun lockedColoursSurviveGenerating() {
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        val myChip = Color(10, 20, 30, 255)
        state.setColor(ThemeColorRole.CHIP_COLOR, myChip)
        state.setLocked(ThemeColorRole.CHIP_COLOR, true)

        state.changeBase(Color(82, 41, 100, 255))

        assertEquals(myChip, state.colors.getValue(ThemeColorRole.CHIP_COLOR))
        assertClose(Color(82, 41, 100, 255), state.colors.getValue(ThemeColorRole.MENU_BACKGROUND), "background")
        assertNotEquals(
            Themes.BmwBlueDoubledPixels.centerGradientWithEdgeHighlight.backgroundGradientColorList[1],
            state.colors.getValue(ThemeColorRole.GRADIENT_CENTER)
        )
    }

    @Test
    fun handEditsStayUntilTheNextGenerate() {
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        state.changeBase(Color(82, 41, 100, 255))
        state.setColor(ThemeColorRole.GRADIENT_CENTER, Color.Red)
        assertEquals(Color.Red, state.colors.getValue(ThemeColorRole.GRADIENT_CENTER))

        state.changePreset(ThemeGenerator.DARK)
        assertNotEquals(Color.Red, state.colors.getValue(ThemeColorRole.GRADIENT_CENTER))
    }

    @Test
    fun loadTakesTheThemesColoursAndClearsLocks() {
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        state.setLocked(ThemeColorRole.CHIP_COLOR, true)
        state.load(Themes.GooseGreenDoubledPixels)
        assertEquals(ThemeColorRole.colorsOf(Themes.GooseGreenDoubledPixels), state.colors)
        assertEquals(Themes.GooseGreenDoubledPixels.colors.menuBackground, state.base)
        assertTrue(state.locked.isEmpty())
    }

    @Test
    fun theThemeHasTheColoursAndTheChosenSizes() {
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        state.load(Themes.RevolutionOrangeDoubledPixels)

        state.pixelDoubled = false
        val small = state.theme()
        assertFalse(small.isPixelDoubled)
        assertEquals(Themes.BmwBlueNormalSize.bigItem, small.bigItem)
        assertEquals(Themes.RevolutionOrangeNormalSize.colors, small.colors)
        assertEquals(
            Themes.RevolutionOrangeNormalSize.centerGradientWithEdgeHighlight,
            small.centerGradientWithEdgeHighlight
        )
        assertEquals(Themes.RevolutionOrangeNormalSize.hmiHeaderFooter, small.hmiHeaderFooter)

        state.pixelDoubled = true
        assertEquals(Themes.RevolutionOrangeDoubledPixels.centerGradientWithEdgeHighlight, state.theme().centerGradientWithEdgeHighlight)
    }

    @Test
    fun contrastCheckFlagsLowContrast() {
        // BMW Blue passes (8.9, 9.3 and 4.85:1).
        assertTrue(ThemeDesignerState(Themes.BmwBlueDoubledPixels).contrasts().none { it.isTooLow })

        // White header text on a light gradient doesn't.
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        state.setColor(ThemeColorRole.GRADIENT_CENTER, Color(200, 220, 240, 255))
        val header = state.contrasts().single { it.foreground == ThemeColorRole.HEADER_FONT }
        assertTrue("${header.ratio}", header.isTooLow)
    }
}
