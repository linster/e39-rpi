package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class ThemeSelectorScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val themeConfigurationStorage: ThemeConfigurationStorage
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "ThemeSelectorScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ThemeSelectorScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val context = SmoothScroll.MenuWindowSmoothScrollContext(tag = TAG)

        with(context) {
            OneColumnSmoothScreen(
                header = "Theme Selector",
                prependGoBackEntry = true,
                items = Themes.availableThemes.filter {
                    if (themeConfigurationStorage.isPixelDoubleThemesSelectable()) {
                        true
                    } else {
                        !it.isPixelDoubled
                    }
                }.map {
                    TextMenuItem(
                        title = it.friendlyName,
                        onClicked = {
                            themeConfigurationStorage.setTheme(it)
                        }
                    )
                }
            )
        }
    }
}