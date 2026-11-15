package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.Contact
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactField
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactInput
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactSummary
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneNumber
import ca.stefanm.ibus.gui.pim.contacts.repo.PostalAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/** A contact being written: [input] (what the form shows), [original] (null for a new one). */
data class ContactDraft(
    val input: ContactInput,
    /** The contact as loaded, for an edit: what's unchanged isn't sent. */
    val original: Contact? = null,
    /** New contacts: where (null: the first writable address book). */
    val addressBookId: String? = null,
) {
    val isNew: Boolean get() = original == null

    /** The fields that differ from [original] (all of them for a new contact). */
    fun changedFields(): Set<ContactField> {
        val before = original?.let { ContactInput.from(it) } ?: return ContactField.entries.toSet()
        val now = input
        return buildSet {
            if (now.givenName.trim() != before.givenName) add(ContactField.GIVEN_NAME)
            if (now.familyName.trim() != before.familyName) add(ContactField.FAMILY_NAME)
            if (now.organization.trim() != before.organization) add(ContactField.ORGANIZATION)
            if (now.nickname.trim() != before.nickname) add(ContactField.NICKNAME)
            if (now.title.trim() != before.title) add(ContactField.TITLE)
            if (now.phones.map { Triple(it.number, it.type, it.preferred) } != before.phones.map { Triple(it.number, it.type, it.preferred) }) add(ContactField.PHONES)
            if (now.emails.map { Triple(it.address, it.type, it.preferred) } != before.emails.map { Triple(it.address, it.type, it.preferred) }) add(ContactField.EMAILS)
            if (now.addresses.map { it.copy(formatted = "") } != before.addresses.map { it.copy(formatted = "") }) add(ContactField.ADDRESSES)
            if (now.birthday != before.birthday) add(ContactField.BIRTHDAY)
            if (now.note != before.note) add(ContactField.NOTE)
            if (now.urls != before.urls) add(ContactField.URLS)
        }
    }

    /** What the contact would be called (for titles and checks). */
    val displayName: String get() = with(input) {
        "${givenName.trim()} ${familyName.trim()}".trim().ifEmpty {
            listOf(organization, nickname, emails.firstOrNull()?.address ?: "", phones.firstOrNull()?.number ?: "")
                .map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: ""
        }
    }
}

/**
 * Creating, editing and deleting contacts. The contact being edited is kept
 * here (not in a screen), so it survives the keyboard and other screens,
 * like MailComposeRepo's draft. Start with [startNew], [startEdit] or
 * [startFromMail]; change it; [save] or [discard] it.
 */
