package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** One account's folders, as the mail screens list them. */
data class MailAccountGroup(
    val accountId: String,
    val accountName: String,
    /** Unread mail worth opening the account for (not in trash, junk, sent, drafts, outbox). */
    val unreadCount: Long,
    /** The inbox first, then the folder tree (the bridge's order). */
    val folders: List<MailFolder>,
) {
    val inbox: MailFolder? get() = folders.firstOrNull { it.role == MailFolderRole.INBOX }
}

/** The accounts and folders screen, and the "Mail" entry's badge. */
@ApplicationScope
class MailboxListRepo @Inject constructor(
    private val mail: MailRepository,
) {
    companion object {
        /** Unread mail in these doesn't count towards an account's badge. */
        val UNCOUNTED_ROLES = setOf(MailFolderRole.TRASH, MailFolderRole.JUNK, MailFolderRole.SENT, MailFolderRole.DRAFTS, MailFolderRole.OUTBOX)

        fun group(folders: List<MailFolder>): List<MailAccountGroup> =
            folders.groupBy { it.accountId }.map { (accountId, own) ->
                MailAccountGroup(
                    accountId = accountId,
                    accountName = own.first().accountName,
                    unreadCount = own.filter { it.role !in UNCOUNTED_ROLES }.sumOf { it.unreadCount },
                    folders = own,
                )
            }
    }

    fun accounts(): Flow<List<MailAccountGroup>> = mail.folders().map { group(it) }.distinctUntilChanged()

    /** Null once the folder is gone. */
    fun folder(folderId: String): Flow<MailFolder?> =
        mail.folders().map { all -> all.firstOrNull { it.id == folderId } }.distinctUntilChanged()

    /** Unread in all inboxes: the main menu's badge. */
    fun unreadInboxCount(): Flow<Long> = mail.unreadInboxCount()

    /** Where [message] can be moved: the other folders of its account that hold messages and can be written. */
    suspend fun moveTargets(message: MailSummary): List<MailFolder> {
        val all = mail.folders().first()
        val accountId = all.firstOrNull { it.id == message.folderId }?.accountId ?: return emptyList()
        return all.filter { it.accountId == accountId && it.id != message.folderId && it.holdsMessages && !it.readOnly }
    }

    suspend fun folderOf(message: MailSummary): MailFolder? = mail.folders().first().firstOrNull { it.id == message.folderId }
}
