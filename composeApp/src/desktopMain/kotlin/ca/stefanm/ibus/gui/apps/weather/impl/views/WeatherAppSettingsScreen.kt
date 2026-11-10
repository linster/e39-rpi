package ca.stefanm.ibus.gui.apps.weather.impl.views

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "WeatherAppSettingsScreen",
    description = "Settings for the weather app"
)
@AutoDiscover
class WeatherAppSettingsScreen @Inject constructor() : NavigationNode<Nothing> {

    companion object {
        const val TAG = "WeatherAppSettingsScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = WeatherAppSettingsScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {
        val context = SmoothScroll.MenuWindowSmoothScrollContext()
        context.OneColumnSmoothScreen(
            header = "Weather App Settings",
            items = listOf(
                //The default forecast the weather app opens to.
                "Select Default Forecast" to {
                    //The map screen needs to be able to let the user to pick an item from
                    // a list of POIs on the map.
                },

                "Add to Multi-City List" to {
                    // Add goes to a screen that also opens a map selector.
                },
                "Delete from Multi-City List" to {
                    // Open the editor in delete mode
                },
                "Re-order Multi-City List" to { navigateToNode(MultiCityListEditorScreen::class.java) },

                // Tell the repo to sync fresh weather
                "Fetch fresh weather" to {},
            )
        )
    }
}