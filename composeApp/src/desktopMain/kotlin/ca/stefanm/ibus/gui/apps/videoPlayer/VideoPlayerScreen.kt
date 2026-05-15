package ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarController
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.volume.VolumePaneService
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.utils.toKotlinxIoPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AutoDiscover
class VideoPlayerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,

    private val bottomBarController: BottomBarController,

    private val modalVolumePaneService: VolumePaneService

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
        val seekToTimeOnOpen : Duration = Duration.ZERO,
        val loopPlaybackByDefault : Boolean = false
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

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = contents@ { params ->

        if (params == null) {
            navigationNodeTraverser.setResultAndGoBack(
                this,
                VideoPlayerScreenResult.ClosedDueToParameterError(
                    VideoPlayerScreenResult.ClosedDueToParameterError.ParameterError.NoParameterPassed
                )
            )
            return@contents
        }

        val inputParams = params.requestParameters as? VideoPlayerScreenParams
        if (inputParams == null) {
            navigationNodeTraverser.setResultAndGoBack(
                this,
                VideoPlayerScreenResult.ClosedDueToParameterError(
                    VideoPlayerScreenResult.ClosedDueToParameterError.ParameterError.NoParameterPassed
                )
            )
            return@contents
        }

        bottomBarController.HideBottomPanelWhileInComposition()

        //https://github.com/kdroidFilter/ComposeMediaPlayer

        val playerState = rememberVideoPlayerState()

        LaunchedEffect(inputParams.loopPlaybackByDefault) {
            playerState.loop = inputParams.loopPlaybackByDefault
        }

        var hasShownLoadingThrobber by remember { mutableStateOf(false) }
        DisposableEffect(playerState.isLoading, playerState.sliderPos) {
            val isLoading = playerState.isLoading
            val hasStartedPlaying = playerState.sliderPos > 0
            if (isLoading && hasStartedPlaying && !hasShownLoadingThrobber) {
                modalMenuService.showModalWaitDialog(
                    throbber = true,
                    headerText = "Loading Video...",
                    autoCloseTimeout = 3.seconds,
                    isCancellable = true,
                    onCancel = { navigationNodeTraverser.navigateToRoot() }
                )
                hasShownLoadingThrobber = true
            }
            onDispose {
                if (!hasStartedPlaying) {
                    modalMenuService.closeModalMenu()
                }
            }
        }

        LaunchedEffect(Unit) {
            playerState.openFile(
                PlatformFile(inputParams.file),
                initializeplayerState = if (inputParams.playOnOpen) {
                    InitialPlayerState.PLAY
                } else {
                    InitialPlayerState.PAUSE
                }
            )
        }

        LaunchedEffect(playerState.isLoading) {
            logger.d(TAG, "isLoading ${playerState.isLoading}, ${
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(LocalDateTime.Formats.ISO)
            }")
        }

        LaunchedEffect(playerState.isPlaying) {
            logger.d(TAG, "isPlaying ${playerState.isPlaying}, ${
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(LocalDateTime.Formats.ISO)
            }")
        }

        LaunchedEffect(playerState.error) {
            playerState.error?.let { error ->
                logger.e(TAG, "Player error: $error")
            }
        }

        var overlayEnabled by remember { mutableStateOf(false) }

        LaunchedEffect(overlayEnabled) {
            //Clicking the knob opens the overlay
            if (!overlayEnabled) {
                knobListenerService.knobTurnEvents()
                    .filterIsInstance<InputEvent.NavKnobPressed>()
                    .onEach {
                        logger.d(TAG, "Nav knob pressed while no overlay")
                    }
                    .collect {
                        overlayEnabled = true
                    }
            }
        }

        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            VideoPlayerSurface(
                playerState = playerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        overlayEnabled = true
                    },
                contentScale = ContentScale.Fit,
                overlay = {
                    if (overlayEnabled) {
                        Overlay(
                            inputParams,
                            playerState,
                            onRequestOverlayClose = { overlayEnabled = false }
                        )
                    }
                }
            )
        }

    }

    @Composable
    fun BoxScope.Overlay(
        params: VideoPlayerScreenParams,
        playerState: VideoPlayerState,
        onRequestOverlayClose : () -> Unit
    ) {
        //VideoPlayerSurface makes this a full size box.

        val knobState = setupListener(
            knobListenerService,
            logger,
            "slotLayoutTestWindow"
        )

        Column(
            Modifier.fillMaxHeight()
        ) {

            BmwSingleLineHeader(params.file.name)

            Spacer(Modifier.weight(2F, true))

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.backgroundGradientColorList
                        )
                    )
            ) {

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = "🡄 ",
                        boxModifier = Modifier.Companion,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            onRequestOverlayClose()
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    val isPlaying = playerState.isPlaying
                    MenuItem(
                        label = if (isPlaying) { " ⏸ " } else { " ⏵ " },
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            if (isPlaying) {
                                playerState.pause()
                            } else {
                                playerState.play()
                            }
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = "⏪ ",
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.pause()
                            playerState.sliderPos = (playerState.sliderPos - 10).coerceIn(0F, 1000F)
                            playerState.play()
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = " ⏩",
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.pause()
                            playerState.sliderPos = (playerState.sliderPos + 10).coerceIn(0F, 1000F)
                            playerState.play()
                        }
                    )
                }


                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = "Seek...",
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.pause()
                            GlobalScope.launch {
                                onRequestOverlayClose()
                                modalMenuService.showIntSlider(
                                    hintText = "Seek",
                                    initialValue = playerState.sliderPos.toInt(),
                                    validItems = 0 .. 1000 step 10,
                                    onCurrentValueChanged = {
                                        playerState.seekTo(it.toFloat())
                                    },
                                    onClose = {
                                        GlobalScope.launch {
                                            playerState.pause()
                                            delay(5)
                                            playerState.play()
                                            delay(5)
                                            playerState.pause()
                                        }

                                    }
                                )
                            }
                        }
                    )
                }

                Spacer(Modifier.weight(2F, true))

                MenuItem(
                    label = "${playerState.positionText} / ${playerState.durationText}",
                    boxModifier = Modifier,
                    isSmallSize = true,
                    isSelected = false,
                    chipOrientation = ItemChipOrientation.NONE,
                    onClicked = {}
                )

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    val speed = playerState.playbackSpeed
                    val validSpeeds = listOf(0.5F, 1.0F, 1.5F, 2.0F)

                    //Account for float error
                    fun matchSpeedToValidSpeedIndex() : Int {
                        val tolerance = 0.001
                        return validSpeeds.indexOfFirst {
                            abs(speed - it) < tolerance
                        }
                    }

                    MenuItem(
                        label = matchSpeedToValidSpeedIndex().let {
                            when (it) {
                                0 -> "0.5x"
                                1 -> "1.0x"
                                2 -> "1.5x"
                                3 -> "2.0x"
                                else -> "??"
                            }
                        },
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.playbackSpeed = validSpeeds.circular()[matchSpeedToValidSpeedIndex() + 1]
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    val loop = playerState.loop
                    MenuItem(
                        label = if (loop) {
                            "🔁 "
                        } else { "🔂 "},
                        boxModifier = Modifier,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.loop = !playerState.loop
                        }
                    )
                }


                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = "🔉 ",
                        boxModifier = Modifier.Companion,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalVolumePaneService.showVolumePane()
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        label = " ⏏ ",
                        boxModifier = Modifier.Companion,
                        isSmallSize = true,
                        isSelected = (allocatedIndex == currentIndex),
                        chipOrientation = ItemChipOrientation.S,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            playerState.stop()
                            navigationNodeTraverser.setResultAndGoBack(
                                this@VideoPlayerScreen,
                                calculateResultFromState(playerState)
                            )
                        }
                    )
                }
            }
        }
    }

    private fun calculateResultFromState(playerState: VideoPlayerState) : VideoPlayerScreenResult {
        return if (playerState.currentTime == 0.0) {
            VideoPlayerScreenResult.ClosedWithoutPlayback
        } else {
            VideoPlayerScreenResult.PlaybackCompleted(
                timeFromStart = playerState.currentTime.toDuration(DurationUnit.MILLISECONDS)
            )

        }
    }
}