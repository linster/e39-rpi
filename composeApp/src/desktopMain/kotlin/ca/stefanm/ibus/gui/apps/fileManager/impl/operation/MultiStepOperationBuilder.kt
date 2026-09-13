package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject

@ApplicationScope
class MultiStepOperationBuilder @Inject constructor(
    private val logger: Logger
) {

    companion object {
        const val TAG = "MultiStepOperationBuilder"
    }

    enum class Operation {
        NONE,
        COPY_FILE_TO_FOLDER,
        MOVE_FILE_TO_FOLDER,
        COPY_FOLDER_TO_FOLDER,
        MOVE_FOLDER_TO_FOLDER
    }
    private var operation = Operation.NONE

    internal fun getOperation() : Operation = this.operation

    fun setOperation(operation: Operation) {
        this.operation = operation
    }

    private var source : File? = null
    private var destinationFolder : File? = null

    fun setSource(file : File) {
        logger.d(TAG, "Setting source file: ${file.absolutePath}")
        source = file
    }

    fun operationBuildingStarted() : Boolean = this.source != null && operation != Operation.NONE

    fun operationBuilt() : Boolean {
        return this.source != null && this.destinationFolder != null && operation != Operation.NONE
    }

    fun setDestinationFolder(folder : File) {
        if (folder.isDirectory) {
            logger.d(TAG,"Setting destination folder ${folder.absolutePath}")
            destinationFolder = folder
        } else {
            logger.w(TAG, "Selected a destination folder ${folder.absolutePath} that is not a directory.")
        }
    }
    fun clear() {
        operation = Operation.NONE
        source = null
        destinationFolder = null
    }

}