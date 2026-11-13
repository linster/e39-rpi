package ca.stefanm.ibus.gui.pim.calendar.setup.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PendingLogin
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteAccountRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import javax.inject.Inject

/**
 * The accounts the PIM bridge syncs calendars from, live: state, sync
 * progress, errors. Selecting one shows what can be done with it (sync,
 * offline/online, log in again, remove). Logins waiting for a browser are
 * listed first.
 *
 * There's no browser in the HMI yet: a Google login can only be finished
 * with "Open in Browser", which opens the desktop's browser (development
 * on a laptop).
 */
@ScreenDoc(
    screenName = "AccountListScreen",
    description = "Calendar accounts: list, add, remove, sync",
    navigatesTo = [
        ScreenDoc.NavigateTo(AddCalDavAccountScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AccountListScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val accountRepository: RemoteAccountRepository,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "AccountListScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val accounts = accountRepository.accounts.collectAsState().value
        val logins = remember { accountRepository.pendingLogins }.collectAsState(emptyList()).value
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            accountRepository.loginResults.collect { result ->
                if (result.success) {
                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Logged in", "Syncing calendars")
                } else {
                    notify(Notification.NotificationImage.ALERT_TRIANGLE, "Login not finished", result.message)
                }
            }
        }

        val items: List<MenuItem> = buildList {
            logins.forEach { login ->
                add(TextMenuItem("Waiting: ${login.title}...", onClicked = { showLoginPane(scope, login) }))
            }
            when {
                accounts == null -> add(TextMenuItem("Asking the PIM bridge...", isSelectable = false, onClicked = {}))
                accounts.isEmpty() -> add(TextMenuItem("No accounts yet", isSelectable = false, onClicked = {}))
                else -> accounts.forEach { account ->
                    add(TextMenuItem("${account.name}: ${stateLabel(account)}", onClicked = { showAccountPane(scope, account) }))
                }
            }
            add(TextMenuItem("Add CalDAV Account...", onClicked = {
                navigationNodeTraverser.navigateToNode(AddCalDavAccountScreen::class.java)
            }))
            add(TextMenuItem("Add Google Account...", onClicked = { addGoogleAccount(scope) }))
            if (!accounts.isNullOrEmpty()) {
                add(TextMenuItem("Sync All Now", onClicked = {
                    scope.launch { accountRepository.syncNow().onFailure { notifyFailure("Not synced", it) } }
                }))
            }
        }

        // SmoothScroll allocates knob positions once: rebuild it when the
        // number of entries changes. Labels (sync progress) update in place.
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Calendar Accounts",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun typeLabel(account: PimAccount): String = when (account.type) {
        PimAccount.Type.CALDAV -> "CalDAV"
        PimAccount.Type.GOOGLE -> "Google"
        PimAccount.Type.ICAL_FILE -> "Calendar file"
        PimAccount.Type.OTHER -> "Other"
    }

    /** One short word or two for the list. */
    private fun stateLabel(account: PimAccount): String = when {
        account.needsLogin -> "log in needed"
        account.state == PimAccount.State.OFFLINE -> "offline"
        account.state == PimAccount.State.SYNCING ->
            if (account.progressPercent > 0) "syncing ${account.progressPercent}%" else "syncing"
        account.state == PimAccount.State.BROKEN -> "not working"
        account.state == PimAccount.State.NOT_CONFIGURED -> "setting up"
        account.lastError.isNotEmpty() -> "last sync failed"
        else -> "OK"
    }

    private fun showAccountPane(scope: CoroutineScope, account: PimAccount) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = account.name,
                text = @Composable {
                    InfoLabel("${typeLabel(account)}: ${stateLabel(account)}", FontWeight.Bold)
                    if (account.statusMessage.isNotBlank()) InfoLabel(account.statusMessage)
                    if (account.lastError.isNotBlank()) InfoLabel("Last error: ${account.lastError.lines().first()}")
                },
                buttons = buildList {
                    if (account.type == PimAccount.Type.GOOGLE) {
                        add(TextMenuItem(if (account.needsLogin) "Log In..." else "Log In Again...", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            scope.launch {
                                accountRepository.startLogin(account).onFailure { notifyFailure("Login not started", it) }
                            }
                        }))
                    }
                    add(TextMenuItem("Sync Now", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { accountRepository.syncNow(account).onFailure { notifyFailure("Not synced", it) } }
                    }))
                    add(TextMenuItem(if (account.online) "Go Offline" else "Go Online", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch {
                            accountRepository.setOnline(account, !account.online).onFailure { notifyFailure("Not changed", it) }
                        }
                    }))
                    add(TextMenuItem("Remove...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        confirmRemove(scope, account)
                    }))
                    add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    private fun confirmRemove(scope: CoroutineScope, account: PimAccount) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Remove Account?",
                text = @Composable {
                    InfoLabel(account.name, FontWeight.Bold)
                    InfoLabel("Its calendars disappear from the car.")
                    InfoLabel("Nothing is deleted on the server.")
                },
                buttons = listOf(
                    TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Remove", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch {
                            accountRepository.removeAccount(account).fold(
                                onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Account removed", account.name) },
                                onFailure = { notifyFailure("Not removed", it) }
                            )
                        }
                    }),
                )
            )
        }
    }

    private fun addGoogleAccount(scope: CoroutineScope) {
        scope.launch {
            accountRepository.addGoogleAccount().fold(
                // The login shows up as "Waiting: Log in to Google".
                onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Google account added", "Log in to finish") },
                onFailure = { notifyFailure("Not added", it) }
            )
        }
    }

    private fun canOpenBrowser(): Boolean =
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

    private fun showLoginPane(scope: CoroutineScope, login: PendingLogin) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = login.title,
                text = @Composable {
                    InfoLabel("This needs a web browser.", FontWeight.Bold)
                    if (canOpenBrowser()) {
                        InfoLabel("Open it in this computer's browser,")
                        InfoLabel("log in there, then come back.")
                    } else {
                        InfoLabel("There's no browser in the car yet.")
                    }
                },
                buttons = buildList {
                    if (canOpenBrowser()) {
                        add(TextMenuItem("Open in Browser", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            try {
                                Desktop.getDesktop().browse(URI(login.url))
                            } catch (e: Exception) {
                                notifyFailure("Browser didn't open", e)
                            }
                        }))
                    }
                    add(TextMenuItem("Cancel Login", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { accountRepository.cancelLogin(login).onFailure { notifyFailure("Not cancelled", it) } }
                    }))
                    add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content))
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notify(
            Notification.NotificationImage.ALERT_TRIANGLE,
            top,
            when (e) {
                is PimBackendException.Unavailable -> "Calendar not reachable: ${e.message}"
                else -> e.message ?: e.toString()
            }
        )
    }
}