@ApplicationScope
class ContactEditRepo @Inject constructor(
    private val contacts: ContactsRepository,
) {
    private val _draft = MutableStateFlow<ContactDraft?>(null)
    /** Null when nothing is being edited. */
    val draft: StateFlow<ContactDraft?> = _draft.asStateFlow()

    private var started: ContactDraft? = null

    /** Whether discarding loses something the user did. */
    val hasChanges: Boolean get() = _draft.value != null && _draft.value != started

    /** Address books new contacts can go in. */
    fun writableAddressBooks(): Flow<List<AddressBook>> = contacts.addressBooks().map { books -> books.filter { !it.readOnly } }

    /** Whether [contact] can be edited (its address book isn't read-only). */
    suspend fun canEdit(contact: ContactSummary): Boolean =
        contacts.addressBooks().first().firstOrNull { it.id == contact.addressBookId }?.readOnly == false

    fun startNew(addressBookId: String? = null, input: ContactInput = ContactInput()): ContactDraft = start(ContactDraft(input, null, addressBookId))

    /** Loads [contactId] fresh (its current revision) to edit it. */
    suspend fun startEdit(contactId: String): Result<ContactDraft> =
        contacts.getContact(contactId).map { start(ContactDraft(ContactInput.from(it), it)) }

    /** "Save as contact" for a mail sender: [name] split into given and family name, [email]. */
    fun startFromMail(name: String, email: String): ContactDraft {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() && !it.contains('@') }
        val input = ContactInput(
            givenName = words.dropLast(1).joinToString(" ").ifEmpty { words.firstOrNull() ?: "" },
            familyName = if (words.size > 1) words.last() else "",
            emails = listOf(EmailAddress(email.trim(), EmailAddress.Type.OTHER, preferred = true)),
        )
        return start(ContactDraft(input))
    }

    private fun start(draft: ContactDraft): ContactDraft {
        started = draft
        _draft.value = draft
        return draft
    }

    fun discard() {
        _draft.value = null
        started = null
    }

    // --- changes ---------------------------------------------------------

    fun change(f: (ContactInput) -> ContactInput) = _draft.update { d -> d?.copy(input = f(d.input)) }

    fun setAddressBook(addressBookId: String) = _draft.update { it?.copy(addressBookId = addressBookId) }

    /** A number (as typed); the first one is preferred. */
    fun addPhone(number: String, type: PhoneNumber.Type = PhoneNumber.Type.MOBILE) = change { i ->
        i.copy(phones = i.phones + PhoneNumber(number.trim(), "", type, "", preferred = i.phones.isEmpty()))
    }

    fun updatePhone(index: Int, f: (PhoneNumber) -> PhoneNumber) = change { i ->
        i.copy(phones = i.phones.mapIndexed { n, p -> if (n == index) f(p) else p })
    }

    /** Only one preferred number. */
    fun preferPhone(index: Int) = change { i -> i.copy(phones = i.phones.mapIndexed { n, p -> p.copy(preferred = n == index) }) }

    fun removePhone(index: Int) = change { i ->
        val left = i.phones.filterIndexed { n, _ -> n != index }
        i.copy(phones = if (left.isNotEmpty() && left.none { it.preferred }) left.mapIndexed { n, p -> p.copy(preferred = n == 0) } else left)
    }

    fun addEmail(address: String, type: EmailAddress.Type = EmailAddress.Type.OTHER) = change { i ->
        i.copy(emails = i.emails + EmailAddress(address.trim(), type, preferred = i.emails.isEmpty()))
    }

    fun updateEmail(index: Int, f: (EmailAddress) -> EmailAddress) = change { i ->
        i.copy(emails = i.emails.mapIndexed { n, e -> if (n == index) f(e) else e })
    }

    fun preferEmail(index: Int) = change { i -> i.copy(emails = i.emails.mapIndexed { n, e -> e.copy(preferred = n == index) }) }

    fun removeEmail(index: Int) = change { i ->
        val left = i.emails.filterIndexed { n, _ -> n != index }
        i.copy(emails = if (left.isNotEmpty() && left.none { it.preferred }) left.mapIndexed { n, e -> e.copy(preferred = n == 0) } else left)
    }

    /** The address at [index], or a new one ([index] = the list's size). */
    fun setAddress(index: Int, address: PostalAddress) = change { i ->
        i.copy(addresses = if (index >= i.addresses.size) i.addresses + address else i.addresses.mapIndexed { n, a -> if (n == index) address else a })
    }

    fun removeAddress(index: Int) = change { i -> i.copy(addresses = i.addresses.filterIndexed { n, _ -> n != index }) }

    /** "1990-05-17" or empty (no birthday); IllegalArgumentException for anything else. */
    fun setBirthday(text: String) {
        val t = text.trim()
        val date = if (t.isEmpty()) null else runCatching { LocalDate.parse(t) }.getOrElse {
            throw IllegalArgumentException("Not a date (year-month-day, e.g. 1990-05-17): $t")
        }
        change { it.copy(birthday = date) }
    }

    // --- saving ------------------------------------------------------------

    /**
     * Creates the contact, or changes what changed in the edited one.
     * Failures (the draft is kept): IllegalArgumentException (nothing to call
     * it by, nothing changed), the repository's (a bad number or address,
     * read-only, Conflict: it changed meanwhile). [discard]: forget the
     * draft once saved (false: the caller does, e.g. after leaving the form).
     */
    suspend fun save(discard: Boolean = true): Result<Contact> {
        val d = _draft.value ?: return Result.failure(IllegalStateException("Nothing to save"))
        if (d.displayName.isEmpty()) return Result.failure(IllegalArgumentException("Give it a name, organization, number or e-mail address"))
        val input = cleaned(d.input)
        val result = if (d.isNew) {
            contacts.createContact(input, d.addressBookId)
        } else {
            val fields = d.copy(input = input).changedFields()
            if (fields.isEmpty()) return Result.success(d.original!!).also { if (discard) discard() }
            contacts.updateContact(d.original!!.summary, input, fields)
        }
        return result.onSuccess { if (discard && _draft.value == d) discard() }
    }

    /** Deletes [contact] (its revision is checked: Conflict if it changed meanwhile). */
    suspend fun delete(contact: ContactSummary): Result<Unit> = contacts.deleteContact(contact)

    /** Empty numbers, addresses and the like dropped. */
    private fun cleaned(i: ContactInput) = i.copy(
        phones = i.phones.filter { it.number.isNotBlank() },
        emails = i.emails.filter { it.address.isNotBlank() },
        addresses = i.addresses.filter { listOf(it.street, it.locality, it.region, it.postalCode, it.country).any { p -> p.isNotBlank() } },
        urls = i.urls.filter { it.isNotBlank() },
    )
}
