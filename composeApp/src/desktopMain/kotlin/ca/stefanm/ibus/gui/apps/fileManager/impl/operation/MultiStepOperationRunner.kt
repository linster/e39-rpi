package ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Companion.TAG
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Operation
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SnapshotPair
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class MultiStepOperationRunner @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val builder: MultiStepOperationBuilder
) {


    suspend fun doOperation(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>) : Boolean {
        if (!builder.operationBuilt()) {
            //There's a check in the GUI before showing the "Do Operation" button,
            //so no need to do another graphical callback from here.
            logger.w(TAG, "Trying to run an incomplete operation")
            return false
        }
        return when (builder.getOperation()) {
            Operation.NONE -> { logger.w(TAG, "Selected operation was NONE.") ; false }
            Operation.COPY_FILE_TO_FOLDER -> copyFileToFolder(ui, builder)
            Operation.MOVE_FILE_TO_FOLDER -> moveFileToFolder(ui, builder)
            Operation.COPY_FOLDER_TO_FOLDER -> copyFolderToFolder(ui, builder)
            Operation.MOVE_FOLDER_TO_FOLDER -> moveFolderToFolder(ui, builder)
        }
    }

    @VisibleForTesting
    suspend fun copyFileToFolder(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        builder: MultiStepOperationBuilder
    ) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isFile) {
            logger.d(TAG, "Source is not file. Cancelling.")
            return false
        }

        //Check if dest file already exists
        val preEmptiveDestFile = File(destination, source.getName())
        if (preEmptiveDestFile.exists()) {
            val shouldOverwrite = promptUserForYesNo(ui, listOf(
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
                notifyUserPrompts(ui, listOf("Copy complete."))
                notifyGoBackEntry(ui)
                true
            },
            onFailure = {
                logger.e(TAG, "Failed to copy file", it)
                notifyUserPrompts(ui, listOf(
                    "Failed to copy file.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                notifyGoBackEntry(ui)
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun moveFileToFolder(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        builder: MultiStepOperationBuilder
    ) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isFile) {
            return false
        }

        //Check if file already exists in directory. If it does, prompt the user if it would like to overwrite
        //by deleting the destination first.
        val preEmptiveDestFile = File(destination, source.getName())
        if (preEmptiveDestFile.exists()) {
            val shouldOverwrite = promptUserForYesNo(ui, listOf(
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
                notifyUserPrompts(ui, listOf("Elected to overwrite ${preEmptiveDestFile.absolutePath} but could not delete it.",
                    "Move file operation failed.")
                )
                notifyGoBackEntry(ui)
                return false
            }
        }

        return runCatching { FileUtils.moveFileToDirectory(source, destination, true) }.fold(
            onSuccess = {
                notifyUserPrompts(ui, listOf("Move succeeded."))
                notifyGoBackEntry(ui)
                true
            },
            onFailure = {
                logger.e(TAG, "Failed to move file", it)
                notifyUserPrompts(ui, listOf(
                    "Failed to move file.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                notifyGoBackEntry(ui)
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun copyFolderToFolder(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        builder: MultiStepOperationBuilder
    ) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isDirectory) {
            return false
        }

        return runCatching { FileUtils.copyDirectoryToDirectory(source, destination) }.fold(
            onSuccess = {
                notifyUserPrompts(ui, listOf("Copy complete."))
                notifyGoBackEntry(ui)
                true
            },
            onFailure = {
                notifyUserPrompts(ui, listOf(
                    "Failed to copy directory.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                notifyGoBackEntry(ui)
                false
            }
        )
    }

    @VisibleForTesting
    suspend fun moveFolderToFolder(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        builder: MultiStepOperationBuilder
    ) : Boolean {
        val source = builder.getSource()!!
        val destination = builder.getDestinationFolder()!!

        if (!source.isDirectory) {
            return false
        }

        return runCatching { FileUtils.moveDirectoryToDirectory(source, destination, true) }.fold(
            onSuccess = {
                notifyUserPrompts(ui, listOf("Move complete."))
                notifyGoBackEntry(ui)
                true
            },
            onFailure = {
                notifyUserPrompts(ui, listOf(
                    "Failed to move directory.",
                    " Source: ${source.absolutePath}",
                    " Dest: ${destination.absolutePath}",
                    " Reason: ${it.toString()}"
                ))
                notifyGoBackEntry(ui)
                false
            }
        )
    }


    @VisibleForTesting
    suspend fun promptUserForYesNo(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        prompt : List<String>,
        yesText : String = "Yes",
        noText : String = "No"
    ) : Boolean {

        notifyUserPrompts(ui, prompt)

        logger.d(TAG, "Prompting Yes/No")
        return suspendCoroutine { continuation ->
            ui.add(
                SnapshotPair(
                    ListEntrySource.RUNNING_ARF,
                    TextMenuItem(
                        title = yesText,
                        onClicked = {
                            GlobalScope.launch {
                                purgeArfEntries(ui)
                                notifyUserPrompts(ui, listOf("Selected $yesText"))
                                continuation.resume(true)
                            }
                        }
                    ))
            )
            ui.add(
                SnapshotPair(
                    ListEntrySource.RUNNING_ARF,
                    TextMenuItem(
                        title = noText,
                        onClicked = {
                            GlobalScope.launch {
                                purgeArfEntries(ui)
                                notifyUserPrompts(ui, listOf("Selected $noText"))
                                continuation.resume(false)
                            }
                        }
                    ))
            )
        }
    }

    @VisibleForTesting
    fun notifyUserPrompts(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,
        messages : List<String>) {
        logger.d(TAG, "Notifying user prompts: $messages")
        ui.addAll(
            messages.map { message ->
                SnapshotPair(
                    ListEntrySource.RUNNING,
                    TextMenuItem(
                        title = message,
                        isSelectable = false,
                        onClicked = {}
                    )
                )
            }
        )
    }

    @VisibleForTesting
    fun notifyGoBackEntry(
        ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>
    ) {
        ui.add(
            SnapshotPair(
                ListEntrySource.RUNNING,
                TextMenuItem(
                    title = "Go Back",
                    onClicked = {
//                        navigationNodeTraverser.cleanupBackStackDescendentsOf(FileManagerScreen::class.java)
//                        navigationNodeTraverser.cleanupBackStackDescendentsOf(MultiStepOperationProgressScreen::class.java)
                        builder.clear()
                        navigationNodeTraverser.navigateToRoot()
                        FileManagerScreen.openForBrowsing(navigationNodeTraverser)
                    }
                )
            )
        )
    }

    private fun purgeArfEntries(ui : SnapshotStateList<SnapshotPair<ListEntrySource, TextMenuItem>>,) {
        ui.removeIf { it.first == ListEntrySource.RUNNING_ARF }
    }

}