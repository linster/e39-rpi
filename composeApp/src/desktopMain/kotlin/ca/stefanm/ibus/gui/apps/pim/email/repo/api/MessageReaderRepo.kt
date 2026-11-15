package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.MailMessage
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import javax.inject.Inject

/** A message as the reader shows it. */
data class ReaderContent(
    val message: MailMessage,
    /** Label and value: From, To, Cc, Date (empty ones left out). */
    val headers: List<Pair<String, String>>,
    /** "invoice.pdf (182 KB)". */
    val attachments: List<String>,
    /** The folder it's in (null if unknown): what deleting does depends on it. */
    val folder: MailFolder?,
) {
    val summary: MailSummary get() = message.summary
    val subject: String get() = message.summary.subject.ifBlank { "(no subject)" }
    /** Deleting from the trash is for good. */
    val inTrash: Boolean get() = folder?.role == MailFolderRole.TRASH
}

/**
 * Reading one message and what can be done with it from the reader. The
 * actions return the message's new state, so a screen can show it without
 * reloading.
 */
@ApplicationScope
class MessageReaderRepo @Inject constructor(
    private val mail: MailRepository,
    private val mailbox: MailboxListRepo,
) {
    /** Loads it (may download it first) and marks it read. */
    suspend fun open(messageId: String): Result<ReaderContent> = mail.getMessage(messageId, markSeen = true).map { message ->
        ReaderContent(message, headersOf(message), attachmentsOf(message), mailbox.folderOf(message.summary))
    }

    suspend fun setSeen(summary: MailSummary, seen: Boolean): Result<MailSummary> =
        mail.setSeen(listOf(summary.id), seen).map { summary.copy(seen = seen) }

    suspend fun setFlagged(summary: MailSummary, flagged: Boolean): Result<MailSummary> =
        mail.setFlagged(listOf(summary.id), flagged).map { summary.copy(flagged = flagged) }

    suspend fun moveTargets(summary: MailSummary): List<MailFolder> = mailbox.moveTargets(summary)

    suspend fun move(summary: MailSummary, folder: MailFolder): Result<Unit> = mail.move(listOf(summary.id), folder.id)

    /** To the trash; for good if it's there already. */
    suspend fun delete(summary: MailSummary): Result<Unit> = mail.trash(listOf(summary.id))

    companion object {
        /** The message after [current] in [ids] (a list page), going [forward] (older) or back (newer); null at the ends. */
        fun neighbour(ids: List<String>, current: String, forward: Boolean): String? {
            val i = ids.indexOf(current)
            if (i < 0) return null
            return ids.getOrNull(if (forward) i + 1 else i - 1)
        }

        fun headersOf(message: MailMessage): List<Pair<String, String>> = buildList {
            add("From" to message.summary.from.toString())
            if (message.summary.to.isNotEmpty()) add("To" to MailFormat.addresses(message.summary.to, max = 5))
            if (message.cc.isNotEmpty()) add("Cc" to MailFormat.addresses(message.cc, max = 5))
            add("Date" to MailFormat.fullDate(message.summary.date))
        }

        fun attachmentsOf(message: MailMessage): List<String> =
            message.attachments.map { "${it.filename.ifBlank { "(unnamed ${it.mimeType})" }} (${MailFormat.size(it.sizeBytes)})" }
    }
}
