package ca.stefanm.ibus.gui.menu.widgets.themes

import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorMath
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import com.typesafe.config.ConfigValueType
import java.io.File

/**
 * A theme file: one [ThemeScheme], in HOCON, e.g. themes/JessPurple.conf:
 *
 * ```
 * name = "JessPurple"
 * friendlyName = "Jess Purple"
 * colors {
 *   menuBackground = "#522964FF"
 *   textMenuColorAccent = "#F5E5FFFF"
 *   ...
 * }
 * ```
 *
 * Colours are "#RRGGBB" or "#RRGGBBAA", named by [ThemeColorRole.key]. One
 * left out is BMW Blue's; menuBackground is required.
 */
object ThemeFiles {

    const val EXTENSION = "conf"

    class BadThemeFile(message: String) : Exception(message)

    /** [text]'s scheme; [BadThemeFile] (saying what's wrong) if it isn't one. */
    fun parse(text: String): ThemeScheme {
        val config = try {
            ConfigFactory.parseString(text)
        } catch (e: ConfigException) {
            throw BadThemeFile("not HOCON: ${e.message}")
        }
        fun string(path: String): String {
            if (!config.hasPath(path)) throw BadThemeFile("no $path")
            val value = config.getValue(path)
            if (value.valueType() != ConfigValueType.STRING) throw BadThemeFile("$path isn't a string")
            return value.unwrapped() as String
        }
        val name = string("name")
        if (!ThemeScheme.NAME.matches(name)) throw BadThemeFile("name \"$name\" isn't letters and digits, starting with a letter")
        val friendlyName = string("friendlyName")
        if (friendlyName.isBlank()) throw BadThemeFile("friendlyName is empty")

        if (!config.hasPath("colors") || config.getValue("colors").valueType() != ConfigValueType.OBJECT) {
            throw BadThemeFile("no colors { }")
        }
        val keys = config.getObject("colors").keys
        val unknown = keys - ThemeColorRole.entries.map { it.key }.toSet()
        if (unknown.isNotEmpty()) {
            throw BadThemeFile("unknown colours: ${unknown.sorted().joinToString()} " +
                    "(known: ${ThemeColorRole.entries.joinToString { it.key }})")
        }
        if (ThemeColorRole.MENU_BACKGROUND.key !in keys) throw BadThemeFile("no colors.${ThemeColorRole.MENU_BACKGROUND.key}")

        val colors = ThemeColorRole.entries.filter { it.key in keys }.associateWith { role ->
            val hex = string("colors.${role.key}")
            ThemeColorMath.parseHex(hex) ?: throw BadThemeFile("colors.${role.key} \"$hex\" isn't #RRGGBB or #RRGGBBAA")
        }
        return ThemeScheme(name, friendlyName, Themes.BmwBlue.colors + colors)
    }

    /** [scheme] as a theme file, every colour written out, in [ThemeColorRole]'s order. */
    fun write(scheme: ThemeScheme): String {
        fun quoted(text: String) = ConfigValueFactory.fromAnyRef(text).render(ConfigRenderOptions.concise())
        return buildString {
            appendLine("# A theme for the E39 HMI: its 1X and 2X themes are ${scheme.name}${Themes.NORMAL_SIZE} and ${scheme.name}${Themes.DOUBLED_PIXELS}.")
            appendLine("# Colours are #RRGGBB or #RRGGBBAA; one left out is BMW Blue's.")
            appendLine("name = ${quoted(scheme.name)}")
            appendLine("friendlyName = ${quoted(scheme.friendlyName)}")
            appendLine("colors {")
            ThemeColorRole.entries.forEach { role ->
                appendLine("  ${role.key} = ${quoted(ThemeColorMath.toHex(scheme.colors.getValue(role)))}")
            }
            appendLine("}")
        }
    }

    /** Where [scheme] is saved in [folder]. */
    fun fileFor(folder: File, scheme: ThemeScheme) = File(folder, "${scheme.name}.$EXTENSION")
}
