package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.chat.screens.chat.roomScreen.ChatMessage
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

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

@Composable
fun ImageMessageView(imageMessage: ChatMessage.ImageMessage, isSelected: Boolean, onClick: (message : ChatMessage.ImageMessage) -> Unit) {
    ArbitraryContentsMenuItem(isSelected = isSelected, onClicked = { onClick(imageMessage) }) {
        Column {
            Text(
                text = imageMessage.author.toString(),
                color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = imageMessage.metadata.time.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Formats.ISO),
                color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
                fontWeight = FontWeight.Normal
            )
            //TODO flesh out the data type here and load an image from some kinda handle or something.
        }
    }
}