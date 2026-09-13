package ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Companion.TAG
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Operation
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class MultiStepOperationRunner @Inject constructor(
    private val builder: MultiStepOperationBuilder,
    private val logger: Logger
) {

    //TODO maybe split out the doing of the operation into a runner of sorts.
    //
    /** Perform the operation that was built-up. Do not overwrite anything.
     * @return true if successful
     */
//    fun doOperation() : Boolean {
//        return when (operation) {
//            Operation.NONE -> { logger.w(TAG, "Selected operation was NONE.") ; false }
//            Operation.COPY_FILE_TO_FOLDER -> TODO()
//            Operation.MOVE_FILE_TO_FOLDER -> TODO()
//            Operation.COPY_FOLDER_TO_FOLDER -> TODO()
//            Operation.MOVE_FOLDER_TO_FOLDER -> TODO()
//        }
//    }
//
//    sealed interface CopyFileResult {
//        object Success : CopyFileResult
//        object PromptForOverwrite : CopyFileResult
//        object ParameterNull : CopyFileResult
//        object PostWriteAttributeSetFailed : CopyFileResult
//        data class IOError(val cause : Throwable?) : CopyFileResult
//    }
//
//    //TODO maybe instead of having a modal to prompt for Abort,Retry,Fail
//    //TODO have a screen to run the operation once it's ready to be run?
//    //TODO that screen could have a smooth scroll and then pop on new items
//    //TODO to the end for ARF?
//    private fun doCopyFile(allowOverwrite : Boolean) : CopyFileResult {
//        val sourceFile = this.source
//        val destinationFolder = this.destinationFolder
//        if (sourceFile == null) {
//            logger.w(TAG, "doCopy(). Source file was null.")
//
//            return CopyFileResult.ParameterNull
//        }
//        if (destinationFolder == null) {
//            logger.w(TAG, "doCopy(). Destination folder was null.")
//            return CopyFileResult.ParameterNull
//        }
//
//        val newFileResult = runCatching {
//            sourceFile.copyTo(
//                target = destinationFolder,
//                overwrite = allowOverwrite)
//        }
//        if (newFileResult.isFailure) {
//
//            if (newFileResult.exceptionOrNull() is FileAlreadyExistsException && !allowOverwrite) {
//                logger.e(TAG, "doCopyFile(). File in folder $destinationFolder already exists", newFileResult.exceptionOrNull())
//                return CopyFileResult.PromptForOverwrite
//            }
//
//            logger.e(TAG, "doCopyFile(). Could not create new file", newFileResult.exceptionOrNull())
//            return CopyFileResult.IOError(newFileResult.exceptionOrNull())
//        }
//
//        //Set attributes on new file to match old
//        val newFile = newFileResult.getOrNull() ?: return CopyFileResult.IOError(null)
//
//        with(newFile) {
//            setLastModified(sourceFile.lastModified()).also { success ->
//                if (!success) {
//                    logger.w(TAG, "Could not set last modified")
//                    return CopyFileResult.PostWriteAttributeSetFailed
//                }
//            }
//            setExecutable(sourceFile.canExecute()).also { success ->
//                if (!success) {
//                    logger.w(TAG, "Could not set executable")
//                    return CopyFileResult.PostWriteAttributeSetFailed
//                }
//            }
//            setReadable(sourceFile.canRead()).also { success ->
//                if (!success) {
//                    logger.w(TAG, "Could not set readable")
//                    return CopyFileResult.PostWriteAttributeSetFailed
//                }
//            }
//            setWritable(sourceFile.canWrite()).also { success ->
//                if (!success) {
//                    logger.w(TAG, "Could not set writable")
//                    return CopyFileResult.PostWriteAttributeSetFailed
//                }
//            }
//        }
//        return CopyFileResult.Success
//    }
//
//    private fun doMoveFile(allowOverwrite : Boolean) : Boolean {
//        val copyResult = doCopyFile(allowOverwrite)
//        if (copyResult !is CopyFileResult.Success) {
//            logger.w(TAG, "doMoveFile(). Copy failed, not deleting original.")
//            return false
//        }
//        return source?.delete() ?: false
//    }
}