package ca.stefanm.ibus.gui.menu.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

//A "Chip" is the little nubbin shown to indicate a scrollable item
//https://cdn.shopify.com/s/files/1/0366/7093/products/21c061cb-1bd7-4183-b72e-4a72a4b211a7_zpshihdw7rj.jpg?v=1571438673

@Composable
fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this
@Composable
fun TextUnit.halveIfNotPixelDoubled() : TextUnit = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).sp else this

@Composable
fun Float.halveIfNotPixelDoubled() : Float = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this / 2F) else this

enum class ItemChipOrientation{
    NONE,
    NW,
    NE,
    E,
    SE,
    SW,
    W,

    N,
    S
}

object ChipItemColors {
    val TEXT_WHITE = Color.White
    val TEXT_BLUE_LIGHT = Color.Blue
    val TEXT_BLUE_DARK = Color.Blue
    val TEXT_RED = Color.Red
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
            fontSize = 30.sp
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
    boxModifier : Modifier = Modifier.fillMaxWidth(),
    label : String = " ",
    labelColor : Color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
    chipOrientation: ItemChipOrientation = ItemChipOrientation.NONE,
    isSelected: Boolean = false,
    isSmallSize : Boolean = false,
    onClicked : () -> Unit
) {

    @Composable
    fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this

    val measurements = if (isSmallSize)
        ThemeWrapper.ThemeHandle.current.smallItem
    else
        ThemeWrapper.ThemeHandle.current.bigItem


    val chipWidth = measurements.chipWidth
    val chipColor = ThemeWrapper.ThemeHandle.current.colors.chipColor
    val chipHighlights = ThemeWrapper.ThemeHandle.current.colors.chipHighlights
    val highlightWidth = measurements.highlightWidth

    Box(modifier = boxModifier) {
        Text(
            text = label,
            color = labelColor,
            fontSize = measurements.fontSize,
            modifier = Modifier
                .clickable { onClicked() }
                .then(
                    when (chipOrientation) { //TODO N, S
                        ItemChipOrientation.N,
                        ItemChipOrientation.NW,
                        ItemChipOrientation.NE -> {
                            Modifier.padding(top = (chipWidth).dp.halveIfNotPixelDoubled(), bottom = highlightWidth.dp.halveIfNotPixelDoubled(), start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.S,
                        ItemChipOrientation.SW,
                        ItemChipOrientation.SE -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), end = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.W -> {
                            Modifier.padding(start = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), bottom = 5.dp.halveIfNotPixelDoubled())
                        }
                        else -> {
                            Modifier.padding(
                                top = 5.dp.halveIfNotPixelDoubled(),
                                bottom = 5.dp.halveIfNotPixelDoubled(),
                                start = 25.dp.halveIfNotPixelDoubled()
                            )
                        }
                    }
                )
        )

        Canvas(modifier = Modifier.matchParentSize(), onDraw = {
            when (chipOrientation) { //TODO N, S
                ItemChipOrientation.NW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.NE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(
                                this.size.width - (chipWidth * 0.5).dp.toPx(),
                                this.size.height - highlightWidth
                            ),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.E -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SW -> {
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.W -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                else -> {}
            }

            if (isSelected) {
                //Selected

                val rectY =
                    if (chipOrientation == ItemChipOrientation.NW || chipOrientation == ItemChipOrientation.NE) {
                        (highlightWidth) + chipWidth.dp.toPx()
                    } else {
                        highlightWidth * 0.5
                    }.toFloat()

                val rectHeight =
                    if (chipOrientation == ItemChipOrientation.SW || chipOrientation == ItemChipOrientation.SE) {
                        this.size.height - rectY - (chipWidth.dp.toPx()) - (highlightWidth)
                    } else {
                        this.size.height - rectY
                    }.toFloat()

                this.drawRect(
                    color = Color.Green,
                    topLeft = Offset(
                        x = 0F,
                        y = rectY
                    ),
                    style = Stroke(width = highlightWidth),
                    size = Size((this.size.width - (highlightWidth * 0.5)).toFloat(), rectHeight)
                )

                this.drawRect(
                    color = Color.Red,
                    topLeft = Offset(
                        x =
                        if (chipOrientation == ItemChipOrientation.E ||
                            chipOrientation == ItemChipOrientation.SE ||
                            chipOrientation == ItemChipOrientation.NE
                        ) {
                            this.size.width - (chipWidth.dp.toPx())
                        } else {
                            0.0F
                        }, y = rectY
                    ),
                    style = Fill,
                    size = Size(
                        chipWidth.dp.toPx(),
                        when (chipOrientation) {
                            ItemChipOrientation.NW,
                            ItemChipOrientation.NE,
                            ItemChipOrientation.SW,
                            ItemChipOrientation.SE -> this.size.height - chipWidth.dp.toPx() - highlightWidth
                            else -> this.size.height
                        }
                    ),
                )
            }
        })

    }
}



@Composable
fun ImageMenuItem(
    boxModifier : Modifier = Modifier.fillMaxWidth(),
    painter: Painter,
    imageModifier: Modifier,
    alignment: Alignment = Alignment.Center,
    tintColor : Color?,
    chipOrientation: ItemChipOrientation = ItemChipOrientation.NONE,
    isSelected: Boolean = false,
    isSmallSize : Boolean = false,
    onClicked : () -> Unit
) {

    @Composable
    fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this

    val measurements = if (isSmallSize)
        ThemeWrapper.ThemeHandle.current.smallItem
    else
        ThemeWrapper.ThemeHandle.current.bigItem


    val chipWidth = measurements.chipWidth
    val chipColor = ThemeWrapper.ThemeHandle.current.colors.chipColor
    val chipHighlights = ThemeWrapper.ThemeHandle.current.colors.chipHighlights
    val highlightWidth = measurements.highlightWidth

    Box(modifier = boxModifier) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = imageModifier
                .clickable { onClicked() }
                .then(
                    when (chipOrientation) { //TODO N, S
                        ItemChipOrientation.N -> {
                            Modifier.padding(top = (chipWidth).dp.halveIfNotPixelDoubled(), bottom = highlightWidth.dp.halveIfNotPixelDoubled())
                        }
                        ItemChipOrientation.NW,
                        ItemChipOrientation.NE -> {
                            Modifier.padding(top = (chipWidth).dp.halveIfNotPixelDoubled(), bottom = highlightWidth.dp.halveIfNotPixelDoubled(), start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.S -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled())
                        }
                        ItemChipOrientation.SW,
                        ItemChipOrientation.SE -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), end = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.W -> {
                            Modifier.padding(start = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), bottom = 5.dp.halveIfNotPixelDoubled())
                        }
                        else -> {
                            Modifier.padding(
                                top = 5.dp.halveIfNotPixelDoubled(),
                                bottom = 5.dp.halveIfNotPixelDoubled(),
                                start = 25.dp.halveIfNotPixelDoubled()
                            )
                        }
                    }
                ),
            alignment = alignment,
            contentScale = ContentScale.Inside,
            colorFilter = tintColor?.let { ColorFilter.tint(it, BlendMode.SrcAtop) }
        )

        Canvas(modifier = Modifier.matchParentSize(), onDraw = {
            when (chipOrientation) {
                ItemChipOrientation.NW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.NE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(
                                this.size.width - (chipWidth * 0.5).dp.toPx(),
                                this.size.height - highlightWidth
                            ),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.E -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SW -> {
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.W -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                else -> {}
            }

            if (isSelected) {
                //Selected

                val rectY =
                    if (chipOrientation == ItemChipOrientation.NW || chipOrientation == ItemChipOrientation.NE) {
                        (highlightWidth) + chipWidth.dp.toPx()
                    } else {
                        highlightWidth * 0.5
                    }.toFloat()

                val rectHeight =
                    if (chipOrientation == ItemChipOrientation.SW || chipOrientation == ItemChipOrientation.SE) {
                        this.size.height - rectY - (chipWidth.dp.toPx()) - (highlightWidth)
                    } else {
                        this.size.height - rectY
                    }.toFloat()

                this.drawRect(
                    color = Color.Green,
                    topLeft = Offset(
                        x = 0F,
                        y = rectY
                    ),
                    style = Stroke(width = highlightWidth),
                    size = Size((this.size.width - (highlightWidth * 0.5)).toFloat(), rectHeight)
                )

                this.drawRect(
                    color = Color.Red,
                    topLeft = Offset(
                        x =
                        if (chipOrientation == ItemChipOrientation.E ||
                            chipOrientation == ItemChipOrientation.SE ||
                            chipOrientation == ItemChipOrientation.NE
                        ) {
                            this.size.width - (chipWidth.dp.toPx())
                        } else {
                            0.0F
                        }, y = rectY
                    ),
                    style = Fill,
                    size = Size(
                        chipWidth.dp.toPx(),
                        when (chipOrientation) {
                            ItemChipOrientation.NW,
                            ItemChipOrientation.NE,
                            ItemChipOrientation.SW,
                            ItemChipOrientation.SE -> this.size.height - chipWidth.dp.toPx() - highlightWidth
                            else -> this.size.height
                        }
                    ),
                )
            }
        })

    }
}

