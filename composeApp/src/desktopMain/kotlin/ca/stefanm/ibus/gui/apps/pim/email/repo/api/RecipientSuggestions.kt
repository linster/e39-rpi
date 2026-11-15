package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Someone to send to, as the contact picker lists them. */
data class Recipient(
    val address: MailAddress,
    /** Where they're from: "Recent", the address book ("Personal"), or both ("Recent · Personal"). */
    val detail: String,
    val recent: Boolean,
)

/** [recipients], and how many more matched than are listed. */
data class Suggestions(val recipients: List<Recipient>, val more: Int = 0)

/**
 * Who a message could go to, for the contact picker. MailModule provides
 * ContactRecipientSuggestions (the address book and recent mail).
 */
interface RecipientSuggestions {
    /** Matching [query] in name or address (all if blank), best first, at most [limit]. */
    suspend fun suggest(query: String = "", limit: Int = 100): Suggestions
}

/** The people in recent mail: senders in the inboxes, recipients in sent mail; newest first, without the user. */
class RecentCorrespondents @Inject constructor(
    private val mail: MailRepository,
    private val senders: MailSenders,
) {
    /** Matching [query] in name or address (all if blank), newest first. */
    suspend fun recent(query: String = "", limit: Int = 50): List<MailAddress> {
        val own = senders.ownAddresses()
        val folders = mail.folders().first().filter { it.role == MailFolderRole.INBOX || it.role == MailFolderRole.SENT }
        val dated = folders.flatMap { folder ->
            mail.messagePage(folder.id, 0).first().messages.flatMap { m ->
                (if (folder.role == MailFolderRole.SENT) m.to else listOf(m.from)).map { it to m.date }
            }
        }
        val q = query.trim().lowercase()
        return dated.sortedByDescending { it.second }
            .map { it.first }
            .filter { it.email.isNotBlank() && it.email.lowercase() !in own }
            // The newest one for an address, but a name from any of them.
            .groupBy { it.email.lowercase() }
            .map { (_, all) -> all.firstOrNull { it.name.isNotBlank() } ?: all.first() }
            .filter { q.isEmpty() || it.name.lowercase().contains(q) || it.email.lowercase().contains(q) }
            .take(limit)
    }
}
