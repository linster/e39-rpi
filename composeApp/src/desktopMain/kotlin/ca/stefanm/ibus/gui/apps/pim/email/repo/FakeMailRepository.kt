package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Sample mail in memory, for building screens without the PIM bridge
 * (E39Config.PimConfig.Mail.useFakeData). Two accounts; everything works
 * (flags, moves, trash, sending, search) until the HMI restarts. Sent mail
 * "goes out" after [SEND_DELAY]; [simulateNewMail] delivers a message.
 */
@ApplicationScope
class FakeMailRepository @Inject constructor() : MailRepository {

    companion object {
        val SEND_DELAY: Duration = 2.seconds
        const val ME_NAME = "Stefan"
        const val ME_EMAIL = "stefan@example.com"
    }

    private data class Store(val folders: List<MailFolder>, val messages: List<StoredMessage>)

    private data class StoredMessage(val summary: MailSummary, val text: String, val html: String = "", val cc: List<MailAddress> = emptyList())

    private var nextId = 1000
    private fun newId() = (nextId++).toString()

    private val store = MutableStateFlow(sampleData())
    private val _events = MutableSharedFlow<MailEvent>(extraBufferCapacity = 16)

    override val events: Flow<MailEvent> = _events.asSharedFlow()

    override fun folders(): Flow<List<MailFolder>> = store.map { it.withCounts() }.distinctUntilChanged()

    override fun messages(folderId: String, limit: Int, unreadOnly: Boolean): Flow<MessageList> {
        require(limit in 1..MailRepository.MAX_MESSAGES) { "limit $limit not in 1..${MailRepository.MAX_MESSAGES}" }
        return store.map { s ->
            val all = s.messages.map { it.summary }
                .filter { it.folderId == folderId && (!unreadOnly || !it.seen) }
                .sortedByDescending { it.date }
            MessageList(all.take(limit), all.size.toLong())
        }.distinctUntilChanged()
    }

    override fun unreadInboxCount(): Flow<Long> = folders()
        .map { folders -> folders.filter { it.role == MailFolderRole.INBOX }.sumOf { it.unreadCount } }
        .distinctUntilChanged()

    override suspend fun getMessage(messageId: String, markSeen: Boolean): Result<MailMessage> =
        getMessageHtml(messageId, markSeen).map { it.message }

    override suspend fun getMessageHtml(messageId: String, markSeen: Boolean): Result<MailMessageHtml> = attempt {
        val stored = find(messageId)
        if (markSeen && !stored.summary.seen) {
            modify(listOf(messageId)) { it.copy(seen = true) }
        }
        val summary = find(messageId).summary
        MailMessageHtml(
            message = MailMessage(
                summary = summary,
                cc = stored.cc,
                replyTo = emptyList(),
                messageId = "<fake-${summary.id}@example.com>",
                text = stored.text,
                textWasHtml = stored.html.isNotEmpty(),
                attachments = if (summary.hasAttachments) {
                    listOf(MailAttachment(1, "document.pdf", "application/pdf", summary.sizeBytes / 2))
                } else emptyList(),
            ),
            html = stored.html,
        )
    }

    override suspend fun setSeen(messageIds: Collection<String>, seen: Boolean): Result<Unit> = attempt {
        modify(messageIds) { it.copy(seen = seen) }
    }

    override suspend fun setFlagged(messageIds: Collection<String>, flagged: Boolean): Result<Unit> = attempt {
        modify(messageIds) { it.copy(flagged = flagged) }
    }

    override suspend fun move(messageIds: Collection<String>, folderId: String): Result<Unit> = attempt {
        val target = folder(folderId)
        messageIds.forEach { id ->
            val from = folder(find(id).summary.folderId)
            require(from.accountId == target.accountId) { "Messages can only move within their account" }
        }
        modify(messageIds) { it.copy(folderId = folderId) }
    }

    override suspend fun trash(messageIds: Collection<String>): Result<Unit> = attempt {
        messageIds.forEach { find(it) }
        store.update { s ->
            val trashOf = s.folders.filter { it.role == MailFolderRole.TRASH }.associateBy { it.accountId }
            val accountOf = s.folders.associate { it.id to it.accountId }
            s.copy(messages = s.messages.mapNotNull { m ->
                if (m.summary.id !in messageIds) return@mapNotNull m
                val trash = trashOf[accountOf[m.summary.folderId]]
                when {
                    trash == null || m.summary.folderId == trash.id -> null
                    else -> m.copy(summary = m.summary.copy(folderId = trash.id))
                }
            })
        }
    }

