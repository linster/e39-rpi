package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import ca.stefanm.ibus.gui.pim.email.repo.MessagePage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.time.Clock

/** A message as a list row shows it. */
data class MessageRow(
    val summary: MailSummary,
    /** The sender; in sent mail, drafts and the outbox "To: " the recipients. */
    val correspondent: String,
    val subject: String,
    val date: String,
    val unread: Boolean,
    val flagged: Boolean,
    val hasAttachments: Boolean,
    val answered: Boolean,
    val forwarded: Boolean,
) {
    val id: String get() = summary.id
}

/** What the message list shows: one page of a folder. */
data class MessageListState(
    /** Null while unknown, or when the folder is gone. */
    val folder: MailFolder?,
    val page: MessagePage,
    val rows: List<MessageRow>,
    val unreadOnly: Boolean,
) {
    /** "1–100 of 250", "No messages". */
    val rangeLabel: String get() = MailFormat.range(page.firstNumber, rows.size, page.totalCount)
    /** "2/3". */
    val pageLabel: String get() = "${page.page + 1}/${page.pageCount}"
    /** The page's message IDs in order, for the reader's newer/older. */
    val messageIds: List<String> get() = rows.map { it.id }
}

/**
 * Message lists, [MailRepository.PAGE_SIZE] messages at a time: a screen's
 * knob loop runs through the toolbar and every row, so long folders are
 * paged rather than listed whole. Remembers each folder's page and filter
 * for the rest of the run, so coming back from a message keeps the place.
 */
@ApplicationScope
class MessageListRepo @Inject constructor(
    private val mail: MailRepository,
    private val mailbox: MailboxListRepo,
) {
    data class Position(val page: Int = 0, val unreadOnly: Boolean = false)

    private val positions = ConcurrentHashMap<String, Position>()

    /** A pager starting where the folder was left. */
    fun pager(folderId: String, pageSize: Int = MailRepository.PAGE_SIZE): MessagePager =
        MessagePager(folderId, pageSize, mail, mailbox.folder(folderId), positions[folderId] ?: Position()) {
            positions[folderId] = it
        }

    /** Back to the first page (e.g. on opening a folder from the folder list). */
    fun resetPosition(folderId: String) {
        positions[folderId] = (positions[folderId] ?: Position()).copy(page = 0)
    }
}

class MessagePager internal constructor(
    val folderId: String,
    private val pageSize: Int,
    private val mail: MailRepository,
    private val folder: Flow<MailFolder?>,
    initial: MessageListRepo.Position,
    private val onPositionChanged: (MessageListRepo.Position) -> Unit,
) {
    private val _position = MutableStateFlow(initial)
    val position: StateFlow<MessageListRepo.Position> = _position.asStateFlow()

    // Of the last page loaded: next() stops at the end.
    @Volatile
    private var pageCount = Int.MAX_VALUE

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<MessageListState> = _position.flatMapLatest { p ->
        combine(mail.messagePage(folderId, p.page, pageSize, p.unreadOnly), folder) { page, f ->
            val now = Clock.System.now()
            val outgoing = f?.role in setOf(MailFolderRole.SENT, MailFolderRole.DRAFTS, MailFolderRole.OUTBOX)
            MessageListState(f, page, page.messages.map { it.toRow(outgoing, now) }, p.unreadOnly)
        }.onEach { s ->
            pageCount = s.page.pageCount
            // The folder shrank: the repository gave its last page.
            if (s.page.page != p.page) move { it.copy(page = s.page.page) }
        }
    }

    fun next() = move { it.copy(page = minOf(it.page + 1, pageCount - 1)) }

    fun previous() = move { it.copy(page = maxOf(0, it.page - 1)) }

    fun firstPage() = move { it.copy(page = 0) }

    fun setUnreadOnly(unreadOnly: Boolean) = move { MessageListRepo.Position(0, unreadOnly) }

    /** Marks the unread messages of [state] (a page of this pager) read. */
    suspend fun markRead(state: MessageListState): Result<Unit> {
        val unread = state.rows.filter { it.unread }.map { it.id }
        return if (unread.isEmpty()) Result.success(Unit) else mail.setSeen(unread, true)
    }

    private fun move(change: (MessageListRepo.Position) -> MessageListRepo.Position) {
        _position.update(change)
        onPositionChanged(_position.value)
    }

    private fun MailSummary.toRow(outgoing: Boolean, now: kotlin.time.Instant) = MessageRow(
        summary = this,
        correspondent = if (outgoing) "To: ${MailFormat.addresses(to, max = 2)}" else from.display,
        subject = subject.ifBlank { "(no subject)" },
        date = MailFormat.listDate(date, now),
        unread = !seen,
        flagged = flagged,
        hasAttachments = hasAttachments,
        answered = answered,
        forwarded = forwarded,
    )
}
