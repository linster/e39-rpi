package ca.stefanm.ibus.gui.pim.email.views.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.accounts.views.PimAccountsScreen
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import javax.inject.Inject

/**
 * The mail app's settings: new-mail and sent notifications, muted
 * accounts, quick replies, and sample data instead of the bridge.
 */
@ScreenDoc(
    screenName = "MailSettingsScreen",
    description = "Mail settings: notifications, muted accounts, quick replies",
    navigatesTo = [
        ScreenDoc.NavigateTo(MailMuteScreen::class),
        ScreenDoc.NavigateTo(QuickRepliesScreen::class),
        ScreenDoc.NavigateTo(PimAccountsScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MailSettingsScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val settings: MailSettingsRepository,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MailSettingsScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val notifications = settings.notifications.collectAsState().value
        val quickReplies = settings.quickReplies.collectAsState().value
        // Not a StateFlow: read again after changing it.
        val fake = remember { mutableStateOf(settings.useFakeData) }

        val items: List<MenuItem> = listOf(
            CheckBoxMenuItem(
                title = "Notify About New Mail",
                isChecked = notifications.newMail,
                onCheckChanged = { settings.setNewMailNotifications(it) },
            ),
            CheckBoxMenuItem(
                title = "Notify When Mail Is Sent or Fails",
                isChecked = notifications.sent,
                onCheckChanged = { settings.setSentNotifications(it) },
            ),
            TextMenuItem(
                title = "Muted Accounts..." + if (notifications.mutedAccountIds.isEmpty()) "" else " (${notifications.mutedAccountIds.size})",
                onClicked = { navigationNodeTraverser.navigateToNode(MailMuteScreen::class.java) },
            ),
            TextMenuItem(
                title = "Quick Replies... (${quickReplies.size})",
                onClicked = { navigationNodeTraverser.navigateToNode(QuickRepliesScreen::class.java) },
            ),
            TextMenuItem(
                title = "Mail Accounts...",
                onClicked = { navigationNodeTraverser.navigateToNode(PimAccountsScreen::class.java) },
            ),
            CheckBoxMenuItem(
                title = "Sample Mail Instead of the Bridge (After Restart)",
                isChecked = fake.value,
                onCheckChanged = {
                    settings.setUseFakeData(it)
                    fake.value = settings.useFakeData
                },
            ),
        )

        // SmoothScroll allocates knob positions once per row: rebuild it when
        // the number of rows changes. Labels update in place (the knob stays).
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Mail Settings",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }
}
