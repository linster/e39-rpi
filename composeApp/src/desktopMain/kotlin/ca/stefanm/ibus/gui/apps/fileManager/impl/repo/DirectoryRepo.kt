package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo


import androidx.compose.runtime.mutableStateOf
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryNavigatorReader
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryStateRequestor
import ca.stefanm.ibus.lib.logging.Logger
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FalseFileFilter
import org.apache.commons.io.filefilter.FileFilterUtils
import org.apache.commons.io.filefilter.HiddenFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.eclipse.jgit.treewalk.filter.TreeFilter
import java.io.File
import java.io.FileFilter
import javax.inject.Inject

// A class that gives a flow for a directory of all the files in it.
class DirectoryRepo @Inject constructor(
    private val logger: Logger,
    private val backStackManager: CurrentDirectoryBackStackManager
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

    private var baseDirectory : File = FileUtils.getUserDirectory()

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
        showFakeSelectThisDirectoryEntry : Boolean = false,
        filter: FilePickerScreen.Companion.FilerPickerParameters.Filter,
        showHiddenFiles : Boolean = false,
        showHiddenFolders : Boolean = false,
    ) : Flow<List<DirectoryEntry>> {

        //Calculate filters here
        //FileFilter, DirFilter.

        return kfsWatcherFlow(currentDirectory.value).flatMapLatest {
            callbackFlow {
                val iterator = FileUtils.iterateFiles(
                    currentDirectory.value,
                    if (showHiddenFiles) {
                        HiddenFileFilter.VISIBLE
                    } else {
                        TrueFileFilter.TRUE
                    },
                    FalseFileFilter.FALSE
                )
                val fileList: List<File> = listOf(*iterator.asSequence().toList().toTypedArray())
                send(fileList)
                awaitClose {}
            }.map { upstream ->
                val (directories, files) = upstream.partition { it.isDirectory }
                val decoratedDirectories = directories.map {
                    DirectoryEntry.Directory(it)
                }.also {
                    if (showFakeSelectThisDirectoryEntry) {
                        listOf(DirectoryEntry.SelectThisDirectory(currentDirectory.value)) + it
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