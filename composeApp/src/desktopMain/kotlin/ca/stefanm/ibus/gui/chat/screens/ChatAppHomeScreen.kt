package ca.stefanm.ibus.gui.chat.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.MostRecentStorage
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup.LoginScreen
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.gui.chat.screens.chat.CreateRoomScreen
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatRoomScreen
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.chat.RoomInfoScreen
import ca.stefanm.ibus.gui.chat.screens.chat.RoomSelectorScreen
import ca.stefanm.ibus.gui.menu.BMWMainMenu
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import net.folivo.trixnity.client.MatrixClient
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatAppHomeScreen",
    description = "The main menu for the matrix chat client",
    navigatesTo = [
        ScreenDoc.NavigateTo(CreateRoomScreen::class)
    ]
)
@ScreenDoc.AllowsGoRoot
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatAppHomeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val matrixService: MatrixService,
    private val mostRecentStorage: MostRecentStorage,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatAppHomeScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        LaunchedEffect(Unit) {
            if (matrixService.getMatrixClient() == null
                || matrixService.getMatrixClient()?.loginState?.value != MatrixClient.LoginState.LOGGED_IN) {
                navigationNodeTraverser.navigateToNode(LoginScreen::class.java)
            }
        }

        //If there is an incoming result for a room selection, open the room. (and save the result)
        if (params != null && params.resultFrom == RoomSelectorScreen::class.java) {
            when (val selectionResult = params.result as? RoomSelectorScreen.RoomSelectorResult) {
                is RoomSelectorScreen.RoomSelectorResult.DmSelected -> {
                    mostRecentStorage.saveMostRecentDmId(selectionResult.roomId)
                }
                is RoomSelectorScreen.RoomSelectorResult.RoomSelected -> {
                    mostRecentStorage.saveMostRecentRoomId(selectionResult.roomId)
                }
                else -> {}
            }
        }


        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Chat")

            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(
                    TextMenuItem(
                        "Enter most recent room",
                        onClicked = {
                            val mostRecentRoom = mostRecentStorage.loadMostRecentRoomId()
                            if (mostRecentRoom != null) {
                                ChatRoomScreen.openForRoomId(navigationNodeTraverser, mostRecentRoom)
                            } else {
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.MESSAGE_CIRCLE,
                                    "Matrix Chat",
                                    "Cannot load room, please select one"
                                ))

                            }
                        }
                    ),
                    TextMenuItem(
                        "Enter most recent DM",
                        onClicked = {
                            val mostRecentDm = mostRecentStorage.loadMostRecentDmId()
                            if (mostRecentDm != null) {
                                ChatRoomScreen.openForRoomId(navigationNodeTraverser, mostRecentDm)
                            } else {
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.MESSAGE_CIRCLE,
                                    "Matrix Chat",
                                    "Cannot load DM, please select one"
                                ))
                            }
                        }
                    )
                ),
                ne = listOf(
                    TextMenuItem(
                        "View Rooms",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(RoomSelectorScreen::class.java)
                        }
                    ),
                    TextMenuItem(
                        "View Contacts",
                        onClicked = {}
                    )
                ),
                sw = listOf(
                    TextMenuItem(
                        "Settings",
                        onClicked = { navigationNodeTraverser.navigateToNode(ChatSetupMenuRoot::class.java)}
                    ),
                    TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.navigateToRoot() })
                ),
                se = listOf(
                    TextMenuItem(
                        "New Chat",
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "New Room",
                        onClicked = { navigationNodeTraverser.navigateToNode(CreateRoomScreen::class.java)}
                    )
                )
            )
        }
    }
}