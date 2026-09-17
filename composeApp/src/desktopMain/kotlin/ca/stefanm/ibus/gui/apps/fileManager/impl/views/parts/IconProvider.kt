package ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.apps.fileManager.impl.repo.DirectoryRepo
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.folder_blue
import ca.stefanm.ibus.resources.notification_alert_circle
import ca.stefanm.ibus.resources.notification_alert_octagon
import ca.stefanm.ibus.resources.notification_alert_triangle
import ca.stefanm.ibus.resources.notification_bluetooth
import ca.stefanm.ibus.resources.notification_map
import ca.stefanm.ibus.resources.notification_map_pin
import ca.stefanm.ibus.resources.notification_message_circle
import ca.stefanm.ibus.resources.notification_message_square
import ca.stefanm.ibus.resources.notification_music
import ca.stefanm.ibus.resources.notification_navigation
import ca.stefanm.ibus.resources.notification_phone
import ca.stefanm.ibus.resources.notification_phone_incoming
import ca.stefanm.ibus.resources.notification_phone_missed
import ca.stefanm.ibus.resources.notification_voicemail
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import javax.inject.Inject

class IconProvider @Inject constructor(

) {

    //For drawing icons when you don't want a preview.

    @Composable
    fun IconForEntry(entry : DirectoryRepo.DirectoryEntry) {


            //image here
            //https://feathericons.com/?query=nav
            val resource = painterResource(
                when (entry) {
                    is DirectoryRepo.DirectoryEntry.Directory -> Res.drawable.folder_blue
                    is DirectoryRepo.DirectoryEntry.DirectoryFile -> Res.drawable.folder_blue
                    is DirectoryRepo.DirectoryEntry.SelectThisDirectory -> Res.drawable.folder_blue
                }
            ) //TODO these might be svgz. Convert to svg first.
            Image(
                painter = resource,
                contentDescription = entry.toString(),
                modifier = Modifier
                    .fillMaxSize(0.75F)
                    //.border(2.dp, Color.Red)
                    .aspectRatio(1.0F)
            )

    }
}