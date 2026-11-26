package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme

/** Each colour a theme has, as the Theme Designer edits it. */
enum class ThemeColorRole(val label: String, val description: String) {
    MENU_BACKGROUND("menuBackground", "Menu background"),
    TEXT_MENU_COLOR_ACCENT("textMenuColorAccent", "Accent text"),
    SELECTED_COLOR("selectedColor", "Highlight on the selected item"),
    CHIP_COLOR("chipColor", "Menu chips"),
    CHIP_HIGHLIGHTS("chipHighlights", "Menu chip highlights"),
    SIDE_MENU_BORDER("sideMenuBorder", "Side pane border"),
    HEADER_FONT("hmiHeaderFooter.fontColor", "Header and footer text"),
    GRADIENT_EDGE("gradient edges", "Header/footer gradient, both ends"),
    GRADIENT_CENTER("gradient center", "Header/footer gradient, middle"),
    EDGE_HIGHLIGHT("edgeHighlightColor", "Header/footer edge highlight line"),
    TEXT_WHITE("TEXT_WHITE", "White text"),
    TEXT_BLUE_LIGHT("TEXT_BLUE_LIGHT", "Light blue text"),
    TEXT_BLUE_DARK("TEXT_BLUE_DARK", "Dark blue text"),
    TEXT_RED("TEXT_RED", "Red text");

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
         * [base] (its sizes, fonts and padding) with [colors]; a role missing
         * from [colors] keeps [base]'s colour.
         */
        fun applyTo(base: Theme, colors: Map<ThemeColorRole, Color>, configFileName: String, friendlyName: String): Theme {
            val all = colorsOf(base) + colors
            fun get(role: ThemeColorRole) = all.getValue(role)
            return base.copy(
                configFileName = configFileName,
                friendlyName = friendlyName,
                colors = base.colors.copy(
                    menuBackground = get(MENU_BACKGROUND),
                    textMenuColorAccent = get(TEXT_MENU_COLOR_ACCENT),
                    selectedColor = get(SELECTED_COLOR),
                    chipColor = get(CHIP_COLOR),
                    chipHighlights = get(CHIP_HIGHLIGHTS),
                    sideMenuBorder = get(SIDE_MENU_BORDER),
                    TEXT_WHITE = get(TEXT_WHITE),
                    TEXT_BLUE_LIGHT = get(TEXT_BLUE_LIGHT),
                    TEXT_BLUE_DARK = get(TEXT_BLUE_DARK),
                    TEXT_RED = get(TEXT_RED),
                ),
                hmiHeaderFooter = base.hmiHeaderFooter.copy(fontColor = get(HEADER_FONT)),
                centerGradientWithEdgeHighlight = base.centerGradientWithEdgeHighlight.copy(
                    backgroundGradientColorList = listOf(get(GRADIENT_EDGE), get(GRADIENT_CENTER), get(GRADIENT_EDGE)),
                    edgeHighlightColor = get(EDGE_HIGHLIGHT),
                ),
            )
        }
    }
}
