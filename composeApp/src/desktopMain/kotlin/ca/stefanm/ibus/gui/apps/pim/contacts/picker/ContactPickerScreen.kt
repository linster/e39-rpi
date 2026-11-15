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
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.PersonRow
import ca.stefanm.ibus.lib.logging.Logger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * Picks someone to send mail to. A stub until the contacts app: it offers
 * [RecipientSuggestions] (people from recent mail for now), a search by
 * keyboard, and typing an address. Returns [Result.Picked] with
 * setResultAndGoBack; going back picks no one.
 *
 * TODO contacts app (e39-rpi-pim TODO.md phase 11): the address book,
 *  A–Z with knob jumps, a contact's several addresses (and phone numbers,
 *  for other callers).
 */
@ScreenDoc(
    screenName = "ContactPickerScreen",
    description = "Choose a recipient (stub: people from recent mail)",
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
        val found = remember { mutableStateOf<List<MailAddress>?>(null) }
        LaunchedEffect(query.value) {
            found.value = try {
                suggestions.suggest(query.value)
            } catch (e: Exception) {
                logger.w(TAG, "Suggestions: ${e.message}")
                emptyList()
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
                list.isEmpty() -> add(row(if (query.value.isBlank()) "No one in recent mail" else "No one matches", "") {})
                else -> list.forEach { address ->
                    add { allocatedIndex, currentIndex ->
                        PersonRow(
                            name = address.name.ifBlank { address.email },
                            detail = if (address.name.isBlank()) "" else address.email,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) { pick(address) },
                        )
                    }
                }
            }
        }

        // Rebuilt when the rows change (TODO.md: SmoothScroll and the knob).
        key(query.value, found.value) {
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
