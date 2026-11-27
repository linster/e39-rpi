package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject


//We need a composition local provider for each thing in the theme.

object ThemeWrapper {

    val defaultTheme : Theme = DaggerApplicationComponent.create()
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

            content()
        }
    }
}

/**
 * The chosen theme (theme.conf's themeName: a configFileName from
 * [ThemeRepository], built in or from a theme file), and the Theme Designer's
 * preview. It follows the repository: a changed theme file shows at once.
 */
@ApplicationScope
class ThemeConfigurationStorage @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val themeRepository: ThemeRepository,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
) {

    private object ThemeConfigSpec : ConfigSpec() {
        val themeOverridesWindowSize by optional(true, "Theme setting for windowsize takes precedence over main conf file")
        val themeName by optional(Themes.DEFAULT_THEME_NAME)

        val allowSelectPixelDoubledThemes by optional(true, "Whether the pixel doubled themes are available to select in the UI?")
    }

    private val themeConfigFile = File(ConfigurationStorage.e39BaseFolder, "theme.conf")

    private val themeConfig = Config { addSpec(ThemeConfigSpec) }
        .from.hocon.file(themeConfigFile, optional = true)

    private val currentTheme = MutableStateFlow(resolve(themeConfig[ThemeConfigSpec.themeName]))

    /** The Theme Designer's theme, shown instead of the chosen one while set; never written to theme.conf. */
    private val previewTheme = MutableStateFlow<Theme?>(null)

    init {
        if (!themeConfigFile.exists()) {
            themeConfig.toHocon.toFile(themeConfigFile)
        }
        themeConfig.afterSet { item, value ->
            if (item == ThemeConfigSpec.themeName) {
                val theme = resolve(themeConfig[ThemeConfigSpec.themeName])
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
        // Theme files reloaded: the chosen theme may have new colours (or be gone).
        coroutineScope.launch {
            themeRepository.themes.collect {
                currentTheme.value = resolve(themeConfig[ThemeConfigSpec.themeName])
            }
        }
    }

    /**
     * The theme named [configFileName]; BMW Blue (2X) if there's none (e.g. a
     * theme file removed or broken), without changing the stored choice, so
     * it's back once the file is.
     */
    private fun resolve(configFileName: String): Theme =
        themeRepository.find(configFileName) ?: Themes.BmwBlueDoubledPixels.also {
            logger.w(TAG, "No theme \"$configFileName\"; showing ${it.configFileName}")
        }

    fun isPixelDoubleThemesSelectable() = themeConfig[ThemeConfigSpec.allowSelectPixelDoubledThemes]

    fun setPixelDoubleThemesSelectable(new : Boolean)  {
        themeConfig[ThemeConfigSpec.allowSelectPixelDoubledThemes] = new
    }

    fun setTheme(theme: Theme) {
        themeConfig[ThemeConfigSpec.themeName] = theme.configFileName
    }

    /** The theme to show: the Theme Designer's preview while there is one, else the chosen theme. */
    fun getTheme() : Flow<Theme> {
        return combine(currentTheme, previewTheme) { chosen, preview -> preview ?: chosen }
    }

    /** What [getTheme] gives now. */
    fun shownTheme(): Theme = previewTheme.value ?: currentTheme.value

    /** Show [theme] instead of the chosen one, until set back to null. Not stored. */
    fun setPreviewTheme(theme: Theme?) {
        previewTheme.value = theme
    }

    /** The chosen theme (BMW Blue (2X) if it isn't there). */
    fun getStoredTheme() : Theme = resolve(themeConfig[ThemeConfigSpec.themeName])

    companion object {
        const val TAG = "ThemeConfigurationStorage"
    }
}