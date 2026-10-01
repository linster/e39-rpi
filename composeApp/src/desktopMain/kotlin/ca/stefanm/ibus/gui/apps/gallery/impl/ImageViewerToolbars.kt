package ca.stefanm.ca.stefanm.ibus.gui.apps.gallery.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.apps.gallery.ImageViewerScreen
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

object ImageViewerToolbars {


    /** Only used when not in slideshow mode. When in slideshow mode,
     *  the BottomBar also has the picture filename and number visible.
     */
    @Composable
    fun HeaderBar(params : ImageViewerScreen.ImageViewerScreenOpenParameters) {
        val headerText = when (params) {
            is ImageViewerScreen.ImageViewerScreenOpenParameters.SingleBytes -> {
                // No point showing the user "Image Viewer" because if they opened the viewer from file
                // bytes, they probably already wanted to perform an action (such as from Matrix chat) to
                // display an image.
                return
            }
            is ImageViewerScreen.ImageViewerScreenOpenParameters.SingleFile -> params.image.absolutePath.takeLast(30)
            is ImageViewerScreen.ImageViewerScreenOpenParameters.SlideShow -> { return }
        }
        BmwSingleLineHeader(headerText)
    }

    @Composable
    fun SlideShowToolbar(
        knobState : KnobObserverBuilderState,
        params: ImageViewerScreen.ImageViewerScreenOpenParameters.SlideShow
    ) {

        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
        ) {
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = "➡ ",
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        params.onImageNavigateForward()
                    }
                )
            }
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = " ⬅",
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        params.onImageNavigateBackward()
                    }
                )
            }
            MenuItem(
                boxModifier = Modifier.weight(2F, true),
                label = "Show:",
                chipOrientation = ItemChipOrientation.NONE,
                isSelected = false,
                onClicked = {}
            )
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(2F, true),
                    label = "End",
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        params.onEndSlideshowRequested()
                    }
                )
            }

            if (params.wasStartedWithAutoAdvance) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier.weight(2F, true),
                        label = "Pause",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            params.onPauseAutoAdvanceRequested()
                        }
                    )
                }
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier.weight(2F, true),
                        label = "Resume",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            params.onResumeAutoAdvanceRequested()
                        }
                    )
                }
            }
        }
    }


    @Composable
    fun SingleToolbar(
        knobState : KnobObserverBuilderState,
        onExit : () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
        ) {
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = "Close",
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onExit()
                    }
                )
            }
        }

        //TODO zoom items, etc...
    }

}