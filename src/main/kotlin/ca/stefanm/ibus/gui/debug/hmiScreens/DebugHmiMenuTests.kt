package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.*
import javax.inject.Inject

@AutoDiscover
class DebugHmiMenuTest @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiMenuTest::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("DebugHmiMenuTests")

            HalfScreenMenu.OneColumn(
                alignment = Alignment.Start,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    ),
                    TextMenuItem(
                        title = "Two Column",
                        onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiMenuTestTwoColumn::class.java) }
                    ),
                    TextMenuItem(
                        title = "Keyboard Input Test",
                        onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiKeyboard::class.java) }
                    ),
                    TextMenuItem(
                        title = "Options Prompt Test",
                        onClicked = { navigationNodeTraverser.navigateToNode(OptionPromptTest::class.java)}
                    )
                )
            )
        }
    }
}

@AutoDiscover
class DebugHmiMenuTestTwoColumn @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiMenuTestTwoColumn::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Debug -> MenuTest -> DebugHmiMenuTestTwoColumn")

            val isBottomShowing = remember { mutableStateOf(false) }
            val isTopShowing = remember { mutableStateOf(true) }

            val leftItems = listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                TextMenuItem(
                    title = "One Column",
                    onClicked = { navigationNodeTraverser.navigateToNode(DebugHmiMenuTestOneColumn::class.java) }
                ),
                MenuItem.SPACER,
                TextMenuItem(
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
                                    ),
                                    ModalMenu.ModalMenuItem(
                                        title = "Second",
                                        onClicked = { modalMenuService.closeModalMenu() }
                                    )
                                )
                            )
                        )
                    }
                ),
                TextMenuItem(
                    title = "Modal 2",
                    onClicked = {
                        modalMenuService.showModalMenu(
                            menuTopLeft = IntOffset(800, 200),
                            menuWidth = 400,
                            menuData = ModalMenu(
                                chipOrientation = ItemChipOrientation.E,
                                items = listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = "Third",
                                        onClicked = {
                                            notificationHub.postNotificationBackground(
                                                Notification(
                                                    Notification.NotificationImage.ALERT_CIRCLE,
                                                    topText = "FIRST!!"
                                                )
                                            )
                                        }
                                    ),
                                    ModalMenu.ModalMenuItem(
                                        title = "Fourth",
                                        onClicked = { modalMenuService.closeModalMenu() }
                                    )
                                )
                            )
                        )
                    }
                )
            )

            val rightItems = (1..4).map {
                TextMenuItem(
                    title = "Item: $it",
                    isSelectable = it != 2,
                    onClicked = {}
                )
            }

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromTop(
                    leftItems = leftItems,
                    rightItems = rightItems
                )
            }
        }
    }
}

@AutoDiscover
class DebugHmiMenuTestOneColumn @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiMenuTestOneColumn::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {

            BmwSingleLineHeader("... -> DebugHmiMenuTestTwoColumn -> DebugHmiMenuTestOneColumn")

            HalfScreenMenu.OneColumn(
                items = mutableListOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    ),
                    TextMenuItem(
                        title = "2",
                        onClicked = {
                            notificationHub.postNotificationBackground(
                                Notification(
                                    image = Notification.NotificationImage.ALERT_OCTAGON,
                                    topText = "2!"
                                )
                            )
                        }
                    ),
                    TextMenuItem(
                        title = "3",
                        onClicked = {
                            modalMenuService.showModalMenu(
                                menuTopLeft = IntOffset(500, 300),
                                menuWidth = 500,
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.E,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "1",
                                            onClicked = {
                                                notificationHub.postNotificationBackground(
                                                    Notification(
                                                        Notification.NotificationImage.ALERT_TRIANGLE,
                                                        topText = "1"
                                                    )
                                                )
                                            }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "2",
                                            onClicked = {
                                                notificationHub.postNotificationBackground(
                                                    Notification(
                                                        Notification.NotificationImage.ALERT_TRIANGLE,
                                                        topText = "2"
                                                    )
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        }
                    ),
                    TextMenuItem(title = "4", onClicked = {}),
                    TextMenuItem(title = "5", onClicked = {}),
                    TextMenuItem(title = "6", onClicked = {}),
                    TextMenuItem(title = "7", onClicked = {}),
                ),
                alignment = Alignment.Start
            )
        }
    }
}

@AutoDiscover
class OptionPromptTest @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Boolean> {

    override val thisClass: Class<out NavigationNode<Boolean>>
        get() = OptionPromptTest::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        FullScreenPrompts.OptionPrompt(
            header = "Pair with Device?",
            options = FullScreenPrompts.YesNoOptions(
                onYesSelected = { navigationNodeTraverser.setResultAndGoBack(this, true)},
                onNoSelected = { navigationNodeTraverser.setResultAndGoBack(this, false)}
            )
        ) {
            Column(
                Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            ) {
                Text("Do you want to pair with this device?", color = Color.White, fontSize = 28.sp)
                Text("Name: Pixel 4a", color = Color.White, fontSize = 28.sp)

                Text("123456", color = Color.White, fontSize = 64.sp, textAlign = TextAlign.Center)
            }
        }

    }
}
