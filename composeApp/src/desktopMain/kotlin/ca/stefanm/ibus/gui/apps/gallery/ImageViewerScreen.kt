package ca.stefanm.ibus.gui.apps.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ca.stefanm.ca.stefanm.ibus.gui.apps.gallery.impl.ImageViewerToolbars
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarController
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import coil3.compose.AsyncImage
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class ImageViewerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService,
    private val bottomBarController: BottomBarController
) : NavigationNode<Nothing> {

    // Just show one image with some zoom controls, information, etc.
    // Can be called from the slide show app

    companion object {
        const val TAG = "ImageViewerScreen"

        fun openForBrowse(
            navigationNodeTraverser: NavigationNodeTraverser,
            file: File
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                ImageViewerScreen::class.java,
                ImageViewerScreenOpenParameters.SingleFile(
                    image = file
                )
            )
        }
        fun openForImageInSlideShow(
            navigationNodeTraverser: NavigationNodeTraverser,
            parameters : ImageViewerScreenOpenParameters.SlideShow
        ) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                ImageViewerScreen::class.java,
                parameters
            )
        }
    }

    sealed interface ImageViewerScreenOpenParameters {
        /* Open one file in the image viewer */
        data class SingleFile(
            val image : File
        ) : ImageViewerScreenOpenParameters

        /* Image bytes are provided in the args (no file saved) */
        data class SingleBytes(
            val bytes : ByteArray
        ) : ImageViewerScreenOpenParameters {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as SingleBytes

                if (!bytes.contentEquals(other.bytes)) return false

                return true
            }

            override fun hashCode(): Int {
                return bytes.contentHashCode()
            }
        }

        /* Image is opened part of a slideshow */
        data class SlideShow(
            val image : File,
            val onImageNavigateForward : () -> Unit = {},
            val onImageNavigateBackward : () -> Unit = {},
            val onEndSlideshowRequested : () -> Unit = {},
            /** Don't allow a slideshow that wasn't started with a slide duration to resume auto advance */
            val wasStartedWithAutoAdvance : Boolean,
            val onPauseAutoAdvanceRequested : () -> Unit = {},
            val onResumeAutoAdvanceRequested : () -> Unit = {}
        ) : ImageViewerScreenOpenParameters
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ImageViewerScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->

        if (params == null) {
            notificationHub.postNotificationBackground(Notification(
                topText = "No image selected",
                contentText = "You must select a picture to view"
            ))
            navigationNodeTraverser.goBack()
            return@content
        }

        val openParameters = params.requestParameters as? ImageViewerScreenOpenParameters
        if (openParameters == null) {
            logger.w(TAG, "Invalid parameters given")
            return@content
        }

        val knobState =setupListener(
            knobListenerServiceMain,
            logger,
            TAG
        )

        Column(
            Modifier.fillMaxSize().background(Color.Black)
        ) {
            if (openParameters is ImageViewerScreenOpenParameters.SlideShow) {
                ImageViewerToolbars.SlideShowToolbar(
                    knobState,
                    openParameters
                )
            } else {
                ImageViewerToolbars.SingleToolbar(
                    knobState,
                    onExit = {
                        navigationNodeTraverser.goBack()
                    }
                )
            }

            if (openParameters !is ImageViewerScreenOpenParameters.SlideShow) {
                bottomBarController.HideBottomPanelWhileInComposition()
            }

            AsyncImage(
                modifier = Modifier.weight(2F, true).align(Alignment.CenterHorizontally),
                model = when (openParameters) {
                    is ImageViewerScreenOpenParameters.SingleBytes -> openParameters.bytes
                    is ImageViewerScreenOpenParameters.SingleFile -> openParameters.image
                    is ImageViewerScreenOpenParameters.SlideShow -> openParameters.image
                },
                contentDescription = openParameters.toString()
            )
        }

    }

    //This was just for prototyping the slideshow service.
    @Composable
    fun GrossSlideshowViewerStub(openParameters: ImageViewerScreenOpenParameters) {
        if (openParameters is ImageViewerScreenOpenParameters.SlideShow) {
            //Quick and dirty draw some buttons
            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        "Empty item", //Hack because legacy menus break if the first item isn't clickable
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "Filename: ${openParameters.image}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "Navigate Forward...",
                        onClicked = { openParameters.onImageNavigateForward() }
                    ),
                    TextMenuItem(
                        "Navigate backward...",
                        onClicked = { openParameters.onImageNavigateBackward() }
                    ),
                    TextMenuItem(
                        "End Slideshow",
                        onClicked = { openParameters.onEndSlideshowRequested() }
                    ),
                    TextMenuItem(
                        "Pause Auto Advance",
                        onClicked = { openParameters.onPauseAutoAdvanceRequested() }
                    ),
                    TextMenuItem(
                        "Resume Auto Advance",
                        onClicked = { openParameters.onResumeAutoAdvanceRequested() }
                    )
                )
            )
        }

    }
}