    override suspend fun send(mail: OutgoingMail): Result<String> = attempt {
        require(mail.to.isNotEmpty() || mail.cc.isNotEmpty() || mail.bcc.isNotEmpty()) { "No recipients" }
        val folders = store.value.folders
        val accountId = mail.accountId ?: folders.first().accountId
        val sent = folders.firstOrNull { it.accountId == accountId && it.role == MailFolderRole.SENT }
            ?: throw PimBackendException.NotFound("No account $accountId", null)
        val outboxId = newId()
        val summary = MailSummary(
            id = outboxId, folderId = sent.id, subject = mail.subject,
            from = MailAddress(ME_NAME, ME_EMAIL), to = mail.to, date = Clock.System.now(),
            seen = true, flagged = false, answered = false, forwarded = false,
            hasAttachments = mail.forwardOf != null, sizeBytes = mail.text.length.toLong() + 400,
        )
        // Like the bridge: it's queued now, and sent a moment later.
        delay(SEND_DELAY)
        store.update { it.copy(messages = it.messages + StoredMessage(summary, mail.text, cc = mail.cc)) }
        mail.inReplyTo?.let { modify(listOf(it.id)) { s -> s.copy(answered = true) } }
        mail.forwardOf?.let { modify(listOf(it.id)) { s -> s.copy(forwarded = true) } }
        _events.tryEmit(MailEvent.Sent(outboxId, mail.subject))
        outboxId
    }

    override suspend fun search(text: String, folderId: String?, limit: Int): Result<MessageList> = attempt {
        require(text.isNotBlank()) { "Nothing to search for" }
        val needle = text.trim()
        val hits = store.value.messages.map { it.summary }
            .filter { folderId == null || it.folderId == folderId }
            .filter { s ->
                (listOf(s.subject, s.from.name, s.from.email) + s.to.flatMap { listOf(it.name, it.email) })
                    .any { it.contains(needle, ignoreCase = true) }
            }
            .sortedByDescending { it.date }
        MessageList(hits.take(limit), hits.size.toLong())
    }

    /** A new unread message in the first account's inbox, with a [MailEvent.NewMail]. */
    fun simulateNewMail(
        from: MailAddress = MailAddress("Anna Kowalski", "anna@example.com"),
        subject: String = "Are you close?",
        text: String = "We're at the restaurant already. Table by the window.",
    ) {
        val inbox = store.value.folders.first { it.role == MailFolderRole.INBOX }
        val summary = MailSummary(
            id = newId(), folderId = inbox.id, subject = subject, from = from,
            to = listOf(MailAddress(ME_NAME, ME_EMAIL)), date = Clock.System.now(),
            seen = false, flagged = false, answered = false, forwarded = false,
            hasAttachments = false, sizeBytes = text.length.toLong() + 600,
        )
        store.update { it.copy(messages = it.messages + StoredMessage(summary, text)) }
        _events.tryEmit(MailEvent.NewMail(inbox.id, inbox.accountId, 1, summary))
    }

    private fun find(messageId: String): StoredMessage =
        store.value.messages.firstOrNull { it.summary.id == messageId }
            ?: throw PimBackendException.NotFound("No such message", null)

    private fun folder(folderId: String): MailFolder =
        store.value.folders.firstOrNull { it.id == folderId }
            ?: throw PimBackendException.NotFound("No such folder", null)

    private fun modify(messageIds: Collection<String>, change: (MailSummary) -> MailSummary) {
        messageIds.forEach { find(it) }
        store.update { s ->
            s.copy(messages = s.messages.map { if (it.summary.id in messageIds) it.copy(summary = change(it.summary)) else it })
        }
    }

