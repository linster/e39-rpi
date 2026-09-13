package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerSettingsScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettings
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverridesRepo
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.io.files.Path
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "RenameFileScreen",
    description = "A screen that prompts the user to rename a file"
)
@AutoDiscover
class RenameFileScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "RenameFileScreen"
        fun renameFile(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                RenameFileScreen::class.java,
                file
            )
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RenameFileScreen::class.java

    private val context = object : SmoothScroll.SmoothScrollContext {
        override fun knobListenerService() = knobListenerServiceMain
        override fun tag() = FileManagerSettingsScreen.TAG
        override fun logger() = logger
        override fun navigationNodeTraverser() = navigationNodeTraverser
    }

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->

        if (!FileManagerSettingsOverridesRepo.config[FileManagerSettings.allowFilesystemModification]) {
            logger.d(TAG, "Filesystem modification is not allowed, returning")
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                "Rename File Screen",
                "Filesystem modification is disabled."
            ))
            navigationNodeTraverser.goBack()
        }

        val file : File? = params?.requestParameters as? File

        if (file == null) {
            navigationNodeTraverser.goBack()
            return@content
        }

        val proposedRename = remember { mutableStateOf(file.name) }

        with (context) {
            OneColumnSmoothScreenCustomViews(
                header = "Rename file?",
                items = listOf(
                    TextMenuItem(
                        title = "Current file name: ${file.name}",
                        isSelectable = false,
                        onClicked = {}
                    ).toDynamicLambda(),
                    TextMenuItem(
                        title = file.absolutePath,
                        isSelectable = false,
                        onClicked = {}
                    ).toDynamicLambda(),
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "Proposed name: ${proposedRename.value}",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                modalMenuService.showKeyboard(
                                    Keyboard.KeyboardType.FULL,
                                    prefilled = proposedRename.value,
                                    onTextEntered = { new ->
                                        if (validateNewFileName(file, new)) {
                                            proposedRename.value = new
                                        }
                                    }
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "Do rename...",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                doRename(file, proposedRename.value)
                            }
                        )
                    }
                )
            )
        }

    }

    fun validateNewFileName(current: File, new : String) : Boolean {
        runCatching { FilenameUtils.concat(current.parentFile.path, new) }.fold(
            onSuccess = {
                return it != null //Could be null result if invalid
            },
            onFailure = {
                return false
            }
        )
    }

    fun doRename(current : File, new : String) {

        val newFileHandle = File(current.parentFile, new)

        val result = current.renameTo(newFileHandle)
        if (!result) {
            logger.w(TAG, "Could not rename file $current to $new")
        } else {
            navigationNodeTraverser.goBack()
        }
    }


}