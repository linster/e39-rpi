package ca.stefanm.ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.debug.hmiScreens.SmoothScrollTest
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.*
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.SPACER
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@ApplicationScope
@Stable
@AutoDiscover
class DebugHmiRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column {
            BmwSingleLineHeader("Debug")

            HalfScreenMenu.OneColumn(
                alignment = Alignment.Start,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {navigationNodeTraverser.goBack()}
                    ),
                    TextMenuItem(
                        title = "DebugHmiMenuTests",
                        onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiMenuTest::class.java) }
                    ),
                    TextMenuItem(
                        title = "Smooth Scroll test",
                        onClicked = { navigationNodeTraverser.navigateToNode(SmoothScrollTest::class.java)}
                    )
//                    TextMenuItem(
//                        title = "DBus-BlueZ",
//                        onClicked = { }
//                    )
                )
            )
        }

    }
}