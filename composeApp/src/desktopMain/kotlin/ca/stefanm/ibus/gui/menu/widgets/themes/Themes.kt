package ca.stefanm.ibus.gui.menu.widgets.themes


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole.*


/**
 * A theme's colours, written once: its 1X ([normalSize]) and 2X
 * ([doubledPixels]) themes are made from them, with the sizes, fonts and
 * padding in [Themes.make]. Built in ([Themes]) or from a theme file
 * (ThemeFiles).
 *
 * [name] makes the themes' configFileNames ("BmwBlue": "BmwBlueNormalSize",
 * "BmwBlueDoubledPixels"); a theme file with a built-in scheme's name
 * replaces it.
 */
data class ThemeScheme(
    val name: String,
    val friendlyName: String,
    val colors: Map<ThemeColorRole, Color>,
) {
    init {
        require(NAME.matches(name)) { "A theme's name is letters and digits, starting with a letter: \"$name\"" }
        require(friendlyName.isNotBlank()) { "A theme needs a friendly name" }
        val missing = ThemeColorRole.entries - colors.keys
        require(missing.isEmpty()) { "Theme $name has no colour for ${missing.joinToString { it.key }}" }
    }

    val normalSize: Theme get() = Themes.make(name + Themes.NORMAL_SIZE, friendlyName, false, colors)
    val doubledPixels: Theme get() = Themes.make(name + Themes.DOUBLED_PIXELS, "$friendlyName (2X)", true, colors)

    /** 2X first, as the theme lists show them. */
    val themes: List<Theme> get() = listOf(doubledPixels, normalSize)

    companion object {
        val NAME = Regex("[A-Za-z][A-Za-z0-9]*")

        /**
         * A name from a friendly one: its letters and digits, each word
         * capitalised ("Jess Purple": "JessPurple"); null with none.
         */
        fun nameFor(friendlyName: String): String? =
            friendlyName.split(Regex("[^A-Za-z0-9]+"))
                .filter { it.isNotEmpty() }
                .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
                .dropWhile { it.isDigit() }
                .takeIf { it.isNotEmpty() }
    }
}

object Themes {

    const val NORMAL_SIZE = "NormalSize"
    const val DOUBLED_PIXELS = "DoubledPixels"

    const val DEFAULT_THEME_NAME = "BmwBlueDoubledPixels"

    val PixelDoubledWindowSize = DpSize(800.dp, 468.dp)
    val NormalWindowSize = DpSize(400.dp, 234.dp)

    /** Every built-in theme shares these. */
    private val shared = mapOf(
        SELECTED_COLOR to Color(240, 189, 176, 255),
        CHIP_HIGHLIGHTS to Color.White,
        TEXT_WHITE to Color.White,
        TEXT_BLUE_LIGHT to Color.Blue,
        TEXT_BLUE_DARK to Color.Blue,
        TEXT_RED to Color.Red,
    )

    val BmwBlue = ThemeScheme("BmwBlue", "BMW Blue", shared + mapOf(
        MENU_BACKGROUND to Color(48, 72, 107, 255),
        TEXT_MENU_COLOR_ACCENT to Color(229, 255, 255, 255),
        CHIP_COLOR to Color(121, 181, 220, 255),
        SIDE_MENU_BORDER to Color(61, 112, 176, 255),
        HEADER_FONT to Color(229, 255, 255, 255),
        GRADIENT_EDGE to Color(68, 128, 192, 255),
        GRADIENT_CENTER to Color(61, 112, 176, 255),
        EDGE_HIGHLIGHT to Color(86, 139, 191, 255),
    ))

