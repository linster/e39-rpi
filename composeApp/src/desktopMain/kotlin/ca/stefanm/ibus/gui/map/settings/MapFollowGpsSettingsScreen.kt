package ca.stefanm.ibus.gui.map.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.mapScreen.GpsMapCenterProvider
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

/**
 * Whether the map follows the GPS when the HMI starts
 * (MapConfig.followGpsByDefault). Changing it also starts or stops following
 * now.
 */
@ScreenDoc(
    screenName = "MapFollowGpsSettingsScreen",
    description = "Choose whether the map follows the GPS when the HMI starts.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MapFollowGpsSettingsScreen @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val gpsMapCenterProvider: GpsMapCenterProvider,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "MapFollowGpsSettingsScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapFollowGpsSettingsScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val config = configurationStorage.config
        val follow = remember { mutableStateOf(config[E39Config.MapConfig.followGpsByDefault]) }

        fun mark(on: Boolean, text: String) = (if (on) "✓ " else "   ") + text

        val items = listOf(
            TextMenuItem(mark(follow.value, "Follow GPS by default"), onClicked = {
                follow.value = !follow.value
                config[E39Config.MapConfig.followGpsByDefault] = follow.value
                // Without a fix yet, following starts at the next startup.
                gpsMapCenterProvider.followGps(follow.value)
            })
        )

        // The label changes with the choice; SmoothScroll keeps its first items.
        key(follow.value) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Follow GPS",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }
}
