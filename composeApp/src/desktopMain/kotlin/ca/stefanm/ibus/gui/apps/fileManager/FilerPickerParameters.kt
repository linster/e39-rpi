package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import java.io.File

data class FilerPickerParameters(
    val rootDirectory : File,
    val allowNavigateUpFromRoot : Boolean = false,
    val allowNavigateIntoChildFolders : Boolean = true,

    val filter : Filter,

    val allowRenameFiles : Boolean = true,
    val allowMakeDirectory : Boolean = true,
) {
    sealed interface Filter {
        object AllFilesAndFolders : Filter
        object FoldersOnly : Filter
        object AllFilesOnly : Filter
        object Videos : Filter
        object Pictures : Filter
        object Pdf : Filter
        data class MatchingFileTypes(
            val types : List<FileType>
        ) : Filter
    }
}