package ca.stefanm.ibus.gui.map.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.GuidanceConfig.SidePanelInstructionEnablementState
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

/**
 * How turn-by-turn instructions are shown while guiding: as notifications,
 * in a side panel (never, only over the map, or always), both, or neither.
 */
@ScreenDoc(
    screenName = "GuidanceInstructionSettingsScreen",
    description = "Choose how guidance instructions are shown: notifications and/or a side panel.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class GuidanceInstructionSettingsScreen @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "GuidanceInstructionSettingsScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = GuidanceInstructionSettingsScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val config = configurationStorage.config
        val notifications = remember {
            mutableStateOf(config[E39Config.GuidanceConfig.notificationInstructionConsumerIsEnabled])
        }
        val sidePanel = remember {
            mutableStateOf(config[E39Config.GuidanceConfig.sidePanelScreenInstructionConsumerIsEnabled])
        }

        fun mark(on: Boolean, text: String) = (if (on) "\u2713 " else "   ") + text

        val sidePanelOptions = listOf(
            SidePanelInstructionEnablementState.NEVER to "Side panel: never",
            SidePanelInstructionEnablementState.ONLY_OVER_MAP to "Side panel: over the map",
            SidePanelInstructionEnablementState.ALWAYS to "Side panel: always",
        )

        val items = listOf(
            TextMenuItem(mark(notifications.value, "Notifications"), onClicked = {
                notifications.value = !notifications.value
                config[E39Config.GuidanceConfig.notificationInstructionConsumerIsEnabled] = notifications.value
            })
        ) + sidePanelOptions.map { (option, label) ->
            TextMenuItem(mark(sidePanel.value == option, label), onClicked = {
                sidePanel.value = option
                config[E39Config.GuidanceConfig.sidePanelScreenInstructionConsumerIsEnabled] = option
            })
        }

        // The labels change with the choices; SmoothScroll keeps its first items.
        key(notifications.value, sidePanel.value) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Guidance Instructions",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }
}
