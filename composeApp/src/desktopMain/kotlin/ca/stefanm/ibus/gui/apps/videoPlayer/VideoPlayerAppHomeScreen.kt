package ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import java.io.File
import javax.inject.Inject

@AutoDiscover
class VideoPlayerAppHomeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = VideoPlayerAppHomeScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->


        Column(
            Modifier.fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("Video Player")

            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    )
                ),
                rightItems = listOf(
                    TextMenuItem(
                        "Remington Steele S03E09",
                        onClicked = {
                            VideoPlayerScreen.openWithFile(
                                navigationNodeTraverser,
                                VideoPlayerScreen.VideoPlayerScreenParams(
                                    File("/home/stefan/Videos/Remington Steele 1982 Season 3 Complete TVRip x264 [i_c]/Remington Steele S03E09 Cast in Steele.mkv")
                                )
                            )
                        }
                    ),
                    TextMenuItem(
                        "Open File Picker",
                        onClicked = {}
                    )
                )
            )
        }



    }
}