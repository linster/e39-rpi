package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes

/**
 * Each colour a theme has. [label]: as the Theme Designer shows it; [key]: the
 * colour's name in a theme file (ThemeFiles).
 */
enum class ThemeColorRole(val label: String, val description: String, val key: String) {
    MENU_BACKGROUND("menuBackground", "Menu background", "menuBackground"),
    TEXT_MENU_COLOR_ACCENT("textMenuColorAccent", "Accent text", "textMenuColorAccent"),
    SELECTED_COLOR("selectedColor", "Highlight on the selected item", "selectedColor"),
    CHIP_COLOR("chipColor", "Menu chips", "chipColor"),
    CHIP_HIGHLIGHTS("chipHighlights", "Menu chip highlights", "chipHighlights"),
    SIDE_MENU_BORDER("sideMenuBorder", "Side pane border", "sideMenuBorder"),
    HEADER_FONT("hmiHeaderFooter.fontColor", "Header and footer text", "headerFontColor"),
    GRADIENT_EDGE("gradient edges", "Header/footer gradient, both ends", "gradientEdge"),
    GRADIENT_CENTER("gradient center", "Header/footer gradient, middle", "gradientCenter"),
    EDGE_HIGHLIGHT("edgeHighlightColor", "Header/footer edge highlight line", "edgeHighlightColor"),
    TEXT_WHITE("TEXT_WHITE", "White text", "textWhite"),
    TEXT_BLUE_LIGHT("TEXT_BLUE_LIGHT", "Light blue text", "textBlueLight"),
    TEXT_BLUE_DARK("TEXT_BLUE_DARK", "Dark blue text", "textBlueDark"),
    TEXT_RED("TEXT_RED", "Red text", "textRed");

    companion object {
        /** [theme]'s colours. */
        fun colorsOf(theme: Theme): Map<ThemeColorRole, Color> {
            val c = theme.colors
            val gradient = theme.centerGradientWithEdgeHighlight
            return mapOf(
                MENU_BACKGROUND to c.menuBackground,
                TEXT_MENU_COLOR_ACCENT to c.textMenuColorAccent,
                SELECTED_COLOR to c.selectedColor,
                CHIP_COLOR to c.chipColor,
                CHIP_HIGHLIGHTS to c.chipHighlights,
                SIDE_MENU_BORDER to c.sideMenuBorder,
                HEADER_FONT to theme.hmiHeaderFooter.fontColor,
                GRADIENT_EDGE to gradient.backgroundGradientColorList.first(),
                GRADIENT_CENTER to gradient.backgroundGradientColorList[gradient.backgroundGradientColorList.size / 2],
                EDGE_HIGHLIGHT to gradient.edgeHighlightColor,
                TEXT_WHITE to c.TEXT_WHITE,
                TEXT_BLUE_LIGHT to c.TEXT_BLUE_LIGHT,
                TEXT_BLUE_DARK to c.TEXT_BLUE_DARK,
                TEXT_RED to c.TEXT_RED,
            )
        }

        /**
         * [base]'s sizes, fonts and padding (1X or 2X) with [colors]; a role
         * missing from [colors] keeps [base]'s colour.
         */
        fun applyTo(base: Theme, colors: Map<ThemeColorRole, Color>, configFileName: String, friendlyName: String): Theme =
            Themes.make(configFileName, friendlyName, base.isPixelDoubled, colorsOf(base) + colors)
    }
}
