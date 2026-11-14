package ca.stefanm.ibus.gui.pim.email.repo

import kotlin.time.Instant

/** What a folder is for; drives icons, ordering and where trash/sent go. */
enum class MailFolderRole { INBOX, SENT, DRAFTS, TRASH, JUNK, OUTBOX, OTHER }

data class MailFolder(
    val id: String,
    val accountId: String,
    val accountName: String,
    /** This folder's own name, e.g. "Work" ("Inbox" for inboxes where the account names it). */
    val name: String,
    /** From the account's top folder down, e.g. ["Inbox", "Work"]. */
    val path: List<String>,
    /** Null for top-level folders. */
    val parentId: String?,
    val role: MailFolderRole,
    val unreadCount: Long,
    val totalCount: Long,
    /** False for folders that only group others. */
    val holdsMessages: Boolean,
    val readOnly: Boolean,
) {
    /** Nesting depth for indenting in lists (0 = top). */
    val depth: Int get() = (path.size - 1).coerceAtLeast(0)
}

data class MailAddress(val name: String, val email: String) {
    /** The name if there is one, else the address. */
    val display: String get() = name.ifBlank { email }

    override fun toString(): String = if (name.isBlank()) email else "$name <$email>"
}

data class MailSummary(
    val id: String,
    val folderId: String,
    val subject: String,
    val from: MailAddress,
    val to: List<MailAddress>,
    /** When it was sent (Date header), else when it arrived. */
    val date: Instant,
    val seen: Boolean,
    val flagged: Boolean,
    val answered: Boolean,
    val forwarded: Boolean,
    /** From the headers in lists; exact on a [MailMessage]. */
    val hasAttachments: Boolean,
    val sizeBytes: Long,
)

data class MailAttachment(
    val index: Int,
    val filename: String,
    val mimeType: String,
    val sizeBytes: Long,
)

/** A whole message: headers, its text (HTML turned into text by the bridge), attachments. */
data class MailMessage(
    val summary: MailSummary,
    val cc: List<MailAddress>,
    val replyTo: List<MailAddress>,
    /** The Message-ID header. */
    val messageId: String,
    val text: String,
    val textWasHtml: Boolean,
    val attachments: List<MailAttachment>,
)

/**
 * [message] plus its HTML body (empty if it has none), for a web view.
 * Not sanitised: don't run its scripts or load remote content.
 */
data class MailMessageHtml(
    val message: MailMessage,
    val html: String,
)

/** The newest [messages] of a folder (or search), out of [totalCount] that match. */
data class MessageList(
    val messages: List<MailSummary>,
    val totalCount: Long,
) {
    val hasMore: Boolean get() = messages.size < totalCount
}

/** A message to send. Reply: [inReplyTo]; forward: [forwardOf] (attached whole). */
data class OutgoingMail(
    val to: List<MailAddress>,
    val subject: String,
    val text: String,
    val cc: List<MailAddress> = emptyList(),
    val bcc: List<MailAddress> = emptyList(),
    /** Whose address and server to send with; null = the default. */
    val accountId: String? = null,
    val inReplyTo: MailSummary? = null,
    val forwardOf: MailSummary? = null,
)

/** Things that happen without being asked: for notifications. */
sealed interface MailEvent {
    /** [count] new unread messages in an inbox; [latest] is the newest. */
    data class NewMail(val folderId: String, val accountId: String, val count: Int, val latest: MailSummary) : MailEvent
    data class Sent(val outboxMessageId: String, val subject: String) : MailEvent
    data class SendFailed(val outboxMessageId: String, val subject: String, val reason: String) : MailEvent
}
