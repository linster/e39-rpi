package ca.stefanm.ibus.gui.pim.email.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.pim.email.repo.api.MailAccountGroup
import ca.stefanm.ibus.gui.pim.email.repo.api.MailFormat
import ca.stefanm.ibus.gui.pim.email.repo.api.MailboxListRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageListRepo
import ca.stefanm.ibus.gui.pim.email.views.parts.FolderRowView
import ca.stefanm.ibus.gui.pim.email.views.parts.MailInfoText
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import javax.inject.Inject

/**
 * The mail app's first screen: every account's folders with unread counts
 * (the account's name above its first folder). Selecting a folder lists its
 * messages.
 */
@ScreenDoc(
    screenName = "MailScreen",
    description = "Mail: accounts and folders with unread counts",
    navigatesTo = [
        ScreenDoc.NavigateTo(MessageListScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MailScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val mailboxListRepo: MailboxListRepo,
    private val messageListRepo: MessageListRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MailScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val accounts = remember { mailboxListRepo.accounts() }.collectAsState(null).value
        val total = accounts?.sumOf { it.unreadCount } ?: 0L

        val items: List<MailListItem> = when {
            accounts == null -> listOf(info("Asking the PIM bridge..."))
            accounts.isEmpty() -> listOf(info("No mail accounts yet"))
            else -> accounts.flatMap { folderItems(it) }
        }

        // SmoothScroll allocates knob positions once: rebuild it when the
        // number of rows changes. Counts update in place.
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = if (total > 0) "Mail · ${MailFormat.unreadBadge(total)} unread" else "Mail",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun folderItems(account: MailAccountGroup): List<MailListItem> = account.folders.mapIndexed { i, folder ->
        { allocatedIndex, currentIndex ->
            FolderRowView(
                folder = folder,
                accountCaption = if (i == 0) account.accountName else null,
                accountUnread = account.unreadCount,
                isSelected = allocatedIndex == currentIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                    if (folder.holdsMessages) {
                        messageListRepo.resetPosition(folder.id)
                        MessageListScreen.open(navigationNodeTraverser, folder.id)
                    }
                },
            )
        }
    }

    private fun info(text: String): MailListItem = { allocatedIndex, currentIndex ->
        ArbitraryContentsMenuItem(
            chipOrientation = ItemChipOrientation.NONE,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {},
        ) { MailInfoText(text) }
    }
}