    val RevolutionOrange = ThemeScheme("RevolutionOrange", "Revolution Orange", shared + mapOf(
        MENU_BACKGROUND to Color(173, 101, 0, 255),
        TEXT_MENU_COLOR_ACCENT to Color(255, 255, 229, 255),
        CHIP_COLOR to Color(218, 154, 81, 255),
        SIDE_MENU_BORDER to Color(233, 137, 6, 255),
        HEADER_FONT to Color(229, 255, 255, 255),
        GRADIENT_EDGE to Color(249, 148, 11, 255),
        GRADIENT_CENTER to Color(233, 137, 6, 255),
        EDGE_HIGHLIGHT to Color(177, 126, 67, 255),
    ))

    val GooseGreen = ThemeScheme("GooseGreen", "Goose Green", shared + mapOf(
        MENU_BACKGROUND to Color(0, 82, 41, 255),
        TEXT_MENU_COLOR_ACCENT to Color(229, 255, 229, 255),
        CHIP_COLOR to Color(0, 154, 81, 255),
        SIDE_MENU_BORDER to Color(0, 102, 53, 255),
        HEADER_FONT to Color(229, 255, 229, 255),
        GRADIENT_EDGE to Color(0, 123, 65, 255),
        GRADIENT_CENTER to Color(0, 112, 59, 255),
        EDGE_HIGHLIGHT to Color(0, 84, 44, 255),
    ))

    /**
     * Themes made in the Theme Designer and shipped in the jar, as theme files
     * (resources/themes/), so a fresh install has them. Edit them in the
     * designer and save over the file here.
     */
    private val bundledFiles = listOf("JessPurple", "FormulaBrown", "CheyenneTeal")

    val bundledSchemes: List<ThemeScheme> = bundledFiles.map { name ->
        val path = "/themes/$name.${ThemeFiles.EXTENSION}"
        val text = Themes::class.java.getResource(path)?.readText() ?: error("No $path in the jar")
        ThemeFiles.parse(text).also { require(it.name == name) { "$path is the theme ${it.name}" } }
    }

    val builtInSchemes = listOf(BmwBlue, RevolutionOrange, GooseGreen) + bundledSchemes

    val BmwBlueDoubledPixels = BmwBlue.doubledPixels
    val BmwBlueNormalSize = BmwBlue.normalSize
    val RevolutionOrangeDoubledPixels = RevolutionOrange.doubledPixels
    val RevolutionOrangeNormalSize = RevolutionOrange.normalSize
    val GooseGreenDoubledPixels = GooseGreen.doubledPixels
    val GooseGreenNormalSize = GooseGreen.normalSize

    /**
     * The built-in themes, 2X ones first. ThemeRepository has these with the
     * theme files' themes.
     */
    fun themesOf(schemes: List<ThemeScheme>): List<Theme> =
        schemes.map { it.doubledPixels } + schemes.map { it.normalSize }

    val builtInThemes: List<Theme> = themesOf(builtInSchemes)

    /** A theme from its colours (every [ThemeColorRole]) and the 1X or 2X sizes. */
    fun make(configFileName: String, friendlyName: String, isPixelDoubled: Boolean, colors: Map<ThemeColorRole, Color>): Theme {
        fun get(role: ThemeColorRole) = colors[role] ?: error("$configFileName has no ${role.key}")
        return Theme(
            configFileName = configFileName,
            friendlyName = friendlyName,
            isPixelDoubled = isPixelDoubled,
            windowSize = if (isPixelDoubled) PixelDoubledWindowSize else NormalWindowSize,
            colors = Theme.Colors(
                menuBackground = get(MENU_BACKGROUND),
                textMenuColorAccent = get(TEXT_MENU_COLOR_ACCENT),
                selectedColor = get(SELECTED_COLOR),
                TEXT_WHITE = get(TEXT_WHITE),
                TEXT_BLUE_LIGHT = get(TEXT_BLUE_LIGHT),
                TEXT_BLUE_DARK = get(TEXT_BLUE_DARK),
                TEXT_RED = get(TEXT_RED),
                chipColor = get(CHIP_COLOR),
                chipHighlights = get(CHIP_HIGHLIGHTS),
                sideMenuBorder = get(SIDE_MENU_BORDER),
            ),
            smallItem = if (isPixelDoubled) Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL
                        else Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
            bigItem = if (isPixelDoubled) Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG
                      else Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
            hmiHeaderFooter = Theme.HmiHeaderFooter(
                fontSize = if (isPixelDoubled) Theme.HmiHeaderFooter.PixelDoubledSize else Theme.HmiHeaderFooter.normalSize,
                fontColor = get(HEADER_FONT),
                headerPadding = if (isPixelDoubled) Theme.HmiHeaderFooter.PixelDoubledHeaderPadding
                                else Theme.HmiHeaderFooter.NormalSizedHeaderPadding,
            ),
            centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
                backgroundGradientColorList = listOf(get(GRADIENT_EDGE), get(GRADIENT_CENTER), get(GRADIENT_EDGE)),
                edgeHighlightColor = get(EDGE_HIGHLIGHT),
                edgeHighlightHeight = if (isPixelDoubled) Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
                                      else Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize,
            ),
        )
    }
}

