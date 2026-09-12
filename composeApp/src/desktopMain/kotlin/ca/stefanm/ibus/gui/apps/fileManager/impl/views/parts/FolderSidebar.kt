package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FileSidebar.Companion.TAG
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

class FolderSidebar @Inject constructor(
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub
) {

    fun openSidebarForFolder(
        folder : File,
        allowModify : Boolean = false,
        allowCopy : Boolean = false,
        onCopyToSelected : (File) -> Unit = {},
        onMoveToSelected : (File) -> Unit = {},
        onPermissionsActivityRequested : (File) -> Unit = {},
        onRenameSelected : (File) -> Unit = {},

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

                val scope = rememberCoroutineScope()
                val directorySize = remember { mutableStateOf<String?>(null) }

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


                        add(
                            TextMenuItem(
                                title = "Open...",
                                onClicked = {
                                    modalMenuService.closeSidePaneOverlay(true)
                                    onOpenSelected(folder)
                                }
                            ).toDynamicLambda())

                        if (allowCopy) {
                            add(
                                TextMenuItem(
                                    title = "Copy To...",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onCopyToSelected(folder)
                                    }
                                ).toDynamicLambda())
                            if (allowModify) {
                                add(
                                    TextMenuItem(
                                        title = "Move To...",
                                        onClicked = {
                                            modalMenuService.closeSidePaneOverlay(true)
                                            onMoveToSelected(folder)
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
                                        onRenameSelected(folder)
                                    }
                                ).toDynamicLambda())
                            add(
                                TextMenuItem(
                                    title = "Delete",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onDeleteSelected(folder)
                                    }
                                ).toDynamicLambda())
                        }

                        //Readable,
                        add(CheckBoxMenuItem(
                            title = "Readable?",
                            isSelectable = false,
                            isChecked = folder.canRead(),
                            onCheckChanged = { }
                        ).toDynamicLambda())
                        //Writable,
                        add(CheckBoxMenuItem(
                            title = "Writable?",
                            isSelectable = false,
                            isChecked = folder.canWrite(),
                            onCheckChanged = { }
                        ).toDynamicLambda())
                        //Executable?
                        add(CheckBoxMenuItem(
                            title = "Executable?",
                            isSelectable = false,
                            isChecked = folder.canExecute(),
                            onCheckChanged = { }
                        ).toDynamicLambda())

                        if (allowModify) {
                            add(
                                TextMenuItem(
                                    title = "Change Permissions...",
                                    onClicked = {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onPermissionsActivityRequested(folder)
                                    }
                                ).toDynamicLambda())
                        }

                        add(
                            { allocatedIndex, currentIndex ->
                                MenuItem(
                                    label = "Calculated folder size: ${directorySize.value}",
                                    chipOrientation = ItemChipOrientation.NONE,
                                    isSelected = allocatedIndex == currentIndex,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {}
                                )
                            }
                        )
                        add(
                            TextMenuItem(
                                title = "Calculate folder size...",
                                onClicked = {
                                    scope.launch(Dispatchers.IO) {
                                        val size = FileUtils.sizeOfDirectoryAsBigInteger(folder)
                                        directorySize.value = FileUtils.byteCountToDisplaySize(size)
                                    }
                                }
                            ).toDynamicLambda()
                        )

                        add(
                            TextMenuItem(
                                title = "Show complete path...",
                                onClicked = {
                                    notificationHub.postNotificationBackground(Notification(
                                        Notification.NotificationImage.NONE,
                                        folder.name,
                                                folder.absolutePath
                                    ))
                                }
                            ).toDynamicLambda()
                        )
                    }
                )
            }
        }
    }
}