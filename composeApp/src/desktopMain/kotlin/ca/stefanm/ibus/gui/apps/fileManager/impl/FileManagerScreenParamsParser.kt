package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilerPickerParameters.Filter
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverrides
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import java.io.File
import javax.inject.Inject

//Declare browsing mode, selection modes, etc.

enum class OpenMode {
    /** Open the file manager screen to just browse for files */
    BROWSE,

    /** Open the file manager screen to prompt the user to select
     *  a file for an app.
     */
    SELECT_FILE,

    /** Open the file manager screen to prompt the user to select
     *  a folder to copy a file into.
     */
    SELECT_COPY_TO_FOLDER,

    /** Open the file manager screen to prompt the user to select
     *  a folder to move a file into.
     */
    SELECT_MOVE_TO_FOLDER
}

data class FileManagerScreenOpenParameters(
    val openMode: OpenMode = OpenMode.BROWSE,

    /** What's the directory the user cannot go up out of? */
    val baseDirectory : File = FileManagerSettingsOverrides.Default.defaultBrowseFolder,
    /** What directory does the file manager open to? */
    val openDirectory : File = FileManagerSettingsOverrides.Default.defaultBrowseFolder,

    val fileFilter : Filter = Filter.AllFilesAndFolders,

    /** Should the view show a "Select this folder" dummy entry? */
    val showSelectThisFolderEntries : Boolean = false
)

/** Heavily use the screen back-stack to re-open the same Screen, but with a different
 *  opening mode, to allow a builder object to build-up an operation.
 *
 *  For example, open for browse. The user selects "Copy To Folder". Open
 *  fileManagerScreen with OpenMode == SELECT_COPY_TO_FOLDER. Then, when the user
 *  selects a folder, return a result back to the preceding FileManagerScreen which
 *  then adds the information into the FileOperationBuilder (singleton) to then
 *  perform the operation.
 */
sealed interface FileManagerScreenResult {
    sealed interface FileManagerScreenResultForSelectFile : FileManagerScreenResult {
        object NoFileSelected : FileManagerScreenResultForSelectFile
        data class FileSelected(val file : File) : FileManagerScreenResultForSelectFile
    }
    sealed interface FileManagerScreenResultForSelectFolder : FileManagerScreenResult {
        object NoFolderSelected : FileManagerScreenResultForSelectFolder
        data class FolderSelected(val directory : File) : FileManagerScreenResultForSelectFolder
    }
}

interface FileManagerScreenOpener {
    fun openForBrowsing(navigationNodeTraverser: NavigationNodeTraverser) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            FileManagerScreen::class.java,
            FileManagerScreenOpenParameters(
                openMode = OpenMode.BROWSE
            )
        )
    }
    fun openForFileSelection(navigationNodeTraverser: NavigationNodeTraverser) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            FileManagerScreen::class.java,
            FileManagerScreenOpenParameters(
                openMode = OpenMode.SELECT_FILE
            )
        )
    }
    fun openForFileSelection(navigationNodeTraverser: NavigationNodeTraverser,
                             baseDirectory: File = FileManagerSettingsOverrides.Default.defaultBrowseFolder,
                             filter : Filter = Filter.AllFilesAndFolders
    ) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            FileManagerScreen::class.java,
            FileManagerScreenOpenParameters(
                openMode = OpenMode.SELECT_FILE,
                baseDirectory = baseDirectory,
                fileFilter = filter
            )
        )
    }


}

internal interface FileManagerScreenSelfOpener {
    fun openForCopyTo(
        navigationNodeTraverser: NavigationNodeTraverser,
        baseDirectory: File,
        currentDirectory: File
    ) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            FileManagerScreen::class.java,
            FileManagerScreenOpenParameters(
                openMode = OpenMode.SELECT_COPY_TO_FOLDER,
                baseDirectory = baseDirectory,
                openDirectory = currentDirectory,
                fileFilter = Filter.FoldersOnly,
                showSelectThisFolderEntries = true
            )
        )
    }
    fun openForMoveTo(
        navigationNodeTraverser: NavigationNodeTraverser,
        baseDirectory: File,
        currentDirectory: File
    ) {
        navigationNodeTraverser.navigateToNodeWithParameters(
            FileManagerScreen::class.java,
            FileManagerScreenOpenParameters(
                openMode = OpenMode.SELECT_MOVE_TO_FOLDER,
                baseDirectory = baseDirectory,
                openDirectory = currentDirectory,
                fileFilter = Filter.FoldersOnly,
                showSelectThisFolderEntries = true
            )
        )

    }
}

/** For use in VideoPlayer, PDF Reader, other apps to they don't have to re-write
 *  the same parsing logic repeatedly while still preserving their own result parsing
 *  logic
 */
class FileManagerScreenFileSelectionResultHelper @Inject constructor(){
    fun parseSelectedFile(incomingResult: Navigator.IncomingResult?) : File? {
        return if (incomingResult?.result is FileManagerScreenResult.FileManagerScreenResultForSelectFile.FileSelected) {
            incomingResult.result.file
        } else {
            null
        }
    }
}


//Parse the navigator input params here
class FileManagerScreenParamsParser @Inject constructor() {
    fun parse(incomingResult : Navigator.IncomingResult?) : FileManagerScreenOpenParameters {
        return if (incomingResult == null) {
            FileManagerScreenOpenParameters()
        } else {
            val params : FileManagerScreenOpenParameters? = incomingResult.requestParameters as? FileManagerScreenOpenParameters
            params ?: FileManagerScreenOpenParameters()
        }
    }
}