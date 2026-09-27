package ca.stefanm.ibus.car.desktop.gui.slideshow

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.LongRunningGuiServices
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.gallery.ImageViewerScreen
import ca.stefanm.ibus.gui.apps.gallery.SlideShowAppHomeScreen
import ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarController
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SnapshotPair
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@PlatformServiceInfo(
    name = "SlideshowService",
    description = "A service that runs the slideshow. "
)
@LongRunningGuiServices
@ConfiguredCarScope
class SlideshowService @Inject constructor(
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher,
    private val configurablePlatform: ConfigurablePlatform
) : LongRunningService(coroutineScope, parsingDispatcher) {

    companion object {
        const val TAG = "SlideshowService"
    }

    data class SlideShowOptions(
        val fileList : List<Pair<File, FileType>>,
        /** Start at index 0 of the fileList */
        val startAtIndex : Int = 0,
        /** No auto-advance is encoded as Duration.Infinite */
        val delayBetweenPictures : Duration,

        //TODO This breaks the architecture a bit and I really should
        //TODO one day make "Gui Services", but that's not today, and I
        //TODO want to one day migrate to KSP + Metro. I don't want to play
        //TODO with Dagger more than I have to for this project.
        val modalMenuService: ModalMenuService,
        val navigationNodeTraverser: NavigationNodeTraverser,
        val bottomBarController: BottomBarController
    )

    override suspend fun doWork() {
        val startedOptions = options
        if (startedOptions == null) {
            logger.w(TAG, "Options were null")
            return
        }

        val fileList = startedOptions.fileList.circular()

        logger.d(TAG, "File List: ${fileList.map { it.first.absolutePath }}")

        val currentIndex = MutableStateFlow(startedOptions.startAtIndex)

        //TODO is today the day we finally learn how the hell molecule works?

        updateBottomBarViewState {
            it.copy(
                currentFileNumber = currentIndex.value,
                totalFiles = fileList.size
            )
        }

        if (startedOptions.delayBetweenPictures.isFinite()) {
            autoAdvanceCanRun = true
            coroutineScope.launch {
                while (serviceIsRunning) {
                    if (autoAdvanceCanRun) {
                        delay(startedOptions.delayBetweenPictures)
                        currentIndex.value += 1
                        logger.d(TAG, "Advanced currentIndex: ${currentIndex.value}")
                    } else {
                        delay(startedOptions.delayBetweenPictures)
                        yield()
                    }
                }
            }
        }

        moleculeFlow(RecompositionMode.Immediate) {
            val index by currentIndex.collectAsState(currentIndex.value)
            ComposeStableFileListItem(fileList[index])
        }.collectLatest { item ->
            logger.d(TAG, "Showing $item")
            updateBottomBarViewState {
                it.copy(
                    currentFileName = item.file.name,
                    currentFileNumber = fileList.indexOf(item.file to item.type) + 1
                )
            }
            when (showFile(item.file, item.type)) {
                SlideshowItemNavigationEvent.Forward -> currentIndex.value += 1
                SlideshowItemNavigationEvent.Backward -> currentIndex.value -= 1
                SlideshowItemNavigationEvent.EndShow -> {
                    GlobalScope.launch {
                        configurablePlatform.stopServiceByName("SlideshowService")
                    }
                }
                SlideshowItemNavigationEvent.PauseAutoAdvance -> {
                    autoAdvanceCanRun = false
                }
                SlideshowItemNavigationEvent.ResumeAutoAdvance -> {
                    autoAdvanceCanRun = true
                }
                null -> { /** Noop, showFile logs the type was wrong */ }
            }
        }
    }

    private var serviceIsRunning = false
    private var autoAdvanceCanRun = false
        set(value) {
            field = value
            updateBottomBarViewState {
                it.copy(autoAdvanceRunning = value)
            }
        }
    override fun onCreate() {
        serviceIsRunning = true
        autoAdvanceCanRun = true
        super.onCreate()
        initializeBottomBarViewState()
    }
    override fun onShutdown() {
        serviceIsRunning = false
        autoAdvanceCanRun = false
        bottomBarController.bottomBarViewState.value = BottomBarController.BottomBarViewState.DateTime
        GlobalScope.launch {
            delay(1)
            SlideShowAppHomeScreen.openAfterSlideShowEnds(navigationNodeTraverser)
        }
        super.onShutdown()
    }

    private val slideshowInfo = MutableStateFlow(BottomBarController.BottomBarViewState.SlideShow.SlideShowInfo(
        "No file", 0, 0, false
    ))

    private fun initializeBottomBarViewState() {
        if (bottomBarController.bottomBarViewState.value is BottomBarController.BottomBarViewState.SlideShow) {
            return
        }
        bottomBarController.bottomBarViewState.value = BottomBarController.BottomBarViewState.SlideShow(
            slideShowInfo = slideshowInfo
        )
    }

    private fun updateBottomBarViewState(
        updater : (BottomBarController.BottomBarViewState.SlideShow.SlideShowInfo) -> BottomBarController.BottomBarViewState.SlideShow.SlideShowInfo
    ) {
        initializeBottomBarViewState()
        val current = bottomBarController.bottomBarViewState.value
        if (current is BottomBarController.BottomBarViewState.SlideShow) {
            slideshowInfo.value = updater(slideshowInfo.value)
        }
    }

    var options : SlideShowOptions? = null

    private val navigationNodeTraverser: NavigationNodeTraverser
        get() = options!!.navigationNodeTraverser
    private val modalMenuService: ModalMenuService
        get() = options!!.modalMenuService
    private val bottomBarController : BottomBarController
        get() = options!!.bottomBarController

    class ComposeStableFileListItem(
        val file: File,
        val type : FileType
    ) : SnapshotPair<String, FileType>(file.absolutePath, type) {
        constructor(pair : Pair<File, FileType>) : this(pair.first, pair.second)
    }


    enum class SlideshowItemNavigationEvent {
        Forward,
        Backward,
        EndShow,
        PauseAutoAdvance,
        ResumeAutoAdvance
    }

    private suspend fun showFile(file : File, type : FileType) : SlideshowItemNavigationEvent? {
        navigationNodeTraverser.cleanupBackStackDescendentsOf(SlideShowAppHomeScreen::class.java)
        navigationNodeTraverser.navigateToNode(SlideShowAppHomeScreen::class.java)
        if (type == FileType.Picture) {
            return showImage(file)
        }
        if (type == FileType.Movie) {
            return showMovie(file)
        }
        logger.d(TAG, "An invalid $file, $type was asked to be opened, no action returned.")
        return null
    }

    private suspend fun showImage(
        file : File
    ) : SlideshowItemNavigationEvent = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            //Maybe cleanup the backstack if we auto-advance??
            //navigationNodeTraverser.cleanupBackStackDescendentsOf(ImageViewerScreen::class.java)
        }
        ImageViewerScreen.openForImageInSlideShow(
            navigationNodeTraverser,
            ImageViewerScreen.ImageViewerScreenOpenParameters.SlideShow(
                image = file,
                onImageNavigateForward = {
                    if (continuation.isActive) {
                        continuation.resume(SlideshowItemNavigationEvent.Forward)
                    }
                },
                onImageNavigateBackward = {
                    if (continuation.isActive) {
                        continuation.resume(SlideshowItemNavigationEvent.Backward)
                    }
                },
                onEndSlideshowRequested = {
                    if (continuation.isActive) {
                        continuation.resume(SlideshowItemNavigationEvent.EndShow)
                    }
                },
                wasStartedWithAutoAdvance = options?.delayBetweenPictures?.isFinite() == true,
                onPauseAutoAdvanceRequested = {
                    if (continuation.isActive) {
                        continuation.resume(SlideshowItemNavigationEvent.PauseAutoAdvance)
                    }
                },
                onResumeAutoAdvanceRequested = {
                    if (continuation.isActive) {
                        continuation.resume(SlideshowItemNavigationEvent.ResumeAutoAdvance)
                    }
                }
            )
        )
    }

    //Movies are not auto-advanced through, just pictures.
    private suspend fun showMovie(
        file: File
    ) : SlideshowItemNavigationEvent = suspendCoroutine { continuation ->
        VideoPlayerScreen.openWithFile(
            navigationNodeTraverser,
            VideoPlayerScreen.VideoPlayerScreenParams(
                file = file,
                callOnPlaybackEnd = {
                    modalMenuService.showModalMenu(
                        dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                            IntOffset(50, 120),
                            610
                        ).toNormalModalMenuDimensions(),
                        ModalMenu(
                            items = listOf(
                                ModalMenu.ModalMenuItem(
                                    "Next Slideshow Item",
                                        onClicked = { continuation.resume(SlideshowItemNavigationEvent.Forward) }
                                ),
                                ModalMenu.ModalMenuItem(
                                    "Previous Slideshow Item",
                                    onClicked = { continuation.resume(SlideshowItemNavigationEvent.Backward) }
                                ),
                                ModalMenu.ModalMenuItem(
                                    "End Slideshow",
                                    onClicked = { continuation.resume(SlideshowItemNavigationEvent.EndShow) }
                                )
                            ),
                            chipOrientation = ItemChipOrientation.W
                        )
                    )
                }
            )
        )
    }
}