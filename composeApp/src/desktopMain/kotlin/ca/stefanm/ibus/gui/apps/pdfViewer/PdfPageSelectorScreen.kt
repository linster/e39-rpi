package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl.LoaderUtils
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.editor.CalendarEventEditScreen
import ca.stefanm.ibus.gui.pim.calendar.views.editor.TodoItemEditorScreen
import ca.stefanm.ibus.lib.logging.Logger
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.PdfThumbnail
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class PdfPageSelectorScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,


    @Named(ApplicationModule.KNOB_LISTENER_MODAL)
    private val knobListenerServiceModal: KnobListenerService,

    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val loaderUtils: LoaderUtils
) : NavigationNode<PdfPageSelectorScreen.PageSelectorResult> {

    companion object {
        const val TAG = "PdfPageSelectorScreen"

        //The caller should have already chosen the file, made sure it could be opened, and read it.
        fun openWithByteArray(navigationNodeTraverser: NavigationNodeTraverser, name : File?, bytes : ByteArray) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PdfPageSelectorScreen::class.java,
                ScreenParameters(name, bytes)
            )
        }
    }

    data class ScreenParameters(
        val fileName : File?,
        val fileBytes : ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ScreenParameters

            if (!fileBytes.contentEquals(other.fileBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return fileBytes.contentHashCode()
        }

        override fun toString(): String {
            return "ScreenParameters(fileName: ${fileName.toString()}, fileBytes (len) : ${fileBytes.size})"
        }
    }

    sealed interface PageSelectorResult {
        object NoSelection : PageSelectorResult
        data class PageSelected(
            val selectedPageNumber : Int,
            val lastPageNumber : Int,
        )
    }

    override val thisClass
        get() = PdfPageSelectorScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->
        //Load a grid view of all the pages.
        //Title bar with the full file path
        //North Menu with zoom controls? Maybe have a slide out menu do zoom too, and have a bigger preview.

        //https://github.com/kdroidFilter/ComposePdf

        if (params == null || params?.requestParameters !is ScreenParameters) {
            return@content
        }

        val fileBytes = loaderUtils.loadFileBytes(null, params.requestParameters.fileBytes)

        val reader = rememberPdfReaderState()
        fileBytes.value?.let {
            loaderUtils.loadPdf(reader, it)
        }
        Selector(
            params.requestParameters.fileName,
            reader
        )
    }

    @Composable
    fun Selector(
        fileName : File?,
        reader : PdfReaderState,
        ) {

        val knobState = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerService,
            logger,
            TAG
        )

        Column(
            Modifier.fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            BmwSingleLineHeader("Page Selector : ${fileName?.absolutePath}")

            val rowHeightFraction = remember { mutableStateOf(0.5F) }
            val desiredItemAspectRatio = remember { mutableStateOf(1.0F)}

            ZoomRow(
                knobState = knobState,
                rowHeight = rowHeightFraction.value,
                aspectRatio = desiredItemAspectRatio.value,
                onChangeRowHeightFraction = { rowHeightFraction.value = it },
                onChangeDesiredItemAspectRatio = { desiredItemAspectRatio.value = it }
            )

            val items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> =
                (0 .. reader.pageCount).map { pageNumber ->
                    { allocatedIndex, currentIndex ->
                        val chipWidth = ThemeWrapper.ThemeHandle.current.smallItem.chipWidth
                        val selectedColor = ThemeWrapper.ThemeHandle.current.colors.selectedColor

                        val onClick = CallWhen(currentIndexIs = allocatedIndex) {
                            openSidebarForPage(
                                reader = reader,
                                pageNumber = pageNumber,
                                lastPageNumber = reader.pageCount
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding((chipWidth * 2).dp)
                                .clickable { onClick() }
                                .wrapContentHeight(unbounded = true)
                                .then(if (currentIndex == allocatedIndex) {
                                    Modifier.border(
                                        chipWidth.dp, selectedColor
                                    )
                                } else {
                                    Modifier
                                })
                        ) {
                            PdfPage(
                                state = reader,
                                pageIndex = pageNumber,
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                            )
                        }
                    }
                }

            SmoothScroll.GridScroll(
                modifier = Modifier,
                knobState = knobState,
                tag = TAG,
                logger = logger,
                items = items,
                prependGoBackEntry = false,
                navigationNodeTraverser = navigationNodeTraverser,
                rowHeightFraction = rowHeightFraction.value,
                desiredItemAspectRatio = desiredItemAspectRatio.value
            )
        }
    }

    @Composable
    fun ZoomRow(
        knobState : KnobObserverBuilderState,
        rowHeight : Float,
        aspectRatio : Float,
        onChangeRowHeightFraction : (new : Float) -> Unit,
        onChangeDesiredItemAspectRatio : (new : Float) -> Unit
    ) {
        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = "Close Screen",
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        navigationNodeTraverser.setResultAndGoBack(
                            thisClass,
                            PageSelectorResult.NoSelection
                        )
                    }
                )
            }
            val measurements = ThemeWrapper.ThemeHandle.current.bigItem

            Row(Modifier.weight(2F)) {
                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = "Zoom (${"%.2f".format(rowHeight)}) ...",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = allocatedIndex == currentIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalMenuService.showFloatSlider(
                                currentValue = flowOf(rowHeight),
                                initialValue = rowHeight,
                                validItems = 0.3F .. 1.1F,
                                step = 0.1F,
                                onCurrentValueChanged = {
                                    onChangeRowHeightFraction(it)
                                },
                                hintText = "Zoom"
                            )
                        }
                    )
                }
                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = "Aspect (${"%.2f".format(aspectRatio)}) ...",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = allocatedIndex == currentIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalMenuService.showFloatSlider(
                                hintText = "Aspect",
                                initialValue = aspectRatio,
                                validItems = 0.5F .. 2.0F,
                                step = 0.1F,
                                onCurrentValueChanged = {
                                    onChangeDesiredItemAspectRatio(it)
                                },
                                currentValue = flowOf(aspectRatio)
                            )
                        }
                    )
                }

            }

        }

    }


    fun openSidebarForPage(
        reader: PdfReaderState,
        pageNumber : Int,
        lastPageNumber: Int
    ) {
        modalMenuService.showSidePaneOverlay(true) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .border(width = 4.dp.halveIfNotPixelDoubled(), color = ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                    .shadow(4.dp.halveIfNotPixelDoubled(), RectangleShape),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                BmwSingleLineHeader("Page $pageNumber", Modifier.weight(1F))

                PdfPage(
                    state = reader,
                    pageIndex = pageNumber,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.weight(2F, true).align(Alignment.CenterHorizontally)
                )

                Box(Modifier.weight(1F), contentAlignment = Alignment.BottomStart) {
                    HalfScreenMenu.OneColumn(
                        alignment = Alignment.End,
                        fullWidth = true,
                        items = listOf(
                            TextMenuItem("Select Page", onClicked = {
                                modalMenuService.closeSidePaneOverlay(true)
                                navigationNodeTraverser.setResultAndGoBack(
                                    thisClass,
                                    PageSelectorResult.PageSelected(
                                        selectedPageNumber = pageNumber,
                                        lastPageNumber = lastPageNumber
                                    )
                                )
                            }),
                            TextMenuItem("Close Menu", onClicked = {
                                modalMenuService.closeSidePaneOverlay(true)
                            })
                        )

                    )
                }
            }
        }
    }
}