package ca.stefanm.ibus.car.desktop.gui.slideshow

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.LongRunningGuiServices
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.gui.apps.actionRouter.ActionRouter
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.gallery.ImageViewerScreen
import ca.stefanm.ibus.gui.apps.gallery.SlideShowAppHomeScreen
import ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SnapshotPair
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates
import kotlin.time.Duration

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
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val actionRouter: ActionRouter,
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform
) : LongRunningService(coroutineScope, parsingDispatcher) {

    companion object {
        const val TAG = "SlideshowService"
    }

    data class SlideShowOptions(
        val fileList : List<Pair<File, FileType>>,
        /** No auto-advance is encoded as Duration.Infinite */
        val delayBetweenPictures : Duration,
    )

    override suspend fun doWork() {
        val startedOptions = options
        if (startedOptions == null) {
            logger.w(TAG, "Options were null")
            return
        }

            //TODO I just realized I could use the navigation back stack and pop screens rather than maintain a list.

        val fileList = startedOptions.fileList.circular()
        val currentIndex = MutableStateFlow(fileList.indices.first)

        //TODO is today the day we finally learn how the hell molecule works?

        moleculeFlow(RecompositionMode.Immediate) {
            val index by currentIndex.collectAsState(currentIndex.value)
            //TODO need a compose stable data type for the entry record.

            //TODO for auto advance I should should manipulate the state flow in a coroutine .
            fileList[index]
        }.collect { item ->
            when (val action = showFile(item)) {
                SlideshowItemNavigationEvent.Forward -> currentIndex.value += 1
                SlideshowItemNavigationEvent.Backward -> currentIndex.value -= 1
                SlideshowItemNavigationEvent.EndShow -> {
                    SlideShowAppHomeScreen.openAfterSlideShowEnds(navigationNodeTraverser)
                    configurablePlatform.stopServiceByName("SlideshowService")
                }
                null -> { /** Noop, showFile logs the type was wrong */ }
            }
        }
    }

    var options : SlideShowOptions? = null

    @Stable
    data class ComposeStableFileListItem() : SnapshotPair


    enum class SlideshowItemNavigationEvent {
        Forward,
        Backward,
        EndShow
    }

    private suspend fun showFile(item : Pair<File, FileType>) : SlideshowItemNavigationEvent? {
        return showFile(item.first, item.second)
    }

    private suspend fun showFile(file : File, type : FileType) : SlideshowItemNavigationEvent? {
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
    ) : SlideshowItemNavigationEvent = suspendCoroutine { continuation ->
        ImageViewerScreen.openForImageInSlideShow(
            navigationNodeTraverser,
            ImageViewerScreen.ImageViewerScreenOpenParameters.SlideShow(
                image = file,
                onImageNavigateForward = { continuation.resume(SlideshowItemNavigationEvent.Forward) },
                onImageNavigateBackward = { continuation.resume(SlideshowItemNavigationEvent.Backward) },
                onEndSlideshowRequested = { continuation.resume(SlideshowItemNavigationEvent.EndShow) }
            )
        )
    }

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
                            IntOffset(50, 50),
                            210
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
                                    "Return to Movie (Close Menu)",
                                    onClicked = { modalMenuService.closeModalMenu()}
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