package ca.stefanm.ibus.gui.pim.contacts.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.accounts.views.PimAccountsScreen
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactEditRepo
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactListRepo
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactListState
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactPager
import ca.stefanm.ibus.gui.pim.contacts.views.parts.ContactRowView
import ca.stefanm.ibus.gui.pim.email.views.parts.MailInfoText
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbar
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbarButton
import ca.stefanm.ibus.gui.pim.email.views.parts.SidePaneItem
import ca.stefanm.ibus.gui.pim.email.views.parts.showMailSidePane
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

/**
 * The contacts, by name, a page (ContactListRepo.PAGE_SIZE) at a time:
 * the toolbar and the rows share the knob. Toolbar: back, previous page,
 * A–Z (the page starts at a letter), next page, menu (which address book).
 */
@ScreenDoc(
    screenName = "ContactsScreen",
    description = "Contacts: the list, by name, with an A–Z index",
    navigatesTo = [
        ScreenDoc.NavigateTo(ContactDetailsScreen::class),
        ScreenDoc.NavigateTo(ContactEditScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ContactsScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val contactListRepo: ContactListRepo,
    private val editRepo: ContactEditRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "ContactsScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        val bookId = contactListRepo.selectedAddressBookId.collectAsState().value
        val books = remember { contactListRepo.addressBooks() }.collectAsState(emptyList()).value
        val pager = remember(bookId) { contactListRepo.pager(bookId) }
        val state = remember(pager) { pager.state }.collectAsState(null).value

        // A new page is a new screen for the knob (fresh positions for the
        // toolbar and the rows).
        key(bookId, state?.offset, state?.rows?.size) {
            val knobState = KnobObserverBuilderState.setupListener(knobListenerServiceMain, logger, TAG)
            Column(
                Modifier
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .fillMaxSize()
            ) {
                BmwSingleLineHeader(state?.let { "Contacts · ${it.bookLabel} · ${it.rangeLabel}" } ?: "Contacts")
                MailToolbar(
                    knobState = knobState,
                    buttons = listOf(
                        MailToolbarButton("Back", 1.2f) { navigationNodeTraverser.goBack() },
                        MailToolbarButton("◀", 0.8f) { pager.previous() },
                        MailToolbarButton("A–Z", 1.2f) { state?.let { showLetters(pager, it) } },
                        MailToolbarButton("▶", 0.8f) { pager.next() },
                        MailToolbarButton("☰", 0.8f) { state?.let { showMenu(pager, it, books) } },
                    ),
                )
                SmoothScroll.SmoothScroll(
                    modifier = Modifier,
                    knobState = knobState,
                    tag = TAG,
                    logger = logger,
                    items = rows(state),
                )
            }
        }
    }

    private fun rows(state: ContactListState?): List<MailListItem> = when {
        state == null -> listOf(info("Loading..."))
        state.rows.isEmpty() -> listOf(info("No contacts"))
        else -> state.rows.map { row ->
            { allocatedIndex, currentIndex ->
                ContactRowView(
                    row = row,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        ContactDetailsScreen.open(navigationNodeTraverser, row.id)
                    },
                )
            }
        }
    }

    private fun info(text: String): MailListItem = { allocatedIndex, currentIndex ->
        ArbitraryContentsMenuItem(
            chipOrientation = ItemChipOrientation.NONE,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {},
        ) { MailInfoText(text) }
    }

    private fun showLetters(pager: ContactPager, state: ContactListState) {
        modalMenuService.showMailSidePane(logger, "$TAG letters", "Jump To", items = state.initials.map { initial ->
            SidePaneItem("${initial.letter} · ${initial.count}") { pager.jumpTo(initial.letter) }
        })
    }

    private fun showMenu(pager: ContactPager, state: ContactListState, books: List<AddressBook>) {
        val total = books.sumOf { it.contactCount }
        modalMenuService.showMailSidePane(logger, "$TAG menu", "Contacts", listOf(state.bookLabel, state.rangeLabel), buildList {
            add(SidePaneItem("New Contact") {
                editRepo.startNew(state.addressBook?.takeIf { !it.readOnly }?.id)
                ContactEditScreen.open(navigationNodeTraverser)
            })
            add(SidePaneItem("All Address Books · $total") { contactListRepo.selectAddressBook(null) })
            books.forEach { book ->
                add(SidePaneItem("${book.name} (${book.accountName}) · ${book.contactCount}") { contactListRepo.selectAddressBook(book.id) })
            }
            if (state.hasPrevious) add(SidePaneItem("Back to the Start") { pager.firstPage() })
            add(SidePaneItem("Accounts...") { navigationNodeTraverser.navigateToNode(PimAccountsScreen::class.java) })
        })
    }
}
