package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.FileManagerViewState
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryNavigatorReader
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryStateRequestor
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.INavigationButtonVisibleProvider
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.INewButtonVisibleProvider
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

object ToolbarViews {

    @Composable
    fun HeaderBar(
        viewState : FileManagerViewState
    ) {

        //TODO don't forget we can use the same window to select a destination for
        //TODO "Copy to.." and "Move to.." operations, and that should be refelected
        //TODO in the title bar.
        BmwSingleLineHeader()
    }

    @Composable
    fun Toolbar(
        knobState : KnobObserverBuilderState,
        modalMenuService: ModalMenuService,
        viewState: FileManagerViewState,
        navigationButtonVisibleProvider: INavigationButtonVisibleProvider,
        newButtonVisibleProvider : INewButtonVisibleProvider,
        directoryNavigatorReader : IDirectoryNavigatorReader,
        directoryStateRequestor : IDirectoryStateRequestor,
        onNewFileClicked : () -> Unit,
        onNewFolderClicked : () -> Unit,
        exitButtonText: String,
        onExitButtonClicked: () -> Unit
    ) {

        ToolbarView(
            knobState = knobState,
            modalMenuService = modalMenuService,
            isFolderBackVisible = navigationButtonVisibleProvider.backVisible(),
            isFolderBackSelectable = directoryNavigatorReader.canGoBack(),
            isFolderForwardVisible = navigationButtonVisibleProvider.forwardVisible(),
            isFolderForwardSelectable = directoryNavigatorReader.canGoForward(),
            isFolderUpVisible = navigationButtonVisibleProvider.upVisible(),
            isFolderUpSelectable = directoryNavigatorReader.canGoUp(),
            onNewViewMode = { viewState.itemStyle = it },
            previewsEnabled = viewState.showPreview,
            onNewPreviewsEnabled = { viewState.showPreview = it },
            previewZoomDp = viewState.previewItemHeightPx,
            onNewPreviewZoom = { viewState.previewItemHeightPx = it},
            onFolderBackClicked = { directoryStateRequestor.requestNavigateBack() },
            onFolderForwardClicked = { directoryStateRequestor.requestNavigateForward() },
            onFolderUpClicked = { directoryStateRequestor.requestNavigateUp() },
            isNewFileVisible = newButtonVisibleProvider.isNewFileVisible(),
            isNewFolderVisible = newButtonVisibleProvider.isNewFolderVisible(),
            onNewFileClicked = { onNewFileClicked() },
            onNewFolderClicked = { onNewFolderClicked() },
            exitButtonText = exitButtonText,
            onExitButtonClicked = onExitButtonClicked
        )
    }

