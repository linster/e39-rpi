package ca.stefanm.ibus.gui.pim.email.repo

import kotlinx.coroutines.flow.Flow

/**
 * Mail for the HMI's screens and the new-mail notifier. [RemoteMailRepository]
 * (the PIM bridge) or [FakeMailRepository] (sample data), chosen by
 * E39Config.PimConfig.Mail.useFakeData (MailModule).
 *
 * Flows re-emit when the mail changes; remember them in composables. Calls
 * return [Result]s, failing with PimBackendException (or
 * IllegalArgumentException for bad input).
 */
interface MailRepository {

    companion object {
        /** The bridge's largest page; [messages] lists at most this many. */
        const val MAX_MESSAGES = 500
        /** A page of [messagePage]: what a screen lists at once. */
        const val PAGE_SIZE = 100
    }

    /** Every account's folders: per account the inbox first, then the folder tree. */
    fun folders(): Flow<List<MailFolder>>

    /**
     * The newest [limit] (1..[MAX_MESSAGES]) messages of [folderId]. To show
     * more, collect again with a bigger limit ([MessageList.hasMore]).
     */
    fun messages(folderId: String, limit: Int, unreadOnly: Boolean = false): Flow<MessageList>

    /**
     * Page [page] (from 0) of [folderId], [pageSize] (1..[MAX_MESSAGES]) per
     * page: for folders too long to list at once. A page past the end gives
     * the last one (the folder shrank). Re-emits when the folder changes.
     */
    fun messagePage(folderId: String, page: Int, pageSize: Int = PAGE_SIZE, unreadOnly: Boolean = false): Flow<MessagePage>

    /** Unread messages in all inboxes (for a badge). */
    fun unreadInboxCount(): Flow<Long>

    /** New mail, sent, send failed: for notifications. Hot; nothing is replayed. */
    val events: Flow<MailEvent>

    /** The whole message; [markSeen] also marks it read. May download it first (slow). */
    suspend fun getMessage(messageId: String, markSeen: Boolean = true): Result<MailMessage>

    /** [getMessage] plus the HTML body, for a web view. */
    suspend fun getMessageHtml(messageId: String, markSeen: Boolean = true): Result<MailMessageHtml>

    suspend fun setSeen(messageIds: Collection<String>, seen: Boolean): Result<Unit>

    suspend fun setFlagged(messageIds: Collection<String>, flagged: Boolean): Result<Unit>

    /** Within one account. */
    suspend fun move(messageIds: Collection<String>, folderId: String): Result<Unit>

    /** To the account's trash; deleted for good if already there. */
    suspend fun trash(messageIds: Collection<String>): Result<Unit>

    /** Queues [mail]; [MailEvent.Sent]/[MailEvent.SendFailed] follow. Returns the outbox message ID. */
    suspend fun send(mail: OutgoingMail): Result<String>

    /** Subject, sender and recipients containing [text]; [folderId] null: all folders. Newest first. */
    suspend fun search(text: String, folderId: String? = null, limit: Int = 50): Result<MessageList>
}
