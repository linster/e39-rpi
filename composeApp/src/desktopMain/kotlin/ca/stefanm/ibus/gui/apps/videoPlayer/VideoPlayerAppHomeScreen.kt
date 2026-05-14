package ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import java.io.File
import javax.inject.Inject

@AutoDiscover
class VideoPlayerAppHomeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = VideoPlayerAppHomeScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        VideoPlayerScreen.openWithFile(
            navigationNodeTraverser,
            VideoPlayerScreen.VideoPlayerScreenParams(
                File("/home/stefan/Videos/Remington Steele 1982 Season 3 Complete TVRip x264 [i_c]/Remington Steele S03E09 Cast in Steele.mkv")
            )
        )

    }
}