package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings

data class FileManagerSettingsOverrides(

    /** When false, no operation that can break anything actually runs */
    val allowFilesystemModification : Boolean = false
)