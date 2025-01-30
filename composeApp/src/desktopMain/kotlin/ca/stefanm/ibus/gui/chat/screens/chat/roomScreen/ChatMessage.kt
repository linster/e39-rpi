package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatRoomScreen
import com.javadocmd.simplelatlng.LatLng
import kotlinx.datetime.Instant
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.UserId
import java.io.File


//All these are also a StateObject so that compose can update when they are in a list
data class MessageAuthor(val name : String? = null, val userId: UserId) : StateObject {
    val backingList = mutableStateListOf(name, userId.full)
    override val firstStateRecord: StateRecord
        get() = backingList.firstStateRecord

    override fun prependStateRecord(value: StateRecord) {
        backingList.prependStateRecord(value)
    }
}
data class MessageMetadata(
    val time : Instant,
    val eventId : EventId
) : StateObject {
    val backingList = mutableStateListOf(time.toEpochMilliseconds().toString(), eventId.full)
    override val firstStateRecord: StateRecord
        get() = backingList.firstStateRecord

    override fun prependStateRecord(value: StateRecord) {
        backingList.prependStateRecord(value)
    }
}

sealed class ChatMessage(
    val author : MessageAuthor,
    val metadata: MessageMetadata
) : StateObject{
    class EmptyMessage(author: MessageAuthor, metadata: MessageMetadata) : ChatMessage(author, metadata), StateObject {
        val backingList = mutableStateListOf(author, metadata)
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }
    class TextChat(
        val contents : String,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata) , StateObject {
        val backingList = mutableStateListOf(contents, author, metadata)
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }

    class ImageMessage(
        val text : String,
        val image : File, // Populated by trixnity-okio
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata), StateObject {
        val backingList = mutableStateListOf(text, image.name, author, metadata)
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }

    class PollMessage(
        val question : String,
        val pollItems : List<PollItem>,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata), StateObject {
        data class PollItem(
            val title : String,
            val votes : Int
        ) : StateObject {
            val backingList = mutableStateListOf(toString())
            override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

            override fun prependStateRecord(value: StateRecord) {
                backingList.prependStateRecord(value)
            }
        }

        val backingList = mutableStateListOf(question, *pollItems.toTypedArray())
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }

    class GeoLocation(
        val latLng: LatLng,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata), StateObject {
        val backingList = mutableStateListOf(latLng.toString(), author, metadata)
        override val firstStateRecord: StateRecord
            get() = backingList.firstStateRecord

        override fun prependStateRecord(value: StateRecord) {
            backingList.prependStateRecord(value)
        }
    }
}