package ca.stefanm.ibus.gui.pim.contacts.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactDetails
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactDetailsRepo
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactEditRepo
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ca.stefanm.ibus.gui.pim.contacts.repo.api.DetailRow
import ca.stefanm.ibus.gui.pim.contacts.views.parts.ContactHeaderView
import ca.stefanm.ibus.gui.pim.email.repo.api.MailComposeRepo
import ca.stefanm.ibus.gui.pim.email.views.MailComposeScreen
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailInfoText
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

/**
 * One contact: picture, name, title and organization, then numbers,
 * e-mail addresses (select one to write mail), addresses, birthday,
 * nickname, web pages, note.
 *
 * TODO calling (the Bluetooth code has no hands-free calls yet) and
 *  navigating to an address (needs geocoding: linster/e39-rpi-maps#4).
 */
@ScreenDoc(
    screenName = "ContactDetailsScreen",
    description = "Contacts: one contact's details",
    navigatesTo = [
        ScreenDoc.NavigateTo(MailComposeScreen::class),
        ScreenDoc.NavigateTo(ContactEditScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ContactDetailsScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val detailsRepo: ContactDetailsRepo,
    private val composeRepo: MailComposeRepo,
    private val editRepo: ContactEditRepo,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "ContactDetailsScreen"

        fun open(navigationNodeTraverser: NavigationNodeTraverser, contactId: String) {
            navigationNodeTraverser.navigateToNodeWithParameters(ContactDetailsScreen::class.java, Params(contactId))
        }
    }

    data class Params(val contactId: String)

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{ incoming ->
        val params = incoming?.requestParameters as? Params
        if (params == null) {
            LaunchedEffect(Unit) {
                logger.w(TAG, "Opened without a contact")
                navigationNodeTraverser.goBack()
            }
            return@content
        }
        val details = remember(params) { mutableStateOf<ContactDetails?>(null) }
        val error = remember(params) { mutableStateOf<String?>(null) }
        val photo = remember(params) { mutableStateOf<ByteArray?>(null) }
        LaunchedEffect(params) {
            detailsRepo.open(params.contactId).fold(
                onSuccess = { d ->
                    details.value = d
                    photo.value = detailsRepo.photo(d)
                },
                onFailure = {
                    logger.w(TAG, "Opening ${params.contactId}: ${it.message}")
                    error.value = it.message ?: it.toString()
                },
            )
        }

        val d = details.value
        val scope = rememberCoroutineScope()
        val items: List<MailListItem> = when {
            d != null -> listOf(header(d, photo.value)) + d.rows.map { row(d, it) } +
                if (d.addressBook?.readOnly == false) listOf(action("✎ Edit Contact") { edit(scope, d) }, action("🗑 Delete Contact...") { confirmDelete(scope, d) })
                else emptyList()
            error.value != null -> listOf(info("Couldn't open this contact: ${error.value}"))
            else -> listOf(info("Loading..."))
        }
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = d?.name ?: "Contact",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun header(d: ContactDetails, photo: ByteArray?): MailListItem = { allocatedIndex, currentIndex ->
        ContactHeaderView(d, photo, isSelected = allocatedIndex == currentIndex, onClicked = CallWhen(currentIndexIs = allocatedIndex) {})
    }

    private fun row(d: ContactDetails, row: DetailRow): MailListItem = { allocatedIndex, currentIndex ->
        ComposeFieldRow(
            label = row.label,
            value = row.value,
            isSelected = allocatedIndex == currentIndex,
            valueLines = if (row.kind == DetailRow.Kind.ADDRESS || row.kind == DetailRow.Kind.NOTE) 4 else 1,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                if (row.kind == DetailRow.Kind.EMAIL && row.email != null) showEmailActions(d, row)
            },
        )
    }

    private fun action(label: String, onClicked: () -> Unit): MailListItem = { allocatedIndex, currentIndex ->
        ComposeFieldRow(label, "", isSelected = allocatedIndex == currentIndex, onClicked = CallWhen(currentIndexIs = allocatedIndex) { onClicked() })
    }

    private fun edit(scope: CoroutineScope, d: ContactDetails) {
        scope.launch {
            editRepo.startEdit(d.contact.summary.id).fold(
                onSuccess = { ContactEditScreen.open(navigationNodeTraverser) },
                onFailure = { notifyFailure("Can't edit", it) },
            )
        }
    }

    private fun confirmDelete(scope: CoroutineScope, d: ContactDetails) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Delete ${d.name}?",
                text = @Composable {
                    InfoLabel("From ${d.addressBook?.name ?: "the address book"} (${d.addressBook?.accountName ?: ""}),")
                    InfoLabel("and from its server at the next sync.")
                },
                buttons = listOf(
                    TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Delete", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch {
                            editRepo.delete(d.contact.summary).fold(
                                onSuccess = {
                                    notificationHub.postNotificationBackground(
                                        Notification(Notification.NotificationImage.MESSAGE_CIRCLE, "Contact deleted", d.name))
                                    navigationNodeTraverser.goBack()
                                },
                                onFailure = { notifyFailure("Not deleted", it) },
                            )
                        }
                    }),
                )
            )
        }
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notificationHub.postNotificationBackground(Notification(Notification.NotificationImage.ALERT_TRIANGLE, top,
            if (e is ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException.Conflict) "It changed meanwhile: open it again"
            else e.message ?: e.toString()))
    }

    private fun showEmailActions(d: ContactDetails, row: DetailRow) {
        modalMenuService.showMailSidePane(logger, "$TAG email", row.value, listOf(d.name), listOf(
            SidePaneItem("Write Mail") {
                composeRepo.startNew(to = listOf(detailsRepo.mailRecipient(d, row.email!!)))
                MailComposeScreen.open(navigationNodeTraverser)
            },
        ))
    }

    private fun info(text: String): MailListItem = { allocatedIndex, currentIndex ->
        ArbitraryContentsMenuItem(
            chipOrientation = ItemChipOrientation.NONE,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {},
        ) { MailInfoText(text) }
    }
}
