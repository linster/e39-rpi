package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import javax.inject.Inject

class DebugHmiRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Debug")

            val isBottomShowing = remember { mutableStateOf(false) }
            val isTopShowing = remember { mutableStateOf(false) }

            val leftItems = listOf(
                MenuItem.TextMenuItem(
                    title = "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                MenuItem.CheckBoxMenuItem(
                    title = "Half Screen HMI Top",
                    isChecked = isTopShowing.value,
                    onClicked = {
                        isTopShowing.value = true
                    }
                ),
                MenuItem.SPACER,
                MenuItem.CheckBoxMenuItem(
                    title = "Half Screen HMI Bottom",
                    isChecked = isBottomShowing.value,
                    onClicked = {
                        isBottomShowing.value = true
                    }
                ),
                MenuItem.TextMenuItem(
                    title = "Open Modal",
                    onClicked = {
                        modalMenuService.showModalMenu(
                            menuTopLeft = IntOffset(300, 200),
                            menuWidth = 400,
                            menuData = ModalMenu(
                                chipOrientation = ItemChipOrientation.E,
                                items = listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = "First",
                                        onClicked = {
                                            notificationHub.postNotificationBackground(
                                                Notification(
                                                    Notification.NotificationImage.ALERT_CIRCLE,
                                                    topText = "FIRST!!"
                                                )
                                            )
                                        }
                                    )
                                )
                            )
                        )
                    }
                )
            )

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromTop(
                    leftItems = leftItems,
                    rightItems = leftItems
                )

                if (isTopShowing.value) {
                    HalfScreenMenu.TopHalfTwoColumn(leftItems, leftItems)
                }

                if (isBottomShowing.value) {
                    HalfScreenMenu.BottomHalfTwoColumn(leftItems, leftItems)
                }
            }
        }
    }
}