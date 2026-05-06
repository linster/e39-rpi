package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import java.io.File
import javax.inject.Inject

class FilePickerScreen @Inject constructor(

) {

    companion object {
        sealed interface FilePickerResult {
            object NoFileChosen : FilePickerResult
            data class FileChosen(val file : File) : FilePickerResult
        }

        sealed interface FilerPickerParameters

        fun showFilePickerMRUSelect(
            navigationNodeTraverser: NavigationNodeTraverser,
            parameters: FilerPickerParameters,
            onQuickFileSelect : (FilePickerResult) -> Unit) {


        }
    }


    //TODO the file picker should first allow the user to open a sidebar to select a file. That sidebar should have
    // TODO a most recently used list per filetype, then the user could optionally pick that file, or open th efull blown screen.
}