@Composable
fun ArbitraryContentsMenuItem(
    boxModifier : Modifier = Modifier.fillMaxWidth(),
    labelColor : Color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
    chipOrientation: ItemChipOrientation = ItemChipOrientation.NONE,
    isSelected: Boolean = false,
    isSmallSize : Boolean = false,
    onClicked : () -> Unit,
    contents : @Composable () -> Unit
) {

    @Composable
    fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this

    val measurements = if (isSmallSize)
        ThemeWrapper.ThemeHandle.current.smallItem
    else
        ThemeWrapper.ThemeHandle.current.bigItem


    val chipWidth = measurements.chipWidth
    val chipColor = ThemeWrapper.ThemeHandle.current.colors.chipColor
    val chipHighlights = ThemeWrapper.ThemeHandle.current.colors.chipHighlights
    val highlightWidth = measurements.highlightWidth

    Box(modifier = boxModifier) {
        Box(
            modifier = Modifier
                .clickable { onClicked() }
                .then(
                    when (chipOrientation) { //TODO N, S
                        ItemChipOrientation.N,
                        ItemChipOrientation.NW,
                        ItemChipOrientation.NE -> {
                            Modifier.padding(top = (chipWidth).dp.halveIfNotPixelDoubled(), bottom = highlightWidth.dp.halveIfNotPixelDoubled(), start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.S,
                        ItemChipOrientation.SW,
                        ItemChipOrientation.SE -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), end = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.W -> {
                            Modifier.padding(start = (chipWidth * 1.5).dp, top = 5.dp.halveIfNotPixelDoubled(), bottom = 5.dp.halveIfNotPixelDoubled())
                        }
                        else -> {
                            Modifier.padding(
                                top = 5.dp.halveIfNotPixelDoubled(),
                                bottom = 5.dp.halveIfNotPixelDoubled(),
                                start = 25.dp.halveIfNotPixelDoubled()
                            )
                        }
                    }
                )
        ) {
            contents()
        }

        Canvas(modifier = Modifier.matchParentSize(), onDraw = {
            when (chipOrientation) { //TODO N, S
                ItemChipOrientation.NW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.NE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(
                                this.size.width - (chipWidth * 0.5).dp.toPx(),
                                this.size.height - highlightWidth
                            ),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                }
                ItemChipOrientation.E -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.width, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset(this.size.width - (chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (chipWidth.dp.toPx()), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SW -> {
                    if (!isSelected) {
                        this.drawLine(
                            brush = SolidColor(chipColor),
                            start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                            end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height),
                            strokeWidth = chipWidth.dp.toPx()
                        )
                    }
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        end = Offset(this.size.height, this.size.height - (chipWidth * 0.5).dp.toPx()),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.W -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset((chipWidth * 0.5).dp.toPx(), 0.0f),
                        end = Offset((chipWidth * 0.5).dp.toPx(), this.size.height - highlightWidth),
                        strokeWidth = chipWidth.dp.toPx()
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth.dp.toPx(), highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                else -> {}
            }

            if (isSelected) {
                //Selected

                val rectY =
                    if (chipOrientation == ItemChipOrientation.NW || chipOrientation == ItemChipOrientation.NE) {
                        (highlightWidth) + chipWidth.dp.toPx()
                    } else {
                        highlightWidth * 0.5
                    }.toFloat()

                val rectHeight =
                    if (chipOrientation == ItemChipOrientation.SW || chipOrientation == ItemChipOrientation.SE) {
                        this.size.height - rectY - (chipWidth.dp.toPx()) - (highlightWidth)
                    } else {
                        this.size.height - rectY
                    }.toFloat()

                this.drawRect(
                    color = Color.Green,
                    topLeft = Offset(
                        x = 0F,
                        y = rectY
                    ),
                    style = Stroke(width = highlightWidth),
                    size = Size((this.size.width - (highlightWidth * 0.5)).toFloat(), rectHeight)
                )

                this.drawRect(
                    color = Color.Red,
                    topLeft = Offset(
                        x =
                            if (chipOrientation == ItemChipOrientation.E ||
                                chipOrientation == ItemChipOrientation.SE ||
                                chipOrientation == ItemChipOrientation.NE
                            ) {
                                this.size.width - (chipWidth.dp.toPx())
                            } else {
                                0.0F
                            }, y = rectY
                    ),
                    style = Fill,
                    size = Size(
                        chipWidth.dp.toPx(),
                        when (chipOrientation) {
                            ItemChipOrientation.NW,
                            ItemChipOrientation.NE,
                            ItemChipOrientation.SW,
                            ItemChipOrientation.SE -> this.size.height - chipWidth.dp.toPx() - highlightWidth
                            else -> this.size.height
                        }
                    ),
                )
            }
        })

    }
}
