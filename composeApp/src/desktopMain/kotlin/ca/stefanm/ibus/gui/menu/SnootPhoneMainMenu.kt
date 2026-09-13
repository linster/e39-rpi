package ca.stefanm.ibus.gui.menu

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfViewerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerAppHomeScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.audio.NowPlayingMenu
import ca.stefanm.ibus.gui.chat.screens.ChatAppHomeScreen
import ca.stefanm.ibus.gui.generalSettings.SettingsRootMenu
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.pim.calendar.CalendarScreen
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "SnootPhoneMainMenu",
    description = "A main menu that is meant for 'app' usage, not strictly in the BMW"
)
@AutoDiscover
class SnootPhoneMainMenu @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SnootPhoneMainMenu::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {
        FullScreenMenu.OneColumnSmoothScreen(
            header = "Snoot phone main menu",
            knobListenerService = knobListenerServiceMain,
            logger = logger,
            navigationNodeTraverser = navigationNodeTraverser,
            logTag = "SnootPhoneMainMenu",
            prependGoBackEntry = false,
            items = listOf(
                "GPS-Navigation" to { navigateToNode(MapScreen::class.java) },
                "Matrix Chat" to { navigateToNode(ChatAppHomeScreen::class.java) },
                "Calendar" to { navigateToNode(CalendarScreen::class.java) },
                "Now Playing" to { navigateToNode(NowPlayingMenu::class.java) },
                "PDF Viewer" to { navigateToNode(PdfViewerScreen::class.java) },
                "File Manager" to { FileManagerScreen.openForBrowsing(navigationNodeTraverser) },
                "Video Player" to { navigateToNode(VideoPlayerAppHomeScreen::class.java) },

                "Slide Show" to {},
                "Image Viwer" to {},
                "Weather" to {},

                "Settings" to { navigateToNode(SettingsRootMenu::class.java) }
            )
        )
    }
}