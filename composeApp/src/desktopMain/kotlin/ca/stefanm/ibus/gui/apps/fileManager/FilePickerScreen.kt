package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.FilePickerMruPane
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import io.github.vinceglb.filekit.dialogs.FileKitType
import java.io.File
import javax.inject.Inject

@AutoDiscover
class FilePickerScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val filePickerMruPane: FilePickerMruPane
) : NavigationNode<FilePickerScreen.Companion.FilePickerResult>{

    companion object {
        sealed interface FilePickerResult {
            object NoFileChosen : FilePickerResult
            data class FileChosen(val file : File) : FilePickerResult
        }

        data class FilerPickerParameters(
            val rootDirectory : File,
            val allowNavigateUpFromRoot : Boolean = false,
            val allowNavigateIntoChildFolders : Boolean = true,

            val filter : Filter,

            val allowRenameFiles : Boolean = true,
            val allowMakeDirectory : Boolean = true,
        ) {
            sealed interface Filter {
                object AllFilesAndFolders : Filter
                object AllFilesOnly : Filter
                data class MatchingFileTypes(
                    val types : List<FileType>
                ) : Filter
            }
        }

        fun showFilePickerScreen(
            navigationNodeTraverser: NavigationNodeTraverser,
            parameters: FilerPickerParameters,
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                FilePickerScreen::class.java,
                parameters
            )
        }
    }

    override val thisClass: Class<out NavigationNode<FilePickerResult>>
        get() = FilePickerScreen::class.java


    fun showFilePickerMRUSelectPane(
        parameters: FilerPickerParameters,
        onQuickFileSelect : (FilePickerResult) -> Unit) {

        filePickerMruPane.showMruPane(
            parameters, onQuickFileSelect
        )
    }
    //TODO the file picker should first allow the user to open a sidebar to select a file. That sidebar should have
    // TODO a most recently used list per filetype, then the user could optionally pick that file, or open th efull blown screen.

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

    }
}


class FilePickerParameterProvider @Inject constructor(
    private val configurationStorage: ConfigurationStorage
) {

    fun getVideoPlayerParameters() : FilePickerScreen.Companion.FilerPickerParameters {
        return FilePickerScreen.Companion.FilerPickerParameters(
            rootDirectory = File("/home/stefan/Videos"),
            allowNavigateUpFromRoot = false,
            allowNavigateIntoChildFolders = true,
            filter = FilePickerScreen.Companion.FilerPickerParameters.Filter.MatchingFileTypes(
                listOf(FileType.Movie)
            ),
            allowRenameFiles = false,
            allowMakeDirectory = false
        )
    }

}