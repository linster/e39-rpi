package ca.stefanm.ibus.car.desktop.gui.slideshow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningGuiServices
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.gui.apps.actionRouter.ActionRouter
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.gallery.ImageViewerScreen
import ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
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
    private val modalMenuService: ModalMenuService
) : LongRunningService(coroutineScope, parsingDispatcher) {

    companion object {
        const val TAG = "SlideshowService"
    }

    data class SlideShowOptions(
        val fileList : List<Pair<File, FileType>>,
        val delayBetweenPictures : Duration
    )

    override suspend fun doWork() {
        val startedOptions = options
        if (startedOptions == null) {
            logger.w(TAG, "Options were null")
            return
        }


        startedOptions.fileList.forEachIndexed { index, (file, type) ->

        }

    }

    var options : SlideShowOptions? = null



    enum class SlideshowItemNavigationEvent {
        Forward,
        Backward,
        Close,
        EndShow
    }

    private suspend fun showFile(file : File, type : FileType) : SlideshowItemNavigationEvent? {
        if (type == FileType.Picture) {
            return showImage(file)
        }
        if (type == FileType.Movie) {
            return showMovie(file)
        }
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
                onImageNavigateClosed = { continuation.resume(SlideshowItemNavigationEvent.Close) },
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