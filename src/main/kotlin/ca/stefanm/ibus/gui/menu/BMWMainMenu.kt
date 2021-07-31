package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugScreen2
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

class BMWMainMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = BMWMainMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Menu")


//TextMenuItem(
// label = "On-Board Computer",
//onClicked = { logger.d("MENU","Selected 0")}
//),
//TextMenuItem(
// label = "Telephone",
//onClicked = { logger.d("MENU", "Selected 1")}
//),
//TextMenuItem(
// label = "Code", labelColor = ChipItemColors.TEXT_BLUE_DARK,
//onClicked = { logger.d("MENU", "Selected 2")}
//),
//MenuItem.SPACER,
//TextMenuItem(
// label = "Emergency", labelColor = Color.Red,
//onClicked = { logger.d("MENU", "Selected 3")}
//),
//TextMenuItem(
// label = "Settings",
//onClicked = { logger.d("MENU", "Selected 4")}
//),
////                },
////                contentRight = {
////                    val currentSelected = listener.currentSelectedIndex.value
////
//TextMenuItem(
// label = "GPS-Navigation",
//onClicked = { logger.d("MENU", "Selected 5")}
//),
//TextMenuItem(
// label = "Aux. Ventilation",
//onClicked = { logger.d("MENU", "Selected 6")}
//),
//TextMenuItem(
// label = "Aux. Ventilation",
//onClicked = { logger.d("MENU", "Selected 7")}
//),
//MenuItem.SPACER,
//MenuItem.SPACER,
//TextMenuItem(
// label = "Monitor Off",
//onClicked = { logger.d("MENU", "Selected 9")}
//),

            val leftItems = listOf(
                TextMenuItem(
                    title = "GPS-Navigation",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                MenuItem.SPACER,
                MenuItem.SPACER,
                TextMenuItem(
                    title = "Bluetooth Pairing",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                TextMenuItem(
                    title = "Settings",
                    onClicked = { navigationNodeTraverser.navigateToNode(DebugScreen2::class.java) }
                ),
                TextMenuItem(
                    title = "Debug",
                    labelColor = Color.Red,
                    onClicked = {}
                )
            )

            val rightItems = listOf(
                TextMenuItem(
                    title = "Telephone Dialer",
                    onClicked = {}
                ),
                TextMenuItem(
                    title = "Chat Notifications",
                    onClicked = {}
                ),
                MenuItem.SPACER,
                MenuItem.SPACER,
                MenuItem.SPACER,
                TextMenuItem(
                    title = "Back to BMW",
                    labelColor = Color.Red,
                    onClicked = {}
                )
            )

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromTop(
                    leftItems = leftItems,
                    rightItems = rightItems
                )
            }
        }
    }
}