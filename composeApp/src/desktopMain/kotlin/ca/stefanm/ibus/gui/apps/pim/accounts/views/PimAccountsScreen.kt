package ca.stefanm.ibus.gui.pim.accounts.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AccountListRepo
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AccountRow
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AddMailAccountRepo
import ca.stefanm.ibus.gui.pim.accounts.repo.api.PhoneLoginRelay
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PendingLogin
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.setup.views.AddCalDavAccountScreen
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import javax.inject.Inject

/**
 * Every account the PIM bridge syncs, for all the PIM apps (mail,
 * calendar, contacts): mail accounts first, then calendar and contacts
 * accounts, with their state. Selecting one: sync, offline/online,
 * contacts on/off (CalDAV), log in again (Google), remove. Adds mail,
 * CalDAV and Google accounts. Logins waiting for a browser come first.
 *
 * Gmail: added as a mail account with an app password (the browser login
 * for Gmail's mail isn't done: e39-rpi-pim#5).
 * Logins open in the HMI's browser if the configuration says there is one
 * (hasBrowser; not written yet), else in the desktop's browser where there
 * is one (a laptop).
 */
@ScreenDoc(
    screenName = "PimAccountsScreen",
    description = "Accounts for mail, calendars and contacts: list, add, remove, sync",
    navigatesTo = [
        ScreenDoc.NavigateTo(AddMailAccountScreen::class),
        ScreenDoc.NavigateTo(AddCalDavAccountScreen::class),
        ScreenDoc.NavigateTo(PhoneLoginScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PimAccountsScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val accountListRepo: AccountListRepo,
    private val addMailAccountRepo: AddMailAccountRepo,
    private val configurationStorage: ConfigurationStorage,
    private val phoneLoginRelay: PhoneLoginRelay,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "PimAccountsScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val rows = remember { accountListRepo.rows() }.collectAsState(null).value
        val logins = remember { accountListRepo.pendingLogins }.collectAsState(emptyList()).value
        val scope = rememberCoroutineScope()

        // A login started here (Add Google Account, Log In Again): its QR code for the phone.
        LaunchedEffect(Unit) {
            var known: Set<String>? = null
            accountListRepo.pendingLogins.collect { pending ->
                val new = known?.let { k -> pending.filter { it.id !in k } }.orEmpty()
                known = pending.map { it.id }.toSet()
                new.firstOrNull()?.let { showOnPhone(it) }
            }
        }

        LaunchedEffect(Unit) {
            accountListRepo.loginResults.collect { result ->
                if (result.success) notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Logged in", "Syncing")
                else notify(Notification.NotificationImage.ALERT_TRIANGLE, "Login not finished", result.message)
            }
        }

        val items: List<MailListItem> = buildList {
            logins.forEach { login -> add(row("Waiting", "${login.title}...") { showLoginPane(scope, login) }) }
            when {
                rows == null -> add(row("Asking the PIM bridge...", "") {})
                rows.isEmpty() -> add(row("No accounts yet", "") {})
                else -> rows.forEach { r -> add(row(r.caption, r.title) { showAccountPane(scope, r) }) }
            }
            add(row("Add Email Account...", "") {
                addMailAccountRepo.start()
                navigationNodeTraverser.navigateToNode(AddMailAccountScreen::class.java)
            })
            add(row("Add CalDAV Account...", "") { navigationNodeTraverser.navigateToNode(AddCalDavAccountScreen::class.java) })
            add(row("Add Google Account...", "") { addGoogleAccount(scope) })
            if (!rows.isNullOrEmpty()) {
                add(row("Sync All Now", "") {
                    scope.launch { accountListRepo.syncNow().onFailure { notifyFailure("Not synced", it) } }
                })
            }
        }

        // Rebuilt when the number of rows changes; labels (sync progress) update in place.
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = "Accounts",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun row(label: String, value: String, onClicked: () -> Unit): MailListItem = { allocatedIndex, currentIndex ->
        ComposeFieldRow(label, value, isSelected = allocatedIndex == currentIndex, onClicked = CallWhen(currentIndexIs = allocatedIndex) { onClicked() })
    }

    private fun showAccountPane(scope: CoroutineScope, row: AccountRow) {
        val a = row.account
        val info = listOfNotNull(
            "${row.kind}: ${row.state}",
            a.statusMessage.takeIf { it.isNotBlank() },
            a.lastError.takeIf { it.isNotBlank() }?.let { "Last error: ${it.lines().first()}" },
        )
        modalMenuService.showMailSidePane(logger, "$TAG account", a.name, info, buildList {
            if (a.type == PimAccount.Type.GOOGLE) {
                add(SidePaneItem(if (a.needsLogin) "Log In..." else "Log In Again...") {
                    scope.launch { accountListRepo.startLogin(a).onFailure { notifyFailure("Login not started", it) } }
                })
            }
            add(SidePaneItem("Sync Now") {
                scope.launch { accountListRepo.syncNow(a).onFailure { notifyFailure("Not synced", it) } }
            })
            add(SidePaneItem(if (a.online) "Go Offline" else "Go Online") {
                scope.launch { accountListRepo.setOnline(a, !a.online).onFailure { notifyFailure("Not changed", it) } }
            })
            if (a.type == PimAccount.Type.CALDAV) {
                add(SidePaneItem(if (a.contactsEnabled) "Turn Contacts Off" else "Turn Contacts On") {
                    scope.launch {
                        accountListRepo.setContactsEnabled(a, !a.contactsEnabled).fold(
                            onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE,
                                if (it.contactsEnabled) "Contacts on" else "Contacts off", a.name) },
                            onFailure = { notifyFailure("Not changed", it) },
                        )
                    }
                })
            }
            add(SidePaneItem("Remove...") { confirmRemove(scope, a) })
        })
    }

    private fun confirmRemove(scope: CoroutineScope, account: PimAccount) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Remove Account?",
                text = @Composable {
                    InfoLabel(account.name, FontWeight.Bold)
                    InfoLabel(when (account.type) {
                        PimAccount.Type.IMAP -> "Its mail disappears from the car."
                        else -> "Its calendars and contacts disappear from the car."
                    })
                    InfoLabel("Nothing is deleted on the server.")
                },
                buttons = listOf(
                    TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Remove", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch {
                            accountListRepo.remove(account).fold(
                                onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Account removed", account.name) },
                                onFailure = { notifyFailure("Not removed", it) },
                            )
                        }
                    }),
                )
            )
        }
    }

    private fun addGoogleAccount(scope: CoroutineScope) {
        scope.launch {
            accountListRepo.addGoogleAccount().fold(
                // The login shows up as "Waiting: Log in to Google".
                onSuccess = { notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Google account added", "Log in to finish (calendars and contacts)") },
                onFailure = { notifyFailure("Not added", it) },
            )
        }
    }

    /** Where a login page can be shown. */
    private enum class LoginBrowser { HMI, DESKTOP, NONE }

    private fun loginBrowser(): LoginBrowser = when {
        configurationStorage.config[E39Config.CarPlatformConfigSpec._hasBrowser] -> LoginBrowser.HMI
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) -> LoginBrowser.DESKTOP
        else -> LoginBrowser.NONE
    }

    private fun openLoginPage(browser: LoginBrowser, login: PendingLogin) {
        when (browser) {
            // TODO HMI browser (e39-rpi-pim TODO.md, phase 5; e39-rpi-pim#5).
            LoginBrowser.HMI -> notify(Notification.NotificationImage.ALERT_TRIANGLE, "No browser yet",
                "hasBrowser is set, but the HMI's browser isn't written yet.")
            LoginBrowser.DESKTOP -> try {
                Desktop.getDesktop().browse(URI(login.url))
            } catch (e: Exception) {
                notifyFailure("Browser didn't open", e)
            }
            LoginBrowser.NONE -> {}
        }
    }

    private fun showLoginPane(scope: CoroutineScope, login: PendingLogin) {
        val browser = loginBrowser()
        val info = listOf("This needs a web browser: log in on a phone (a QR code)") + when (browser) {
            LoginBrowser.HMI -> listOf("or on the page that opens here.")
            LoginBrowser.DESKTOP -> listOf("or in this computer's browser.")
            LoginBrowser.NONE -> listOf()
        }
        modalMenuService.showMailSidePane(logger, "$TAG login", login.title, info, buildList {
            add(SidePaneItem("Log In on a Phone (QR Code)") { showOnPhone(login) })
            if (browser != LoginBrowser.NONE) add(SidePaneItem("Open in Browser") { openLoginPage(browser, login) })
            add(SidePaneItem("Cancel Login") {
                scope.launch { accountListRepo.cancelLogin(login).onFailure { notifyFailure("Not cancelled", it) } }
            })
        })
    }

    private fun showOnPhone(login: PendingLogin) {
        phoneLoginRelay.show(login)
        navigationNodeTraverser.navigateToNode(PhoneLoginScreen::class.java)
    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content))
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notify(Notification.NotificationImage.ALERT_TRIANGLE, top, AccountListRepo.describe(e))
    }
}
