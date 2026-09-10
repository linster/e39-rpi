package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings

import java.io.File

data class FileManagerSettings(

    /** When false, no operation that can break anything actually runs */
    val allowFilesystemModification : Boolean = false,

    val defaultBrowseFolder : File = File("/home/stefan/BMW/fileManTest"),
    val allowUpFromDefaultBrowseFolder : Boolean = false,
)

object FileManagerSettingsOverrides {

    val Default = FileManagerSettings()
}