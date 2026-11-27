package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes

/**
 * Makes a theme's colours from one base colour, the menu background.
 *
 * A [Preset] is measured from an existing theme: how far each of its colours
 * is from its background in hue, saturation and lightness, and the colours
 * every theme shares as they are (the selection colour, chip highlights, the
 * TEXT_ colours). Generating from that theme's own background with its own
 * preset gives the theme back.
 */
object ThemeGenerator {

    sealed interface Rule {
        /** The base's hue, saturation and lightness, moved by these. */
        data class Relative(val dh: Float, val ds: Float, val dl: Float) : Rule
        /** This colour, whatever the base. */
        data class Absolute(val color: Color) : Rule
    }

    data class Preset(val name: String, val rules: Map<ThemeColorRole, Rule>) {
        companion object {
            /** The colours that follow the base's hue. */
            private val RELATIVE = listOf(
                ThemeColorRole.TEXT_MENU_COLOR_ACCENT,
                ThemeColorRole.CHIP_COLOR,
                ThemeColorRole.SIDE_MENU_BORDER,
                ThemeColorRole.HEADER_FONT,
                ThemeColorRole.GRADIENT_EDGE,
                ThemeColorRole.GRADIENT_CENTER,
                ThemeColorRole.EDGE_HIGHLIGHT,
            )

            fun measure(name: String, theme: Theme): Preset {
                val colors = ThemeColorRole.colorsOf(theme)
                val base = Hsl.of(colors.getValue(ThemeColorRole.MENU_BACKGROUND))
                val rules = colors.mapValues { (role, color) ->
                    when (role) {
                        ThemeColorRole.MENU_BACKGROUND -> Rule.Relative(0F, 0F, 0F)
                        in RELATIVE -> {
                            val hsl = Hsl.of(color)
                            Rule.Relative(hueDelta(base.h, hsl.h), hsl.s - base.s, hsl.l - base.l)
                        }
                        else -> Rule.Absolute(color)
                    }
                }
                return Preset(name, rules)
            }

            /** From -180 to 180. */
            private fun hueDelta(from: Float, to: Float): Float = ((to - from + 540F) % 360F) - 180F
        }
    }

    /** For lighter themes: BMW Blue's spacing (gradient +0.16, chips +0.37 lighter than the background). */
    val LIGHT: Preset = Preset.measure("Light (BMW Blue)", Themes.BmwBlueNormalSize)

    /** For darker themes: Goose Green's spacing (gradient +0.06, chips +0.14 lighter than the background). */
    val DARK: Preset = Preset.measure("Dark (Goose Green)", Themes.GooseGreenNormalSize)

    val presets = listOf(LIGHT, DARK)

    /**
     * Every colour from [base] (the menu background) and [preset]; [lightnessShift]
     * moves every lightness level, the background's too, up or down together.
     */
    fun generate(base: Color, preset: Preset, lightnessShift: Float = 0F): Map<ThemeColorRole, Color> {
        val b = Hsl.of(base)
        return preset.rules.mapValues { (_, rule) ->
            when (rule) {
                is Rule.Absolute -> rule.color
                is Rule.Relative -> Hsl(
                    h = (b.h + rule.dh + 360F) % 360F,
                    s = (b.s + rule.ds).coerceIn(0F, 1F),
                    l = (b.l + rule.dl + lightnessShift).coerceIn(0F, 1F),
                    alpha = b.alpha,
                ).toColor()
            }
        }
    }
}
