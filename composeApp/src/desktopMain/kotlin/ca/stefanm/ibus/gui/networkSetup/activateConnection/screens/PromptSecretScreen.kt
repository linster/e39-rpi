package ca.stefanm.ibus.gui.networkSetup.activateConnection.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.WifiSecurity
import javax.inject.Inject

fun NavigationNodeTraverser.promptForWifiSecret(params : PromptSecretScreen.PromptSecretParameters) {
    this.navigateToNodeWithParameters(PromptSecretScreen::class.java, params)
}

@ScreenDoc(
    screenName = "PromptSecretScreen",
    description = "Asks for the password of a wifi network before connecting to it."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PromptSecretScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser,
) : NavigationNode<PromptSecretScreen.PromptSecretResult> {

    data class PromptSecretParameters(
        val networkName : String,
        val security : WifiSecurity,
        //True when replacing the password of a saved network, false for a new network.
        val isChangingPassword : Boolean,
        val connection : Nmt.NmtConnectConnection
    )

    sealed interface PromptSecretResult {
        data object Cancelled : PromptSecretResult
        data class SecretEntered(
            val secret : String,
            val connection : Nmt.NmtConnectConnection
        ) : PromptSecretResult
    }

    override val thisClass: Class<out NavigationNode<PromptSecretResult>>
        get() = PromptSecretScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = content@ {

        val parameters = it?.requestParameters as? PromptSecretParameters ?: return@content

        val secret = remember(parameters) { mutableStateOf("") }
        val showSecret = remember(parameters) { mutableStateOf(false) }

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader(if (parameters.isChangingPassword) "Change Wi-Fi Password" else "Wi-Fi Password")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Network: ${parameters.networkName}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = "Security: ${parameters.security.label}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = "Password: ${
                            when {
                                secret.value.isEmpty() -> "<none>"
                                showSecret.value -> secret.value
                                else -> "*".repeat(secret.value.length)
                            }
                        }",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = if (secret.value.isEmpty()) "Enter Password..." else "Edit Password...",
                        onClicked = {
                            modalMenuService.showKeyboard(
                                type = Keyboard.KeyboardType.FULL,
                                prefilled = secret.value,
                                onTextEntered = { entered -> secret.value = entered }
                            )
                        }
                    ),
                    TextMenuItem(
                        title = if (showSecret.value) "Hide Password" else "Show Password",
                        onClicked = { showSecret.value = !showSecret.value }
                    ),
                    TextMenuItem(
                        title = "Connect",
                        onClicked = {
                            if (parameters.security.isPasswordValid(secret.value)) {
                                navigationNodeTraverser.setResultAndGoBack(
                                    this@PromptSecretScreen,
                                    PromptSecretResult.SecretEntered(secret.value, parameters.connection)
                                )
                            } else {
                                notificationHub.postNotificationBackground(Notification(
                                    image = Notification.NotificationImage.ALERT_TRIANGLE,
                                    topText = "Invalid password",
                                    contentText = if (parameters.security == WifiSecurity.WPA_PSK) {
                                        "WPA passwords are 8 to 63 characters long."
                                    } else {
                                        "Enter a password first."
                                    }
                                ))
                            }
                        }
                    ),
                    TextMenuItem(
                        title = "Cancel",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(
                                this@PromptSecretScreen,
                                PromptSecretResult.Cancelled
                            )
                        }
                    )
                )
            )
        }
    }
}
