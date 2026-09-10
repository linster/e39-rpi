package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo.recent

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilerPickerParameters
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject

class MostRecentlyUsedRepo @Inject constructor(
    private val logger : Logger,
) {

    companion object {
        const val TAG = "MostRecentlyUsedRepo"
    }


    data class RecentEntry(
        val absolutePath : File,
        val type: FileType
    ) {

    }

    fun addFileToMru(file: File, type: FileType) {
        //TODO call the interface
    }

    fun getEntriesForType(filter: FilerPickerParameters.Filter) : Flow<List<RecentEntry>> {

        return flowOf()
        //return flowOf(repo[FileManagerMru.entries].filter { it.type == type })
    }

    fun clearAll() {

    }

    fun clearType(filter : FilerPickerParameters.Filter) {

    }

}