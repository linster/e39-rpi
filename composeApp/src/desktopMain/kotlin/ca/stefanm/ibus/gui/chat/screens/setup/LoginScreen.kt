package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

///Prompt the user for a server url, username, password
// Skip through this screen if logged in
@ScreenDoc(
    screenName = "LoginScreen",
    description = "Prompt the user for a server url, username, password. Skip through this screen if logged in",
    navigatesTo = [
        ScreenDoc.NavigateTo(ChatAppHomeScreen::class),
        ScreenDoc.NavigateTo(ChatSetupMenuRoot::class)
    ]
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.AllowsGoRoot
@AutoDiscover
class LoginScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val matrixService: MatrixService
) : NavigationNode<Nothing>{


    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = LoginScreen::class.java

    override fun provideMainContent(): (incomingResult: Navigator.IncomingResult?) -> Unit = @Composable {

    }


    @Composable fun LoadingScreen() {
        /// Show while logging in / checking login state
    }

    @Composable fun PromptUserCredentialScreen() {

    }

    @Composable fun LoginFailedMessage() {
        //Login failed, redirecting to setup screen
        // put a button here to go root in case the user gets stuck.
    }


}