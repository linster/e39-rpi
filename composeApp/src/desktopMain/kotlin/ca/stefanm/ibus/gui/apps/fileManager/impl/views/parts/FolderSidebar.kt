package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FileSidebar.Companion.TAG
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

class FolderSidebar @Inject constructor(
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val navigationNodeTraverser: NavigationNodeTraverser
) {

    fun openSidebarForFolder(
        folder : File,
        allowModify : Boolean = false,
        allowCopy : Boolean = false,
        onCopyToSelected : (File) -> Unit = {},
        onMoveToSelected : (File) -> Unit = {},
        onPermissionsActivityRequested : (File) -> Unit = {},
        onRenameSelected : (File) -> Unit = {},

        //TODO probably don't need this if relying on fake directory entries.
        allowSelect : Boolean = false,
        onSelectFolder : (File) -> Unit = {},
        //TODO probably don't need this if relying on fake directory entries.


        onDeleteSelected : (File) -> Unit = {},

        onOpenSelected : (File) -> Unit = {}, //Opening the folder changes the directory navigation
    ) {
        if (!folder.isDirectory) {
            logger.w(TAG, "File $folder is not a directory.")
            return
        }
        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->
            SidePanelMenu.SidePanelMenu(
                title = folder.name
            ) {

                //FileUtils.sizeOfDirectoryAsBigInteger(folder)

                SmoothScroll.SmoothScroll(
                    modifier = Modifier.fillMaxWidth(),
                    knobListenerService = knobListenerServiceModal,
                    tag = TAG,
                    logger = logger,
                    prependGoBackEntry = false,
                    navigationNodeTraverser = navigationNodeTraverser,
                    items = buildList {
                        add(
                            TextMenuItem(
                                title = "Go Back",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay(true)
                                }).toDynamicLambda()
                        )


                        //Complete path
                        //Calculated folder size
                        //  TODO put into mutableState
                        //Button to calculate the folder size
                        // Count of the number of child files and folders (first level)
                        // Count of the number of child files and folders (all levels?)
                        // Delete
                        // Rename
                        // Copy To
                        // Move To
                        // Permissions...
                        //  TODO have the permissions activity here.
                    }
                )
            }
        }
    }
}