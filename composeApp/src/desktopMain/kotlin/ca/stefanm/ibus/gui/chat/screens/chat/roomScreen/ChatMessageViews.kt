package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatMessage
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.media
import net.folivo.trixnity.clientserverapi.model.media.FileTransferProgress
import net.folivo.trixnity.clientserverapi.model.media.ThumbnailResizingMethod
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Drawable
import org.jetbrains.skia.Image

@Composable
fun TextMessageView(textMessage: ChatMessage.TextChat, isSelected: Boolean, onClick: (message : ChatMessage.TextChat) -> Unit) {
    ArbitraryContentsMenuItem(isSelected = isSelected, onClicked = { onClick(textMessage) }) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = textMessage.author.name.toString(),
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = textMessage.metadata.time.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Formats.ISO),
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Text(
                text = textMessage.contents,
                color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageMessageView(matrixClient: MatrixClient, imageMessage: ChatMessage.ImageMessage, isSelected: Boolean, onClick: (message : ChatMessage.ImageMessage) -> Unit) {

    val scope = rememberCoroutineScope()
    val imageIsDone = remember { mutableStateOf(false) }

    val imageBytes = remember { mutableStateOf(emptyArray<Byte>().toByteArray()) }

    scope.launch {
        if (imageMessage.image != null) {
            matrixClient.media.getEncryptedMedia(
                encryptedFile = imageMessage.image
            ).fold(
                onSuccess = {
                    it.onEach {
                        println("WAT GOT A PICTURE ${imageMessage.image.url} ${it.size} / ${imageMessage.imageFileSize}")
                    }.runningReduce { accumulator, value ->
                        byteArrayOf(*accumulator, *value)
                    }.dropWhile {
                        it.size.toLong() < imageMessage.imageFileSize ?: 0L
                    }.onEach {
                        println("WAT ACCUMULATED A PICTURE ${imageMessage.image.url} ${it.size} / ${imageMessage.imageFileSize}")
                    }.collect {
                        imageIsDone.value = true
                        imageBytes.value = it
                    }
                },
                onFailure = {
                    DaggerApplicationComponent.create().logger().e("ImageMessageView", "Could not load image", it)
                }
            )
        }
    }

    ArbitraryContentsMenuItem(isSelected = isSelected, onClicked = { onClick(imageMessage) }) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = imageMessage.author.name.toString(),
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = imageMessage.metadata.time.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Formats.ISO),
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            key(imageIsDone.value) {
                if (imageIsDone.value) {
                    Image(
                        bitmap = imageBytes.value.decodeToImageBitmap(),
                        contentDescription = "",
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
            Text(
                text = imageMessage.text,
                color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
