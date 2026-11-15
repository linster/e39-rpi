package ca.stefanm.ibus.gui.pim.email.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.contacts.picker.ContactPickerScreen
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.api.MailAddresses
import ca.stefanm.ibus.gui.pim.email.repo.api.MailComposeRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.MailDraft
import ca.stefanm.ibus.gui.pim.email.repo.api.MailFormat
import ca.stefanm.ibus.gui.pim.email.repo.api.MailSender
import ca.stefanm.ibus.gui.pim.email.repo.api.RecipientField
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Writing a message: MailComposeRepo's draft as a form. Each row opens what
 * changes it: recipients (the contact picker, or typed), subject and text
 * (the keyboard), quick replies, the quoted original. Leaving with Cancel
 * asks before throwing away what was written. Start a draft
 * (MailComposeRepo.startNew/startReply/startForward), then [open] this.
 */
@ScreenDoc(
    screenName = "MailComposeScreen",
    description = "Mail: writing a new message, reply or forward",
    navigatesTo = [
        ScreenDoc.NavigateTo(ContactPickerScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MailComposeScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val composeRepo: MailComposeRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MailComposeScreen"

        fun open(navigationNodeTraverser: NavigationNodeTraverser) {
            navigationNodeTraverser.navigateToNode(MailComposeScreen::class.java)
        }
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{ incoming ->
        val draft = composeRepo.draft.collectAsState().value
        if (draft == null) {
            // Sent or thrown away.
            LaunchedEffect(Unit) { navigationNodeTraverser.goBack() }
            return@content
        }
        LaunchedEffect(incoming) {
            val picked = incoming?.result as? ContactPickerScreen.Result.Picked
            if (incoming?.resultFrom == ContactPickerScreen::class.java && picked != null) {
                if (!composeRepo.applyPick(picked.token, picked.address)) {
                    logger.d(TAG, "Pick ${picked.token} already applied or already a recipient")
                }
            }
        }
        val senders = produceState(emptyList<MailSender>()) { value = runCatching { composeRepo.senders() }.getOrDefault(emptyList()) }.value
        val quickReplies = composeRepo.quickReplies.collectAsState().value
        val sending = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val items: List<MailListItem> = buildList {
            add(row("Cancel", "") { cancel() })
            if (senders.size > 1) {
                val sender = senders.firstOrNull { it.accountId == draft.accountId } ?: senders.first()
                add(row("From", sender.label) { showFromPane(senders) })
            }
            add(row("To", recipientsText(draft.to), dim = draft.to.isEmpty()) { showRecipientsPane(RecipientField.TO, draft.to) })
            add(row("Cc", recipientsText(draft.cc), dim = draft.cc.isEmpty()) { showRecipientsPane(RecipientField.CC, draft.cc) })
            add(row("Subject", draft.subject.ifBlank { "(no subject)" }, dim = draft.subject.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, draft.subject) { composeRepo.setSubject(it) }
            })
            add(row("Message", draft.text.ifBlank { "Write..." }, lines = 3, dim = draft.text.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, draft.text) { composeRepo.setText(it) }
            })
            if (quickReplies.isNotEmpty()) {
                add(row("Quick Reply...", "") { showQuickReplies(quickReplies) })
            }
            if (draft.quote.isNotEmpty()) {
                add(row("Original Message", if (draft.includeQuote) "Quoted below the text" else "Left out", dim = !draft.includeQuote) {
                    composeRepo.setIncludeQuote(!draft.includeQuote)
                })
            }
            if (draft.mode == MailDraft.Mode.FORWARD && draft.original != null) {
                add(row("Attached", draft.original.subject.ifBlank { "(no subject)" }) {})
            }
            add(row(if (sending.value) "Sending..." else "Send", "") {
                if (!sending.value) send(scope, sending)
            })
        }

        // SmoothScroll allocates knob positions once per row: rebuild it when
        // the number of rows changes. Labels update in place (the knob stays).
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = title(draft),
                prependGoBackEntry = false,
                items = items,
            )
        }
    }

    private fun title(draft: MailDraft): String = when (draft.mode) {
        MailDraft.Mode.NEW -> "New Message"
        MailDraft.Mode.REPLY -> "Reply"
        MailDraft.Mode.REPLY_ALL -> "Reply All"
        MailDraft.Mode.FORWARD -> "Forward"
    }

    private fun recipientsText(list: List<MailAddress>) = if (list.isEmpty()) "Add..." else MailFormat.addresses(list, max = 3)

    private fun row(label: String, value: String, lines: Int = 1, dim: Boolean = false, onClicked: () -> Unit): MailListItem =
        { allocatedIndex, currentIndex ->
            ComposeFieldRow(
                label = label,
                value = value,
                isSelected = allocatedIndex == currentIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) { onClicked() },
                valueLines = lines,
                dim = dim,
            )
        }

    private fun send(scope: CoroutineScope, sending: androidx.compose.runtime.MutableState<Boolean>) {
        sending.value = true
        scope.launch {
            // On success the draft is gone, and the screen goes back by itself;
            // MailNotifierService says when it's sent (or not).
            composeRepo.send().onFailure {
                sending.value = false
                notifyFailure("Not sent", it)
            }
        }
    }

    private fun cancel() {
        if (!composeRepo.hasChanges) {
            composeRepo.discard()
            return
        }
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Throw Away This Message?",
                text = @Composable { InfoLabel("What you wrote will be lost.") },
                buttons = listOf(
                    TextMenuItem("Keep Writing", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Throw Away", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        composeRepo.discard()
                    }),
                )
            )
        }
    }

    private fun showRecipientsPane(field: RecipientField, current: List<MailAddress>) {
        val name = if (field == RecipientField.TO) "To" else "Cc"
        modalMenuService.showMailSidePane(logger, "$TAG recipients", name, items = buildList {
            add(SidePaneItem("Add from Contacts...") {
                composeRepo.pickTarget = field
                ContactPickerScreen.open(navigationNodeTraverser, "$name: Choose Recipient")
            })
            add(SidePaneItem("Type an Address...") { typeAddress(field) })
            current.forEach { address ->
                add(SidePaneItem("✕ ${address.display}") { composeRepo.removeRecipient(field, address) })
            }
        })
    }

    private fun typeAddress(field: RecipientField) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { typed ->
            val address = MailAddresses.parse(typed)
            when {
                address == null -> notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not an e-mail address", typed)
                !composeRepo.addRecipient(field, address) -> notify(Notification.NotificationImage.ALERT_TRIANGLE, "Already a recipient", address.email)
            }
        }
    }

    private fun showFromPane(senders: List<MailSender>) {
        modalMenuService.showMailSidePane(logger, "$TAG from", "Send From", items = senders.map { sender ->
            SidePaneItem(sender.label) { composeRepo.setAccount(sender.accountId) }
        })
    }

    private fun showQuickReplies(replies: List<String>) {
        modalMenuService.showMailSidePane(logger, "$TAG quick replies", "Quick Reply", listOf("Replaces the message text."),
            replies.map { reply -> SidePaneItem(reply) { composeRepo.setText(reply) } })
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
                is PimBackendException.Unavailable -> "Mail not reachable: ${e.message}"
                else -> e.message ?: e.toString()
            }
        )
    }
}
