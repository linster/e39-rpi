package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.debug.screens

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FileSidebar
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FolderSidebar
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "FilePaneDebugScreen",
    description = "A screen for debugging the file panes"
)
@AutoDiscover
class FilePaneDebugScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX)
    private val knobListenerServiceMainAux: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,

    private val fileSidebar : FileSidebar,
    private val folderSidebar : FolderSidebar
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "FilePaneDebugScreen"
    }
    override val thisClass: Class<out NavigationNode<Nothing>> = FilePaneDebugScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

        val context = object : SmoothScroll.SmoothScrollContext {
            override fun knobListenerService() = knobListenerServiceMain
            override fun tag() = TAG
            override fun logger() = logger
            override fun navigationNodeTraverser() = navigationNodeTraverser
        }

        with (context) {
            OneColumnSmoothScreen(
                header = "File pane debug",
                items = listOf(
                    File("/home/stefan/Downloads/win95_ico/Windows 95 ALL ICONS v1.1/All [Without duplicates]/3D computer.ico"),
                    File("/home/stefan/Documents/camera manual.pdf"),
                    File("/home/stefan/Documents/bmw_navigation.png"),
                    File("/home/stefan/Pictures/Sep12025CanonCardBackup/IMG_6821.JPG"),
                    File("/home/stefan/Videos/Remington Steele 1982 Season 3 Complete TVRip x264 [i_c]/Remington Steele S03E03 Maltese Steele.mkv"),
                    File("/home/stefan/websites/compaw/remember.the-aero.org/speaker/speaker.txt"),
                    File("/home/stefan/Pictures/2025 Volvo Boroscope")
                ).map {
                    val prefix = if (it.isFile) {
                        "File"
                    } else if (it.isDirectory) {
                        "Directory"
                    } else {
                        ""
                    }
                    TextMenuItem(
                        title = "$prefix ${it.absolutePath}",
                        onClicked = {
                            if (it.isFile) {
                                fileSidebar.openSidebarForFile(it)
                            }
                            if (it.isDirectory) {
                                folderSidebar.openSidebarForFolder(it)
                            }
                        }
                    )
                }
            )
        }
    }
}