package ca.stefanm.ibus.gui.pim.contacts.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneNumber
import ca.stefanm.ibus.gui.pim.contacts.repo.PostalAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactDetailsRepo
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactDraft
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactEditRepo
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Writing a contact: ContactEditRepo's draft as a form. Each row opens what
 * changes it: the keyboard for names, numbers (the telephone keyboard),
 * addresses; a side pane for a number's or address's type, preferred,
 * remove. Save creates the contact (then shows it) or changes what changed;
 * Cancel asks before throwing away changes. Start a draft
 * (ContactEditRepo.startNew/startEdit/startFromMail), then [open] this.
 */
@ScreenDoc(
    screenName = "ContactEditScreen",
    description = "Contacts: a new contact, or editing one",
    navigatesTo = [
        ScreenDoc.NavigateTo(ContactDetailsScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ContactEditScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val editRepo: ContactEditRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "ContactEditScreen"

        fun open(navigationNodeTraverser: NavigationNodeTraverser) {
            navigationNodeTraverser.navigateToNode(ContactEditScreen::class.java)
        }

        private val PHONE_TYPES = listOf(
            PhoneNumber.Type.MOBILE to "Mobile", PhoneNumber.Type.HOME to "Home", PhoneNumber.Type.WORK to "Work",
            PhoneNumber.Type.MAIN to "Main", PhoneNumber.Type.FAX to "Fax", PhoneNumber.Type.OTHER to "Other",
        )
        private val EMAIL_TYPES = listOf(EmailAddress.Type.HOME to "Home", EmailAddress.Type.WORK to "Work", EmailAddress.Type.OTHER to "Other")
        private val ADDRESS_TYPES = listOf(PostalAddress.Type.HOME to "Home", PostalAddress.Type.WORK to "Work", PostalAddress.Type.OTHER to "Other")
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{
        val draft = editRepo.draft.collectAsState().value
        val leaving = remember { mutableStateOf(false) }
        if (draft == null) {
            // Thrown away (Cancel), or nothing to edit.
            if (!leaving.value) LaunchedEffect(Unit) { navigationNodeTraverser.goBack() }
            return@content
        }
        val books = remember { editRepo.writableAddressBooks() }.collectAsState(emptyList()).value
        val saving = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val input = draft.input

        val items: List<MailListItem> = buildList {
            add(row("Cancel", "") { cancel() })
            if (draft.isNew && books.size > 1) {
                val book = books.firstOrNull { it.id == draft.addressBookId } ?: books.first()
                add(row("Address Book", "${book.name} (${book.accountName})") { showBooks(books) })
            }
            add(text("Given Name", input.givenName) { v -> editRepo.change { it.copy(givenName = v) } })
            add(text("Family Name", input.familyName) { v -> editRepo.change { it.copy(familyName = v) } })
            add(text("Organization", input.organization) { v -> editRepo.change { it.copy(organization = v) } })
            add(text("Title", input.title) { v -> editRepo.change { it.copy(title = v) } })
            add(text("Nickname", input.nickname) { v -> editRepo.change { it.copy(nickname = v) } })
            input.phones.forEachIndexed { i, p ->
                add(row(ContactDetailsRepo.phoneLabel(p), p.number) { showPhone(i, p) })
            }
            add(row("Add Phone Number...", "") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.TELEPHONE, "") { if (it.isNotBlank()) editRepo.addPhone(it) }
            })
            input.emails.forEachIndexed { i, e ->
                add(row(ContactDetailsRepo.emailLabel(e), e.address) { showEmail(i, e) })
            }
            add(row("Add E-mail Address...", "") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { if (it.isNotBlank()) editRepo.addEmail(it) }
            })
            input.addresses.forEachIndexed { i, a ->
                add(row(ContactDetailsRepo.addressLabel(a), ContactDetailsRepo.addressText(a).ifBlank { "(empty)" }, lines = 3) { showAddress(i, a) })
            }
            add(row("Add Address...", "") {
                val index = input.addresses.size
                val empty = PostalAddress(PostalAddress.Type.HOME, "", "", "", "", "", "", "")
                editRepo.setAddress(index, empty)
                showAddress(index, empty)
            })
            add(row("Birthday", input.birthday?.toString() ?: "Year-month-day...", dim = input.birthday == null) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, input.birthday?.toString() ?: "") { v ->
                    try {
                        editRepo.setBirthday(v)
                    } catch (e: IllegalArgumentException) {
                        notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not changed", e.message ?: "")
                    }
                }
            })
            add(row("Note", input.note.ifBlank { "Write..." }, lines = 3, dim = input.note.isBlank()) {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, input.note) { v -> editRepo.change { it.copy(note = v) } }
            })
            add(row(if (saving.value) "Saving..." else "Save", "") {
                if (!saving.value) save(scope, draft, saving, leaving)
            })
        }

        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
                header = if (draft.isNew) "New Contact" else "Edit: ${draft.original!!.summary.displayName}",
                prependGoBackEntry = false,
                items = items,
            )
        }
    }

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

    /** A text field: the keyboard, prefilled. */
    private fun text(label: String, value: String, set: (String) -> Unit): MailListItem =
        row(label, value.ifBlank { "(none)" }, dim = value.isBlank()) {
            modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, value) { set(it.trim()) }
        }

    private fun save(scope: CoroutineScope, draft: ContactDraft, saving: androidx.compose.runtime.MutableState<Boolean>,
                     leaving: androidx.compose.runtime.MutableState<Boolean>) {
        saving.value = true
        scope.launch {
            editRepo.save(discard = false).fold(
                onSuccess = { contact ->
                    leaving.value = true
                    navigationNodeTraverser.goBack()
                    if (draft.isNew) ContactDetailsScreen.open(navigationNodeTraverser, contact.summary.id)
                    editRepo.discard()
                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, if (draft.isNew) "Contact saved" else "Contact changed",
                        contact.summary.displayName)
                },
                onFailure = {
                    saving.value = false
                    notifyFailure("Not saved", it)
                },
            )
        }
    }

    private fun cancel() {
        if (!editRepo.hasChanges) {
            editRepo.discard()
            return
        }
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Throw Away the Changes?",
                text = @Composable { InfoLabel("What you changed will be lost.") },
                buttons = listOf(
                    TextMenuItem("Keep Editing", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Throw Away", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        editRepo.discard()
                    }),
                )
            )
        }
    }

    private fun showBooks(books: List<AddressBook>) {
        modalMenuService.showMailSidePane(logger, "$TAG books", "Save In", items = books.map { b ->
            SidePaneItem("${b.name} (${b.accountName})") { editRepo.setAddressBook(b.id) }
        })
    }

    private fun showPhone(index: Int, p: PhoneNumber) {
        modalMenuService.showMailSidePane(logger, "$TAG phone", p.number, listOf(ContactDetailsRepo.phoneLabel(p)), buildList {
            add(SidePaneItem("Change Number...") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.TELEPHONE, p.number) { v ->
                    if (v.isBlank()) editRepo.removePhone(index) else editRepo.updatePhone(index) { it.copy(number = v.trim(), e164 = "") }
                }
            })
            PHONE_TYPES.filter { it.first != p.type }.forEach { (type, name) ->
                add(SidePaneItem("Make It $name") { editRepo.updatePhone(index) { it.copy(type = type, label = "") } })
            }
            if (!p.preferred) add(SidePaneItem("Make It Preferred") { editRepo.preferPhone(index) })
            add(SidePaneItem("Remove") { editRepo.removePhone(index) })
        })
    }

    private fun showEmail(index: Int, e: EmailAddress) {
        modalMenuService.showMailSidePane(logger, "$TAG email", e.address, listOf(ContactDetailsRepo.emailLabel(e)), buildList {
            add(SidePaneItem("Change Address...") {
                modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, e.address) { v ->
                    if (v.isBlank()) editRepo.removeEmail(index) else editRepo.updateEmail(index) { it.copy(address = v.trim()) }
                }
            })
            EMAIL_TYPES.filter { it.first != e.type }.forEach { (type, name) ->
                add(SidePaneItem("Make It $name") { editRepo.updateEmail(index) { it.copy(type = type) } })
            }
            if (!e.preferred) add(SidePaneItem("Make It Preferred") { editRepo.preferEmail(index) })
            add(SidePaneItem("Remove") { editRepo.removeEmail(index) })
        })
    }

    private fun showAddress(index: Int, a: PostalAddress) {
        fun field(name: String, value: String, set: (PostalAddress, String) -> PostalAddress) = SidePaneItem("$name: ${value.ifBlank { "..." }}") {
            modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, value) { v ->
                val current = editRepo.draft.value?.input?.addresses?.getOrNull(index) ?: return@showKeyboard
                editRepo.setAddress(index, set(current, v.trim()))
            }
        }
        modalMenuService.showMailSidePane(logger, "$TAG address", ContactDetailsRepo.addressLabel(a), emptyList(), buildList {
            add(field("Street", a.street) { x, v -> x.copy(street = v) })
            add(field("City", a.locality) { x, v -> x.copy(locality = v) })
            add(field("Province / State", a.region) { x, v -> x.copy(region = v) })
            add(field("Postal Code", a.postalCode) { x, v -> x.copy(postalCode = v) })
            add(field("Country", a.country) { x, v -> x.copy(country = v) })
            ADDRESS_TYPES.filter { it.first != a.type }.forEach { (type, name) ->
                add(SidePaneItem("Make It $name") {
                    val current = editRepo.draft.value?.input?.addresses?.getOrNull(index) ?: return@SidePaneItem
                    editRepo.setAddress(index, current.copy(type = type, label = ""))
                })
            }
            add(SidePaneItem("Remove") { editRepo.removeAddress(index) })
        })
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
                is PimBackendException.Conflict -> "It changed meanwhile: go back and edit it again"
                is PimBackendException.Unavailable -> "Contacts not reachable: ${e.message}"
                else -> e.message ?: e.toString()
            }
        )
    }
}
