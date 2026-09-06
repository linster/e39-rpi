package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.Path
import kotlin.time.Instant

class FileSidebar @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MODAL)
    private val knobListenerServiceModal: KnobListenerService,

    private val logger: Logger,
    private val modalMenuService: ModalMenuService,

    private val navigationNodeTraverser: NavigationNodeTraverser,

    private val mimeTools: MimeTools,

    private val previewProvider: PreviewProvider
) {

    companion object {
        const val TAG = "FileSidebar"
    }
    //TODO show some extra non-selectable options (video length, etc)
    //TODO depending on file type.
    fun openSidebarForFile(
        file : File,
        allowModify : Boolean = false,
        allowOpen : Boolean = false,
        allowSelect : Boolean = false,
        allowCopy : Boolean = false,
        onCopyToSelected : (File) -> Unit = {},
        onMoveToSelected : (File) -> Unit = {},
        onPermissionsActivityRequested : (File) -> Unit = {},
        onRenameSelected : (File) -> Unit = {},
        onOpenSelected : (File) -> Unit = {},
        onSelectFile : (File) -> Unit = {},
        onDeleteSelected : (File) -> Unit = {}
    ) {
        if (!file.isFile) {
            logger.w(TAG, "File $file is not a file.")
            return
        }
        //TODO can we get a mime type on a file here?
        //TODO maybe need a class for that....
        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->

            SidePanelMenu.SidePanelMenu(
                //TODO just put the filename in the header, and
                // the full path in the list.
                title = file.name
            ) {

                //TODO this might be expensive.
                val metaData = mimeTools
                    .getFileTypeAndMetaDataForFile(file, allData = false)

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
                        add { allocatedIndex, currentIndex ->
                            ArbitraryContentsMenuItem(onClicked = {}) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(130.dp.halveIfNotPixelDoubled()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    previewProvider.FilePreview(
                                        file = file,
                                        type = metaData.first
                                    )
                                }
                            }
                        }
                        if (allowOpen) {
                            add(
                                TextMenuItem(
                                title = "Open...",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay(true)
                                    onOpenSelected(file)
                                }
                            ).toDynamicLambda())
                        }
                        if (allowSelect) {
                            add(
                                TextMenuItem(
                                    title = "Select this file",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onSelectFile(file)
                                    }
                                ).toDynamicLambda())
                        }

                        if (allowCopy) {
                            add(
                                TextMenuItem(
                                    title = "Copy To...",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onCopyToSelected(file)
                                    }
                                ).toDynamicLambda())
                            if (allowModify) {
                                add(
                                    TextMenuItem(
                                        title = "Move To...",
                                        onClicked = {
                                            modalMenuService.closeSidePaneOverlay(true)
                                            onMoveToSelected(file)
                                        }
                                    ).toDynamicLambda())
                            }
                        }
                        if (allowModify) {
                            add(
                                TextMenuItem(
                                    title = "Rename",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onRenameSelected(file)
                                    }
                                ).toDynamicLambda())
                            add(
                                TextMenuItem(
                                    title = "Delete",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onDeleteSelected(file)
                                    }
                                ).toDynamicLambda())
                        }

                        //Readable,
                        add(CheckBoxMenuItem(
                            title = "Readable?",
                            isSelectable = false,
                            isChecked = file.canRead(),
                            onClicked = { }
                        ).toDynamicLambda())
                        //Writable,
                        add(CheckBoxMenuItem(
                            title = "Writable?",
                            isSelectable = false,
                            isChecked = file.canWrite(),
                            onClicked = { }
                        ).toDynamicLambda())
                        //Executable?
                        add(CheckBoxMenuItem(
                            title = "Executable?",
                            isSelectable = false,
                            isChecked = file.canExecute(),
                            onClicked = { }
                        ).toDynamicLambda())
                        //File size
                        add(TextMenuItem(
                            title = "Size: ${
                                FileUtils.byteCountToDisplaySize(Files.size(Path(file.path)))
                            }",
                            isSelectable = false,
                            onClicked = {}
                        ).toDynamicLambda())
                        //Modified date
                        add(TextMenuItem(
                            title = "Modified: ${file.lastModified().let { 
                                Instant
                                    .fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .format(LocalDateTime.Formats.ISO)
                            }}",
                            isSelectable = false,
                            onClicked = {}
                        ).toDynamicLambda())
                        if (allowModify) {
                            add(
                                TextMenuItem(
                                    title = "Change Permissions...",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        //TODO maybe have the permission grid be in this class?
                                        onPermissionsActivityRequested(file)
                                    }
                                ).toDynamicLambda())
                        }

                        if (metaData.second.isNotEmpty()) {
                            add(
                                TextMenuItem(
                                title = "Metadata",
                                isSelectable = false,
                                onClicked = {}
                            ).toDynamicLambda())
                            metaData
                                .second
                                .forEach { (name, value) ->
                                    add(
                                        TextMenuItem(
                                            title = name,
                                            isSelectable = false,
                                            onClicked = {}
                                        ).toDynamicLambda()
                                    )
                                    add(
                                        TextMenuItem(
                                            title = "  $value",
                                            isSelectable = false,
                                            onClicked = {}
                                        ).toDynamicLambda()
                                    )
                                }
                        }
                    }
                )
                }
            }
        }
}
