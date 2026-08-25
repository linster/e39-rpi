package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo

import java.io.File
import javax.inject.Inject

// A class that gives a flow for a directory of all the files in it.
class DirectoryRepo @Inject constructor() {

    sealed class DirectoryEntry(open val path : File) {
        /** A real directory */
        data class Directory(override val path : File) : DirectoryEntry(path)

        /** A file in a folder */
        data class DirectoryFile(val file : File) : DirectoryEntry(file)

        /** A fake "Select this directory" entry used for folder selection */
        data class SelectThisDirectory(override val path : File) : DirectoryEntry(path)
    }

    //TODO the griffin power mate code has a directory watcher library in it.
}