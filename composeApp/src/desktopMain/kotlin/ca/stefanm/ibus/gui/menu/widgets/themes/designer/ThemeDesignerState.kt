package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeScheme

/**
 * What the Theme Designer is editing: every colour, which are locked, and
 * the base colour, preset and lightness shift they're generated from.
 *
 * Generating (on [generate], or when the base, preset or shift changes)
 * replaces every colour that isn't locked; a colour can be changed by hand
 * any time after.
 */
class ThemeDesignerState(start: Theme) {

    var colors: Map<ThemeColorRole, Color> by mutableStateOf(ThemeColorRole.colorsOf(start))
        private set

    var locked: Set<ThemeColorRole> by mutableStateOf(emptySet())
        private set

    var base: Color by mutableStateOf(ThemeColorRole.colorsOf(start).getValue(ThemeColorRole.MENU_BACKGROUND))
        private set

    var preset: ThemeGenerator.Preset by mutableStateOf(ThemeGenerator.LIGHT)
        private set

    var lightnessShift: Float by mutableStateOf(0F)
        private set

    /** The preview's sizes, fonts and padding: 2X or 1X. */
    var pixelDoubled: Boolean by mutableStateOf(start.isPixelDoubled)

    /** What it's saved as ("Jess Purple": themes/JessPurple.conf, [ThemeScheme.nameFor]). */
    var friendlyName: String by mutableStateOf(start.friendlyName.removeSuffix(TWO_X))

    /** Start from [theme]'s colours (its background as the base) and name; locks are cleared. */
    fun load(theme: Theme) {
        load(ThemeColorRole.colorsOf(theme), theme.friendlyName.removeSuffix(TWO_X))
    }

    /** Start from [scheme]'s colours and name; locks are cleared. */
    fun load(scheme: ThemeScheme) = load(scheme.colors, scheme.friendlyName)

    private fun load(colors: Map<ThemeColorRole, Color>, friendlyName: String) {
        this.colors = colors
        this.friendlyName = friendlyName
        base = colors.getValue(ThemeColorRole.MENU_BACKGROUND)
        lightnessShift = 0F
        locked = emptySet()
    }

    /**
     * The scheme to save, or null while [friendlyName] has no letters or
     * digits. A friendly name of one of [existing] (ignoring case, e.g. "BMW
     * Blue") saves as that one, replacing it; another gets a new name
     * ([ThemeScheme.nameFor]).
     */
    fun scheme(existing: List<ThemeScheme> = Themes.builtInSchemes): ThemeScheme? {
        val friendly = friendlyName.trim()
        val name = existing.firstOrNull { it.friendlyName.equals(friendly, ignoreCase = true) }?.name
            ?: ThemeScheme.nameFor(friendly)
            ?: return null
        return ThemeScheme(name, friendly, colors)
    }

    /** A colour changed by hand. */
    fun setColor(role: ThemeColorRole, color: Color) {
        colors = colors + (role to color)
    }

    fun setLocked(role: ThemeColorRole, isLocked: Boolean) {
        locked = if (isLocked) locked + role else locked - role
    }

    fun changeBase(color: Color) {
        base = color
        generate()
    }

    fun changePreset(preset: ThemeGenerator.Preset) {
        this.preset = preset
        generate()
    }

    fun changeLightnessShift(shift: Float) {
        lightnessShift = shift
        generate()
    }

    /** Every colour that isn't locked, from the base, preset and shift. */
    fun generate() {
        colors = colors + ThemeGenerator.generate(base, preset, lightnessShift).filterKeys { it !in locked }
    }

    /** The theme as it stands, on the 1X or 2X sizes. */
    fun theme(): Theme = ThemeColorRole.applyTo(
        base = if (pixelDoubled) Themes.BmwBlueDoubledPixels else Themes.BmwBlueNormalSize,
        colors = colors,
        configFileName = PREVIEW_CONFIG_NAME,
        friendlyName = "Theme Designer preview",
    )

    data class Contrast(val foreground: ThemeColorRole, val background: ThemeColorRole, val ratio: Double) {
        val isTooLow: Boolean get() = ratio < ThemeColorMath.MIN_CONTRAST
    }

    /** Text colours against what they're drawn on. */
    fun contrasts(): List<Contrast> = CONTRAST_PAIRS.map { (fg, bg) ->
        Contrast(fg, bg, ThemeColorMath.contrast(colors.getValue(fg), colors.getValue(bg)))
    }

    companion object {
        const val PREVIEW_CONFIG_NAME = "ThemeDesignerPreview"
        private const val TWO_X = " (2X)"

        val CONTRAST_PAIRS = listOf(
            ThemeColorRole.TEXT_MENU_COLOR_ACCENT to ThemeColorRole.MENU_BACKGROUND,
            ThemeColorRole.TEXT_WHITE to ThemeColorRole.MENU_BACKGROUND,
            ThemeColorRole.HEADER_FONT to ThemeColorRole.GRADIENT_CENTER,
        )
    }
}
