package ca.stefanm.ibus.gui.pim.email.views.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

/** The canned replies offered when writing mail: add, edit, reorder, delete. */
@ScreenDoc(
    screenName = "QuickRepliesScreen",
    description = "Mail settings: editing the quick replies",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class QuickRepliesScreen @Inject constructor(
    private val settings: MailSettingsRepository,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val logger: Logger,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "QuickRepliesScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val replies = settings.quickReplies.collectAsState().value

        val items: List<MenuItem> = buildList {
            add(TextMenuItem("Add Quick Reply...", onClicked = {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { text ->
                    attempt("Not added") { settings.addQuickReply(text) }
                }
            }))
            replies.forEachIndexed { i, reply ->
                add(TextMenuItem(reply, onClicked = { showReplyPane(replies, i) }))
            }
        }

        // SmoothScroll allocates knob positions once per row: rebuild it when
        // the number of rows changes. Labels update in place (the knob stays).
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "Quick Replies",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun showReplyPane(replies: List<String>, index: Int) {
        val reply = replies[index]
        modalMenuService.showMailSidePane(logger, "$TAG reply", "Quick Reply", listOf(reply), buildList {
            add(SidePaneItem("Edit...") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, reply) { text ->
                    attempt("Not changed") { settings.replaceQuickReply(index, text) }
                }
            })
            if (index > 0) add(SidePaneItem("Move Up") { settings.moveQuickReply(index, index - 1) })
            if (index < replies.lastIndex) add(SidePaneItem("Move Down") { settings.moveQuickReply(index, index + 1) })
            add(SidePaneItem("Delete") { settings.removeQuickReply(reply) })
        })
    }

    private fun attempt(failure: String, block: () -> Unit) {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            notificationHub.postNotificationBackground(
                Notification(Notification.NotificationImage.ALERT_TRIANGLE, failure, e.message ?: "")
            )
        }
    }
}
