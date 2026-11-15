package ca.stefanm.ibus.gui.pim.accounts.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AccountListRepo
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AddMailAccountRepo
import ca.stefanm.ibus.gui.pim.accounts.repo.api.MailAccountForm
import ca.stefanm.ibus.gui.pim.accounts.repo.api.MailServerRole
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.MailServerSettings
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "Add Email Account": the address; the bridge looks for the servers
 * (shown to confirm, editable; typed in when nothing is found); the
 * password (with a hint where the provider wants an app password, e.g.
 * Gmail); the sender's name; an account name. Add tries both logins first.
 * AddMailAccountRepo holds the form: start it, then open this.
 */
@ScreenDoc(
    screenName = "AddMailAccountScreen",
    description = "Accounts: adding a mail account (servers found or typed, app passwords)",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AddMailAccountScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val repo: AddMailAccountRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "AddMailAccountScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{
        val form = repo.form.collectAsState().value
        if (form == null) {
            // Added, or cancelled.
            LaunchedEffect(Unit) { navigationNodeTraverser.goBack() }
            return@content
        }
        val busy = remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val items: List<MailListItem> = buildList {
            add(row("Cancel", "") { repo.discard() })
            add(row("E-mail Address", form.email.ifBlank { "Enter..." }, dim = form.email.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, form.email) { typed ->
                    repo.setEmail(typed)
                    if (typed.isNotBlank()) discover(scope, busy)
                }
            })
            when {
                busy.value == "discovering" -> add(row("Looking for the servers...", "") {})
                form.email.isNotBlank() && form.settings == null -> add(row("Find Servers", "") { discover(scope, busy) })
            }
            if (form.settings != null) {
                add(row("Servers", form.sourceText) {})
                add(server(form, MailServerRole.INCOMING))
                add(server(form, MailServerRole.OUTGOING))
            }
            form.appPasswordHint?.let { hint -> add(row("App Password", hint, lines = 4) {}) }
            add(row("Your Name", form.senderName.ifBlank { "As the mail's sender..." }, dim = form.senderName.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, form.senderName) { repo.setSenderName(it) }
            })
            add(row(if (form.appPasswordHint != null) "App Password (Not Your Usual One)" else "Password",
                if (form.password.isEmpty()) "Enter..." else "•".repeat(form.password.length.coerceAtMost(16)), dim = form.password.isEmpty()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { repo.setPassword(it) }
            })
            add(row("Account Name", form.name.ifBlank { form.email.ifBlank { "(the address)" } }, dim = form.name.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, form.name) { repo.setName(it) }
            })
            add(row(if (busy.value == "adding") "Adding... (trying the logins)" else "Add Account", "") {
                if (busy.value == null) add(scope, busy, form)
            })
        }

        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = "Add Email Account",
                prependGoBackEntry = false,
                items = items,
            )
        }
    }

    private fun row(label: String, value: String, lines: Int = 1, dim: Boolean = false, onClicked: () -> Unit): MailListItem =
        { allocatedIndex, currentIndex ->
            ComposeFieldRow(label, value, isSelected = allocatedIndex == currentIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) { onClicked() }, valueLines = lines, dim = dim)
        }

    private fun server(form: MailAccountForm, role: MailServerRole): MailListItem {
        val s = form.server(role)!!
        val label = if (role == MailServerRole.INCOMING) "Incoming (IMAP)" else "Outgoing (SMTP)"
        return row(label, AddMailAccountRepo.serverText(s), dim = s.host.isBlank()) { showServer(role, label, s) }
    }

    private fun showServer(role: MailServerRole, label: String, s: MailServerSettings) {
        modalMenuService.showMailSidePane(logger, "$TAG server", label, listOf(AddMailAccountRepo.serverText(s)), buildList {
            add(SidePaneItem("Server: ${s.host.ifBlank { "..." }}") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, s.host) { v -> repo.editServer(role) { it.copy(host = v.trim()) } }
            })
            add(SidePaneItem("Port: ${s.port}") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.NUMERIC, s.port.toString()) { v ->
                    val port = v.trim().toIntOrNull()
                    if (port == null || port !in 1..65535) notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not a port", v)
                    else repo.editServer(role) { it.copy(port = port) }
                }
            })
            MailServerSettings.Security.entries.filter { it != s.security }.forEach { sec ->
                add(SidePaneItem("Use ${AddMailAccountRepo.securityText(sec)}") { repo.setSecurity(role, sec) })
            }
            add(SidePaneItem("User Name: ${s.username.ifBlank { "(the address)" }}") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, s.username) { v -> repo.editServer(role) { it.copy(username = v.trim()) } }
            })
        })
    }

    private fun discover(scope: CoroutineScope, busy: MutableState<String?>) {
        busy.value = "discovering"
        scope.launch {
            repo.discover().fold(
                onSuccess = { f ->
                    if (f.manual) notify(Notification.NotificationImage.ALERT_TRIANGLE, "No servers found", "Enter them: Incoming and Outgoing")
                },
                onFailure = { notifyFailure("Servers not found", AccountListRepo.describe(it)) },
            )
            busy.value = null
        }
    }

    private fun add(scope: CoroutineScope, busy: MutableState<String?>, form: MailAccountForm) {
        busy.value = "adding"
        scope.launch {
            repo.add().fold(
                // The form is gone now: the screen goes back by itself.
                onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Mail account added", it.name) },
                onFailure = { notifyFailure("Not added", AddMailAccountRepo.describe(it, form)) },
            )
            busy.value = null
        }
    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content, duration = Notification.NotificationDuration.LONG))
    }

    private fun notifyFailure(top: String, text: String) {
        logger.w(TAG, "$top: $text")
        notify(Notification.NotificationImage.ALERT_TRIANGLE, top, text)
    }
}
