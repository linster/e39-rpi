package ca.stefanm.ca.stefanm.ibus.gui.chat.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.CreateRoomScreen
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatRoomScreen
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.BMWMainMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatAppHomeScreen",
    description = "The main menu for the matrix chat client",
    navigatesTo = [
        ScreenDoc.NavigateTo(CreateRoomScreen::class)
    ]
)
@ScreenDoc.AllowsGoRoot
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatAppHomeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatAppHomeScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Chat")

            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(
                    TextMenuItem(
                        "Enter most recent room",
                        onClicked = {
                            ChatRoomScreen.openForRoomId(navigationNodeTraverser, "sop")
                        }
                    ),
                    TextMenuItem(
                        "Enter most recent DM",
                        onClicked = {}
                    )
                ),
                ne = listOf(
                    TextMenuItem(
                        "View Rooms",
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "View Contacts",
                        onClicked = {}
                    )
                ),
                sw = listOf(
                    TextMenuItem(
                        "Settings",
                        onClicked = { navigationNodeTraverser.navigateToNode(ChatSetupMenuRoot::class.java)}
                    ),
                    TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.navigateToRoot() })
                ),
                se = listOf(
                    TextMenuItem(
                        "New Chat",
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "New Room",
                        onClicked = { navigationNodeTraverser.navigateToNode(CreateRoomScreen::class.java)}
                    )
                )
            )
        }
    }
}