package ca.stefanm.ibus.gui.pim.email.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.api.MailFormat
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageRow
import ca.stefanm.ibus.gui.pim.email.repo.api.ReaderContent

/** A row of a SmoothScroll. */
typealias MailListItem = @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit

/** A toolbar button; [weight] is its share of the row's width. */
data class MailToolbarButton(
    val label: String,
    val weight: Float = 1f,
    val onClicked: () -> Unit,
)

/**
 * The mail screens' toolbar. It takes knob positions from [knobState]
 * before the list below it, so a screen's buttons must not change in
 * number while it's shown (rebuild the screen with key() when they do).
 * [highlightNone]: nothing looks selected (the knob scrolls the text).
 */
@Composable
fun MailToolbar(
    knobState: KnobObserverBuilderState,
    buttons: List<MailToolbarButton>,
    highlightNone: Boolean = false,
) {
    Row(
        modifier = Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxWidth()
    ) {
        buttons.forEach { button ->
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(button.weight, true),
                    label = button.label,
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = !highlightNone && currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) { button.onClicked() },
                )
            }
        }
    }
}

@Composable
private fun bigSize() = ThemeWrapper.ThemeHandle.current.bigItem.fontSize

@Composable
private fun smallSize() = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp

/** Inbox, sent, ...: a symbol in front of the folder's name. */
fun folderIcon(role: MailFolderRole): String = when (role) {
    MailFolderRole.INBOX -> "📥"
    MailFolderRole.SENT -> "📤"
    MailFolderRole.DRAFTS -> "📝"
    MailFolderRole.TRASH -> "🗑"
    MailFolderRole.JUNK -> "⚠"
    MailFolderRole.OUTBOX -> "⏳"
    MailFolderRole.OTHER -> "📁"
}

/**
 * A folder in the folder list: indented by depth, unread count on the
 * right, bold when there's unread mail. [accountCaption]: the account's
 * name above it (on an account's first folder), so accounts need no knob
 * stops of their own.
 */
@Composable
fun FolderRowView(
    folder: MailFolder,
    accountCaption: String?,
    accountUnread: Long,
    isSelected: Boolean,
    onClicked: () -> Unit,
) {
    val colors = ThemeWrapper.ThemeHandle.current.colors
    ArbitraryContentsMenuItem(chipOrientation = ItemChipOrientation.W, isSelected = isSelected, onClicked = onClicked) {
        Column(Modifier.fillMaxWidth().padding(end = 16.dp.halveIfNotPixelDoubled(), top = 4.dp.halveIfNotPixelDoubled(), bottom = 4.dp.halveIfNotPixelDoubled())) {
            if (accountCaption != null) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        accountCaption.uppercase(),
                        color = colors.textMenuColorAccent,
                        fontSize = smallSize(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    if (accountUnread > 0) {
                        Text("${MailFormat.unreadBadge(accountUnread)} unread", color = colors.textMenuColorAccent, fontSize = smallSize())
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(start = (24 * folder.depth).dp.halveIfNotPixelDoubled()), verticalAlignment = Alignment.CenterVertically) {
                val unread = folder.unreadCount > 0
                Text(
                    "${folderIcon(folder.role)} ${folder.name}",
                    color = if (folder.holdsMessages) colors.TEXT_WHITE else colors.textMenuColorAccent,
                    fontSize = bigSize(),
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread) {
                    Text(MailFormat.unreadBadge(folder.unreadCount), color = colors.TEXT_WHITE, fontSize = bigSize(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * A message in a list: correspondent and subject (bold while unread), the
 * date, and markers: ● unread, ⚑ flagged, 📎 attachments, ↩ answered,
 * ↪ forwarded.
 */
@Composable
fun MessageRowView(row: MessageRow, isSelected: Boolean, onClicked: () -> Unit) {
    val colors = ThemeWrapper.ThemeHandle.current.colors
    val weight = if (row.unread) FontWeight.Bold else FontWeight.Normal
    ArbitraryContentsMenuItem(chipOrientation = ItemChipOrientation.W, isSelected = isSelected, onClicked = onClicked) {
        Column(Modifier.fillMaxWidth().padding(end = 16.dp.halveIfNotPixelDoubled(), top = 2.dp.halveIfNotPixelDoubled(), bottom = 2.dp.halveIfNotPixelDoubled())) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (row.unread) "● " else "") + row.correspondent,
                    color = colors.TEXT_WHITE,
                    fontSize = bigSize(),
                    fontWeight = weight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(row.date, color = colors.TEXT_WHITE, fontSize = smallSize(), fontWeight = weight)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.subject,
                    color = colors.TEXT_WHITE,
                    fontSize = smallSize(),
                    fontWeight = weight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Markers(row)
            }
        }
    }
}

@Composable
private fun RowScope.Markers(row: MessageRow) {
    val markers = buildString {
        if (row.flagged) append(" ⚑")
        if (row.hasAttachments) append(" 📎")
        if (row.answered) append(" ↩")
        if (row.forwarded) append(" ↪")
    }
    if (markers.isNotEmpty()) {
        Text(markers, color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE, fontSize = smallSize())
    }
}

/** A line of text that isn't a knob stop's own (e.g. "No messages"), for inside a row. */
@Composable
fun MailInfoText(text: String) {
    Text(
        text,
        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
        fontSize = bigSize(),
        modifier = Modifier.padding(8.dp.halveIfNotPixelDoubled()),
    )
}

@Composable
private fun bodySize() = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 22.sp else 11.sp

/** Text in the reader's body size. */
@Composable
fun MessageBodyText(text: String) {
    Text(text, color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE, fontSize = bodySize())
}

@Composable
private fun Divider() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 8.dp.halveIfNotPixelDoubled()).height(2.dp)
            .background(ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent)
    )
}

/** The reader's contents: headers, flags, the text, attachment names. */
@Composable
fun MessageBodyView(content: ReaderContent) {
    val colors = ThemeWrapper.ThemeHandle.current.colors
    content.headers.forEach { (label, value) ->
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = colors.textMenuColorAccent, fontWeight = FontWeight.Bold)) { append("$label: ") }
                append(value)
            },
            color = colors.TEXT_WHITE,
            fontSize = smallSize(),
        )
    }
    val flags = buildList {
        if (content.summary.flagged) add("⚑ Flagged")
        if (!content.summary.seen) add("● Unread")
    }
    if (flags.isNotEmpty()) {
        Text(flags.joinToString("   "), color = colors.textMenuColorAccent, fontSize = smallSize())
    }
    Divider()
    MessageBodyText(content.message.text.ifBlank { "(no text)" })
    if (content.attachments.isNotEmpty()) {
        Divider()
        Text("📎 Attachments", color = colors.textMenuColorAccent, fontSize = smallSize(), fontWeight = FontWeight.Bold)
        content.attachments.forEach { Text(it, color = colors.TEXT_WHITE, fontSize = smallSize()) }
    }
}
