package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerSettingsScreen
import ca.stefanm.ibus.gui.generalSettings.GriffinPowermateConfigScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.generalSettings.BrightnessCompensationScreen
import ca.stefanm.ibus.gui.map.settings.MapSettingsMainScreen
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeSelectorScreen
import ca.stefanm.ibus.gui.networkInfo.NetworkInfoScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.map.settings.MapTileDownloaderScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "SettingsRootMenu",
    description = "The Root menu for visible settings",
    navigatesTo = [
        ScreenDoc.NavigateTo(BluetoothPairingMenu::class),
        ScreenDoc.NavigateTo(MapSettingsMainScreen::class),
        ScreenDoc.NavigateTo(CarPlatformConfigScreen::class),
        ScreenDoc.NavigateTo(ThemeSelectorScreen::class),
        ScreenDoc.NavigateTo(NetworkInfoScreen::class),
        ScreenDoc.NavigateTo(BrightnessCompensationScreen::class),
        ScreenDoc.NavigateTo(AboutScreen::class),

    ]
)
@ScreenDoc.AllowsGoRoot

@AutoDiscover
class SettingsRootMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    private val logger: Logger

) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SettingsRootMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        FullScreenMenu.OneColumnSmoothScreen(
            header = "Settings",
            knobListenerService = knobListenerServiceMain,
            logger = logger,
            navigationNodeTraverser = navigationNodeTraverser,
            logTag = "SettingsRootMenu",
            prependGoBackEntry = true,
            items = listOf(
                "Bluetooth"     to  { navigateToNode(BluetoothPairingMenu::class.java) },
                "Map Settings"  to { navigateToNode(MapSettingsMainScreen::class.java) },
                "Car Platform Config" to  { navigateToNode(CarPlatformConfigScreen::class.java) },
                "Theme"         to  { navigateToNode(ThemeSelectorScreen::class.java) },
                "Network Info"  to  { navigateToNode(NetworkInfoScreen::class.java) },
                "Brightness"    to  { navigateToNode(BrightnessCompensationScreen::class.java) },
                "Griffin Powermate Sensitivity" to  { navigateToNode(GriffinPowermateConfigScreen::class.java) },
                "File Manager Settings" to { navigateToNode(FileManagerSettingsScreen::class.java) },
                "About" to  { navigateToNode(AboutScreen::class.java) }
            )
        )
    }
}