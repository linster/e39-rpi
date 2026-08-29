package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import javax.inject.Inject

//A class that helps draw icons for panes to show what the file is.
class SidebarPreviewProvider @Inject constructor(
    private val mimeTools: MimeTools
){
    //For a PDF show a PDF page
    //For an image, draw it
    //For a movie, grab a picture of it
}