data class Theme(
    val configFileName : String,
    val friendlyName : String,
    val isPixelDoubled : Boolean,
    val windowSize : DpSize,
    val colors: Colors,
    val smallItem: MenuItemMeasurements,
    val bigItem: MenuItemMeasurements,
    val hmiHeaderFooter: HmiHeaderFooter,
    val centerGradientWithEdgeHighlight: CenterGradientWithEdgeHighlight
) {
    data class Colors(
        val menuBackground: Color,

        val textMenuColorAccent: Color = Color(229, 255, 255, 255),

        /** The selection color of the highlighted item */
        val selectedColor: Color,

        val TEXT_WHITE: Color = Color.White,
        val TEXT_BLUE_LIGHT: Color = Color.Blue,
        val TEXT_BLUE_DARK: Color = Color.Blue,
        val TEXT_RED: Color = Color.Red,

        val chipColor : Color,
        val chipHighlights : Color,

        // Usually the center of the edge highlight
        val sideMenuBorder : Color

    )

    data class HmiHeaderFooter(
        val fontSize : TextUnit,
        val fontColor : Color,
        val headerPadding: HeaderPadding
    ) {

        data class HeaderPadding(
            val start : Dp,
            val end : Dp,
            val top : Dp,
            val bottom : Dp
        )

        companion object {
            val PixelDoubledSize = 18.sp
            val normalSize = 9.sp

            val PixelDoubledHeaderPadding = HeaderPadding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp
            )
            val NormalSizedHeaderPadding = HeaderPadding(
                start = 6.dp,
                end = 6.dp,
                top = 4.dp,
                bottom = 4.dp
            )
        }
    }

    data class MenuItemMeasurements(
        val chipWidth: Float,
        val highlightWidth: Float,
        val fontSize: TextUnit
    ) {
        companion object {
            object PixelDoubled {
                // 800*468 window size
                val BIG = MenuItemMeasurements(
                    chipWidth = 16.0F,
                    highlightWidth = 8.0F,
                    fontSize = 30.sp
                )
                val SMALL = MenuItemMeasurements(
                    chipWidth = 12.0F,
                    highlightWidth = 6.0F,
                    fontSize = 24.sp
                )
            }

            object NormalSized {
                //400*234 window size, for RPI
                val BIG = MenuItemMeasurements(
                    chipWidth = 8.0F,
                    highlightWidth = 4.0F,
                    fontSize = 15.sp
                )
                val SMALL = MenuItemMeasurements(
                    chipWidth = 6.0F,
                    highlightWidth = 3.0F,
                    fontSize = 12.sp
                )
            }
        }
    }

    data class CenterGradientWithEdgeHighlight(
        val backgroundGradientColorList: List<Color>,
        val edgeHighlightColor: Color,
        val edgeHighlightHeight: Dp
    ) {
        companion object {
            val edgeHighlightHeightPixelDoubled = 4.dp
            val edgeHighlightHeightNormalSize = 2.dp
        }
    }
}
