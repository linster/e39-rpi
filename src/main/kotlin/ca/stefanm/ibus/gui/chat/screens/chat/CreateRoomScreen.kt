package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.cosium.matrix_communication_client.CreateRoomInput
import com.cosium.matrix_communication_client.CreateRoomInput.CreationContent
import com.cosium.matrix_communication_client.MatrixResources
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@ScreenDoc(
    screenName = "CreateRoomScreen",
    description = "A screen to prompt the user to create a Matrix chat room",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class CreateRoomScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val matrixResources: Provider<MatrixResources>
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "CreateRoomScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() =  CreateRoomScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val scope = rememberCoroutineScope()
        val roomName = remember { mutableStateOf("") }
        val topic = remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Create Room")


            FullScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),

                TextMenuItem(
                    "Room Name: ${roomName.value}",
                    onClicked = {
                        modalMenuService.showKeyboard(
                            Keyboard.KeyboardType.FULL,
                            prefilled = roomName.value,
                            onTextEntered = { newName -> roomName.value = newName }
                        )
                    }
                ),
                TextMenuItem(
                    "Topic: ${topic.value}",
                    onClicked = {
                        modalMenuService.showKeyboard(
                            Keyboard.KeyboardType.FULL,
                            prefilled = topic.value,
                            onTextEntered = { newTopic -> topic.value = newTopic }
                        )
                    }
                ),
                TextMenuItem("Create Room",
                    onClicked = {
                        scope.launch {
                            val success = createRoom(
                                name = roomName.value,
                                topic = topic.value
                            )

                            if (success) {
                                notificationHub.postNotification(
                                    Notification(Notification.NotificationImage.NONE,
                                        "Room Created")
                                )
                                navigationNodeTraverser.goBack()
                            }
                        }
                    })
            ))

        }
    }

    suspend fun createRoom(
        name : String,
        topic: String?
    ) : Boolean {

        val roomSpec = CreateRoomInput.builder()
            .name(name)
            .topic(topic)
            .build()

        val result = kotlin.runCatching {
            matrixResources.get().rooms().create(roomSpec)
        }

        return if (result.isSuccess) {
            true
        } else {
            result.exceptionOrNull()?.let {
                logger.e(TAG, "Could not create room", it)
                notificationHub.postNotification(
                    Notification(Notification.NotificationImage.ALERT_TRIANGLE,
                        "Could not create room.",
                        it.message ?: ""
                    )
                )
            }
            false
        }
    }
}