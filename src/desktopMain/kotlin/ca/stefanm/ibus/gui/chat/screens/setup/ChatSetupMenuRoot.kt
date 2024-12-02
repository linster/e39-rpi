package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.CreateRoomScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatSetupMenuRoot",
    description = "The main menu for the matrix chat client settings",
    navigatesTo = [
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ChatSetupMenuRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ChatSetupMenuRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Chat Settings")

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                TextMenuItem("Server Credentials", onClicked = {}),
                TextMenuItem("Matrix Service", onClicked = {}),
                TextMenuItem("Notifications", onClicked = {}),
                TextMenuItem("Clear Local Credentials", onClicked = {}),
                TextMenuItem("Clear Local Database", onClicked = {}),
                TextMenuItem("Clear Local Media", onClicked = {})
            ))
        }
    }
}