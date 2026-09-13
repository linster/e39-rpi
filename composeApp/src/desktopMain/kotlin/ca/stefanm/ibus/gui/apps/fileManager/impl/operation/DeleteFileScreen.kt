package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerSettingsScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettings
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverridesRepo
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Named


@AutoDiscover
class DeleteFileScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "DeleteFileScreen"
        fun deleteFile(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                DeleteFileScreen::class.java,
                file
            )
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DeleteFileScreen::class.java

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
                "Delete File Screen",
                "Filesystem modification is disabled."
            ))
            navigationNodeTraverser.goBack()
        }

        val file : File? = params?.requestParameters as? File

        if (file == null) {
            navigationNodeTraverser.goBack()
            return@content
        }

        with (context) {
            OneColumnSmoothScreenCustomViews(
                header = "Delete file?",
                items = listOf(
                    TextMenuItem(
                        title = "File name: ${file.name}",
                        isSelectable = false,
                        onClicked = {}
                    ).toDynamicLambda(),
                    TextMenuItem(
                        title = file.absolutePath,
                        isSelectable = false,
                        onClicked = {}
                    ).toDynamicLambda(),
                    TextMenuItem(
                        title = "Run delete...",
                        onClicked = {
                            runDelete(file)
                        }
                    ).toDynamicLambda()
                )
            )
        }
    }

    internal fun runDelete(file: File) {
        if (file.isDirectory) {
            deleteFolder(file)
        } else {
            deleteFile(file)
        }
    }

    internal fun deleteFile(file: File) {
        val deleteResult = runCatching {
            FileUtils.delete(file)
        }
        val exception = deleteResult.exceptionOrNull()
        if (exception != null) {
            logger.e(TAG, "Could not delete file", exception)
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                "Could not delete file",
                exception.message ?: ""
            ))
        }
    }

    internal fun deleteFolder(folder : File) {
        val deleteResult = runCatching {
            FileUtils.deleteDirectory(folder)
        }
        val exception = deleteResult.exceptionOrNull()
        if (exception != null) {
            logger.e(TAG, "Could not delete folder", exception)
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                "Could not delete folder",
                exception.message ?: ""
            ))
        }
    }

    //Same idea as Rename file screen
}