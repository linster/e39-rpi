package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatMessage
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageAuthor
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageMetadata
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.*
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatRoomScreen",
    description = "Show the chat log for a room",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatRoomScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatRoomScreen::class.java

    companion object {
        const val TAG = "ChatRoomScreen"

        fun openForRoomId(
            navigationNodeTraverser: NavigationNodeTraverser,
            roomId : String
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                ChatRoomScreen::class.java,
                ChatRoomScreenInputParameters(
                    roomId = roomId
                )
            )
        }
    }

    data class ChatRoomScreenInputParameters(
        val roomId : String
    )

    var roomId : String? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { param ->

        roomId = (param?.requestParameters as? ChatRoomScreenInputParameters)?.roomId


        val roomName = remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            //Fetch the Room name for the roomId
            scope.launch {
                delay(1000)
                roomName.value = roomId ?: "null"
            }
        }


        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Room: ${roomName.value}")

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    "Open Message Writer",
                    onClicked = { openMessageWriter() }
                ),
                TextMenuItem(
                    "Open Poll Voter",
                    onClicked = { openPollVoter(
                        ChatMessage.PollMessage(
                            "What day",
                            listOf(
                                ChatMessage.PollMessage.PollItem("Tuesday", 0),
                                ChatMessage.PollMessage.PollItem("Wednesday", 1)),
                            MessageAuthor(),
                            MessageMetadata()
                        )
                    )}
                ),
                TextMenuItem("Room Settings", onClicked = {
                    openRoomSettingsPanel()
                })
            ))
        }
    }


    private suspend fun sendMessageToRoom(textMessage : String) : Boolean {
        return true
    }

    private suspend fun voteInPoll(pollMessage: ChatMessage.PollMessage, itemToVoteFor : ChatMessage.PollMessage.PollItem) {

    }




    var inProgressMessage = ""

    //Need a side-pane for message building
    fun openMessageWriter() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            val scope = rememberCoroutineScope()
            val text = remember { mutableStateOf(inProgressMessage) }
            LaunchedEffect(text) {
                inProgressMessage = text.value
            }
            SidePanelMenu.SidePanelMenu(
                title = "Write Message",
                @Composable {
                    SidePanelMenu.InfoLabel("Contents:")
                    SidePanelMenu.InfoLabel(text.value)
                },
                listOf(
                    TextMenuItem("Clear", onClicked = {
                        text.value = ""
                        inProgressMessage = ""
                    }),
                    TextMenuItem("Edit", onClicked = {
                        modalMenuService.closeSidePaneOverlay(false)
                        modalMenuService.showKeyboard(
                            Keyboard.KeyboardType.FULL,
                            prefilled = text.value,
                            onTextEntered = { newText ->
                                text.value = newText
                                inProgressMessage = newText
                                openMessageWriter()
                            }
                        )
                    }),
                    TextMenuItem("Send", onClicked = {
                        notificationHub.postNotificationBackground(Notification(
                            Notification.NotificationImage.MESSAGE_SQUARE,
                            "Sending Message",
                            text.value
                        ))

                        scope.launch {
                            val sendMessageResult = sendMessageToRoom(text.value)
                            if (sendMessageResult) {
                                //Only clear the message if send was success, so that failure isn't
                                //tedious.
                                inProgressMessage = ""
                            }
                            modalMenuService.closeSidePaneOverlay(true)
                        }
                    }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    })
                )
            )
        }
    }


    // Need a side-pane for voting in polls
    fun openPollVoter(pollMessage: ChatMessage.PollMessage) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            val scope = rememberCoroutineScope()
            SidePanelMenu.SidePanelMenu(
                title = "Vote in Poll",
                @Composable {
                    SidePanelMenu.InfoLabel("Question:")
                    SidePanelMenu.InfoLabel(pollMessage.question)
                },
                pollMessage.pollItems.map {
                    TextMenuItem(
                        "${it.votes}: ${it.title}",
                        onClicked = {
                            scope.launch {
                                voteInPoll(pollMessage, it)
                                modalMenuService.closeSidePaneOverlay(true)
                            }
                        }
                    )
                } +
                TextMenuItem("Go Back", onClicked = {
                    modalMenuService.closeSidePaneOverlay(true)
                })

            )
        }


    }


    fun openRoomSettingsPanel() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Room Settings",
                @Composable {
                    SidePanelMenu.InfoLabel("Room Name: Bob Wat")
                    SidePanelMenu.InfoLabel("Room Id: foo")

                    //TODO room picture, centered, not too big.
                },
                listOf(
                    TextMenuItem("Room Members List", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        roomId?.let {
                            RoomMembersListScreen.openForRoomId(navigationNodeTraverser, it)
                        }
                    }),
                    TextMenuItem("Add person", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        roomId?.let { AddRoomMemberScreen.openForRoom(navigationNodeTraverser, it) }
                    }),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)}),
                )
            )
        }
    }
    //Need a side-pane for Room Outbox (messages not yet sent), so that they can be canceled or retried



}