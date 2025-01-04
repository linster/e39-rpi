package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject

//List all the uploads for a room (list of small pictures)
@AutoDiscover
class RoomUploadsList @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RoomUploadsList::class.java

    companion object {
        const val TAG = "RoomUploadsListScreen"

        fun openForRoomId(
            navigationNodeTraverser: NavigationNodeTraverser,
            roomId: RoomId
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                RoomUploadsList::class.java,
                RoomUploadsListScreenParameters(
                    roomId = roomId
                )
            )
        }
    }


    data class RoomUploadsListScreenParameters(
        val roomId: RoomId
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { param ->

        val roomId = (param?.requestParameters as? RoomUploadsListScreenParameters)?.roomId


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

            BmwSingleLineHeader("Room Uploads for: ${roomName.value}")

            //TODO list the room uploads. Click on each, if picture, opens up the picture viewer.
        }
    }
}