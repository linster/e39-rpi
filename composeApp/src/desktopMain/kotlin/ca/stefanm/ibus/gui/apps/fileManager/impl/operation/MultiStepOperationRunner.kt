package ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.annotation.VisibleForTesting
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Companion.TAG
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Operation
import ca.stefanm.ibus.lib.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

class MultiStepOperationRunner @Inject constructor(
    private val logger: Logger
) {

    //This guy probably has to emit a flow of doo-dads, which end up getting consumed by
    //ConsumeAsState(), or SideEffect? to add into the entries list? .... those guys need TextMenuItems
    // with lambdas in them for the buttons.... maybe?

    suspend fun doOperation(builder: MultiStepOperationBuilder) : Boolean {
        if (!builder.operationBuilt()) {
            //There's a check in the GUI before showing the "Do Operation" button,
            //so no need to do another graphical callback from here.
            logger.w(TAG, "Trying to run an incomplete operation")
            return false
        }
        return when (builder.getOperation()) {
            Operation.NONE -> { logger.w(TAG, "Selected operation was NONE.") ; false }
            Operation.COPY_FILE_TO_FOLDER -> copyFileToFolder(builder)
            Operation.MOVE_FILE_TO_FOLDER -> moveFileToFolder(builder)
            Operation.COPY_FOLDER_TO_FOLDER -> copyFolderToFolder(builder)
            Operation.MOVE_FOLDER_TO_FOLDER -> moveFolderToFolder(builder)
        }
    }

    // TODO the arf prompts could just be a MutableState<List<TextMenuItem>> here that is subscribed to.
    // TODO from the progress screen (and given a tag there for Runner + Runner_ARF). Then, as we run here, we can update
    // TODO the progress here by having one item (or group of items) represent progress (Files # / total, Folder # / total, Megabytes / total)
    // TODO which get updated then re-added to the mutable state.

    // TODO the ARF prompts could work similarly.

    @VisibleForTesting
    suspend fun copyFileToFolder(builder: MultiStepOperationBuilder) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isFile) {
            return false
        }

        //Check if dest file already exists
        val preEmptiveDestFile = File(destination, source.getName())
        if (preEmptiveDestFile.exists()) {
            val shouldOverwrite = promptUserForYesNo(listOf(
                "Should overwrite file?",
                "  Dest path: ${destination.absolutePath}",
                "  File already exists: ${preEmptiveDestFile.lastModified()}")
            )
            if (!shouldOverwrite) {
                logger.d(TAG, "User decided not to overwrite ${preEmptiveDestFile.absolutePath}")
                return false
            }
        }

        //Will overwrite
        return runCatching { FileUtils.copyFileToDirectory(source, destination) }.fold(
            onSuccess = {
                notifyUserPrompts(listOf("Copy complete."))
                true
            },
            onFailure = {
                logger.e(TAG, "Failed to copy file", it)
                notifyUserPrompts(listOf(
                    "Failed to copy file.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun moveFileToFolder(builder: MultiStepOperationBuilder) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isFile) {
            return false
        }

        //Check if file already exists in directory. If it does, prompt the user if it would like to overwrite
        //by deleting the destination first.
        val preEmptiveDestFile = File(destination, source.getName())
        if (preEmptiveDestFile.exists()) {
            val shouldOverwrite = promptUserForYesNo(listOf(
                "Should overwrite file?",
                "Dest file will be deleted!!",
                "  Dest path: ${destination.absolutePath}",
                "  File already exists: ${preEmptiveDestFile.lastModified()}")
            )
            if (!shouldOverwrite) {
                logger.d(TAG, "User decided not to overwrite ${preEmptiveDestFile.absolutePath}")
                return false
            }
            val deleteSuccess = preEmptiveDestFile.delete()
            if (!deleteSuccess) {
                notifyUserPrompts(listOf("Elected to overwrite ${preEmptiveDestFile.absolutePath} but could not delete it.",
                    "Move file operation failed.")
                )
                return false
            }
        }

        return runCatching { FileUtils.moveFileToDirectory(source, destination, true) }.fold(
            onSuccess = {
                notifyUserPrompts(listOf("Move succeeded."))
                true
            },
            onFailure = {
                logger.e(TAG, "Failed to move file", it)
                notifyUserPrompts(listOf(
                    "Failed to move file.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun copyFolderToFolder(builder: MultiStepOperationBuilder) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isDirectory) {
            return false
        }

        return runCatching { FileUtils.copyDirectoryToDirectory(source, destination) }.fold(
            onSuccess = {
                notifyUserPrompts(listOf("Copy complete."))
                true
            },
            onFailure = {
                notifyUserPrompts(listOf(
                    "Failed to copy directory.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun moveFolderToFolder(builder: MultiStepOperationBuilder) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isDirectory) {
            return false
        }

        return runCatching { FileUtils.moveDirectoryToDirectory(source, destination, true) }.fold(
            onSuccess = {
                notifyUserPrompts(listOf("Move complete."))
                true
            },
            onFailure = {
                notifyUserPrompts(listOf(
                    "Failed to move directory.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                false
            }
        )
    }


    @VisibleForTesting
    suspend fun promptUserForYesNo(prompt : List<String>) : Boolean {

    }

    @VisibleForTesting
    suspend fun notifyUserPrompts(messages : List<String>) {

    }




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