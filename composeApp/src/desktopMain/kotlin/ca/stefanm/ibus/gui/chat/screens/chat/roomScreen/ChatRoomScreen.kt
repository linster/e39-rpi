package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ImageMessageView
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageFetcher
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.TextMessageView
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatMessage
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageAuthor
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageMetadata
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.room.toFlowList
import net.folivo.trixnity.client.roomEventEncryptionServices
import net.folivo.trixnity.client.store.eventId
import net.folivo.trixnity.client.store.isEncrypted
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.*
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatRoomScreen",
    description = "Show the chat log for a room",
    navigatesTo = [
        ScreenDoc.NavigateTo(RoomUploadsList::class),
        ScreenDoc.NavigateTo(RoomMembersListScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatRoomScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService,
    private val matrixService: MatrixService,
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatRoomScreen::class.java

    companion object {
        const val TAG = "ChatRoomScreen"

        fun openForRoomId(
            navigationNodeTraverser: NavigationNodeTraverser,
            roomId : RoomId
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
        val roomId : RoomId
    )

    var roomId : RoomId? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { param ->

        roomId = (param?.requestParameters as? ChatRoomScreenInputParameters)?.roomId


        val roomName = remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            //Fetch the Room name for the roomId
            scope.launch {
                delay(1000)
                roomName.value = roomId?.full ?: "null"
            }
        }

        // Load all the members


        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Room: ${roomName.value}")


//            DebugMenu()
            ChatClientScreenHolder(roomId)
        }
    }

    @Composable
    private fun DebugMenu() {
        FullScreenMenu.OneColumn(listOf(
            TextMenuItem(
                "Open Message Writer",
                onClicked = { openMessageWriter() }
            ),
//            TextMenuItem(
//                "Open Poll Voter",
//                onClicked = { openPollVoter(
//                    ChatMessage.PollMessage(
//                        "What day",
//                        listOf(
//                            ChatMessage.PollMessage.PollItem("Tuesday", 0),
//                            ChatMessage.PollMessage.PollItem("Wednesday", 1)),
//                        MessageAuthor(),
//                        MessageMetadata(time = Clock.System.now())
//                    )
//                )}
//            ),
            TextMenuItem("Room Settings", onClicked = {
                openRoomSettingsPanel()
            }),
            TextMenuItem("Open Popup Menu", onClicked = {
                openPopupMenu()
            })
        ))
    }



    private var roomScrollMode = MutableStateFlow(RoomScrollMode.None)

    enum class RoomScrollMode {
        /** Ignore all knob inputs */
        None,
        /** Modal menu is visible */
        ModalMenu,
        /** Rotation moves the virtual scroll bar */
        Scroll,
        /** Rotation goes through each message, does not move the virtual scroll bar */
        ScrollSelect,
        /** Just always keep the scroll bar pinned to the max / bottom */
        AutoScrollToBottom
    }

    //TODO a method to take the matrix messages and convert to our viewable data types.
    @Composable
    fun ChatClientScreenHolder(roomId : RoomId?) {

        val messages : SnapshotStateList<ChatMessage>  = remember { mutableStateListOf() }

        LaunchedEffect(Unit, roomId) {

//            launch {
//                roomId?.let { room ->
//                    matrixService.getMatrixClient()?.let { matrixClient ->
//                        matrixClient.room.getLastTimelineEvents(room) {
//
//                        }.filterNotNull()
//                            .flatMapMerge { it }
//                            .flatMapMerge { it }
//                            .collect {
//                            logger.w("WAT", "IT ${it.content?.getOrNull()} ${it.eventId}")
//                        }
//                    }
//                }
//            }

            launch {
                roomId?.let { room ->
                    matrixService.getMatrixClient()?.let { matrixClient ->
                        MessageFetcher(roomId, matrixClient, logger) {
                            //Put into a data structure so compose can read it
                            messages.clear()
                            messages.addAll(it)
                        }
                    }
                }
            }
        }



        if (messages.isNotEmpty()) {
            ChatClientScreen(messages)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    fun ChatClientScreen(
        messages : List<ChatMessage>
    ) {


        val selectedIndex = remember { mutableStateOf(messages.size - 1) }
        val selectableIndices = remember { messages.indices.toList().circular() }

        val stateVertical = rememberScrollState(0)


        val roomScrollModeFlow = roomScrollMode.collectAsState(RoomScrollMode.None)

        LaunchedEffect(roomScrollModeFlow.value) {
            when (roomScrollModeFlow.value) {
                RoomScrollMode.ModalMenu -> {
                    //Do nothing
                }
                RoomScrollMode.None -> {
                        knobListenerService.knobTurnEvents().collect { event ->
                        if (event is InputEvent.NavKnobPressed) {
                            openPopupMenu()
                        }
                    }
                }
                RoomScrollMode.Scroll -> {
                    knobListenerService.knobTurnEvents().collect { event ->
                        if (event is InputEvent.NavKnobPressed) {
                            roomScrollMode.value = RoomScrollMode.None
                            openPopupMenu()
                        }
                        if (event is InputEvent.NavKnobTurned) {
                            when (event.direction) {
                                InputEvent.NavKnobTurned.Direction.LEFT -> {
                                    stateVertical.scrollBy(-1F * 30)
                                }
                                InputEvent.NavKnobTurned.Direction.RIGHT -> {
                                    stateVertical.scrollBy(30F)
                                }
                            }
                        }
                    }
                }
                RoomScrollMode.ScrollSelect -> {
                    knobListenerService.knobTurnEvents().collect { event ->
                        if (event is InputEvent.NavKnobPressed) {
                            roomScrollMode.value = RoomScrollMode.None
                            openChatMessagePopup(messages[selectedIndex.value])
                        }
                        if (event is InputEvent.NavKnobTurned) {
                            when (event.direction) {
                                InputEvent.NavKnobTurned.Direction.LEFT -> {
                                    selectedIndex.value -= 1
                                }
                                InputEvent.NavKnobTurned.Direction.RIGHT -> {
                                    selectedIndex.value += 1
                                }
                            }
                        }
                    }
                }
                RoomScrollMode.AutoScrollToBottom -> {
                    stateVertical.scrollTo(stateVertical.maxValue)
                    roomScrollMode.value = RoomScrollMode.None
                }
            }
        }


        if (roomScrollModeFlow.value == RoomScrollMode.AutoScrollToBottom) {
            LaunchedEffect(messages.size) {
                stateVertical.scrollTo(stateVertical.maxValue)
            }
        }

        Column {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(stateVertical)
                        .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                        .padding(20.dp)
                ) {

//                    // TODO ScrollableColumn?
//                    VerticalScrollbar(
//                        modifier = Modifier.align(Alignment.CenterEnd)
//                            .fillMaxHeight(),
//                        adapter = rememberScrollbarAdapter(stateVertical)
//                    )
//

                    Column(
                        Modifier.wrapContentHeight()
                    ) {
                        messages.forEachIndexed { index, message ->
                            val isSelected = index == selectableIndices[selectedIndex.value]

                                when (message) {
                                    is ChatMessage.EmptyMessage -> {}
                                    is ChatMessage.GeoLocation -> {}
                                    is ChatMessage.PollMessage -> {}
                                    is ChatMessage.TextChat -> {
                                        TextMessageView(message, isSelected) { openTextMessagePopup(message) }
                                    }

                                    is ChatMessage.ImageMessage -> {
                                        ImageMessageView(message, isSelected) { openImageMessagePopup(message) }
                                    }
                                }

                        }
                    }
                }
            }
        }



    }


    private suspend fun sendMessageToRoom(textMessage : String) {
        roomId?.let {
            matrixService.getMatrixClient()?.room?.sendMessage(it) {
                text(textMessage)
            }
        }
    }

    private suspend fun voteInPoll(pollMessage: ChatMessage.PollMessage, itemToVoteFor : ChatMessage.PollMessage.PollItem) {

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun openPopupMenu() {
        roomScrollMode.value = RoomScrollMode.ModalMenu
        modalMenuService.showModalMenu(
            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                menuTopLeft = IntOffset(32, 32),
                menuWidth = 512
            ).toNormalModalMenuDimensions(),
            autoCloseOnSelect = false,
            menuData = ModalMenu(
                chipOrientation = ItemChipOrientation.W,
                items = listOf(
                    ModalMenu.ModalMenuItem(
                        title = "Close Menu",
                        onClicked = { modalMenuService.closeModalMenu() }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Close Screen",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            navigationNodeTraverser.goBack()
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Scroll",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            roomScrollMode.value = RoomScrollMode.Scroll
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Scroll-select",
                        onClicked = {

                            roomScrollMode.value = RoomScrollMode.ScrollSelect
                            modalMenuService.closeModalMenu()
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Scroll to bottom",
                        onClicked = {
                            roomScrollMode.value = RoomScrollMode.AutoScrollToBottom
                            modalMenuService.closeModalMenu()
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Write message",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            openMessageWriter()
                        }
                    ),
                    ModalMenu.ModalMenuItem(
                        title = "Room Properties...",
                        onClicked = {
                            modalMenuService.closeModalMenu()
                            openRoomSettingsPanel()
                        }
                    )
            ))
        )
    }


    private var inProgressMessage = ""

    //Need a side-pane for message building
    fun openMessageWriter() {
//        modalMenuService.closeModalMenu()
//        roomScrollMode.value = RoomScrollMode.None
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
                            },
                            onCloseWithoutEntry = { modalMenuService.closeModalMenu() ; openMessageWriter() }
                        )
                    }),
                    TextMenuItem("Send", onClicked = {
                        notificationHub.postNotificationBackground(Notification(
                            Notification.NotificationImage.MESSAGE_SQUARE,
                            "Sending Message",
                            text.value
                        ))

                        scope.launch {
                            sendMessageToRoom(text.value)
                            modalMenuService.closeSidePaneOverlay(true)
                            roomScrollMode.value = RoomScrollMode.None
                        }
                    }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        roomScrollMode.value = RoomScrollMode.None
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
//                    SidePanelMenu.InfoLabel("Room Name: Bob Wat")
//                    SidePanelMenu.InfoLabel("Room Id: foo")
                    //TODO room picture, centered, not too big.
                },
                listOf(
                    TextMenuItem("Room Members List", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        roomId?.let {
                            RoomMembersListScreen.openForRoomId(navigationNodeTraverser, it)
                        }
                    }),
                    TextMenuItem("Room Uploads List", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        roomId?.let {
                            RoomMembersListScreen.openForRoomId(navigationNodeTraverser, it)
                        }
                    }),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)}),
                )
            )
        }
    }



    //TODO Need a side-pane for Room Outbox (messages not yet sent), so that they can be canceled or retried
    //TODO put it in as a screen accessible from the RoomProperties pane

    fun openChatMessagePopup(message: ChatMessage) {

    }

    fun openTextMessagePopup(message : ChatMessage.TextChat) {
        //TODO Maybe have a side menu to select a reaction?
    }

    fun openImageMessagePopup(message : ChatMessage.ImageMessage) {
        //TODO Have an option to open the image viewer for this image.
    }

}