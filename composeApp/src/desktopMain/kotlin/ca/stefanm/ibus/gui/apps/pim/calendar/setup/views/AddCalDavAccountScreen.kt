package ca.stefanm.ibus.gui.pim.calendar.setup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
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
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteAccountRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Adds a CalDAV account (Nextcloud, Fastmail, iCloud, a Radicale, ...):
 * server URL, user name, password (an app password where the server offers
 * them), optionally a name. The entries are in the top half; the knob menu
 * below changes them with the keyboard. The bridge tries the login before
 * adding the account.
 */
@ScreenDoc(
    screenName = "AddCalDavAccountScreen",
    description = "Add a CalDAV calendar account",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AddCalDavAccountScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val accountRepository: RemoteAccountRepository,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "AddCalDavAccountScreen"
    }

    private data class Form(
        val url: String = "https://",
        val username: String = "",
        val password: String = "",
        /** Empty: the server's host name. */
        val name: String = "",
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val form = remember { mutableStateOf(Form()) }
        val adding = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Add CalDAV Account")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                summaryLines(form.value).forEach { line ->
                    TextMenuItem(
                        title = line,
                        isSelectable = false,
                        onClicked = {}
                    ).toView(Modifier.fillMaxWidth(), ItemChipOrientation.NONE)()
                }

                Column(Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    // TwoColumn keeps the items of its first composition, so
                    // rebuild it when a label changes.
                    key(adding.value) {
                        HalfScreenMenu.TwoColumn(
                            leftItems = listOf(
                                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                                TextMenuItem("Server URL...", onClicked = {
                                    edit(form.value.url) { form.value = form.value.copy(url = it.trim()) }
                                }),
                                TextMenuItem("User Name...", onClicked = {
                                    edit(form.value.username) { form.value = form.value.copy(username = it.trim()) }
                                }),
                            ),
                            rightItems = listOf(
                                TextMenuItem("Password...", onClicked = {
                                    // Not prefilled: it would show on screen.
                                    edit("") { form.value = form.value.copy(password = it) }
                                }),
                                TextMenuItem("Name...", onClicked = {
                                    edit(form.value.name) { form.value = form.value.copy(name = it.trim()) }
                                }),
                                TextMenuItem(
                                    if (adding.value) "Checking Login..." else "Add Account",
                                    onClicked = { if (!adding.value) add(scope, form.value, adding) }
                                ),
                            )
                        )
                    }
                }
            }
        }
    }

    private fun summaryLines(form: Form): List<String> = listOf(
        "Server: ${form.url}",
        "User: ${form.username.ifEmpty { "(not set)" }}",
        "Password: ${if (form.password.isEmpty()) "(not set)" else "•".repeat(form.password.length.coerceAtMost(12))}",
        "Name: ${form.name.ifEmpty { "(the server's name)" }}",
    )

    private fun edit(prefilled: String, onEntered: (String) -> Unit) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, prefilled) { onEntered(it) }
    }

    private fun add(scope: CoroutineScope, form: Form, adding: MutableState<Boolean>) {
        val missing = when {
            form.url.removePrefix("https://").removePrefix("http://").isBlank() -> "the server's URL"
            form.username.isBlank() -> "a user name"
            form.password.isEmpty() -> "a password"
            else -> null
        }
        if (missing != null) {
            notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not added", "Enter $missing first.")
            return
        }
        adding.value = true
        scope.launch {
            accountRepository.addCalDavAccount(form.name, form.url, form.username, form.password).fold(
                onSuccess = { account ->
                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Account added", "${account.name}: syncing calendars")
                    navigationNodeTraverser.goBack()
                },
                onFailure = { e ->
                    adding.value = false
                    logger.w(TAG, "Not added: ${e.message}")
                    notify(
                        Notification.NotificationImage.ALERT_TRIANGLE,
                        "Not added",
                        when (e) {
                            is PimBackendException.Unauthenticated -> "Wrong user name or password."
                            is PimBackendException.Unavailable -> "Server not reachable: ${e.message}"
                            else -> e.message ?: e.toString()
                        }
                    )
                }
            )
        }
    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content))
    }
}
