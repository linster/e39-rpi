package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject

@ScreenDoc(
    screenName = "RoomMembersListScreen",
    description = "List all the members for a room"
)
@AutoDiscover
class RoomMembersListScreen @Inject constructor(

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

    var roomId : RoomId? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { param ->

        roomId = (param?.requestParameters as? RoomMembersListScreenParameters)?.roomId


        val roomName = remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            //Fetch the Room name for the roomId
            scope.launch {
                delay(1000)
                roomName.value = roomId?.full ?: "null"
            }
        }


        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Room Members for: ${roomName.value}")

            // TODO list all the people in the room, click opens info about them
        }
    }

}