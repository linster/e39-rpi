package ca.stefanm.ibus.gui.chat.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.chat.RoomInfoScreen.RoomInfoScreenParameters
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.flowOf
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.avatarUrl
import net.folivo.trixnity.client.store.membership
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import javax.inject.Inject

@ScreenDoc(
    screenName = "ContactInfoScreen",
    description = "Shows info about a contact"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ContactInfoScreen @Inject constructor(
    private val matrixService: MatrixService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "ContactInfoScreen"

        fun openForRoomAndPerson(
            navigationNodeTraverser: NavigationNodeTraverser,
            roomId: RoomId, personId: UserId) {

            navigationNodeTraverser.navigateToNodeWithParameters(ContactInfoScreen::class.java,
                ContactInfoScreenParameters(roomId, personId)
            )
        }
    }

    data class ContactInfoScreenParameters(
        val roomId: RoomId,
        val personId: UserId
    )

    override val thisClass: Class<out NavigationNode<Nothing>> = ContactInfoScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val personId = (params?.requestParameters as? ContactInfoScreenParameters)?.personId
        if (personId == null) {
            logger.w(TAG, "PersonId ID was null")
            navigationNodeTraverser.goBack()
        }

        val roomId = (params?.requestParameters as? ContactInfoScreenParameters)?.roomId
        if (roomId == null) {
            logger.w(TAG, "Room ID was null")
            navigationNodeTraverser.goBack()
        }

        personId as UserId
        roomId as RoomId

        val person = matrixService
            .getMatrixClient()
            ?.user
            ?.getById(roomId, personId)
            ?.collectAsState(null) ?: flowOf<RoomUser?>(null).collectAsState(null)


        person.value?.let { PersonInfo(it) }
    }

    @Composable fun PersonInfo(person : RoomUser) {

        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("User Info: ${person.name}")

            //person.avatarUrl

            HalfScreenMenu.OneColumn(alignment = Alignment.End, items = listOf(
                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() })
            ))
        }
    }
}