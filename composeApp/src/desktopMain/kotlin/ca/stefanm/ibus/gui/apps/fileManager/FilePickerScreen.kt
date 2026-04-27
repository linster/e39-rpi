package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import java.io.File
import javax.inject.Inject

class FilePickerScreen @Inject constructor(

) {

    companion object {
        sealed interface FilePickerResult {
            object NoFileChosen : FilePickerResult
            data class FileChosen(val file : File) : FilePickerResult
        }
    }

}