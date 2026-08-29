package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import java.io.File
import javax.inject.Inject

//A class that helps draw icons for panes to show what the file is.
class SidebarPreviewProvider @Inject constructor(
){
    //For a PDF show a PDF page
    //For an image, draw it
    //For a movie, grab a picture of it

    @Composable
    fun FilePreview(file: File, type : FileType) {
        EmptyFilePreview()
    }

    @Composable
    fun EmptyFilePreview() {
        //Draw a placeholder icon
    }
}