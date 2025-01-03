package ca.stefanm.ibus.gui.chat.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.flattenValues
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject



@ScreenDoc(
    screenName = "RoomInfoScreen",
    description = "Shows detailed info about a particular room",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.AllowsGoRoot
@AutoDiscover
class RoomInfoScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val matrixService: MatrixService,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "RoomInfoScreen"
        fun openForRoomId(navigationNodeTraverser: NavigationNodeTraverser, roomId : RoomId) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                RoomInfoScreen::class.java,
                RoomInfoScreenParameters(roomId)
            )
        }
    }

    data class RoomInfoScreenParameters(
        val roomId: RoomId,
    )

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RoomInfoScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        val roomId = (params?.requestParameters as? RoomInfoScreenParameters)?.roomId
        if (roomId == null) {
            logger.w(TAG, "Room ID was null")
            navigationNodeTraverser.goBack()
        }

        val room = roomId?.let { roomIdSafe ->
            matrixService.getMatrixClient()?.room?.getById(roomIdSafe)?.collectAsState(null)
        } ?: flowOf<Room>().collectAsState(null)

        room.value?.let {
            RoomInfo(it)
        }
    }

    @Composable fun RoomInfo(room : Room) {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Room: ${room.name?.explicitName}")

            val userList =
                matrixService.getMatrixClient()
                    ?.user?.getAll(room.roomId)?.flattenValues()?.onEach {
                        logger.d(TAG, "Room Member List: ${it.map { user -> user.name }}")
                    }?.collectAsState(emptyList())
                    ?: flowOf<List<RoomUser>>().collectAsState(emptyList())


            key (userList) {
                FullScreenMenu.OneColumn(
                    listOf(
                        TextMenuItem(
                            "Go Back",
                            onClicked = { navigationNodeTraverser.goBack() }
                        ),
                        TextMenuItem(
                            "Is DM? : ${room.isDirect}", isSelectable = false, onClicked = {}),
                        TextMenuItem(
                            "Membership Count: ${userList.value.count()}",
                            isSelectable = false,
                            onClicked = {}
                        ),
                        TextMenuItem(
                            "Members: ${userList.value.map { it.name }}", isSelectable = false, onClicked = {})
                    )
                )
            }
        }

    }
}