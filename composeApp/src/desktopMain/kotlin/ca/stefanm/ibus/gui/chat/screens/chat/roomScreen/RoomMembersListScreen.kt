package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.chat.ContactInfoScreen
import ca.stefanm.ibus.gui.chat.screens.chat.ContactInfoScreen.Companion
import ca.stefanm.ibus.gui.chat.screens.chat.RoomInfoScreen
import ca.stefanm.ibus.gui.chat.screens.chat.RoomSelectorScreen.RoomSelectorResult
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.flattenValues
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject

@ScreenDoc(
    screenName = "RoomMembersListScreen",
    description = "List all the members for a room"
)
@AutoDiscover
class RoomMembersListScreen @Inject constructor(
    private val matrixService: MatrixService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RoomMembersListScreen::class.java

    companion object {
        const val TAG = "RoomMembersListScreen"

        fun openForRoomId(
            navigationNodeTraverser: NavigationNodeTraverser,
            roomId : RoomId
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                RoomMembersListScreen::class.java,
                RoomMembersListScreenParameters(
                    roomId = roomId
                )
            )
        }
    }


    data class RoomMembersListScreenParameters(
        val roomId : RoomId
    )



    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { param ->

        val roomId = (param?.requestParameters as? RoomMembersListScreenParameters)?.roomId
        if (roomId == null) {
            logger.w(TAG, "Room ID was null")
            navigationNodeTraverser.goBack()
        }

        roomId as RoomId

        val roomName = remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            //Fetch the Room name for the roomId
            scope.launch {
                delay(1000)
                roomName.value = roomId.full ?: "null"
            }
        }

        val membersLoaded = remember { mutableStateOf(false) }

        /// Load all the members for the room
        LaunchedEffect(Unit) {
            matrixService
                .getMatrixClient()
                ?.user
                ?.loadMembers(roomId, true)
            membersLoaded.value = true
        }

        if (membersLoaded.value == true) {
            RoomMembersList(roomId)
        }

    }

    @Composable fun RoomMembersList(roomId : RoomId) {
        val matrixClient = matrixService.getMatrixClient()
        if (matrixClient == null) {
            navigationNodeTraverser.goBack()
            return
        }

        val room = matrixClient.room.getById(roomId).collectAsState(null)
        val people = matrixClient.user.getAll(roomId).flattenValues().collectAsState(emptyList())

        Column(modifier = Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Room Members for: ${room.value?.name?.explicitName}")

            ScrollMenu.OneColumnScroll(
                onScrollListExitSelected = {
                    navigationNodeTraverser.goBack()
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 4,
                    isExitItemOnEveryPage = false,
                    isPageCountItemVisible = true,
                    showSpacerRow = false,
                    exitListItemLabel = "Go Back",
                ),
                items = people.value.map {
                    TextMenuItem(
                        it.name,
                        onClicked = { showSelectionMenu(it) }
                    )
                }
            )
        }

    }


    fun showSelectionMenu(user : RoomUser) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "User Info",
                @Composable {
                    """
                        Name: ${user.name}
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("More info...", onClicked = {
                        ContactInfoScreen.openForRoomAndPerson(navigationNodeTraverser, user.roomId, user.userId)
                        modalMenuService.closeSidePaneOverlay(true)
                    }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }

    }
}