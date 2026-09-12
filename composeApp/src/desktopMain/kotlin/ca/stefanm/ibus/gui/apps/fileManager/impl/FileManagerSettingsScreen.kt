package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
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
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import kotlinx.coroutines.NonCancellable.key
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.TimeMark

@AutoDiscover
class FileManagerSettingsScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val folderSelectionResultHelper: FileManagerScreenFolderSelectionResultHelper
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "FileManagerSettingsScreen"
    }

    private val context = object : SmoothScroll.SmoothScrollContext {
        override fun knobListenerService() = knobListenerServiceMain
        override fun tag() = TAG
        override fun logger() = logger
        override fun navigationNodeTraverser() = navigationNodeTraverser

    }

    // TODO Open with xdg-open for unrecognized file types? (no on pi)
    //
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FileManagerSettingsScreen::class.java


    @Composable
    fun subscribeConfig(config : Config) : State<Instant> {
        val result = remember { mutableStateOf(config) }
        val configTimeStamp = remember { mutableStateOf(Clock.System.now())}
        DisposableEffect(Unit) {
            val handler = config.afterSet { item, value ->
                result.value = config
                configTimeStamp.value = Clock.System.now()
            }
            onDispose {
                handler.cancel()
            }
        }
        return configTimeStamp
    }

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val defaultBrowseFolderSelection : File? = folderSelectionResultHelper.parseSelectedFolder(params)

        if (defaultBrowseFolderSelection != null) {
            logger.i(TAG, "Setting default browse folder: ${defaultBrowseFolderSelection.absolutePath}")
            FileManagerSettingsOverridesRepo.config[FileManagerSettings.defaultBrowseFolder] = defaultBrowseFolderSelection.absolutePath
        } else {
            logger.i(TAG, "No change to default browse folder")
        }

        val configState = subscribeConfig(FileManagerSettingsOverridesRepo.config)

        key(configState.value) {
            with(context) {
                OneColumnSmoothScreen(
                    header = "File Manager Settings",
                    items = listOf(
                        CheckBoxMenuItem(
                            title = "Show Hidden Files?",
                            isChecked = FileManagerSettingsOverridesRepo.config[FileManagerSettings.showHiddenFiles],
                            onCheckChanged = {
                                FileManagerSettingsOverridesRepo.config[FileManagerSettings.showHiddenFiles] = it
                            }
                        ),
                        CheckBoxMenuItem(
                            title = "Allow filesystem modification?",
                            isChecked = FileManagerSettingsOverridesRepo.config[FileManagerSettings.allowFilesystemModification],
                            onCheckChanged = {
                                FileManagerSettingsOverridesRepo.config[FileManagerSettings.allowFilesystemModification] =
                                    it
                            }
                        ),
                        TextMenuItem(
                            title = "Set Default Browse folder (text)",
                            onClicked = {
                                //TODO keyboard should allow scrolling in the input box and overflow.
                                modalMenuService.showKeyboard(
                                    Keyboard.KeyboardType.FULL,
                                    prefilled = FileManagerSettingsOverridesRepo.config[FileManagerSettings.defaultBrowseFolder],
                                    onTextEntered = { new ->
                                        val parseFileResult = runCatching { File(URI.create("file://$new")) }
                                        if (parseFileResult.isFailure) {
                                            notificationHub.postNotificationBackground(
                                                Notification(Notification.NotificationImage.ALERT_TRIANGLE,
                                                    "Entered path $new is invalid",
                                                    parseFileResult.exceptionOrNull()?.message ?: ""
                                                )
                                            )
                                            logger.e(TAG, "Could not parse new base folder", parseFileResult.exceptionOrNull())
                                            return@showKeyboard
                                        }
                                        FileManagerSettingsOverridesRepo.config[FileManagerSettings.defaultBrowseFolder] = new
                                    }
                                )
                            }
                        ),
                        TextMenuItem(
                            title = "Select default browse folder (interactive)",
                            onClicked = {
                                FileManagerScreen.openForFolderSelection(
                                    navigationNodeTraverser,
                                    baseDirectory = File("/")
                                )
                            }
                        )
                    )
                )
            }
        }
    }

}