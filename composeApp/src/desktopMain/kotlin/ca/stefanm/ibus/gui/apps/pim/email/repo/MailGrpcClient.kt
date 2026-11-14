package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.e39pim.v1.ListMessagesResponse
import ca.stefanm.e39pim.v1.MailAddress as ProtoAddress
import ca.stefanm.e39pim.v1.MailFolder as ProtoFolder
import ca.stefanm.e39pim.v1.MailMessage as ProtoMessage
import ca.stefanm.e39pim.v1.MailMessageHtml as ProtoMessageHtml
import ca.stefanm.e39pim.v1.MailNotice
import ca.stefanm.e39pim.v1.MailServiceGrpcKt
import ca.stefanm.e39pim.v1.getMessageRequest
import ca.stefanm.e39pim.v1.listMailFoldersRequest
import ca.stefanm.e39pim.v1.listMessagesRequest
import ca.stefanm.e39pim.v1.mailAddress
import ca.stefanm.e39pim.v1.moveMessagesRequest
import ca.stefanm.e39pim.v1.searchMessagesRequest
import ca.stefanm.e39pim.v1.sendMessageRequest
import ca.stefanm.e39pim.v1.setMessageFlagsRequest
import ca.stefanm.e39pim.v1.trashMessagesRequest
import ca.stefanm.e39pim.v1.watchMailRequest
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendChannel
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.pimCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for mail: e39pim.v1 MailService calls, with protobuf
 * messages in and out. [RemoteMailRepository] builds on it. Every call
 * throws [PimBackendException] on failure.
 */
class MailGrpcClient @Inject constructor(
    private val backendChannel: PimBackendChannel,
) {
    companion object {
        /** Opening a message may download it from the account's server first. */
        const val GET_MESSAGE_TIMEOUT_SECONDS = 45L
    }

    private fun stub(seconds: Long = PimBackendChannel.CALL_TIMEOUT_SECONDS) =
        MailServiceGrpcKt.MailServiceCoroutineStub(backendChannel.channel())
            .withDeadlineAfter(seconds, TimeUnit.SECONDS)

    /** [accountId] empty: all accounts. Per account: inbox first, then the folder tree. */
    suspend fun listFolders(accountId: String = ""): List<ProtoFolder> = pimCall {
        stub().listMailFolders(listMailFoldersRequest { this.accountId = accountId }).foldersList
    }

    /** Newest first; [pageSize] 1..500. */
    suspend fun listMessages(folderId: String, pageSize: Int, pageToken: String = "", unreadOnly: Boolean = false): ListMessagesResponse = pimCall {
        stub().listMessages(listMessagesRequest {
            this.folderId = folderId
            this.pageSize = pageSize
            this.pageToken = pageToken
            this.unreadOnly = unreadOnly
        })
    }

    suspend fun getMessage(messageId: String, markSeen: Boolean): ProtoMessage = pimCall {
        stub(GET_MESSAGE_TIMEOUT_SECONDS).getMessage(getMessageRequest {
            this.messageId = messageId
            this.markSeen = markSeen
        })
    }

    suspend fun getMessageHtml(messageId: String, markSeen: Boolean): ProtoMessageHtml = pimCall {
        stub(GET_MESSAGE_TIMEOUT_SECONDS).getMessageHtml(getMessageRequest {
            this.messageId = messageId
            this.markSeen = markSeen
        })
    }

    /** Null: unchanged. */
    suspend fun setFlags(messageIds: Collection<String>, seen: Boolean? = null, flagged: Boolean? = null) {
        pimCall {
            stub().setMessageFlags(setMessageFlagsRequest {
                this.messageIds.addAll(messageIds)
                if (seen != null) this.seen = seen
                if (flagged != null) this.flagged = flagged
            })
        }
    }

    suspend fun move(messageIds: Collection<String>, folderId: String) {
        pimCall {
            stub().moveMessages(moveMessagesRequest {
                this.messageIds.addAll(messageIds)
                this.folderId = folderId
            })
        }
    }

    suspend fun trash(messageIds: Collection<String>) {
        pimCall { stub().trashMessages(trashMessagesRequest { this.messageIds.addAll(messageIds) }) }
    }

    /** Returns the outbox message ID, as [MailNotice] sent/send_failed name it. */
    suspend fun send(mail: OutgoingMail): String = pimCall {
        stub().sendMessage(sendMessageRequest {
            accountId = mail.accountId ?: ""
            to.addAll(mail.to.map { it.toProto() })
            cc.addAll(mail.cc.map { it.toProto() })
            bcc.addAll(mail.bcc.map { it.toProto() })
            subject = mail.subject
            text = mail.text
            inReplyToId = mail.inReplyTo?.id ?: ""
            forwardOfId = mail.forwardOf?.id ?: ""
        }).outboxMessageId
    }

    /** [folderId] empty: all folders. Headers only (subject, sender, recipients). */
    suspend fun search(text: String, folderId: String = "", limit: Int = 50): ListMessagesResponse = pimCall {
        stub().searchMessages(searchMessagesRequest {
            this.text = text
            this.folderId = folderId
            this.limit = limit
        })
    }

    /** `ready` first, then changes and events. */
    fun watchMail(): Flow<MailNotice> =
        MailServiceGrpcKt.MailServiceCoroutineStub(backendChannel.channel())
            .watchMail(watchMailRequest { })
            .catch { throw PimBackendException.from(it) }

    private fun MailAddress.toProto(): ProtoAddress = mailAddress {
        name = this@toProto.name
        email = this@toProto.email
    }
}