    private fun Store.withCounts(): List<MailFolder> = folders.map { f ->
        val inFolder = messages.filter { it.summary.folderId == f.id }
        f.copy(totalCount = inFolder.size.toLong(), unreadCount = inFolder.count { !it.summary.seen }.toLong())
    }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }

    private fun sampleData(): Store {
        fun folder(id: String, account: String, accountName: String, path: List<String>, role: MailFolderRole, parent: String? = null) =
            MailFolder(id, account, accountName, path.last(), path, parent, role, 0, 0, holdsMessages = true, readOnly = false)

        val folders = listOf(
            folder("1", "fake_home", "Home", listOf("Inbox"), MailFolderRole.INBOX),
            folder("2", "fake_home", "Home", listOf("Inbox", "Car club"), MailFolderRole.OTHER, parent = "1"),
            folder("3", "fake_home", "Home", listOf("Sent"), MailFolderRole.SENT),
            folder("4", "fake_home", "Home", listOf("Drafts"), MailFolderRole.DRAFTS),
            folder("5", "fake_home", "Home", listOf("Trash"), MailFolderRole.TRASH),
            folder("11", "fake_work", "Work", listOf("Inbox"), MailFolderRole.INBOX),
            folder("12", "fake_work", "Work", listOf("Sent Items"), MailFolderRole.SENT),
            folder("13", "fake_work", "Work", listOf("Deleted Items"), MailFolderRole.TRASH),
        )

        val now = Clock.System.now()
        val me = listOf(MailAddress(ME_NAME, ME_EMAIL))
        val workMe = listOf(MailAddress(ME_NAME, "stefan@work.example.com"))
        var id = 1
        fun msg(
            folderId: String, ago: Duration, from: MailAddress, subject: String, text: String,
            seen: Boolean = true, flagged: Boolean = false, attachment: Boolean = false, html: String = "",
            to: List<MailAddress> = me, answered: Boolean = false,
        ) = StoredMessage(
            MailSummary(
                id = (id++).toString(), folderId = folderId, subject = subject, from = from, to = to,
                date = now - ago, seen = seen, flagged = flagged, answered = answered, forwarded = false,
                hasAttachments = attachment, sizeBytes = text.length * 2L + (if (attachment) 180_000 else 1_200),
            ),
            text = text, html = html,
        )

        val anna = MailAddress("Anna Kowalski", "anna@example.com")
        val garage = MailAddress("Prairie Euro Garage", "service@prairie-euro.example.com")
        val club = MailAddress("Saskatchewan BMW Club", "news@skbmwclub.example.com")
        val boss = MailAddress("Dana Whitford", "dana@work.example.com")
        val messages = listOf(
            msg("1", 25.minutes, anna, "Road trip plans",
                "Leaving Saturday at 8. Bring the cooler.\n\nI'll pick up coffee on the way.", seen = false,
                html = "<p>Leaving <b>Saturday</b> at 8. Bring the cooler.</p><p>I'll pick up coffee on the way.</p>"),
            msg("1", 3.hours, garage, "Oil change reminder",
                "Your E39 is due for an oil change (last one: 9 800 km ago).\nBook online or call us.", seen = false),
            msg("1", 1.days + 2.hours, MailAddress("SGI", "noreply@sgi.example.com"), "Insurance documents",
                "Your renewed registration and insurance are attached.", attachment = true, flagged = true),
            msg("1", 2.days, anna, "Re: Dinner on Friday",
                "Sounds good, see you at 7!\n\n> How about Friday?", answered = true),
            msg("1", 6.days, MailAddress("", "parts@pelican.example.com"), "Your order has shipped",
                "Order #48213: front control arms (2), tie rod ends (2).\nTracking: 1Z999AA10123456784"),
            msg("2", 4.days, club, "Spring meet in Regina",
                "Spring meet: May 16, Wascana Centre. Bring your car!", seen = false,
                html = "<html><body><h1>Spring&nbsp;meet in <b>Regina</b></h1><p>May 16, Wascana Centre.</p></body></html>"),
            msg("2", 33.days, club, "Winter storage tips",
                "Fuel stabiliser, battery tender, and overinflate the tires a little."),
            msg("3", 2.days + 1.hours, MailAddress(ME_NAME, ME_EMAIL), "Dinner on Friday",
                "How about Friday?", to = listOf(anna)),
            msg("5", 10.days, MailAddress("Deals", "deals@example.com"), "50% off floor mats",
                "This week only."),
            msg("11", 50.minutes, boss, "Standup moved to 10:30",
                "Standup is at 10:30 tomorrow; the room is booked until then.", seen = false, to = workMe),
            msg("11", 1.days + 5.hours, boss, "Q3 report draft",
                "Draft attached. Comments by Thursday please.", attachment = true, to = workMe),
            msg("12", 1.days, MailAddress(ME_NAME, "stefan@work.example.com"), "Re: Q3 report draft",
                "Looks good. Two small comments inline.", to = listOf(boss)),
        )
        return Store(folders, messages)
    }
}
