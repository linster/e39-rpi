package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfPageSelectorScreen.PageSelectorResult
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl.LoaderUtils
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import dev.nucleusframework.pdfium.PdfReader
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.max

class PdfViewerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val loaderUtils: LoaderUtils
) : NavigationNode<Nothing> {

    companion object {

        const val TAG = "PdfViewerScreen"

        fun openWithFilename(navigationNodeTraverser: NavigationNodeTraverser, filename : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PdfViewerScreen::class.java,
                OpenParameters.FileName(filename)
            )
        }

        fun openWithByteArray(navigationNodeTraverser: NavigationNodeTraverser, fileBytes : ByteArray) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PdfViewerScreen::class.java,
                OpenParameters.Bytes(fileBytes)
            )
        }
    }

    sealed interface OpenParameters {
        data class FileName(val filename : File) : OpenParameters
        data class Bytes(val bytes : ByteArray) : OpenParameters {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Bytes

                if (!bytes.contentEquals(other.bytes)) return false

                return true
            }

            override fun hashCode(): Int {
                return bytes.contentHashCode()
            }
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PdfViewerScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = content@ { params ->



        if (params == null) {
            logger.d(TAG, "params were null")
            InstructionalPage()
            return@content
        }


        val fileName =
            if (params.resultFrom == FilePickerScreen::class.java && params.result is FilePickerScreen.Companion.FilePickerResult) {
                when (val incoming = params.result) {
                    is FilePickerScreen.Companion.FilePickerResult.FileChosen -> incoming.file
                    FilePickerScreen.Companion.FilePickerResult.NoFileChosen -> null
                    else -> null
                }
            } else if (params.resultFrom == PdfPageSelectorScreen::class.java && params.result is PageSelectorResult) {
                params.result.fileName
            } else {
                if (params.requestParameters is OpenParameters.FileName) {
                    params.requestParameters.filename
                } else {
                    null
                }
            }

        val bytesFromParameters = if (params.requestParameters is OpenParameters.Bytes) {
            params.requestParameters.bytes
        } else if (params.resultFrom == PdfPageSelectorScreen::class.java && params.result is PageSelectorResult) {
            params.result.fileBytes
        } else {
            null
        }

        if (fileName == null && bytesFromParameters == null) {
            InstructionalPage()
            return@content
        }


        val fileBytes = loaderUtils.loadFileBytes(fileName, bytesFromParameters)

        val reader = rememberPdfReaderState()
        fileBytes.value?.let {
            loaderUtils.loadPdf(reader, it)
        }

        PdfViewer(
            reader = reader,
            fileName = fileName,
            pageSelectorResult = if (
                params.resultFrom == PdfPageSelectorScreen::class.java &&
                params.result is PageSelectorResult
                ) {
                params.result
            } else {
                null
            },
            requestSelectPage = {
                PdfPageSelectorScreen.openWithByteArray(
                    navigationNodeTraverser,
                    fileName,
                    fileBytes.value!!
                )
            })

//                LaunchedEffect(fileBytes) {
//                    fileBytes.value?.let {
//                        if (params.requestParameters is OpenParameters) {
//                            PdfPageSelectorScreen.openWithByteArray(
//                                navigationNodeTraverser,
//                                fileName,
//                                it,
//                                replayParams = params.requestParameters
//                            )
//                        }
//                    }
//                }


    }

    @Composable
    fun InstructionalPage() {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
        ){
            BmwSingleLineHeader("PDF Viewer -- Instructions")

        }
    }

    @Composable
    fun PromptToSelectFile() {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
        ) {
            BmwSingleLineHeader("PDF Viewer -- Instructions")

        }
    }



    @Composable
    fun PdfViewer(
        reader : PdfReaderState,
        fileName : File?,
        pageSelectorResult: PageSelectorResult? = null,
        requestSelectPage: () -> Unit
    ) {
        val knobState = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerService,
            logger,
            TAG
        )

        val readerUiState = ReaderUiState.rememberReaderUiStae(reader)

        val readerRenderScale : Flow<Float> = snapshotFlow { reader.renderScale }

        Column(
            Modifier
                .fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            HeaderBar(
                reader, fileName, readerUiState.currentPage,
            )


            TopMenuBar(
                knobState = knobState,
                scrollMode = ScrollMode.SELECT, //todo borrow the ideas in https://github.com/kdroidFilter/ComposePdf/blob/master/example/src/commonMain/kotlin/dev/nucleusframework/pdf/reader/ReaderScreenState.kt
                zoomPercent = (reader.renderScale * 100).toInt(),
                requestSelectPage = { requestSelectPage() },
                requestZoomPercentSlider = {
                    modalMenuService.showFloatSlider(
                        currentValue = readerRenderScale,
                        initialValue = reader.renderScale,
                        validItems = ReaderUiState.ZOOM_MIN ..ReaderUiState.ZOOM_MAX,
                        step = 5F,
                        hintText = "Zoom",
                        onCurrentValueChanged = {
                            reader.renderScale = it
                        }
                    )
                },
                requestZoomFitWidth = { readerUiState.fitWidth() },
                requestZoomFitHeight = { readerUiState.fitHeight() },
                requestZoomFitPage = { readerUiState.fitPage() },
                requestTextSearch = {},
                requestUiModeScroll = { mode ->

                }
            )

            BoxWithConstraints(
                Modifier.fillMaxHeight()
            ) {
                PdfReader(
                    state = reader,
                    modifier = Modifier,

                )
            }


        }
    }

    enum class ScrollMode {
        SCROLL_LEFT_RIGHT,
        SCROLL_UP_DOWN,
        SELECT
    }

    @Composable
    fun HeaderBar(
        reader: PdfReaderState,
        fileName : File?,
        currentPage : Int?,
    ) {
        CenterGradientWithEdgeHighlight {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PDF Viewer : ${fileName?.absolutePath}".uppercase(),
                    fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
                )

                if (currentPage != null) {
                    Text(
                        text = "Page $currentPage / ${reader.pageCount}".uppercase(),
                        fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
                    )
                }
            }
        }

    }

    @Composable
    fun TopMenuBar(
        knobState : KnobObserverBuilderState,

        scrollMode: ScrollMode,
        zoomPercent : Int,

        requestUiModeScroll : (ScrollMode) -> Unit,

        requestZoomPercentSlider : () -> Unit,
        requestZoomFitWidth : () -> Unit,
        requestZoomFitHeight : () -> Unit,
        requestZoomFitPage : () -> Unit,

        requestSelectPage : () -> Unit,

        requestTextSearch : () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(Modifier.width(IntrinsicSize.Min)) {

                @Composable
                fun Dp.halveIfNotPixelDoubled() : Dp = if (!ThemeWrapper.ThemeHandle.current.isPixelDoubled) (this.value / 2F).dp else this

                val measurements = ThemeWrapper.ThemeHandle.current.smallItem
                val colors = ThemeWrapper.ThemeHandle.current.colors


                Row(
                    modifier = Modifier
                        .padding(
                            top = (measurements.chipWidth * 0.5).dp.halveIfNotPixelDoubled(),
                            start = (measurements.chipWidth * 0.5).dp.halveIfNotPixelDoubled(),
                        )
                        .width(IntrinsicSize.Max),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.background(
                            color = if (scrollMode == ScrollMode.SCROLL_UP_DOWN) { colors.selectedColor } else { colors.textMenuColorAccent },
                            shape = RoundedCornerShape(50)
                        )
                    ) {
                        //Scroll up-down
                        Text(
                            "\uD83D\uDCDC \uD83E\uDC59",
                            fontSize = measurements.fontSize,
                            modifier = Modifier.padding(start = 10.dp.halveIfNotPixelDoubled(), end = 10.dp.halveIfNotPixelDoubled())
                        )
                    }

                    Box(
                        Modifier.background(
                            color = if (scrollMode == ScrollMode.SCROLL_LEFT_RIGHT) { colors.selectedColor } else { colors.textMenuColorAccent },
                            shape = RoundedCornerShape(50)
                            )
                    ) {
                        //Scroll left-right
                        Text("\uD83D\uDCDC \uD83E\uDC58",
                            fontSize = measurements.fontSize,
                            modifier = Modifier.padding(start = 10.dp.halveIfNotPixelDoubled(), end = 10.dp.halveIfNotPixelDoubled()))
                    }

                    Box(
                        Modifier.background(
                            color = if (scrollMode == ScrollMode.SELECT) { colors.selectedColor } else { colors.textMenuColorAccent },
                            shape = RoundedCornerShape(50)
                        )
                    ) {
                        //Mouse
                        Text("\uD83D\uDDB0",
                            fontSize = measurements.fontSize,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )
                    }
                }
            }

            Row(
                Modifier.wrapContentWidth(Alignment.End)
            ) {

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDCDC",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            modalMenuService.showModalMenu(
                                dimensions = ModalMenuService.PixelDoubledModalMenuDimensions(
                                    menuTopLeft = IntOffset(320, 50),
                                    menuWidth = 210
                                ).toNormalModalMenuDimensions(),
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.W,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "\uD83D\uDCDC \uD83E\uDC59",
                                            onClicked = { requestUiModeScroll(ScrollMode.SCROLL_UP_DOWN) }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "\uD83D\uDCDC \uD83E\uDC58",
                                            onClicked = { requestUiModeScroll(ScrollMode.SCROLL_LEFT_RIGHT) }
                                        ),
                                    )
                                )
                            )

                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "⏻",
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
                                            title = "Back to Document",
                                            onClicked = { modalMenuService.closeModalMenu() }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "Close PDF Reader",
                                            onClicked = {
                                                navigationNodeTraverser.navigateToRoot()
                                            }
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
                        label = "\uD83E\uDE83\uD83D\uDCD1",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestSelectPage()
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDD75", //Sleuth
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestTextSearch()
                        }
                    )
                }


                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDD0D$zoomPercent%",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestZoomPercentSlider()
                        }
                    )
                }

                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDD0D\uD83E\uDC58",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestZoomFitWidth()
                        }
                    )
                }


                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDD0D\uD83E\uDC59",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestZoomFitHeight()
                        }
                    )
                }


                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                    MenuItem(
                        boxModifier = Modifier,
                        label = "\uD83D\uDD0D\uD83D\uDDCE",
                        isSmallSize = true,
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            requestZoomFitPage()
                        }
                    )
                }
            }

            val measurements = ThemeWrapper.ThemeHandle.current.bigItem

