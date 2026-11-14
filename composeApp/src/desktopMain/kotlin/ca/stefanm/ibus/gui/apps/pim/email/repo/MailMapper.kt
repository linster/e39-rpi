package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.e39pim.v1.Attachment
import ca.stefanm.e39pim.v1.ListMessagesResponse
import ca.stefanm.e39pim.v1.MailFolderRole as ProtoRole
import ca.stefanm.e39pim.v1.MailAddress as ProtoAddress
import ca.stefanm.e39pim.v1.MailFolder as ProtoFolder
import ca.stefanm.e39pim.v1.MailMessage as ProtoMessage
import ca.stefanm.e39pim.v1.MailMessageHtml as ProtoMessageHtml
import ca.stefanm.e39pim.v1.MailNotice
import ca.stefanm.e39pim.v1.MessageSummary
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.toInstant

// e39pim.v1 mail messages -> the HMI's mail models.

fun ProtoFolder.toMailFolder() = MailFolder(
    id = id,
    accountId = accountId,
    accountName = accountName,
    name = name,
    path = pathList,
    parentId = parentId.ifEmpty { null },
    role = when (role) {
        ProtoRole.MAIL_FOLDER_ROLE_INBOX -> MailFolderRole.INBOX
        ProtoRole.MAIL_FOLDER_ROLE_SENT -> MailFolderRole.SENT
        ProtoRole.MAIL_FOLDER_ROLE_DRAFTS -> MailFolderRole.DRAFTS
        ProtoRole.MAIL_FOLDER_ROLE_TRASH -> MailFolderRole.TRASH
        ProtoRole.MAIL_FOLDER_ROLE_JUNK -> MailFolderRole.JUNK
        ProtoRole.MAIL_FOLDER_ROLE_OUTBOX -> MailFolderRole.OUTBOX
        else -> MailFolderRole.OTHER
    },
    unreadCount = unreadCount,
    totalCount = totalCount,
    holdsMessages = holdsMessages,
    readOnly = readOnly,
)

fun ProtoAddress.toMailAddress() = MailAddress(name = name, email = email)

fun MessageSummary.toMailSummary() = MailSummary(
    id = id,
    folderId = folderId,
    subject = subject,
    from = from.toMailAddress(),
    to = toList.map { it.toMailAddress() },
    date = date.toInstant(),
    seen = seen,
    flagged = flagged,
    answered = answered,
    forwarded = forwarded,
    hasAttachments = hasAttachments,
    sizeBytes = sizeBytes,
)

fun Attachment.toMailAttachment() = MailAttachment(
    index = index,
    filename = filename,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
)

fun ProtoMessage.toMailMessage() = MailMessage(
    summary = summary.toMailSummary(),
    cc = ccList.map { it.toMailAddress() },
    replyTo = replyToList.map { it.toMailAddress() },
    messageId = messageId,
    text = text,
    textWasHtml = textWasHtml,
    attachments = attachmentsList.map { it.toMailAttachment() },
)

fun ProtoMessageHtml.toMailMessageHtml() = MailMessageHtml(
    message = message.toMailMessage(),
    html = html,
)

fun ListMessagesResponse.toMessageList() = MessageList(
    messages = messagesList.map { it.toMailSummary() },
    totalCount = totalCount,
)

/** The notice as an event for notifications; null for "re-query" notices. */
fun MailNotice.toMailEvent(): MailEvent? = when {
    hasNewMail() -> MailEvent.NewMail(newMail.folderId, newMail.accountId, newMail.count, newMail.latest.toMailSummary())
    hasSent() -> MailEvent.Sent(sent.outboxMessageId, sent.subject)
    hasSendFailed() -> MailEvent.SendFailed(sendFailed.outboxMessageId, sendFailed.subject, sendFailed.reason)
    else -> null
}
