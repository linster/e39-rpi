package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class FileSidebar @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MODAL)
    private val knobListenerServiceModal: KnobListenerService,

    private val logger: Logger,
    private val modalMenuService: ModalMenuService,

    private val navigationNodeTraverser: NavigationNodeTraverser
) {

    companion object {
        const val TAG = "FileSidebar"
    }
    fun openSidebarForFile(
        file : File,
        allowModify : Boolean = false
    ) {
        if (!file.isFile) {
            logger.w(TAG, "File $file is not a file.")
            return
        }
        //TODO can we get a mime type on a file here?
        //TODO maybe need a class for that....
        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->

            SidePanelMenu.SidePanelMenu(
                title = file.absolutePath
            ) {
                SmoothScroll.SmoothScroll(
                    modifier = Modifier.fillMaxWidth(),
                    knobListenerService = knobListenerServiceModal,
                    tag = TAG,
                    logger = logger,
                    prependGoBackEntry = false,
                    navigationNodeTraverser = navigationNodeTraverser,
                    items = listOf(
                        TextMenuItem(
                            title = "Go Back",
                            onClicked = {}
                        ).toDynamicLambda(),
                        { allocatedIndex, currentIndex ->
                            ArbitraryContentsMenuItem(onClicked = {}) {
                                Column (
                                    Modifier
                                        .fillMaxWidth()
                                        .height(130.dp.halveIfNotPixelDoubled()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ){
                                    Box(
                                        Modifier
                                            .aspectRatio(1F)
                                            .background(Color.Black)
                                    ) {}
                                }                            }
                        },
                        TextMenuItem(
                            title = "Open...",
                            onClicked = {}
                        ).toDynamicLambda(),
                        TextMenuItem(
                            title = "Copy To...",
                            onClicked = {}
                        ).toDynamicLambda(),
                        TextMenuItem(
                            title = "Move To...",
                            onClicked = {}
                        ).toDynamicLambda(),
                        TextMenuItem(
                            title = "Rename",
                            onClicked = {}
                        ).toDynamicLambda(),
                        TextMenuItem(
                            title = "Delete",
                            onClicked = {}
                        ).toDynamicLambda(),
                        //Readable,
                        //Writable,
                        //Executable?
                        //File size
                        //Modified date
                        TextMenuItem(
                            title = "Change Permissions...",
                            onClicked = {}
                        ).toDynamicLambda()
                    )
                )
            }
            //TODO go to SearchState.showExploreResultsPane() for inspo.

            //Go back
            // A big square preview
            // Open...
            // Copy To...
            // Move To...
            // Rename
            // Delete
            // Permission grid
            // Owner
            // Chmod...
            // Chown...

        }
    }
}