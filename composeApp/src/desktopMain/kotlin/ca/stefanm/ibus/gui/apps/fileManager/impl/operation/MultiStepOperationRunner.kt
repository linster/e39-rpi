package ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Companion.TAG
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Operation
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class MultiStepOperationRunner @Inject constructor(
    private val logger: Logger
) {

    //This guy probably has to emit a flow of doo-dads, which end up getting consumed by
    //ConsumeAsState(), or SideEffect? to add into the entries list? .... those guys need TextMenuItems
    // with lambdas in them for the buttons.... maybe?

    fun doOperation(builder: MultiStepOperationBuilder) : Boolean {
        return when (builder.getOperation()) {
            Operation.NONE -> { logger.w(TAG, "Selected operation was NONE.") ; false }
            Operation.COPY_FILE_TO_FOLDER -> TODO()
            Operation.MOVE_FILE_TO_FOLDER -> TODO()
            Operation.COPY_FOLDER_TO_FOLDER -> TODO()
            Operation.MOVE_FOLDER_TO_FOLDER -> TODO()
        }
    }

    // TODO the arf prompts could just be a MutableState<List<TextMenuItem>> here that is subscribed to.
    // TODO from the progress screen (and given a tag there for Runner + Runner_ARF). Then, as we run here, we can update
    // TODO the progress here by having one item (or group of items) represent progress (Files # / total, Folder # / total, Megabytes / total)
    // TODO which get updated then re-added to the mutable state.

    // TODO the ARF prompts could work similarly.

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