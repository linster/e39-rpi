package ca.stefanm.ibus.gui.chat.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.gui.map.poi.PoiSelectorScreen.PoiSelectionResult
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.folivo.trixnity.client.flattenValues
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject

@ScreenDoc(
    screenName = "RoomSelectorScreen",
    description = "List all the rooms on the server and allow the user to select one.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.AllowsGoRoot
@AutoDiscover
class RoomSelectorScreen @Inject constructor(
    private val logger : Logger,
    private val matrixService: MatrixService,
    private val modalMenuService: ModalMenuService,
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<RoomSelectorScreen.RoomSelectorResult> {

    sealed class RoomSelectorResult {
        object NoSelection : RoomSelectorResult()
        data class RoomSelected(val roomId: RoomId) : RoomSelectorResult()
        data class DmSelected(val roomId: RoomId) : RoomSelectorResult()
    }

    override val thisClass: Class<out NavigationNode<RoomSelectorResult>>
        get() = RoomSelectorScreen::class.java



    data class DisplayedRoom(
        val roomName : String,
        val unreadMessageCount : Long,
        val roomId : RoomId,
        val underlyingRoom : Room
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val rooms =
            matrixService
                .getMatrixClient()
                ?.room
                ?.getAll()
                ?.flattenValues()
                ?.map {
                    it.map { room ->
                        DisplayedRoom(
                            roomId = room.roomId,
                            roomName = room.name?.explicitName ?: "Null name",
                            unreadMessageCount = room.unreadMessageCount,
                            underlyingRoom = room
                        )
                    }
                }
                ?.collectAsState(emptySet()) ?: flowOf(emptySet<DisplayedRoom>()).collectAsState(emptySet())

        LaunchedEffect(Unit) {
            val client = matrixService.getMatrixClient()
            val rooms = client?.room
        }

        Column {
            BmwSingleLineHeader("Select a Room")

            ScrollMenu.OneColumnScroll(
                onScrollListExitSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this@RoomSelectorScreen, RoomSelectorResult.NoSelection)
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 4,
                    isExitItemOnEveryPage = false,
                    isPageCountItemVisible = true,
                    showSpacerRow = false,
                    exitListItemLabel = "Cancel Selection",
                ),
                items = rooms.value.map {
                    TextMenuItem(
                        it.roomName,
                        onClicked = { showSelectionMenu(it) }
                    )
                }
            )
        }





    }


    fun showSelectionMenu(displayedRoom: DisplayedRoom) {
        // Select, Go back, More Info
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Room Selection",
                @Composable {
                    """
                        Name: ${displayedRoom.roomName}
                        Unread message count: ${displayedRoom.unreadMessageCount}
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Select Room", onClicked = {
                        navigationNodeTraverser.setResultAndGoBack(this,
                            if (displayedRoom.underlyingRoom.isDirect) {
                                RoomSelectorResult.DmSelected(displayedRoom.roomId)
                            } else {
                                RoomSelectorResult.RoomSelected(displayedRoom.roomId)
                            }
                        )
                        modalMenuService.closeSidePaneOverlay(true)
                    }),
                    TextMenuItem("More info...", onClicked = {
                        RoomInfoScreen.openForRoomId(navigationNodeTraverser, displayedRoom.roomId)
                        modalMenuService.closeSidePaneOverlay(true)
                    }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }

    }
}