package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatMessage
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageAuthor
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.MessageMetadata
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.toFlowList
import net.folivo.trixnity.client.store.isEncrypted
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.*
import net.folivo.trixnity.core.model.events.m.room.ImageInfo
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.client.flattenValues
import net.folivo.trixnity.client.flatten

suspend fun MessageFetcher(room: RoomId, matrixClient: MatrixClient, logger: Logger, onNewMessages : (List<ChatMessage>) -> Unit) {

    matrixClient.room.getLastTimelineEvents(room)
        .toFlowList(MutableStateFlow(50))
        .mapLatest { timelineEvents ->
            timelineEvents.map { timelineEvent ->
                timelineEvent.map {

                    val event = it.event
                    val messageAuthor = event.sender.let { userId ->
                        MessageAuthor(
                            matrixClient.user.getById(room, userId).first()?.name,
                            userId
                        )
                    }


                    val metadata = MessageMetadata(
                        Instant.fromEpochMilliseconds(event.originTimestamp),
                        event.id
                    )


                    when (it.content?.getOrNull()) {
                        null -> {
                            ChatMessage.TextChat(
                                contents = "${messageAuthor.name} not yet decrypted",
                                author = messageAuthor,
                                metadata = metadata.copy(isEncrypted = true)
                            )
                        }

                        is RoomMessageEventContent -> {

                            if (
                                ((it.content?.getOrNull() as? RoomMessageEventContent) is RoomMessageEventContent.FileBased) &&
                                ((it.content?.getOrNull() as RoomMessageEventContent.FileBased).info is ImageInfo)
                                ) {
                                ChatMessage.ImageMessage(
                                    text = (it.content?.getOrNull() as? RoomMessageEventContent)?.body ?: "",
                                    image = (it.content?.getOrNull() as? RoomMessageEventContent.FileBased)?.file,
                                    imageFileSize = (it.content?.getOrNull() as? RoomMessageEventContent.FileBased)?.info?.size ?: 0L,
                                    author = messageAuthor,
                                    metadata = metadata.copy(isEncrypted = false)
                                )
                            } else {
                                ChatMessage.TextChat(
                                    contents = (it.content?.getOrNull() as? RoomMessageEventContent)?.body ?: "",
                                    author = messageAuthor,
                                    metadata = metadata
                                )
                            }
                        }

                        is MessageEventContent,
                        is RedactedEventContent,
                        is StateEventContent,
                        is UnknownEventContent,
                        EmptyEventContent -> {
                            logger.w("MessageFetcher", it.toString())
                            ChatMessage.EmptyMessage(messageAuthor, metadata)
                        }
                    }
                }
                    //.first { !it.metadata.isEncrypted }
//                    .dropWhile { it.metadata.isEncrypted }
              //      .first()
            }
        }
        .flatten()
        .collect { wat ->
            onNewMessages(wat.reversed())
        }

}