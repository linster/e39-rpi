package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.e39pim.v1.ListMessagesResponse
import ca.stefanm.e39pim.v1.MailNotice
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendChannel
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Mail from the PIM bridge, through [MailGrpcClient]. Flows re-query when
 * the bridge reports changes ([MailChangeStream]) and retry every
 * [RETRY_INTERVAL] while it's unreachable, keeping what they last emitted.
 */
@ApplicationScope
class RemoteMailRepository @Inject constructor(
    private val client: MailGrpcClient,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    private val changeStream: MailChangeStream,
    private val logger: Logger,
) : MailRepository {

    companion object {
        const val TAG = "RemoteMailRepository"
        val RETRY_INTERVAL: Duration = 10.seconds

        /**
         * Fetches page [page] (or the last one there is) with [fetch], using
         * and extending [tokens] (each page's token; [0] is ""). Returns the
         * page's number and response. A known page that's now past the end
         * (the folder shrank) is dropped, back to the last one with messages.
         */
        internal suspend fun walkToPage(
            tokens: MutableList<String>,
            page: Int,
            fetch: suspend (token: String) -> ListMessagesResponse,
        ): Pair<Int, ListMessagesResponse> {
            var index = minOf(page, tokens.lastIndex)
            var response = fetch(tokens[index])
            while (index < page && response.nextPageToken.isNotEmpty()) {
                index++
                if (tokens.size == index) tokens.add(response.nextPageToken) else tokens[index] = response.nextPageToken
                response = fetch(tokens[index])
            }
            while (response.messagesCount == 0 && index > 0) {
                while (tokens.size > index) tokens.removeAt(tokens.lastIndex)
                index--
                response = fetch(tokens[index])
            }
            return index to response
        }
    }

    override fun folders(): Flow<List<MailFolder>> = reloading(
        what = "Folders",
        affects = { it.hasReady() || it.hasFoldersChanged() || it.hasMessagesChanged() },
        load = { client.listFolders().map { it.toMailFolder() } },
    )

    override fun messages(folderId: String, limit: Int, unreadOnly: Boolean): Flow<MessageList> {
        require(limit in 1..MailRepository.MAX_MESSAGES) { "limit $limit not in 1..${MailRepository.MAX_MESSAGES}" }
        return reloading(
            what = "Messages of $folderId",
            affects = { it.hasReady() || it.hasFoldersChanged() || (it.hasMessagesChanged() && it.messagesChanged.folderId == folderId) },
            load = { client.listMessages(folderId, limit, unreadOnly = unreadOnly).toMessageList() },
            // The folder is gone: nothing to retry until the folders change.
            onNotFound = { MessageList(emptyList(), 0) },
        )
    }

    // Page tokens are the bridge's (opaque): each page's is learned from the
    // page before it, and kept, so going back or forward is one call.
    private data class PagingKey(val folderId: String, val pageSize: Int, val unreadOnly: Boolean)
    private val pageTokens = mutableMapOf<PagingKey, MutableList<String>>()

    override fun messagePage(folderId: String, page: Int, pageSize: Int, unreadOnly: Boolean): Flow<MessagePage> {
        require(page >= 0) { "page $page is negative" }
        require(pageSize in 1..MailRepository.MAX_MESSAGES) { "pageSize $pageSize not in 1..${MailRepository.MAX_MESSAGES}" }
        return reloading(
            what = "Messages of $folderId (page $page)",
            affects = { it.hasReady() || it.hasFoldersChanged() || (it.hasMessagesChanged() && it.messagesChanged.folderId == folderId) },
            load = { loadPage(PagingKey(folderId, pageSize, unreadOnly), page) },
            onNotFound = { MessagePage(emptyList(), 0, 0, pageSize) },
        )
    }

    private suspend fun loadPage(key: PagingKey, page: Int): MessagePage {
        val tokens = synchronized(pageTokens) { pageTokens.getOrPut(key) { mutableListOf("") }.toMutableList() }
        val (index, response) = walkToPage(tokens, page) { token ->
            client.listMessages(key.folderId, key.pageSize, token, key.unreadOnly)
        }
        synchronized(pageTokens) { pageTokens[key] = tokens }
        return MessagePage(response.messagesList.map { it.toMailSummary() }, response.totalCount, index, key.pageSize)
    }

    override fun unreadInboxCount(): Flow<Long> = folders()
        .map { folders -> folders.filter { it.role == MailFolderRole.INBOX }.sumOf { it.unreadCount } }
        .distinctUntilChanged()

    override val events: Flow<MailEvent> = changeStream.notices.mapNotNull { it.toMailEvent() }

    override suspend fun getMessage(messageId: String, markSeen: Boolean): Result<MailMessage> = result {
        client.getMessage(messageId, markSeen).toMailMessage()
    }

    override suspend fun getMessageHtml(messageId: String, markSeen: Boolean): Result<MailMessageHtml> = result {
        client.getMessageHtml(messageId, markSeen).toMailMessageHtml()
    }

    override suspend fun setSeen(messageIds: Collection<String>, seen: Boolean): Result<Unit> = result {
        client.setFlags(messageIds, seen = seen)
    }

    override suspend fun setFlagged(messageIds: Collection<String>, flagged: Boolean): Result<Unit> = result {
        client.setFlags(messageIds, flagged = flagged)
    }

    override suspend fun move(messageIds: Collection<String>, folderId: String): Result<Unit> = result {
        client.move(messageIds, folderId)
    }

    override suspend fun trash(messageIds: Collection<String>): Result<Unit> = result {
        client.trash(messageIds)
    }

    override suspend fun send(mail: OutgoingMail): Result<String> = result {
        require(mail.to.isNotEmpty() || mail.cc.isNotEmpty() || mail.bcc.isNotEmpty()) { "No recipients" }
        client.send(mail)
    }

    override suspend fun search(text: String, folderId: String?, limit: Int): Result<MessageList> = result {
        require(text.isNotBlank()) { "Nothing to search for" }
        client.search(text.trim(), folderId ?: "", limit).toMessageList()
    }

    /**
     * Emits [load]'s result, again on notices that [affects] it and on new
     * bridge settings; retries while the bridge can't be reached.
     */
    private fun <T> reloading(
        what: String,
        affects: (MailNotice) -> Boolean,
        load: suspend () -> T,
        onNotFound: (() -> T)? = null,
    ): Flow<T> = channelFlow {
        val reloads = Channel<Unit>(Channel.CONFLATED)
        launch { changeStream.notices.collect { if (affects(it)) reloads.send(Unit) } }
        launch { settingsRepository.settings.drop(1).collect { reloads.send(Unit) } }
        reloads.send(Unit)

        var retry: Job? = null
        for (reload in reloads) {
            retry?.cancel()
            try {
                send(load())
            } catch (e: PimBackendException.NotFound) {
                if (onNotFound != null) {
                    send(onNotFound())
                } else {
                    logger.w(TAG, "$what: ${e.message}")
                }
            } catch (e: PimBackendException) {
                logger.w(TAG, "$what: ${e.message}")
                retry = launch {
                    delay(RETRY_INTERVAL)
                    backendChannel.resetConnectBackoff()
                    reloads.send(Unit)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun <T> result(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        logger.w(TAG, "PIM bridge call failed: ${e.message}")
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }
}
