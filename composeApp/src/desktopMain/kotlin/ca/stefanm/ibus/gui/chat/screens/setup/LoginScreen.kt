package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import ca.stefanm.ibus.resources.bmw_navigation
import ca.stefanm.ibus.resources.*
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
) : NavigationNode<LoginScreen.LoginScreenResult>{

    // Should return a result if the login is okay so that we don't have
    // an infinte screen loop

    sealed class LoginScreenResult {
        object Failed : LoginScreenResult()
        object Success : LoginScreenResult()
    }

    override val thisClass: Class<out NavigationNode<LoginScreenResult>>
        get() = LoginScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = @Composable {

//        LoginFailedMessage()

        LoadingScreen()
    }


    // A story in 8 pictures
    enum class LoadingPictures(val drawableResource: DrawableResource){
        MAIL(Res.drawable.matrix_login_phone_mail),
        BLUE_BOOK(Res.drawable.matrix_login_phone_blue_book),
        NEWSPAPER(Res.drawable.matrix_login_phone_newspaper),

        SCIENCE(Res.drawable.matrix_login_phone_science),

        BRIEFCASE(Res.drawable.matrix_login_phone_briefcase),
        UMBRELLA_OPEN(Res.drawable.matrix_login_phone_umbrella_open),

        GORBACHEV(Res.drawable.matrix_login_phone_gorbachev),
        NUKE(Res.drawable.matrix_login_phone_nuke),
    }

    @Composable fun LoadingScreen() {
        /// Show while logging in / checking login state
        val pictureScope = rememberCoroutineScope()

        val pictureList = LoadingPictures.values().asList().circular()

        val pictureIndex = remember { mutableStateOf(0) }

        pictureScope.launch {
            while (true) {
                delay(500)
                pictureIndex.value += 1
            }
        }

        FullScreenPrompts.OptionPrompt(
            header = "Logging in...",
            options = listOf(
                TextMenuItem(
                    "Cancel Login",
                    onClicked = {
                        matrixService.stop()
                        navigationNodeTraverser.navigateToRoot()
                    }
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            ) {

                key(pictureIndex.value) {
                    Image(
                        painter = painterResource(pictureList[pictureIndex.value].drawableResource),
                        contentDescription = pictureList[pictureIndex.value].name,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier.height(128.dp)
                    )
                }

            }
        }

    }

    @Composable fun PromptUserCredentialScreen() {

    }

    @Composable fun LoginFailedMessage() {
        //Login failed, redirecting to setup screen
        // put a button here to go root in case the user gets stuck.

        FullScreenPrompts.OptionPrompt(
            header = "Matrix Chat -> Login -> Login Failed",
            options = listOf(
                TextMenuItem(
                    "Go to Matrix Settings",
                    onClicked = {
                        navigationNodeTraverser.navigateToRoot()
                        navigationNodeTraverser.navigateToNode(ChatSetupMenuRoot::class.java)
                    }
                ),
                TextMenuItem(
                    "Go to Main Menu",
                    onClicked = {
                        navigationNodeTraverser.navigateToRoot()
                    }
                )
            )
        ) {
            Column(
                Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            ) {
                Text("Login Failed.", color = Color.White, fontSize = 28.sp)
                Text("Try logging out, clearing cache, or going back.", color = Color.White, fontSize = 28.sp)
            }
        }

    }


}