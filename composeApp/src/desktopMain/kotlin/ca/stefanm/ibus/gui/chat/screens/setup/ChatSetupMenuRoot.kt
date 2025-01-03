package ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.gui.chat.screens.chat.CreateRoomScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatSetupMenuRoot",
    description = "The main menu for the matrix chat client settings",
    navigatesTo = [
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatSetupMenuRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val matrixService: MatrixService,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatSetupMenuRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Chat Settings")

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                TextMenuItem("Server Credentials", onClicked = {}),
                TextMenuItem("Matrix Service", onClicked = {
                    // Open a side-pane to start/stop
                }),
                TextMenuItem("Notifications", onClicked = {}),
                TextMenuItem("Logout", onClicked = { scope.launch {
                    matrixService.logout()
                    notificationHub.postNotification(Notification(
                        Notification.NotificationImage.MESSAGE_CIRCLE,
                        "Matrix Service",
                        "Logged out"
                    ))
                } }),
                TextMenuItem("Clear Local Database", onClicked = {
                    scope.launch {
                        matrixService.clearCache()
                        notificationHub.postNotification(Notification(
                            Notification.NotificationImage.MESSAGE_CIRCLE,
                            "Matrix Service",
                            "Cleared cache"
                        ))
                    }
                }),
                TextMenuItem("Clear Local Media", onClicked = {
                    scope.launch {
                        matrixService.clearMediaCache()
                        notificationHub.postNotification(Notification(
                            Notification.NotificationImage.MESSAGE_CIRCLE,
                            "Matrix Service",
                            "Cleared media cache"
                        ))
                    }
                })
            ))
        }
    }
}