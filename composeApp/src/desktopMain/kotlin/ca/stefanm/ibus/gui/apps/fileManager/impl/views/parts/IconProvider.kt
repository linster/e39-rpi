package ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ibus.gui.apps.fileManager.impl.repo.DirectoryRepo
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.widgets.themes.Theme
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.application_pdf
import ca.stefanm.ibus.resources.*
import ca.stefanm.ibus.resources.application_x_archive
import ca.stefanm.ibus.resources.folder_blue
import ca.stefanm.ibus.resources.folder_green
import ca.stefanm.ibus.resources.folder_orange
import ca.stefanm.ibus.resources.folder_txt
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
import ca.stefanm.ibus.resources.text_x_readme
import ca.stefanm.ibus.resources.video_x_mng
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import javax.inject.Inject

class IconProvider @Inject constructor(
    private val mimeTools: MimeTools
) {

    //For drawing icons when you don't want a preview.

    @Composable
    fun IconForEntry(
        modifier: Modifier,
        entry : DirectoryRepo.DirectoryEntry,
        fileType: FileType? = null
    ) {


            //image here
            //https://feathericons.com/?query=nav
            val resource = painterResource(
                when (entry) {
                    is DirectoryRepo.DirectoryEntry.Directory -> when (
                        ThemeWrapper.ThemeHandle.current.configFileName
                    ) {
                        Themes.BmwBlueNormalSize.configFileName,
                        Themes.BmwBlueDoubledPixels.configFileName -> Res.drawable.folder_blue
                        Themes.RevolutionOrangeNormalSize.configFileName,
                        Themes.RevolutionOrangeDoubledPixels.configFileName -> Res.drawable.folder_orange
                        Themes.GooseGreenNormalSize.configFileName,
                        Themes.GooseGreenDoubledPixels.configFileName -> Res.drawable.folder_green
                        else -> Res.drawable.folder_blue
                    }
                    is DirectoryRepo.DirectoryEntry.DirectoryFile -> when(fileType ?: mimeTools.getFileTypeForFile(entry.file)) {
                        FileType.Archive -> Res.drawable.application_x_archive
                        FileType.Audio -> Res.drawable.text_xmcd
                        FileType.Movie -> Res.drawable.tool_animator
                        FileType.PDF -> Res.drawable.application_pdf
                        FileType.Picture -> Res.drawable.video_x_mng
                        FileType.TextFile -> Res.drawable.text_x_readme
//                        FileType.Other,
//                        FileType.Unknown,
                             else -> Res.drawable.Stuff
                    }
                    is DirectoryRepo.DirectoryEntry.SelectThisDirectory -> Res.drawable.folder_txt

                }
            )
            Image(
                painter = resource,
                contentDescription = entry.toString(),
                modifier = modifier
//                    .fillMaxSize(0.25F)
                    //.border(2.dp, Color.Red)
//                    .aspectRatio(1.0F)
            )

    }
}