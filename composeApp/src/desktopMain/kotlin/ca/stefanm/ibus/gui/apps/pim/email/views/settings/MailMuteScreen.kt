package ca.stefanm.ibus.gui.pim.email.views.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import ca.stefanm.ibus.gui.pim.email.repo.api.MailboxListRepo
import javax.inject.Inject

/**
 * Which accounts' new mail shows a notification (checked: it does). Per
 * account, not per folder: the bridge only reports new mail in inboxes.
 */
@ScreenDoc(
    screenName = "MailMuteScreen",
    description = "Mail settings: new-mail notifications per account",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MailMuteScreen @Inject constructor(
    private val settings: MailSettingsRepository,
    private val mailboxListRepo: MailboxListRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MailMuteScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val notifications = settings.notifications.collectAsState().value
        val accounts = remember { mailboxListRepo.accounts() }.collectAsState(null).value

        val items: List<MenuItem> = buildList {
            if (!notifications.newMail) {
                add(TextMenuItem("New-mail notifications are off (Mail Settings)", isSelectable = false, onClicked = {}))
            }
            when {
                accounts == null -> add(TextMenuItem("Asking the PIM bridge...", isSelectable = false, onClicked = {}))
                accounts.isEmpty() -> add(TextMenuItem("No mail accounts yet", isSelectable = false, onClicked = {}))
                else -> accounts.forEach { account ->
                    add(CheckBoxMenuItem(
                        title = account.accountName,
                        isChecked = account.accountId !in notifications.mutedAccountIds,
                        onCheckChanged = { notify -> settings.setAccountMuted(account.accountId, !notify) },
                    ))
                }
            }
        }

        // SmoothScroll allocates knob positions once per row: rebuild it when
        // the number of rows changes. Labels update in place (the knob stays).
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Notify About New Mail In",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }
}
