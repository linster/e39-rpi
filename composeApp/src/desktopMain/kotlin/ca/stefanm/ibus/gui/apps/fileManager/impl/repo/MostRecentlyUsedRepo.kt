package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.ConfigurationStorage.Companion.e39BaseFolder
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.chat.MostRecentStorage
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarViewConfigRepo
import ca.stefanm.ibus.lib.logging.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Config.Companion.invoke
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.optional
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject

// Each filetype can be opened with an MRU picker,
// and that picker can allow a pane to open first to avoid having
// to go to a screen.

//TODO use the KDE and GNOME recently used files libs here to populate the list.
//TODO also use the desktop's list as a base so that we are in-sync.

//https://www.reddit.com/r/kde/comments/yy49ti/what_useful_kioslave_paths_are_there_to_be_used/
//https://wiki.archlinux.org/title/File_manager_functionality#Mounting
//https://zunzuncito.oriole.systems/25/
//https://specifications.freedesktop.org/recent-files/latest/

class MostRecentlyUsedRepo @Inject constructor(
    private val logger : Logger,
    private val configurationStorage: ConfigurationStorage
) {

    companion object {
        const val TAG = "MostRecentlyUsedRepo"
    }

    private val repoFile = File(e39BaseFolder, "fileManagerMru.conf")

    val repo = Config { addSpec(FileManagerMru) }
        .from.hocon.file(repoFile, optional = true)

    init {
        if (!repoFile.exists()) {
            repo.toHocon.toFile(repoFile)
        }
        repo.afterSet { item, value ->
            logger.d(TAG, "Setting ${item.name} to $value")
            repo.toHocon.toFile(repoFile)
        }
    }

    data class RecentEntry(
        val file : File,
        val type: FileType
    ) {

    }

    fun addFileToMru(file: File, type: FileType) {
        repo[FileManagerMru.entries] = repo[FileManagerMru.entries].toMutableSet().let {  it.add(
            MostRecentlyUsedRepo.RecentEntry(file, type)
        ) ; it}
    }

    fun getEntriesForType(filter: FilePickerScreen.Companion.FilerPickerParameters.Filter) : Flow<List<RecentEntry>> {

        return flowOf()
        //return flowOf(repo[FileManagerMru.entries].filter { it.type == type })
    }

    fun clearAll() {
        repo[FileManagerMru.entries] = emptySet()
    }

    fun clearType(filter : FilePickerScreen.Companion.FilerPickerParameters.Filter) {
        if (filter is FilePickerScreen.Companion.FilerPickerParameters.Filter.MatchingFileTypes) {
            repo[FileManagerMru.entries] = repo[FileManagerMru.entries].filterNot { it.type in filter.types }.toSet()
        } else {
            clearAll()
        }
    }

}

private object FileManagerMru : ConfigSpec() {

    val entries by optional(
        emptySet<MostRecentlyUsedRepo.RecentEntry>(), "entries", "mru list entries"
    )
}


