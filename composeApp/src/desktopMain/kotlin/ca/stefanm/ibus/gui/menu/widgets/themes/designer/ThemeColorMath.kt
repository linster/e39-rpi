package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** A colour as hue (0-360), saturation and lightness (0-1), and alpha (0-1). */
data class Hsl(val h: Float, val s: Float, val l: Float, val alpha: Float = 1F) {

    fun toColor(): Color {
        val c = (1F - abs(2F * l - 1F)) * s
        val hp = ((h % 360F) + 360F) % 360F / 60F
        val x = c * (1F - abs(hp % 2F - 1F))
        val (r, g, b) = when {
            hp < 1F -> Triple(c, x, 0F)
            hp < 2F -> Triple(x, c, 0F)
            hp < 3F -> Triple(0F, c, x)
            hp < 4F -> Triple(0F, x, c)
            hp < 5F -> Triple(x, 0F, c)
            else -> Triple(c, 0F, x)
        }
        val m = l - c / 2F
        return Color(
            (r + m).coerceIn(0F, 1F),
            (g + m).coerceIn(0F, 1F),
            (b + m).coerceIn(0F, 1F),
            alpha.coerceIn(0F, 1F)
        )
    }

    companion object {
        fun of(color: Color): Hsl {
            val r = color.red
            val g = color.green
            val b = color.blue
            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            val l = (maxC + minC) / 2F
            val d = maxC - minC
            if (d == 0F) return Hsl(0F, 0F, l, color.alpha)
            val s = d / (1F - abs(2F * l - 1F))
            val h = when (maxC) {
                r -> 60F * (((g - b) / d) % 6F)
                g -> 60F * ((b - r) / d + 2F)
                else -> 60F * ((r - g) / d + 4F)
            }
            return Hsl((h + 360F) % 360F, s.coerceIn(0F, 1F), l, color.alpha)
        }
    }
}

object ThemeColorMath {

    /** "#RRGGBBAA". */
    fun toHex(color: Color): String = "#" + listOf(color.red, color.green, color.blue, color.alpha)
        .joinToString("") { "%02X".format((it * 255F).roundToInt()) }

    /** "#RRGGBB" or "#RRGGBBAA" (the # is optional); null if it isn't one. */
    fun parseHex(text: String): Color? {
        val hex = text.trim().removePrefix("#")
        if (hex.length != 6 && hex.length != 8) return null
        if (!hex.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) return null
        val parts = hex.chunked(2).map { it.toInt(16) }
        return Color(parts[0], parts[1], parts[2], parts.getOrElse(3) { 255 })
    }

    /** WCAG 2 relative luminance. */
    fun luminance(color: Color): Double {
        fun channel(c: Float): Double = if (c <= 0.03928F) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    /** WCAG 2 contrast ratio, 1 to 21 (alpha is ignored). */
    fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    /** WCAG AA for normal text. */
    const val MIN_CONTRAST = 4.5
}
