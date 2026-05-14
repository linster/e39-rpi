package ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration

@AutoDiscover
class VideoPlayerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) : NavigationNode<VideoPlayerScreen.VideoPlayerScreenResult> {

    companion object {
        const val TAG = "VideoPlayerScreen"

        fun openWithFile(
            navigationNodeTraverser: NavigationNodeTraverser,
            params: VideoPlayerScreenParams
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                VideoPlayerScreen::class.java,
                params
            )
        }
    }

    data class VideoPlayerScreenParams(
        val file : File,
        val goBackOnPlaybackEnd : Boolean = false,
        val playOnOpen : Boolean = true,
        val pauseOnOpenAfterSeconds : Int = 0,
        val seekToTimeOnOpen : Duration = Duration.ZERO
    )

    sealed interface VideoPlayerScreenResult {
        object ClosedWithoutPlayback : VideoPlayerScreenResult
        data class PlaybackCompleted(
            val timeFromStart : Duration
        ) : VideoPlayerScreenResult
        data class ClosedDueToParameterError(
            val error : ParameterError
        ) : VideoPlayerScreenResult {
            sealed interface ParameterError {
                object NoParameterPassed : ParameterError
                data class FileReadError(val throwable: Throwable) : ParameterError

            }
        }
    }

    override val thisClass: Class<out NavigationNode<VideoPlayerScreen.VideoPlayerScreenResult>>
        get() = VideoPlayerScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->
        //https://github.com/kdroidFilter/ComposeMediaPlayer

        val playerState = rememberVideoPlayerState()
        val overlayEnabled by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (overlayEnabled) {
                Overlay(playerState)
            }

//            VideoPlayerSurface(
//                playerState = playerState,
//                modifier = Modifier.fillMaxSize(),
//                overlay = {
//                    if (overlayEnabled) {
//                        Overlay(playerState)
//                    }
//                }
//            )
        }

    }

    @Composable
    fun BoxScope.Overlay(
        playerState: VideoPlayerState
    ) {

        //VideoPlayerSurface makes this a full size box.

    }
}