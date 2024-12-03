package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.PersonPickerScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScreenDoc(
    screenName = "Add Room member screen",
    description = "Pick a person from a list, then add it once it's returned"
)
@AutoDiscover
class AddRoomMemberScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = AddRoomMemberScreen::class.java

    companion object {
        const val TAG = "AddRoomMemberScreen"

        fun openForRoom(navigationNodeTraverser: NavigationNodeTraverser, roomId: String) {
            navigationNodeTraverser.navigateToNodeWithParameters(AddRoomMemberScreen::class.java,
                AddRoomMemberScreenInitialParameters(roomId)
            )
        }
    }

    data class AddRoomMemberScreenInitialParameters(
        val roomId : String
    )

    private var storedRoomId : String? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val scope = rememberCoroutineScope()

        val roomToPickFor = (it?.requestParameters as? AddRoomMemberScreenInitialParameters)?.roomId
        storedRoomId = roomToPickFor

        if (it?.requestParameters is AddRoomMemberScreenInitialParameters) {
            //No choice was made, open the person picker screen
            navigationNodeTraverser.navigateToNode(PersonPickerScreen::class.java)
        }


        if (it?.result is PersonPickerScreen.PersonPickerResult.ChosenPerson) {
            val chosenPerson = it.result
            scope.launch {
                storedRoomId?.let { safeRoom -> addPersonToRoom(safeRoom, chosenPerson.personId) }
                navigationNodeTraverser.goBack()
            }
        }

        if (it?.result is PersonPickerScreen.PersonPickerResult.NoChoiceMade) {
            navigationNodeTraverser.goBack()
        }
    }


    private suspend fun addPersonToRoom(roomId: String, personId : String) {
        //TODO add the person

        notificationHub.postNotification(
            Notification(Notification.NotificationImage.NONE, "Added person"))
    }

}