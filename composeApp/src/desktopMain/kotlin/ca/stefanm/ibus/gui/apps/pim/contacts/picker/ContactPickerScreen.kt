package ca.stefanm.ibus.gui.pim.contacts.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.api.MailAddresses
import ca.stefanm.ibus.gui.pim.email.repo.api.RecipientSuggestions
import ca.stefanm.ibus.gui.pim.email.repo.api.Suggestions
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.PersonRow
import ca.stefanm.ibus.lib.logging.Logger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * Picks someone to send mail to: [RecipientSuggestions] (people from recent
 * mail first, then the address book's contacts with an e-mail address), a
 * search by keyboard (names and addresses; every address of the contacts
 * found), and typing an address. A long address book is cut off with a
 * hint to search. Returns [Result.Picked] with setResultAndGoBack; going
 * back picks no one.
 */
@ScreenDoc(
    screenName = "ContactPickerScreen",
    description = "Choose a recipient: recent mail and the address book",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ContactPickerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val suggestions: RecipientSuggestions,
    private val logger: Logger,
) : NavigationNode<ContactPickerScreen.Result> {
    override val thisClass: Class<out NavigationNode<Result>> = this::class.java

    companion object {
        const val TAG = "ContactPickerScreen"
        private val tokens = AtomicLong(System.nanoTime())

        fun open(navigationNodeTraverser: NavigationNodeTraverser, title: String = "Choose Recipient") {
            navigationNodeTraverser.navigateToNodeWithParameters(ContactPickerScreen::class.java, Request(title))
        }
    }

    data class Request(val title: String)

    sealed interface Result {
        /** [token] is unique per pick: the caller's back stack entry keeps the result, so apply it once. */
        data class Picked(val token: Long, val address: MailAddress) : Result
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { incoming ->
        val title = (incoming?.requestParameters as? Request)?.title ?: "Choose Recipient"
        val query = remember { mutableStateOf("") }
        val found = remember { mutableStateOf<Suggestions?>(null) }
        LaunchedEffect(query.value) {
            found.value = try {
                suggestions.suggest(query.value)
            } catch (e: Exception) {
                logger.w(TAG, "Suggestions: ${e.message}")
                Suggestions(emptyList())
            }
        }

        val items: List<MailListItem> = buildList {
            add(row("Cancel", "") { navigationNodeTraverser.goBack() })
            add(row("Search", query.value.ifBlank { "Type a name or address..." }) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, query.value) { query.value = it.trim() }
            })
            add(row("Type an Address", "e.g. anna@example.com") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, query.value.takeIf { '@' in it } ?: "") { typed ->
                    val address = MailAddresses.parse(typed)
                    if (address == null) {
                        notificationHub.postNotificationBackground(
                            Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Not an e-mail address", typed)
                        )
                    } else {
                        pick(address)
                    }
                }
            })
            val list = found.value
            when {
                list == null -> add(row("Looking...", "") {})
                list.recipients.isEmpty() -> add(row(if (query.value.isBlank()) "No one with an e-mail address" else "No one matches", "") {})
                else -> {
                    list.recipients.forEach { recipient ->
                        val address = recipient.address
                        add { allocatedIndex, currentIndex ->
                            PersonRow(
                                name = address.name.ifBlank { address.email },
                                detail = listOfNotNull(address.email.takeIf { address.name.isNotBlank() }, recipient.detail.takeIf { it.isNotBlank() })
                                    .joinToString(" · "),
                                isSelected = allocatedIndex == currentIndex,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) { pick(address) },
                            )
                        }
                    }
                    if (list.more > 0) {
                        add(row("${list.more} more: search to find them", "") {
                            modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, query.value) { query.value = it.trim() }
                        })
                    }
                }
            }
        }

        // SmoothScroll allocates knob positions once per row: rebuild it when
        // the number of rows changes. Labels update in place (the knob stays).
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = if (query.value.isBlank()) title else "$title · \"${query.value}\"",
                prependGoBackEntry = false,
                items = items,
            )
        }
    }

    private fun pick(address: MailAddress) {
        navigationNodeTraverser.setResultAndGoBack(this, Result.Picked(tokens.incrementAndGet(), address) as Result)
    }

    private fun row(label: String, value: String, onClicked: () -> Unit): MailListItem = { allocatedIndex, currentIndex ->
        ComposeFieldRow(
            label = label,
            value = value,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) { onClicked() },
        )
    }
}
