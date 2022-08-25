package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class ThemeSelectorScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val themeConfigurationStorage: ThemeConfigurationStorage
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ThemeSelectorScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {


        Column {
            BmwSingleLineHeader("Theme Selector")
            FullScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = {
                        navigationNodeTraverser.goBack()
                    }
                )
            ) + Themes.availableThemes.map {
                TextMenuItem(
                    title = it,
                    onClicked = {
                        themeConfigurationStorage.setTheme(
                            it.toTheme()
                        )
                    }
                )
            }

            )
        }
    }
}