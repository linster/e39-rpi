package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactSummary
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/** A contact as a list row shows it. */
data class ContactRow(
    val summary: ContactSummary,
    val name: String,
    /** The organization (when the name is someone's), else the first number or e-mail address. */
    val detail: String,
    /** A–Z, or # for names that don't start with a letter. */
    val initial: Char,
    /** The first row of its letter on the page: a caption goes above it. */
    val firstOfInitial: Boolean,
) {
    val id: String get() = summary.id
}

/** A letter of the A–Z index: [count] contacts, the first at [offset] in the list. */
data class ContactInitial(val letter: Char, val count: Int, val offset: Int)

/** What the contact list shows: [rows] from [offset] in an address book's (or all) contacts. */
data class ContactListState(
    /** Null: all address books. */
    val addressBook: AddressBook?,
    val rows: List<ContactRow>,
    val offset: Int,
    val totalCount: Int,
    val pageSize: Int,
    /** The letters that have contacts, in order. */
    val initials: List<ContactInitial>,
) {
    val hasPrevious: Boolean get() = offset > 0
    val hasNext: Boolean get() = offset + rows.size < totalCount
    /** "1–100 of 259", "No contacts". */
    val rangeLabel: String get() =
        if (rows.isEmpty()) "No contacts" else "${offset + 1}–${offset + rows.size} of $totalCount"
    /** The address book's name, or "All". */
    val bookLabel: String get() = addressBook?.name ?: "All"
}

/**
 * Contact lists, [PAGE_SIZE] at a time from a position (the knob's loop
 * runs through the toolbar and every row, so long lists are paged), with
 * an A–Z index: jumping to a letter starts the page at its first contact.
 * The whole list comes from ContactsRepository.allContacts (summaries,
 * sorted by the bridge); paging and letters are slices of it. Remembers
 * each address book's place for the run.
 */
@ApplicationScope
class ContactListRepo @Inject constructor(
    private val contacts: ContactsRepository,
) {
    companion object {
        const val PAGE_SIZE = 100
        private const val ALL = "*"

        fun rowsOf(slice: List<ContactSummary>): List<ContactRow> {
            var previous: Char? = null
            return slice.map { s ->
                val initial = s.initial.uppercaseChar()
                val detail = when {
                    s.organization.isNotBlank() && s.organization != s.displayName -> s.organization
                    s.primaryPhone.isNotBlank() -> s.primaryPhone
                    s.primaryEmail.isNotBlank() && s.primaryEmail != s.displayName -> s.primaryEmail
                    else -> ""
                }
                ContactRow(s, s.displayName.ifBlank { "(no name)" }, detail, initial, initial != previous).also { previous = initial }
            }
        }

        fun initialsOf(all: List<ContactSummary>): List<ContactInitial> =
            all.withIndex().groupBy { it.value.initial.uppercaseChar() }
                .map { (letter, items) -> ContactInitial(letter, items.size, items.first().index) }
                .sortedBy { it.offset }

        /** Where [letter]'s contacts start: its first, else the next letter's; the last page if none come after. */
        fun offsetOf(letter: Char, initials: List<ContactInitial>, total: Int, pageSize: Int): Int =
            initials.firstOrNull { it.letter >= letter.uppercaseChar() }?.offset
                ?: lastPageStart(total, pageSize)

        fun lastPageStart(total: Int, pageSize: Int) = if (total == 0) 0 else ((total - 1) / pageSize) * pageSize
    }

    private val positions = ConcurrentHashMap<String, Int>()

    private val _selected = MutableStateFlow<String?>(null)
    /** The address book the list shows (null: all), for the rest of the run. */
    val selectedAddressBookId: StateFlow<String?> = _selected.asStateFlow()

    fun selectAddressBook(addressBookId: String?) {
        _selected.value = addressBookId
    }

    fun addressBooks(): Flow<List<AddressBook>> = contacts.addressBooks()

    /** A pager for [addressBookId] (null: all), starting where it was left. */
    fun pager(addressBookId: String?, pageSize: Int = PAGE_SIZE): ContactPager {
        val key = addressBookId ?: ALL
        return ContactPager(addressBookId, pageSize, contacts, positions[key] ?: 0) { positions[key] = it }
    }

    fun resetPosition(addressBookId: String?) {
        positions.remove(addressBookId ?: ALL)
    }

    /** Every list back to its start, all address books (UI tests). */
    fun clearPositions() {
        positions.clear()
        _selected.value = null
    }
}

class ContactPager internal constructor(
    val addressBookId: String?,
    private val pageSize: Int,
    private val contacts: ContactsRepository,
    initialOffset: Int,
    private val onMoved: (Int) -> Unit,
) {
    private val _offset = MutableStateFlow(initialOffset)
    val offset: StateFlow<Int> = _offset.asStateFlow()

    // From the last list loaded: next() and jumps stay inside it.
    @Volatile
    private var total = Int.MAX_VALUE

    @Volatile
    private var initials: List<ContactInitial> = emptyList()

    val state: Flow<ContactListState> = combine(contacts.allContacts(addressBookId), contacts.addressBooks(), _offset) { list, books, requested ->
        val all = list.contacts
        total = all.size
        initials = ContactListRepo.initialsOf(all)
        // The list shrank below the place: its last page.
        val offset = if (requested >= all.size) ContactListRepo.lastPageStart(all.size, pageSize) else requested
        ContactListState(books.firstOrNull { it.id == addressBookId }, ContactListRepo.rowsOf(all.drop(offset).take(pageSize)),
            offset, all.size, pageSize, initials)
    }

    fun next() = move { o -> if (o + pageSize < total) o + pageSize else o }

    fun previous() = move { o -> maxOf(0, o - pageSize) }

    fun firstPage() = move { 0 }

    /** The page starts at [letter]'s first contact (or the next letter's). */
    fun jumpTo(letter: Char) = move { ContactListRepo.offsetOf(letter, initials, total, pageSize) }

    private fun move(change: (Int) -> Int) {
        _offset.update(change)
        onMoved(_offset.value)
    }
}