//            Row(Modifier.weight(2F)) {
//                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
//                    MenuItem(
//                        boxModifier = Modifier.weight(1f, fill = true),
//                        label = "Zoom (${"%.2f".format(rowHeight)}) ...",
//                        isSmallSize = true,
//                        chipOrientation = ItemChipOrientation.N,
//                        isSelected = allocatedIndex == currentIndex,
//                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
//                            modalMenuService.showFloatSlider(
//                                currentValue = flowOf(rowHeight),
//                                initialValue = rowHeight,
//                                validItems = 0.3F .. 1.1F,
//                                step = 0.1F,
//                                onCurrentValueChanged = {
//                                    onChangeRowHeightFraction(it)
//                                },
//                                hintText = "Zoom"
//                            )
//                        }
//                    )
//                }
//                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
//                    MenuItem(
//                        boxModifier = Modifier.weight(1f, fill = true),
//                        label = "Aspect (${"%.2f".format(aspectRatio)}) ...",
//                        isSmallSize = true,
//                        chipOrientation = ItemChipOrientation.N,
//                        isSelected = allocatedIndex == currentIndex,
//                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
//                            modalMenuService.showFloatSlider(
//                                hintText = "Aspect",
//                                initialValue = aspectRatio,
//                                validItems = 0.5F .. 2.0F,
//                                step = 0.1F,
//                                onCurrentValueChanged = {
//                                    onChangeDesiredItemAspectRatio(it)
//                                },
//                                currentValue = flowOf(aspectRatio)
//                            )
//                        }
//                    )
//                }
//
//            }

        }

    }

    @Stable
    class ReaderUiState(
        private val reader : PdfReaderState,
        private val scope : CoroutineScope,
        val mainListState : LazyListState
    ) {

        companion object {
            internal const val ZOOM_MIN = 0.1F
            internal const val ZOOM_MAX = 4F

            @Composable
            fun rememberReaderUiStae(
                reader: PdfReaderState
            ) : ReaderUiState {
                val mainListState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                return remember(reader, scope) {
                    ReaderUiState(reader, scope, mainListState)
                }
            }
        }

        /**
         * Size in device pixels of the reader's viewport (the region where pages scroll).
         * The reader surface reports its own dimensions here so the state holder can compute
         * "Fit Width" / "Fit Height" / "Fit Page" scales without peeking into composables.
         */
        var contentViewportPx: IntSize by mutableStateOf(IntSize.Zero)
            private set

        fun updateViewport(size: IntSize) {
            if (size != contentViewportPx) contentViewportPx = size
        }

        val entries : List<Int> by derivedStateOf {
            val count = reader.pageCount
            List(count) { it }
        }


        /** Top-most fully visible page index, clamped to the current document. */
        val currentPage: Int by derivedStateOf {
            val entries = entries
            val itemIdx = mainListState.firstVisibleItemIndex
            val pageIdx = entries.getOrNull(itemIdx) ?: 0
            pageIdx.coerceIn(0, max(0, reader.pageCount - 1))
        }

        fun jumpToPage(index: Int) {
            scope.launch {
                val entries = entries
                val row = entries.indexOfFirst { it == index }
                    .coerceAtLeast(0)
                // scrollToItem teleports to the target; animateScrollToItem would walk through
                // every intermediate item, rendering each page on the way (slow for large jumps).
                mainListState.scrollToItem(row)
            }
        }

        // ---- Fit actions ----
        //
        // Maths: at scale = 1.0, a single page fills the viewport width. Page height = width / aspect.
        //  - Fit Width   → scale = 1.0 (trivial; page width ≡ viewport width).
        //  - Fit Height  → scale so that page height == viewport height:
        //                  pageHeight = (viewportWidth × scale) / aspect = viewportHeight
        //                  ⇒ scale = viewportHeight × aspect / viewportWidth
        //  - Fit Page    → page fits entirely: min(Fit-Width, Fit-Height) — i.e. min(1.0, heightScale).
        //
        // In Double mode, each rendered page is half-width, so "Fit Height" scales up by 2 to keep
        // the same on-screen page height — handled here rather than in the rendering code.

        fun fitWidth() {
            reader.renderScale = 1f.coerceIn(ZOOM_MIN, ZOOM_MAX)
        }

        fun fitHeight() {
            scope.launch {
                val scale = computeFitHeightScale() ?: return@launch
                reader.renderScale = scale.coerceIn(ZOOM_MIN, ZOOM_MAX)
            }
        }

        fun fitPage() {
            scope.launch {
                val heightScale = computeFitHeightScale() ?: return@launch
                val scale = minOf(1f, heightScale).coerceIn(ZOOM_MIN, ZOOM_MAX)
                reader.renderScale = scale
            }
        }

        private suspend fun computeFitHeightScale(): Float? {
            val vw = contentViewportPx.width.toFloat()
            val vh = contentViewportPx.height.toFloat()
            if (vw <= 0f || vh <= 0f || reader.pageCount == 0) return null
            val aspect = reader.pageSize(0)?.aspectRatio?.takeIf { it > 0f } ?: return null
            return (vh * aspect) / vw
        }
    }


}