package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.api.RecentCorrespondents
import ca.stefanm.ibus.gui.pim.email.repo.api.Recipient
import ca.stefanm.ibus.gui.pim.email.repo.api.RecipientSuggestions
import ca.stefanm.ibus.gui.pim.email.repo.api.Suggestions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.text.Normalizer
import javax.inject.Inject

/**
 * Whom mail can go to: people from recent mail first (up to [RECENT]),
 * then the address book's contacts that have an e-mail address, by name.
 * An address in both is listed once, with the contact's name. Without a
 * query, contacts are listed by their preferred address; a query searches
 * names and addresses (ContactsRepository.search) and lists every address
 * of the contacts found. If the address book doesn't answer within
 * [CONTACTS_TIMEOUT_MS] (the bridge unreachable), recent mail only.
 */
class ContactRecipientSuggestions @Inject constructor(
    private val contacts: ContactsRepository,
    private val recent: RecentCorrespondents,
) : RecipientSuggestions {

    companion object {
        const val RECENT = 10
        /** Contacts searched in full (each one's addresses) for a query. */
        const val SEARCHED = 30
        /** How long to wait for the address book (the bridge unreachable: recent mail only). */
        const val CONTACTS_TIMEOUT_MS = 3_000L

        fun fold(text: String) = Normalizer.normalize(text, Normalizer.Form.NFKD).replace(Regex("\\p{Mn}"), "").lowercase().trim()
    }

    private data class Candidate(val address: MailAddress, val book: String)

    private suspend fun fromContacts(q: String, query: String): List<Candidate> {
        val books = contacts.addressBooks().first().associate { it.id to it.name }
        return if (q.isEmpty()) {
            contacts.allContacts().first().contacts
                .filter { it.primaryEmail.isNotBlank() }
                .map { Candidate(MailAddress(it.displayName, it.primaryEmail), books[it.addressBookId] ?: "") }
        } else {
            val found = contacts.search(query.trim(), limit = SEARCHED).getOrNull()?.contacts.orEmpty()
            found.flatMap { summary ->
                val emails = if (summary.primaryEmail.isBlank()) emptyList()
                else contacts.getContact(summary.id).getOrNull()?.emails?.map { it.address } ?: listOf(summary.primaryEmail)
                emails.map { Candidate(MailAddress(summary.displayName, it), books[summary.addressBookId] ?: "") }
            }
        }
    }

    override suspend fun suggest(query: String, limit: Int): Suggestions {
        val q = fold(query)
        val fromContacts: List<Candidate> = withTimeoutOrNull(CONTACTS_TIMEOUT_MS) { fromContacts(q, query) } ?: emptyList()
        val byEmail = fromContacts.groupBy { it.address.email.lowercase() }

        val recents = recent.recent(query, RECENT).map { r ->
            val contact = byEmail[r.email.lowercase()]?.firstOrNull()
            Recipient(
                address = if (contact != null) MailAddress(contact.address.name, r.email) else r,
                detail = listOfNotNull("Recent", contact?.book?.takeIf { it.isNotEmpty() }).joinToString(" · "),
                recent = true,
            )
        }
        val listed = recents.map { it.address.email.lowercase() }.toMutableSet()
        val others = fromContacts.filter { listed.add(it.address.email.lowercase()) }
            .map { Recipient(it.address, it.book, recent = false) }
        val all = recents + others
        return Suggestions(all.take(limit), (all.size - limit).coerceAtLeast(0))
    }
}
