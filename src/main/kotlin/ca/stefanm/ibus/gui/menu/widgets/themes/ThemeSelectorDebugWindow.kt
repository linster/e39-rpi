package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.Themes.toTheme
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IbusInputEventCliPrinter
import javax.inject.Inject
import javax.inject.Provider

class ThemeSelectorDebugWindow @Inject constructor(
    private val themeConfigurationStorage: ThemeConfigurationStorage,
    private val windowManager: Provider<WindowManager>
): WindowManager.E39Window {

    override val title: String = "Theme Selector"
    override val size = DpSize(800.dp, 600.dp)
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        NestingCard() {
            NestingCard {
                NestingCardHeader("HMI Window")
                Button(onClick = {
                    windowManager.get().closeHmiMainWindow()
                }) { Text("Close HMI") }
                Button(onClick = {
                    windowManager.get().openHmiMainWindow()
                }) { Text("Open HMI") }
            }


            NestingCard() {
                NestingCardHeader("Select Theme Preset")
                Themes.availableThemes.forEach { name ->
                    Button(onClick = {
                        themeConfigurationStorage.setTheme(name.toTheme())
                    }) { Text(name) }
                }
            }
        }
    }
}