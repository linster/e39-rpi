package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toName
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject


//We need a composition local provider for each thing in the theme.

object ThemeWrapper {

    val defaultTheme = DaggerApplicationComponent.create()
        .themeConfigurationStorage()
        .getStoredTheme()

    val ThemeHandle = compositionLocalOf { defaultTheme }

    @Composable
    fun ThemedUiWrapper(theme: Theme, content : @Composable () -> Unit) {
        CompositionLocalProvider(
            ThemeHandle provides theme
        ) {

            LaunchedEffect(Unit) {
                val themeStorage = DaggerApplicationComponent.create().themeConfigurationStorage()
                themeStorage.setTheme(themeStorage.getStoredTheme())
            }

            val isPixelDoubled = ThemeHandle.current.isPixelDoubled
//            ProvideTextStyle(
//                    TextStyle(fontSize = if (isPixelDoubled) 16.sp else 8.sp)
//            ) {
//                content()
//            }

            content()
        }
    }
}

//TODO We need a way to serialize a theme, and deserialize to Konf
//TODO for now, just store some preset themes instead of all the params
@ApplicationScope
class ThemeConfigurationStorage @Inject constructor(
    private val configurationStorage: ConfigurationStorage
) {

    private object ThemeConfigSpec : ConfigSpec() {
        val themeOverridesWindowSize by optional(true, "Theme setting for windowsize takes precedence over main conf file")
        val themeName by optional("BmwBlueDoubledPixels")
    }

    private val themeConfigFile = File(ConfigurationStorage.e39BaseFolder, "theme.conf")

    private val themeConfig = Config { addSpec(ThemeConfigSpec) }
        .from.hocon.file(themeConfigFile, optional = true)

    private val currentTheme = MutableStateFlow(ThemeWrapper.defaultTheme)

    init {
        if (!themeConfigFile.exists()) {
            themeConfig.toHocon.toFile(themeConfigFile)
        }
        currentTheme.value = themeConfig[ThemeConfigSpec.themeName].toTheme()
        themeConfig.afterSet { item, value ->
            if (item == ThemeConfigSpec.themeName) {
                val theme = themeConfig[ThemeConfigSpec.themeName].toTheme()
                currentTheme.value = theme
                if (themeConfig[ThemeConfigSpec.themeOverridesWindowSize]) {
                    configurationStorage.config[E39Config.WindowManagerConfig.hmiWindowSize] =
                        Pair(
                            theme.windowSize.width.value.toInt(),
                            theme.windowSize.height.value.toInt()
                        )
                }
            }

            themeConfig.toHocon.toFile(themeConfigFile)
        }
    }

    fun setTheme(theme: Theme) {
        themeConfig[ThemeConfigSpec.themeName] = theme.toName()
    }

    fun getTheme() : Flow<Theme> {
        return currentTheme
    }

    fun getStoredTheme() : Theme{
        return themeConfig[ThemeConfigSpec.themeName].toTheme()
    }
}