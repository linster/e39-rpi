package ca.stefanm.ibus.gui.pim.contacts.views.parts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactDetails
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactRow

@Composable
private fun bigSize() = ThemeWrapper.ThemeHandle.current.bigItem.fontSize

@Composable
private fun smallSize() = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp

/**
 * A contact in the list: the name large, the organization (or first number
 * or address) below; on the first contact of a letter, the letter above it
 * (so letters take no knob stops of their own).
 */
@Composable
fun ContactRowView(row: ContactRow, isSelected: Boolean, onClicked: () -> Unit) {
    val colors = ThemeWrapper.ThemeHandle.current.colors
    ArbitraryContentsMenuItem(chipOrientation = ItemChipOrientation.W, isSelected = isSelected, onClicked = onClicked) {
        Column(Modifier.fillMaxWidth().padding(end = 16.dp.halveIfNotPixelDoubled(), top = 2.dp.halveIfNotPixelDoubled(), bottom = 2.dp.halveIfNotPixelDoubled())) {
            if (row.firstOfInitial) {
                Text(row.initial.toString(), color = colors.textMenuColorAccent, fontSize = smallSize(), fontWeight = FontWeight.Bold)
            }
            Text(row.name, color = colors.TEXT_WHITE, fontSize = bigSize(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (row.detail.isNotEmpty()) {
                Text(row.detail, color = colors.TEXT_WHITE, fontSize = smallSize(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** A JPEG as a Compose image, or null if it can't be read. */
fun jpegToImage(bytes: ByteArray): ImageBitmap? =
    runCatching { org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

/** The details screen's top: the picture (or initials), name, title and organization, address book. */
@Composable
fun ContactHeaderView(details: ContactDetails, photo: ByteArray?, isSelected: Boolean, onClicked: () -> Unit) {
    val colors = ThemeWrapper.ThemeHandle.current.colors
    val image = remember(photo) { photo?.let { jpegToImage(it) } }
    val side = 96.dp.halveIfNotPixelDoubled()
    ArbitraryContentsMenuItem(chipOrientation = ItemChipOrientation.NONE, isSelected = isSelected, onClicked = onClicked) {
        Row(Modifier.fillMaxWidth().padding(end = 16.dp.halveIfNotPixelDoubled(), top = 4.dp.halveIfNotPixelDoubled(), bottom = 4.dp.halveIfNotPixelDoubled()),
            verticalAlignment = Alignment.CenterVertically) {
            if (image != null) {
                Image(image, contentDescription = null, modifier = Modifier.size(side).clip(CircleShape))
            } else {
                Box(Modifier.size(side).clip(CircleShape).background(colors.textMenuColorAccent), contentAlignment = Alignment.Center) {
                    Text(initialsOf(details.name), color = colors.menuBackground, fontSize = bigSize(), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp.halveIfNotPixelDoubled()))
            Column(verticalArrangement = Arrangement.Center) {
                Text(details.name, color = colors.TEXT_WHITE, fontSize = bigSize(), fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (details.subtitle.isNotEmpty()) {
                    Text(details.subtitle, color = colors.TEXT_WHITE, fontSize = smallSize(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                details.addressBook?.let {
                    Text("${it.name} · ${it.accountName}", color = colors.textMenuColorAccent, fontSize = smallSize(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** "AK" for "Anna Kowalski", "P" for "Prairie Euro Garage"'s first word only if it's one word. */
fun initialsOf(name: String): String =
    name.split(' ').filter { it.isNotBlank() && it.first().isLetterOrDigit() }.let { words ->
        when {
            words.isEmpty() -> "?"
            words.size == 1 -> words[0].take(1).uppercase()
            else -> (words.first().take(1) + words.last().take(1)).uppercase()
        }
    }
