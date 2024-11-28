package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatRoomScreen
import com.javadocmd.simplelatlng.LatLng
import java.io.File



class MessageAuthor
class MessageMetadata // All the matrix junk

sealed class ChatMessage(
    val author : MessageAuthor,
    val metadata: MessageMetadata
) {
    class TextChat(
        val contents : String,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata)

    class ImageMessage(
        val text : String,
        val image : File, // Populated by trixnity-okio
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata)

    class PollMessage(
        val question : String,
        val pollItems : List<PollItem>,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata) {
        data class PollItem(
            val title : String,
            val votes : Int
        )
    }

    class GeoLocation(
        val latLng: LatLng,
        author: MessageAuthor,
        metadata: MessageMetadata
    ) : ChatMessage(author, metadata)
}