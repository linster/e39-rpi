package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo


import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilerPickerParameters
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettings
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverridesRepo
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryNavigatorReader
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryStateRequestor
import ca.stefanm.ibus.lib.logging.Logger
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

// A class that gives a flow for a directory of all the files in it.
class DirectoryRepo @Inject constructor(
    private val logger: Logger,
    private val backStackManager: CurrentDirectoryBackStackManager,
    private val mimeTools: MimeTools
) : IDirectoryNavigatorReader, IDirectoryStateRequestor {

    companion object {
        const val TAG = "DirectoryRepo"
    }

    sealed class DirectoryEntry(open val path : File) {
        /** A real directory */
        data class Directory(override val path : File) : DirectoryEntry(path)

        /** A file in a folder */
        data class DirectoryFile(val file : File) : DirectoryEntry(file)

        /** A fake "Select this directory" entry used for folder selection */
        data class SelectThisDirectory(override val path : File) : DirectoryEntry(path)
    }

    private var baseDirectory : File = File(FileManagerSettingsOverridesRepo.config[FileManagerSettings.defaultBrowseFolder])

    /** Set the base director for the file manager that no operation can travel up fro */
    fun setBaseDirectory(file : File) {
      if (!file.isDirectory) {
          logger.w(TAG, "Asked to set a base directory $file which is not a directory")
          return
      }
        baseDirectory = file
        logger.d(TAG, "Now set baseDirectory to ${file.absolutePath}")
    }

    private val currentDirectory = MutableStateFlow(baseDirectory)

    override fun canGoUp(): Boolean {
        //The requested folder cannot be above the baseDirectory
        return FileUtils.directoryContains(baseDirectory, currentDirectory.value.parentFile) || baseDirectory == currentDirectory.value.parentFile
    }

    override fun canGoBack(): Boolean = backStackManager.canGoBack(baseDirectory)
    override fun canGoForward(): Boolean = backStackManager.canGoForward(baseDirectory)

    override fun requestNavigateBack() {
        if (canGoBack()) {
            currentDirectory.value = backStackManager.popBack()
        }
    }

    override fun requestNavigateForward() {
        if (canGoForward()) {
            currentDirectory.value = backStackManager.popForward()
        }
    }

    override fun requestNavigateUp() {
        if (canGoUp()) {
            currentDirectory.value = currentDirectory.value.parentFile
            backStackManager.pushDirectory(currentDirectory.value)
        }
    }

    fun requestNavigateToDirectory(file : File) {
        if (!file.isDirectory) {
            logger.w(TAG, "Asked to set a current directory $file which is not a directory")
            return
        }
        if (!FileUtils.directoryContains(baseDirectory, file) || baseDirectory == file) {
            logger.w(TAG, "Base directory does not contain new current")
            return
        }
        currentDirectory.value = file
    }

    fun getCurrentDirectoryPath() : Flow<String> {
        return currentDirectory.map { it.absolutePath }
    }

    fun getDirectoryFlow(
        showFakeSelectThisDirectoryEntry : Boolean,
        filter: FilerPickerParameters.Filter,
        showHiddenFiles : Boolean = false,
        showHiddenFolders : Boolean = false,
    ) : Flow<List<DirectoryEntry>> {


        //Calculate filters here
        //FileFilter, DirFilter.


        //TODO the dir filter needs to just get directories that are the child of this folder, not every single folder
        // in the sub-tree.
        return kfsWatcherFlow(currentDirectory.value).flatMapLatest {
            callbackFlow {

                val fileList = currentDirectory.value.listFiles { file ->
                    when (filter) {
                        FilerPickerParameters.Filter.AllFilesAndFolders -> true
                        FilerPickerParameters.Filter.AllFilesOnly -> file.isFile
                        FilerPickerParameters.Filter.FoldersOnly -> file.isDirectory
                        is FilerPickerParameters.Filter.MatchingFileTypes -> {
                            (mimeTools.getFileTypeForFile(file) in filter.types) || file.isDirectory
                        }
                        FilerPickerParameters.Filter.Pdf -> (mimeTools.getFileTypeForFile(file) == FileType.PDF) || file.isDirectory
                        FilerPickerParameters.Filter.Pictures -> (mimeTools.getFileTypeForFile(file) == FileType.Picture) || file.isDirectory
                        FilerPickerParameters.Filter.Videos -> (mimeTools.getFileTypeForFile(file) == FileType.Movie) || file.isDirectory
                    }

                }
                send(fileList)
                awaitClose {}
            }.map { upstream ->
                val (directories, files) = upstream.partition { it.isDirectory }
                val decoratedDirectories = directories.map {
                    DirectoryEntry.Directory(it)
                }.let {
                    if (showFakeSelectThisDirectoryEntry) {
                        listOf(DirectoryEntry.SelectThisDirectory(currentDirectory.value)) + it
                    } else {
                        it
                    }
                }
                val decoratedFiles = files.map {
                    DirectoryEntry.DirectoryFile(it)
                }
                decoratedDirectories + decoratedFiles
            }
        }
    }

    private fun kfsWatcherFlow(dir : File) : Flow<Unit> {
        //Emit once on start, and once any file in the directory changes.
        //so that downstream can re-trigger directory scans.
        return callbackFlow producerScope@ {
            val watcher = KfsDirectoryWatcher(this@producerScope)
            watcher.add(dir.absolutePath)
            send(Unit)
            watcher.onEventFlow.collect { send(Unit) }
            awaitClose {
                GlobalScope.launch { watcher.remove(dir.absolutePath)}
            }
        }
    }
}

class CurrentDirectoryBackStackManager @Inject constructor(
    private val logger: Logger
) {
    companion object {
        const val TAG = "CurrentDirectoryBackStackManager"
    }
    private val backStack = ArrayDeque<File>()

    fun pushDirectory(file : File) {
        backStack.add(file)
    }

    fun canGoBack(baseDirectory : File) : Boolean {
        //Check that the thing we're popping is within baseDirectory
        if (backStack.isEmpty()) {
            return false
        }
        val candidate = backStack.last()
        return if (candidateWithinBase(candidate, baseDirectory)) {
            true
        } else {
            logger.d(TAG, "Going back to $candidate would let us escape the base $baseDirectory")
            false
        }
    }

    fun canGoForward(baseDirectory : File) : Boolean {
        //Check that the thing we're popping is within baseDirectory
        if (backStack.isEmpty()) {
            return false
        }
        //TODO forward navigation requires keeping track of what we've popped going back, and whether
        //TODO the things we've popped are still what we're looking at
        return false
    }

    fun popBack() : File {
        return backStack.removeLast()
    }

    fun popForward() : File {
        TODO()
    }

    private fun candidateWithinBase(candidate : File, base : File) : Boolean {
        return FileUtils.directoryContains(base, candidate)
    }

}