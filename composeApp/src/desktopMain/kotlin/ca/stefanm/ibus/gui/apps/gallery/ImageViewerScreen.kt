package ca.stefanm.ibus.gui.apps.gallery

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
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
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    // Just show one image with some zoom controls, information, etc.
    // Can be called from the slide show app

    companion object {
        const val TAG = "ImageViewerScreen"

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
        ) : ImageViewerScreenOpenParameters
        /* Image is opened part of a slideshow */
        data class SlideShow(
            val image : File,
            val onImageNavigateForward : () -> Unit = {},
            val onImageNavigateBackward : () -> Unit = {},
            val onEndSlideshowRequested : () -> Unit = {}
        ) : ImageViewerScreenOpenParameters
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ImageViewerScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->

        //TODO quick and dirty parameter parsing to test out slideshows

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

        if (openParameters is ImageViewerScreenOpenParameters.SlideShow) {
            //Quick and dirty draw some buttons
            FullScreenMenu.OneColumn(
                listOf(
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
                    )
                )
            )
        }

        //TODO parse params
        Text("SOP d00dz")


    }
}