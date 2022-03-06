package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme


object Themes {

    val availableThemes = listOf(
        "BmwBlueDoubledPixels",
        "BmwBlackDoubledPixels",
        "BmwBlueNormalSize",
        "BmwBlackNormalSize"
    )

    fun String.toTheme(): Theme = when (this) {
        "BmwBlueDoubledPixels" -> BmwBlueDoubledPixels
        "BmwBlackDoubledPixels" -> BmwBlackDoubledPixels
        "BmwBlueNormalSize" -> BmwBlueNormalSize
        "BmwBlackNormalSize" -> BmwBlackNormalSize
//        "BlueYellowDoubledPixels" -> BlueYellowDoubledPixels
//        "BlueYellowNormalSize" -> BlueYellowNormalSize
//        "OrangeDoubledPixels" -> OrangeDoubledPixels
//        "OrangeSinglePixels" -> OrangeSinglePixels
        else -> error("Invalid theme name: $this")
    }

    fun Theme.toName() : String = when(this) {
        BmwBlueDoubledPixels -> "BmwBlueDoubledPixels"
        BmwBlackDoubledPixels -> "BmwBlackDoubledPixels"
        BmwBlueNormalSize -> "BmwBlueNormalSize"
        BmwBlackNormalSize -> "BmwBlackNormalSize"
//        "BlueYellowDoubledPixels" -> BlueYellowDoubledPixels
//        "BlueYellowNormalSize" -> BlueYellowNormalSize
//        "OrangeDoubledPixels" -> OrangeDoubledPixels
//        "OrangeSinglePixels" -> OrangeSinglePixels
        else -> error("Invalid theme: $this")
    }

    val PixelDoubledWindowSize = DpSize(800.dp, 468.dp)
    val NormalWindowSize = DpSize(400.dp, 234.dp)


    val BmwBlueDoubledPixels = Theme(
        colors = Theme.Colors.BmwBlue,
        smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlueColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlue,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
        ),
        windowSize = PixelDoubledWindowSize
    )

    val BmwBlackDoubledPixels = Theme(
        colors = Theme.Colors.BmwBlack,
        smallItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.PixelDoubled.BIG,
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlackColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlack,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightPixelDoubled
        ),
        windowSize = PixelDoubledWindowSize
    )

    val BmwBlueNormalSize = Theme(
        colors = Theme.Colors.BmwBlue,
        smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlueColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlue,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
        ),
        windowSize = NormalWindowSize
    )

    val BmwBlackNormalSize = Theme(
        colors = Theme.Colors.BmwBlack,
        smallItem = Theme.MenuItemMeasurements.Companion.NormalSized.SMALL,
        bigItem = Theme.MenuItemMeasurements.Companion.NormalSized.BIG,
        centerGradientWithEdgeHighlight = Theme.CenterGradientWithEdgeHighlight(
            backgroundGradientColorList = Theme.CenterGradientWithEdgeHighlight.BmwBlackColorList,
            edgeHighlightColor = Theme.CenterGradientWithEdgeHighlight.edgeHighlightColorBmwBlack,
            edgeHighlightHeight = Theme.CenterGradientWithEdgeHighlight.edgeHighlightHeightNormalSize
        ),
        windowSize = NormalWindowSize
    )

//    val BlueYellowDoubledPixels = Theme(
//
//    )
//
//    val BlueYellowNormalSize = Theme(
//
//    )
//
//    val OrangeDoubledPixels = Theme(
//
//    )
//
//    val OrangeSinglePixels = Theme(
//
//    )

}

data class Theme(
    val windowSize : DpSize,
    val colors: Colors,
    val smallItem: MenuItemMeasurements,
    val bigItem: MenuItemMeasurements,
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
        val TEXT_RED: Color = Color.Red
    ) {
        companion object {
            val BmwBlue = Colors(
                menuBackground = Color(48, 72, 107, 255),
                textMenuColorAccent = Color(229, 255, 255, 255),
                selectedColor = Color(240, 189, 176, 255),
            )

            val BmwBlack = Colors(
                menuBackground = Color(148, 172, 107, 255),
                textMenuColorAccent = Color(229, 55, 255, 255),
                selectedColor = Color(240, 189, 176, 255),
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

            val edgeHighlightColorBmwBlue = Color(86, 139, 191, 255)
            val edgeHighlightColorBmwBlack = Color(86, 139, 191, 255)

            val edgeHighlightHeightPixelDoubled = 4.dp
            val edgeHighlightHeightNormalSize = 2.dp
        }
    }
}
