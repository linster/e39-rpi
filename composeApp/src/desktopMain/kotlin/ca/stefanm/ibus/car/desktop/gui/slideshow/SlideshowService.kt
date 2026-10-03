package ca.stefanm.ibus.car.desktop.gui.slideshow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SnapshotPair
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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
@OptIn(ExperimentalAtomicApi::class)
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

        fileList = FileList(startedOptions.fileList)


        logger.d(TAG, "File List: ${fileList.initial.map { it.first.absolutePath }}")

        updateBottomBarViewState {
            it.copy(
                totalFiles = fileList.getTotalItems(),
                autoAdvanceRunning = startedOptions.delayBetweenPictures.isFinite()
            )
        }

        showFile(fileList.getFirst())
    }

    private var serviceIsRunning = false

    override fun onCreate() {
        serviceIsRunning = true
        super.onCreate()
        initializeBottomBarViewState()
    }
    override fun onShutdown() {
        serviceIsRunning = false
        bottomBarController.bottomBarViewState.value = BottomBarController.BottomBarViewState.DateTime
        GlobalScope.launch {
            delay(10)
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


    lateinit var fileList: FileList

    private suspend fun showFile(link : SlideshowLink<Pair<File, FileType>>) {
        navigationNodeTraverser.cleanupBackStackDescendentsOf(SlideShowAppHomeScreen::class.java)
        // Navigator isn't thread safe.
        delay(100)
        val file = link.itemToShow.first
        val type = link.itemToShow.second
        updateBottomBarViewState {
            it.copy(
                currentFileName = file.name,
                currentFileNumber = fileList.getCurrentIndex() + 1
            )
        }
        if (type == FileType.Picture) {
            return showImage(link)
        }
        if (type == FileType.Movie) {
            return showMovie(link)
        }
        logger.d(TAG, "An invalid $file, $type was asked to be opened, no action returned.")
    }

    private fun showImage(
        link : SlideshowLink<Pair<File, FileType>>
    ) {

        ImageViewerScreen.openForImageInSlideShow(
            navigationNodeTraverser,
            ImageViewerScreen.ImageViewerScreenOpenParameters.SlideShow(
                image = link.itemToShow.first,
                hasNext = { link.hasSubsequent() },
                onImageNavigateForward = { requestFromAutoAdvance ->
                    logger.d(TAG, "onImageNavigateForward from ${link.itemToShow.first}")
                    if (link.hasSubsequent()) {
                        GlobalScope.launch { showFile(fileList.getNext(link)) }
                    } else {
                        // If we get to the last slide by auto advance, don't just
                        // end the show and force the user to set it up again.
                        if (!requestFromAutoAdvance) {
                            endShow()
                        }
                    }
                },
                hasPrev = { link.hasPrecedent() },
                onImageNavigateBackward = {
                    if (link.hasPrecedent()) {
                        GlobalScope.launch { showFile(fileList.getPrev(link)) }
                    } else {
                        endShow()
                    }
                },
                onEndSlideshowRequested = {
                    endShow()
                },
                wasStartedWithAutoAdvance = options?.delayBetweenPictures?.isFinite() == true,
                advanceAfterDuration = options?.delayBetweenPictures!!,
                onPauseAutoAdvanceRequested = {
                    updateBottomBarViewState {
                        it.copy(
                            autoAdvanceRunning = false
                        )
                    }
                },
                onResumeAutoAdvanceRequested = {
                    updateBottomBarViewState {
                        it.copy(
                            autoAdvanceRunning = true
                        )
                    }
                }
            )
        )
    }

    //Movies are not auto-advanced through, just pictures.
    private fun showMovie(
        link : SlideshowLink<Pair<File, FileType>>
    ) {
        VideoPlayerScreen.openWithFile(
            navigationNodeTraverser,
            VideoPlayerScreen.VideoPlayerScreenParams(
                file = link.itemToShow.first,
                endButonGoesBack = false,
                callOnPlaybackEnd = {
                    logger.d(TAG, "Playback ended")
                    // Although a modal menu would look better, we have to use a side-pane
                    // because the loading throbber in the video player has a race condition
                    // when you stop.
                    modalMenuService.showSidePaneOverlay(true) {
                        SidePanelMenu.SidePanelMenu(
                            title = link.itemToShow.first.name,
                            text = @Composable {
                                SidePanelMenu.InfoLabel("Current/Total :")
                                SidePanelMenu.InfoLabel(
                                    "${slideshowInfo.value.currentFileNumber} / ${slideshowInfo.value.totalFiles}"
                                )
                            },
                            buttons = buildList {
                                if (link.hasSubsequent()) {
                                    add(
                                        TextMenuItem(
                                            "Next Item",
                                            onClicked = {
                                                modalMenuService.closeSidePaneOverlay(true)
                                                GlobalScope.launch { showFile(fileList.getNext(link)) }
                                            }
                                        )
                                    )
                                }
                                if (link.hasPrecedent()) {
                                    add(
                                        TextMenuItem(
                                            "Previous Item",
                                            onClicked = {
                                                modalMenuService.closeSidePaneOverlay(true)
                                                GlobalScope.launch { showFile(fileList.getPrev(link)) }
                                            }
                                        )
                                    )
                                }
                                add(
                                    TextMenuItem(
                                        "End Show",
                                        onClicked = {
                                            modalMenuService.closeSidePaneOverlay(true)
                                            endShow()
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            )
        )
    }

    private fun endShow() {

        SlideShowAppHomeScreen.openAfterSlideShowEnds(navigationNodeTraverser)
        configurablePlatform.stopServiceByName("SlideshowService")
    }

    data class SlideshowLink<T>(
        val itemToShow : T,
        val precedent : T?,
        val subsequent : T?
    ) {
        fun hasPrecedent() = precedent != null
        fun hasSubsequent() = subsequent != null
    }



    class FileList(
        val initial : List<Pair<File, FileType>>
    ) {

        private val sliced = initial.sliceForSlideshowLinks()
        private var currentIndex = 0

        fun getCurrent() : SlideshowLink<Pair<File, FileType>> {
            return sliced[currentIndex]
        }

        fun getCurrentIndex() = currentIndex
        fun getTotalItems() = initial.size

        fun getFirst() = sliced.first()

        fun getNext(item : SlideshowLink<Pair<File, FileType>>) : SlideshowLink<Pair<File, FileType>> {
            val nextItem = sliced.find { it.itemToShow == item.subsequent }
            currentIndex = sliced.indexOf(nextItem)
            return nextItem!!
        }

        fun getPrev(item: SlideshowLink<Pair<File, FileType>>) : SlideshowLink<Pair<File, FileType>> {
            val prevItem = sliced.find { it.itemToShow == item.precedent }
            currentIndex = sliced.indexOf(prevItem)
            return prevItem!!
        }


        fun <T> List<T>.sliceForSlideshowLinks() : List<SlideshowLink<T>> {
            return this.let { it.toMutableList() }.apply {
                addFirst(first())
                addLast(last())
            }.windowed(3, 1, false)
                .map {
                    val itemToShow = it[1]
                    val precedent = it[0]
                    val subsequent = it[2]
                    SlideshowLink(
                        itemToShow,
                        if (precedent != itemToShow) precedent else null,
                        if (subsequent != itemToShow) subsequent else null
                    )
                }
        }
    }


}