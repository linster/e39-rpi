package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.pim.calendar.CalendarScreen
import ca.stefanm.ibus.car.bordmonitor.screenControl.ScreenPowerWriter
import ca.stefanm.ibus.gui.audio.NowPlayingMenu
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.generalSettings.SettingsRootMenu
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.lib.hardwareDrivers.pico.PicoScreenStatusManager
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScreenDoc(
    screenName = "BMWMainMenu",
    description = "The main menu of the HMI",
    navigatesTo = [
        ScreenDoc.NavigateTo(MapScreen::class),
        ScreenDoc.NavigateTo(SettingsRootMenu::class)
    ]
)
@ScreenDoc.AllowsGoRoot
@AutoDiscover
class BMWMainMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val picoScreenStatusManager: PicoScreenStatusManager,
    private val screenPowerWriter: ScreenPowerWriter
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = BMWMainMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Main Menu")

            val nwItems = listOf(
                TextMenuItem(
                    title = "GPS-Navigation",
                    onClicked = { navigationNodeTraverser.navigateToNode(MapScreen::class.java) }
                ),
                TextMenuItem(
                    title = "Matrix Chat",
                    onClicked = { navigationNodeTraverser.navigateToNode(ChatAppHomeScreen::class.java)}
                ),
                TextMenuItem(
                    title = "Calendar",
                    onClicked = { navigationNodeTraverser.navigateToNode(CalendarScreen::class.java)}
                )
            )
            val neItems = listOf<MenuItem>(
                TextMenuItem(
                    title = "Now Playing",
                    onClicked = {
                        navigationNodeTraverser.navigateToNode(
                            NowPlayingMenu::class.java
                        )
                    }
                ),
//                TextMenuItem(
//                    title = "Chat Notifications",
//                    onClicked = {}
//                ),
            )

            val swItems = listOf(
//                TextMenuItem(
//                    title = "Bluetooth Pairing",
//                    onClicked = { navigationNodeTraverser.navigateToNode(BluetoothPairingMenu::class.java) }
//                ),
                TextMenuItem(
                    title = "Settings",
                    onClicked = { navigationNodeTraverser.navigateToNode(SettingsRootMenu::class.java) }
                ),
//                TextMenuItem(
//                    title = "Debug",
//                    labelColor = Color.Red,
//                    onClicked = {}
//                )
            )
            val scope = rememberCoroutineScope()
            val seItems = listOf(
                TextMenuItem(
                    title = "Screen Off",
                    onClicked = {
                        scope.launch {
                            screenPowerWriter.turnScreenOff()
                        }
                    }
                ),
                TextMenuItem(
                    title = "Back to BMW",
                    onClicked = {
                        scope.launch {
                            picoScreenStatusManager.goBackToBmw()
                        }
                    }
                )
            )

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromCorners(
                    nw = nwItems,
                    ne = neItems,
                    sw = swItems,
                    se = seItems
                )
            }
        }
    }
}