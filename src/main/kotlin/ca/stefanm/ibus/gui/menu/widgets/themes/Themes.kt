package ca.stefanm.ibus.gui.menu.widgets.themes


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme


object Themes {

    const val DEFAULT_THEME_NAME = "BMW Blue DoubledPixels"
    
    fun String.toTheme(): Theme = availableThemes
        .firstOrNull { it.configFileName == this } ?: error("Invalid theme name: $this")

    val PixelDoubledWindowSize = DpSize(800.dp, 468.dp)
    val NormalWindowSize = DpSize(400.dp, 234.dp)


    val BmwBlueDoubledPixels = Theme(
        configFileName = "BmwBlueDoubledPixels",
        friendlyName = "BMW Blue (2X)",
        isPixelDoubled = true,
        colors = Theme.Colors.BmwBlue,
        smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.PixelDoubledSize,
            fontColor = Theme.HmiHeaderFooter.bmwBlueColor,
            headerPadding = Theme.HmiHeaderFooter.PixelDoubledHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlueColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlue,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
        ),
        windowSize = PixelDoubledWindowSize
    )

    val BmwBlackDoubledPixels = Theme(
        configFileName = "BmwBlackDoubledPixels",
        friendlyName = "BMW Black (2X)",
        isPixelDoubled = true,
        colors = Theme.Colors.BmwBlack,
        smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.PixelDoubledSize,
            fontColor = Theme.HmiHeaderFooter.bmwBlackColor,
            headerPadding = Theme.HmiHeaderFooter.PixelDoubledHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlackColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlack,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
        ),
        windowSize = PixelDoubledWindowSize
    )

    val BmwBlueNormalSize = Theme(
        configFileName = "BmwBlueNormalSize",
        friendlyName = "BMW Blue",
        isPixelDoubled = false,
        colors = Theme.Colors.BmwBlue,
        smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.normalSize,
            fontColor = Theme.HmiHeaderFooter.bmwBlueColor,
            headerPadding = Theme.HmiHeaderFooter.NormalSizedHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlueColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlue,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
        ),
        windowSize = NormalWindowSize
    )

    val BmwBlackNormalSize = Theme(
        configFileName = "BmwBlackNormalSize",
        friendlyName = "BMW Black",
        isPixelDoubled = false,
        colors = Theme.Colors.BmwBlack,
        smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.normalSize,
            fontColor = Theme.HmiHeaderFooter.bmwBlackColor,
            headerPadding = Theme.HmiHeaderFooter.NormalSizedHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlackColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlack,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
        ),
        windowSize = NormalWindowSize
    )

    val RevolutionOrangeNormalSize = Theme(
        configFileName = "RevolutionOrangeNormalSize",
        friendlyName = "Revolution Orange",
        isPixelDoubled = false,
        colors = Theme.Colors.RevolutionOrange,
        smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.normalSize,
            fontColor = Theme.HmiHeaderFooter.revolutionOrangeColor,
            headerPadding = Theme.HmiHeaderFooter.NormalSizedHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.RevolutionOrangeColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorRevolutionOrange,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
        ),
        windowSize = NormalWindowSize
    )

    val RevolutionOrangeDoubledPixels = Theme(
        configFileName = "RevolutionOrangeDoubledPixels",
        friendlyName = "Revolution Orange (2X)",
        isPixelDoubled = true,
        colors = Theme.Colors.RevolutionOrange,
        smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
        hmiHeaderFooter = Theme.HmiHeaderFooter(
            fontSize = Theme.HmiHeaderFooter.PixelDoubledSize,
            fontColor = Theme.HmiHeaderFooter.revolutionOrangeColor,
            headerPadding = Theme.HmiHeaderFooter.PixelDoubledHeaderPadding
        ),
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.RevolutionOrangeColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorRevolutionOrange,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
        ),
        windowSize = PixelDoubledWindowSize
    )

    val GooseGreenNormalSize = Theme(
            configFileName = "GooseGreenNormalSize",
            friendlyName = "Goose Green",
            isPixelDoubled = false,
            colors = Theme.Colors.GooseGreen,
            smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
            bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
            hmiHeaderFooter = Theme.HmiHeaderFooter(
                fontSize = Theme.HmiHeaderFooter.normalSize,
                fontColor = Theme.HmiHeaderFooter.gooseGreenColor,
                headerPadding = Theme.HmiHeaderFooter.NormalSizedHeaderPadding
            ),
            centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
                backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.GooseGreenColorList,
                edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorGooseGreen,
                edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
            ),
            windowSize = NormalWindowSize
        )

        val GooseGreenDoubledPixels = Theme(
            configFileName = "GooseGreenDoubledPixels",
            friendlyName = "Goose Green (2X)",
            isPixelDoubled = true,
            colors = Theme.Colors.GooseGreen,
            smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
            bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
            hmiHeaderFooter = Theme.HmiHeaderFooter(
                fontSize = Theme.HmiHeaderFooter.PixelDoubledSize,
                fontColor = Theme.HmiHeaderFooter.gooseGreenColor,
                headerPadding = Theme.HmiHeaderFooter.PixelDoubledHeaderPadding
            ),
            centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
                backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.GooseGreenColorList,
                edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlack,
                edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
            ),
            windowSize = PixelDoubledWindowSize
        )


    val availableThemes = listOf(
        BmwBlueDoubledPixels,
        BmwBlackDoubledPixels,
        RevolutionOrangeDoubledPixels,
        GooseGreenDoubledPixels,
        BmwBlueNormalSize,
        BmwBlackNormalSize,
        RevolutionOrangeNormalSize,
        GooseGreenNormalSize
    )
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

    ) {
        companion object {
            val BmwBlue = Colors(
                menuBackground = Color(48, 72, 107, 255),
                textMenuColorAccent = Color(229, 255, 255, 255),
                selectedColor = Color(240, 189, 176, 255),
                chipColor = Color(121, 181, 220, 255),
                chipHighlights = Color.White
            )

            val BmwBlack = Colors(
                menuBackground = Color(148, 172, 107, 255),
                textMenuColorAccent = Color(229, 55, 255, 255),
                selectedColor = Color(240, 189, 176, 255),
                chipColor = Color(121, 181, 220, 255),
                chipHighlights = Color.White
            )

            val RevolutionOrange = Colors(
                menuBackground = Color(173, 101, 0, 255),
                textMenuColorAccent = Color(255, 255, 229, 255),
                selectedColor = Color(240, 189, 176, 255),
                chipColor = Color(121, 181, 220, 255),
                chipHighlights = Color.White
            )

            val GooseGreen = Colors(
                menuBackground = Color(48, 72, 107, 255),
                textMenuColorAccent = Color(229, 255, 255, 255),
                selectedColor = Color(240, 189, 176, 255),
                chipColor = Color(121, 181, 220, 255),
                chipHighlights = Color.White
            )
        }
    }

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
            val bmwBlueColor = Color(229, 255, 255, 255)
            val bmwBlackColor = Color(229, 255, 255, 255)
            val revolutionOrangeColor = Color(229, 255, 255, 255)
            val gooseGreenColor = Color(229, 255, 255, 255)


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

            val BmwBlueColorList = listOf(
                Color(68, 128, 192, 255),
                Color(61, 112, 176, 255),
                Color(68, 128, 192, 255)
            )
            val BmwBlackColorList = listOf(
                Color(68, 128, 192, 255),
                Color(61, 112, 176, 255),
                Color(68, 128, 192, 255)
            )

            val RevolutionOrangeColorList = listOf(
                Color(249, 148, 11, 255),
                Color(233, 137, 6, 255),
                Color(249, 148, 11, 255)
            )
            val GooseGreenColorList = listOf(
                Color(68, 128, 192, 255),
                Color(61, 112, 176, 255),
                Color(68, 128, 192, 255)
            )

            val edgeHighlightColorBmwBlue = Color(86, 139, 191, 255)
            val edgeHighlightColorBmwBlack = Color(86, 139, 191, 255)
            val edgeHighlightColorRevolutionOrange = Color(177, 126, 67, 255)
            val edgeHighlightColorGooseGreen = Color(162, 126, 64, 255)

            val edgeHighlightHeightPixelDoubled = 4.dp
            val edgeHighlightHeightNormalSize = 2.dp
        }
    }
}