    @Composable
    private fun ToolbarView(
        knobState : KnobObserverBuilderState,
        modalMenuService: ModalMenuService,

        /** For some file selection modes, don't want the user
         *  to navigate out of the current folder.
         *  Allow buttons to be visible but not selectable so that
         *  Up doesn't work out of the jail, and so back and forward
         *  don't work when no backstack.
         */
        isFolderBackVisible : Boolean = true,
        isFolderBackSelectable : Boolean = true,
        isFolderForwardVisible : Boolean = true,
        isFolderForwardSelectable : Boolean = true,
        isFolderUpVisible : Boolean = true,
        isFolderUpSelectable : Boolean = true,

        onNewViewMode : (FileManagerViewState.ItemStyle) -> Unit = {},

        previewsEnabled : Boolean = true,
        onNewPreviewsEnabled : (Boolean) -> Unit = {},

        previewZoomDp : Int = 500,
        onNewPreviewZoom : (Int) -> Unit = {},

        onFolderBackClicked : () -> Unit = {},
        onFolderForwardClicked : () -> Unit = {},
        onFolderUpClicked : () -> Unit = {},


        isNewFileVisible : Boolean = true,
        isNewFolderVisible : Boolean = true,
        onNewFileClicked : () -> Unit = {},
        onNewFolderClicked : () -> Unit = {},

        exitButtonText : String = "Close",
        onExitButtonClicked : () -> Unit = {},
    ) {

        @Composable
        fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this

        val measurements = ThemeWrapper.ThemeHandle.current.smallItem
        val colors = ThemeWrapper.ThemeHandle.current.colors


        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .padding(
                    top = (measurements.chipWidth * 0.5).dp.halveIfNotPixelDoubled(),
                    start = (measurements.chipWidth * 0.5).dp.halveIfNotPixelDoubled(),
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            if (isFolderBackVisible) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "⭠ ",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = isFolderBackSelectable && (currentIndex == allocatedIndex),
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            if (isFolderBackSelectable) {
                                onFolderBackClicked()
                            }
                        }
                    )
                }
            }
            if (isFolderForwardVisible) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "⭢ ",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = isFolderForwardSelectable && (currentIndex == allocatedIndex),
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            if (isFolderForwardVisible) {
                                onFolderForwardClicked()
                            }
                        }
                    )
                }
            }
            if (isFolderUpVisible) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "⭡ ",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = isFolderUpSelectable && (currentIndex == allocatedIndex),
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            if (isFolderUpSelectable) {
                                onFolderUpClicked()
                            }
                        }
                    )
                }
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier,
                    label = "View...",
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        modalMenuService.showModalMenu(
                            dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                                menuTopLeft = IntOffset(50, 50),
                                menuWidth = 550
                            ).toNormalModalMenuDimensions(),
                            menuData = ModalMenu(
                                chipOrientation = ItemChipOrientation.W,
                                items = listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = FileManagerViewState.ItemStyle.List.label,
                                        onClicked = { onNewViewMode(FileManagerViewState.ItemStyle.List)}
                                    ),
                                    ModalMenu.ModalMenuItem(
                                        title = FileManagerViewState.ItemStyle.Grid.label,
                                        onClicked = { onNewViewMode(FileManagerViewState.ItemStyle.Grid)}
                                    )
                                )
                            )
                        )
                    }
                )
            }
            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier,
                    label = (if (previewsEnabled) "\uD83D\uDDF9" else "❍") + " Preview",
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onNewPreviewsEnabled(!previewsEnabled)
                    }
                )
            }

            if (previewsEnabled) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "Zoom...",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalMenuService.showIntSlider(
                                initialValue = previewZoomDp,
                                validItems = 10 .. 100 step 5,
                                onCurrentValueChanged = { onNewPreviewZoom(it)},
                                hintText = "Zoom"
                            )
                        }
                    )
                }
            }

            val newVisibles = listOf(isNewFileVisible, isNewFolderVisible)
            val multipleNewVisible = newVisibles.filter { it }.size > 1

            if (multipleNewVisible) {
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "New...",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalMenuService.showModalMenu(
                                dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                                    menuTopLeft = IntOffset(700, 50),
                                    menuWidth = 350
                                ).toNormalModalMenuDimensions(),
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.W,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "Cancel",
                                            onClicked = { modalMenuService.closeModalMenu() }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "New \uD83D\uDCC4...",
                                            onClicked = { onNewFileClicked()}
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "New \uD83D\uDCC1...",
                                            onClicked = { onNewFolderClicked() }
                                        )
                                    )
                                )
                            )

                        }
                    )
                }

            } else {
                if (isNewFileVisible) {
                    KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier,
                            label = "New \uD83D\uDCC4...",
                            isSmallSize = true,
                            chipOrientation = ItemChipOrientation.S,
                            isSelected = currentIndex == allocatedIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                onNewFileClicked()
                            }
                        )
                    }

                }

                if (isNewFolderVisible) {
                    KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier,
                            label = "New \uD83D\uDCC1...",
                            isSmallSize = true,
                            chipOrientation = ItemChipOrientation.S,
                            isSelected = currentIndex == allocatedIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                onNewFolderClicked()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1F, fill = true))

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier,
                    label = exitButtonText, //Could also be "Cancel" for CopyTo operations
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onExitButtonClicked()
                    }
                )
            }

        }
    }
}