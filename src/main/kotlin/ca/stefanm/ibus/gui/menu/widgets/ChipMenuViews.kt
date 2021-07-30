package ca.stefanm.ibus.gui.menu.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//A "Chip" is the little nubbin shown to indicate a scrollable item
//https://cdn.shopify.com/s/files/1/0366/7093/products/21c061cb-1bd7-4183-b72e-4a72a4b211a7_zpshihdw7rj.jpg?v=1571438673

enum class ItemChipOrientation{
    NONE,
    NW,
    NE,
    E,
    SE,
    SW,
    W
}

object ChipItemColors {
    val TEXT_WHITE = Color.White
    val TEXT_BLUE_LIGHT = Color.Blue
    val TEXT_BLUE_DARK = Color.Blue
    val TEXT_RED = Color.Red

    val MenuBackground = Color(48, 72, 107, 255)

    val SelectedColor = Color(240, 189, 176, 255)
}

@Composable
fun EmptyMenuItem() {
    MenuItem(
        onClicked = {}
    )
}

private data class MenuItemMeasurements(
    val chipWidth : Float,
    val highlightWidth : Float,
    val fontSize : TextUnit
) {
    companion object {
        val BIG = MenuItemMeasurements(
            chipWidth = 16.0F,
            highlightWidth = 8.0F,
            fontSize = 36.sp
        )
        val SMALL = MenuItemMeasurements(
            chipWidth = 12.0F,
            highlightWidth = 6.0F,
            fontSize = 24.sp
        )
    }
}

@Composable
fun MenuItem(
    label : String = " ",
    labelColor : Color = ChipItemColors.TEXT_WHITE,
    chipOrientation: ItemChipOrientation = ItemChipOrientation.NONE,
    isSelected: Boolean = false,
    isSmallSize : Boolean = false,
    onClicked : () -> Unit
) {

    val measurements = if (isSmallSize) MenuItemMeasurements.SMALL else MenuItemMeasurements.BIG

    val chipWidth = measurements.chipWidth
    val chipColor = Color(121, 181, 220, 255)
    val chipHighlights = Color.White
    val highlightWidth = measurements.highlightWidth

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = labelColor,
            fontSize = measurements.fontSize,
            modifier = Modifier
                .clickable { onClicked() }
                .then(
                    when (chipOrientation) {
                        ItemChipOrientation.NW,
                        ItemChipOrientation.NE -> {
                            Modifier.padding(top = (chipWidth * 1.5).dp, bottom = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.SW,
                        ItemChipOrientation.SE -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp, end = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.W -> {
                            Modifier.padding(start = (chipWidth * 1.5).dp, top = 5.dp, bottom = 5.dp)
                        }
                        else -> {
                            Modifier.padding(top = 5.dp, bottom = 5.dp, start = 25.dp)
                        }
                    }
                )
        )

        Canvas(modifier = Modifier.matchParentSize(), onDraw = {
            when (chipOrientation) {
                ItemChipOrientation.NW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, chipWidth),
                        end = Offset(this.size.height, chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                }
                ItemChipOrientation.NE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, chipWidth),
                        end = Offset(this.size.width, chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                }
                ItemChipOrientation.E -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (2 * chipWidth), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, this.size.height - chipWidth),
                        end = Offset(this.size.width, this.size.height - chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (2 * chipWidth), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, this.size.height - chipWidth),
                        end = Offset(this.size.height, this.size.height - chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth * 2, highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.W -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth * 2, highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
            }

            if (isSelected) {
                //Selected

                val rectY =
                    if (chipOrientation == ItemChipOrientation.NW || chipOrientation == ItemChipOrientation.NE) {
                        highlightWidth + (2 * chipWidth)
                    } else {
                        highlightWidth
                    }

                val rectHeight =
                    if (chipOrientation == ItemChipOrientation.SW || chipOrientation == ItemChipOrientation.SE) {
                        this.size.height - rectY - (2 * chipWidth)
                    } else {
                        this.size.height - rectY
                    }

                this.drawRect(
                    color = ChipItemColors.SelectedColor,
                    topLeft = Offset(
                        x = highlightWidth,
                        y = rectY
                    ),
                    style = Stroke(width = highlightWidth),
                    size = Size(this.size.width - highlightWidth, rectHeight)
                )

                this.drawRect(
                    color = ChipItemColors.SelectedColor,
                    topLeft = Offset(
                        x =
                        if (chipOrientation == ItemChipOrientation.E ||
                            chipOrientation == ItemChipOrientation.SE ||
                            chipOrientation == ItemChipOrientation.NE
                        ) {
                            this.size.width - (2 * chipWidth)
                        } else {
                            0.0F
                        }, y = rectY
                    ),
                    style = Fill,
                    size = Size(
                        chipWidth * 2F,
                        if (chipOrientation == ItemChipOrientation.SW || chipOrientation == ItemChipOrientation.SE) {
                            this.size.height - (chipWidth * 2)
                        } else {
                            this.size.height
                        }
                    ),
                )
            }
        })

    }


}

@Composable
fun BmwChipMenu(
    contentLeft : @Composable () -> Unit,
    contentRight : @Composable () -> Unit
) {
    Box (Modifier
        .background(ChipItemColors.MenuBackground)
        .fillMaxWidth()
    ){
        Row(Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(Modifier.weight(0.5f, true)) {
                contentLeft()
            }
            Column(Modifier.weight(0.5f, true)) {
                contentRight()
            }
        }
    }
}