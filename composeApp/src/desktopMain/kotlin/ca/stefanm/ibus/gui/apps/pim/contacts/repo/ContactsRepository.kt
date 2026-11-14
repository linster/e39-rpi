package ca.stefanm.ibus.gui.pim.contacts.repo

import kotlinx.coroutines.flow.Flow

/**
 * Contacts for the HMI's screens and caller names. [RemoteContactsRepository]
 * (the PIM bridge) or [FakeContactsRepository] (sample data), chosen by
 * E39Config.PimConfig.Contacts.useFakeData (ContactsModule). Read-only.
 *
 * Flows re-emit when the contacts change; remember them in composables.
 * Calls return [Result]s, failing with PimBackendException (or
 * IllegalArgumentException for bad input).
 */
interface ContactsRepository {

    companion object {
        /** The bridge's largest page; [contacts] lists at most this many. */
        const val MAX_CONTACTS = 500
    }

    /** Every address book, by account, then name. */
    fun addressBooks(): Flow<List<AddressBook>>

    /**
     * The first [limit] (1..[MAX_CONTACTS]) contacts by name, of one address
     * book or all ([addressBookId] null). To show more, collect again with a
     * bigger limit ([ContactList.hasMore]).
     */
    fun contacts(limit: Int, addressBookId: String? = null): Flow<ContactList>

    suspend fun getContact(contactId: String): Result<Contact>

    /** Names, nickname, organization, e-mail; or phone digits if [text] is a number. Sorted by name. */
    suspend fun search(text: String, addressBookId: String? = null, limit: Int = 50): Result<ContactList>

    /** Who has this number: exact matches first. Empty if nobody (or the bridge can't be reached in time). */
    suspend fun findByPhoneNumber(number: String): Result<List<PhoneMatch>>

    /** The picture scaled to fit [maxSize] (16..1024); fails with NotFound if there's none. */
    suspend fun getPhoto(contactId: String, maxSize: Int = 128): Result<ContactPhoto>

    // Writing. Failures: InvalidArgument (nothing to call the contact by, a
    // bad number/e-mail/URL), FailedPrecondition (read-only address book),
    // Conflict (it changed since [ContactSummary.revision]: reload, retry).

    /** In [addressBookId] (null: the first writable one). */
    suspend fun createContact(input: ContactInput, addressBookId: String? = null): Result<Contact>

    /**
     * Changes [fields] of [contact] (empty: all) to [input]'s; the rest of
     * the contact (also what the HMI doesn't show) stays.
     */
    suspend fun updateContact(contact: ContactSummary, input: ContactInput, fields: Set<ContactField> = emptySet()): Result<Contact>

    suspend fun deleteContact(contact: ContactSummary): Result<Unit>
}
