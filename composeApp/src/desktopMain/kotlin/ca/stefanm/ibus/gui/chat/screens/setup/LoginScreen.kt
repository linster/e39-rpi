package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
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
import net.folivo.trixnity.client.MatrixClient
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
    private val matrixService: MatrixService,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val configurationStorage: ConfigurationStorage
) : NavigationNode<LoginScreen.LoginScreenResult>{

    // Should return a result if the login is okay so that we don't have
    // an infinte screen loop

    sealed class LoginScreenResult {
        // we can't actually fail?
        //object Failed : LoginScreenResult()
        object Success : LoginScreenResult()
    }

    override val thisClass: Class<out NavigationNode<LoginScreenResult>>
        get() = LoginScreen::class.java

    enum class ScreenState {
        LOADING,
        PROMPT_CREDENTIALS,
        SUCCESS,
        FAILED
    }
    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = @Composable {

        val screenState = remember { mutableStateOf(ScreenState.LOADING) }

        when (screenState.value) {
            ScreenState.LOADING -> { LoadingScreen { newState -> screenState.value = newState} }
            ScreenState.PROMPT_CREDENTIALS -> { PromptUserCredentialScreen { newState -> screenState.value = newState} }
            ScreenState.FAILED -> { LoginFailedMessage() }
            ScreenState.SUCCESS -> { LoginSuccess() }
        }
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

    @Composable fun LoadingScreen(onRequestScreenStateUpdate : (ScreenState) -> Unit) {
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

        val scope = rememberCoroutineScope()
        //TODO Also actually do some work here to log in
        LaunchedEffect(Unit) {

            //Check if we have stored credentials and can login.
            // We'll do this by just starting the matrix service and seeing if it has a login
            // state after a few seconds.
        // If not, go to prompt the user.

            scope.launch {
                matrixService.start()
                if (configurationStorage.config[E39Config.CarPlatformConfigSpec._isPi]) {
                    delay(8000)
                } else {
                    delay(4000)
                }
                when (matrixService.getMatrixClient()?.loginState?.value) {
                    MatrixClient.LoginState.LOGGED_IN -> onRequestScreenStateUpdate(ScreenState.SUCCESS)
                    MatrixClient.LoginState.LOGGED_OUT_SOFT -> onRequestScreenStateUpdate(ScreenState.PROMPT_CREDENTIALS)
                    MatrixClient.LoginState.LOGGED_OUT -> onRequestScreenStateUpdate(ScreenState.PROMPT_CREDENTIALS)
                    MatrixClient.LoginState.LOCKED -> {
                        notificationHub.postNotification(
                            Notification(Notification.NotificationImage.MESSAGE_CIRCLE,
                                "Matrix Service",
                                "Could not login, account is locked.",
                                Notification.NotificationDuration.LONG))
                        onRequestScreenStateUpdate(ScreenState.FAILED)
                    }
                    null -> onRequestScreenStateUpdate(ScreenState.PROMPT_CREDENTIALS)
                }

            }

        }

    }

    @Composable fun PromptUserCredentialScreen(onRequestScreenStateUpdate : (ScreenState) -> Unit) {

        //Set the state to success or fail after completion.

        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Login")

            val serverUrl = remember { mutableStateOf("https://example.org") }
            val username = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                TextMenuItem("Server URL: ${serverUrl.value}", onClicked = {
                    modalMenuService.showKeyboard(
                        Keyboard.KeyboardType.FULL,
                        prefilled = "https://somechat.ca",
                        onTextEntered = { serverUrl.value = it }
                    )
                }),
                TextMenuItem("Username: ${username.value}", onClicked = {
                    modalMenuService.showKeyboard(
                        Keyboard.KeyboardType.FULL,
                        prefilled = "someuser",
                        onTextEntered = { username.value = it }
                    )

                }),
                TextMenuItem("Password: ${ password.value.let { if (it.isEmpty()) "[blank]" else "[entered]" }}", onClicked = {
                    modalMenuService.showKeyboard(
                        Keyboard.KeyboardType.FULL,
                        prefilled = "",
                        onTextEntered = { password.value = it }
                    )
                }),
                TextMenuItem("Login" , onClicked = {
                    matrixService.login(serverUrl.value, username.value, password.value)
                    notificationHub.postNotificationBackground(Notification(
                        Notification.NotificationImage.MESSAGE_CIRCLE,
                        "Matrix Service",
                        "Logging In",
                        Notification.NotificationDuration.LONG
                    ))
                    onRequestScreenStateUpdate(ScreenState.SUCCESS)
                })
            ))
        }
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

    @Composable fun LoginSuccess() {
        //Set the state and gtfo
        navigationNodeTraverser.setResultAndGoBack(this, LoginScreenResult.Success)
    }


}