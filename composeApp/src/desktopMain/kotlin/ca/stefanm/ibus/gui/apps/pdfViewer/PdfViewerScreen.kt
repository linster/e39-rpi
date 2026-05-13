package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfPageSelectorScreen.PageSelectorResult
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl.LoaderUtils
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
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
import dev.nucleusframework.pdfium.PageSize
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class PdfViewerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX)
    private val knobListenerServiceMainAux: KnobListenerService,

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

        val knobStateMain = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerServiceMain,
            logger,
            TAG
        )

        val rootScope = rememberCoroutineScope()

        val readerUiState = ReaderUiState.rememberReaderUiState(reader, rootScope)

        LaunchedEffect(readerUiState.entries) {
            logger.d(TAG, "Page selector result effect, result is $pageSelectorResult")
            if (pageSelectorResult != null && pageSelectorResult is PageSelectorResult.PageSelected) {
                logger.d(TAG, "Page Selector Result effect scrolling to ${pageSelectorResult.selectedPageNumber}")
                readerUiState.jumpToPage(pageSelectorResult.selectedPageNumber)
            }
        }

        val scrollMode = remember { mutableStateOf(ScrollMode.SELECT)}
        val horizontalScrollState = rememberScrollState()

        LaunchedEffect(scrollMode.value) {
            logger.d(TAG, "ScrollMode changing ${scrollMode.value}")
            when (scrollMode.value) {
                ScrollMode.SCROLL_LEFT_RIGHT,
                ScrollMode.SCROLL_UP_DOWN -> {
                    knobListenerServiceMain.disableListener()
                    knobListenerServiceMainAux.enableListener()
                }
                ScrollMode.SELECT -> {
                    knobListenerServiceMainAux.disableListener()
                    knobListenerServiceMain.enableListener()
                }
            }

            logger.d(TAG, "WAT knobListenerServiceMainAux")
            knobListenerServiceMainAux.knobTurnEvents(false)
                .onStart {
                    logger.d(TAG, "knobListenerServiceMainAux subscription started")
                }
                .onEach {
                    logger.d(TAG, "knobListenerServiceMainAux got event $it")
                }
                .onCompletion {
                    logger.d(TAG, "knobListenerServiceMainAux subscription ended")
                }
                .collect { event ->
                    when (event) {
                        InputEvent.NavKnobPressed -> {
                            // When this listener is enabled, we are only in a scroll mode,
                            // so a click must mean transition to click mode.
                            scrollMode.value = ScrollMode.SELECT
                        }

                        is InputEvent.NavKnobTurned -> {
                            val direction = event.direction.toSign()
                            when (scrollMode.value) {
                                ScrollMode.SCROLL_LEFT_RIGHT -> {
                                    val step = 5F * direction
                                    horizontalScrollState.scrollBy(step)
                                }

                                ScrollMode.SCROLL_UP_DOWN -> {
                                    val step = 10F * direction
                                    readerUiState.mainListState.scrollBy(step)
                                }
                                else -> {}
                            }

                        }

                        InputEvent.NextTrack -> {
                            //Scroll to end of scroll bar
                            when (scrollMode.value) {
                                ScrollMode.SCROLL_LEFT_RIGHT -> {
                                    horizontalScrollState.scrollTo(horizontalScrollState.maxValue)
                                }

                                ScrollMode.SCROLL_UP_DOWN -> {
                                    //TODO scroll to next page
                                }

                                else -> {}
                            }
                        }

                        InputEvent.PrevTrack -> {
                            //Scroll to beginning of scroll bar
                            when (scrollMode.value) {
                                ScrollMode.SCROLL_LEFT_RIGHT -> {
                                    horizontalScrollState.scrollTo(0)
                                }

                                ScrollMode.SCROLL_UP_DOWN -> {
                                    //TODO scroll to prev page
                                }

                                else -> {}
                            }

                        }

                        else -> {}
                    }
                }
            }
        //}

        val searchState = SearchState.rememberSearchState(
            reader,
            rootScope,
            modalMenuService,
            logger,
            navigationNodeTraverser,
            readerUiState
        )

        LaunchedEffect(readerUiState.currentPage) {
            searchState.setCurrentReaderPage(readerUiState.currentPage)
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            HeaderBar(
                reader, fileName, readerUiState.currentPage,
            )


            TopMenuBar(
                knobState = knobStateMain,
                scrollMode = scrollMode.value,
                zoomPercent = (reader.renderScale * 100).toInt(),
                requestSelectPage = { requestSelectPage() },
                requestZoomPercentSlider = {
                    val scale = 100
                    modalMenuService.showFloatSlider(
                        initialValue = reader.renderScale * scale,
                        validItems = (ReaderUiState.ZOOM_MIN * scale) ..(ReaderUiState.ZOOM_MAX * scale),
                        step = 5F,
                        hintText = "Zoom",
                        onCurrentValueChanged = {
                            reader.renderScale = (it / scale)
                        }
                    )
                },
                requestZoomFitWidth = { readerUiState.fitWidth() },
                requestZoomFitHeight = { readerUiState.fitHeight() },
                requestZoomFitPage = {
//                    readerUiState.fitPage()
                    reader.renderScale = 2F
                },
                requestTextSearch = {
                    searchState.openSearchUi()
                },
                requestUiModeScroll = { mode ->
                    scrollMode.value = mode
                }
            )

            BoxWithConstraints(
                Modifier.fillMaxHeight()
            ) {

                LaunchedEffect(Unit) {
                    readerUiState.updateViewport(IntSize(
                        constraints.maxWidth,
                        constraints.maxHeight
                    ))
                }
                val density = LocalDensity.current
                val pageWidthPx = with(density) {
                    ((constraints.maxWidth - 16.dp.toPx()) * reader.renderScale).coerceAtLeast(80.dp.toPx())

                }
                val pages = (0 until reader.pageCount).toList()


                LazyColumn(
                    modifier = Modifier
                        .width(constraints.maxWidth.dp)
                        .horizontalScroll(horizontalScrollState)
                        .padding(bottom = 16.dp)
                        .onSizeChanged { readerUiState.updateViewport(it)},
                    state = readerUiState.mainListState,
                    //contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(pages.size, key = { it }) { pageIndex ->
                        //TODO draw the highlights if we got any from the search results.

                        Box(
                            Modifier
                                .padding(horizontal = 0.dp)
                                .width(pageWidthPx.dp)
                        ) {
                            PdfPage(
                                state = reader,
                                pageIndex = pageIndex,
                                modifier = Modifier
                                    //.matchParentSize()
                            )
                            val items = searchState.highlights.value.filter { it.page == pageIndex }
                            if (items.isNotEmpty()) {
                                var pageSize by remember { mutableStateOf<PageSize?>(null) }
                                LaunchedEffect(pageIndex) { pageSize = reader.pageSize(pageIndex) }
                                val size = pageSize ?: return@Box
                                Canvas(Modifier.matchParentSize()) {
                                    val sx = this.size.width  / size.widthPoints
                                    val sy = this.size.height / size.heightPoints
                                    items.forEach { item ->
                                        val r = item.rect
                                        drawRect(
                                            color = Color(0x665AB1FF),
                                            topLeft = Offset(r.left * sx, r.top * sy),
                                            size = Size((r.right - r.left) * sx, (r.bottom - r.top) * sy),
                                        )
                                    }
                                }
                            }
                        }


                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                        .align(Alignment.TopEnd)
                    ,
                    adapter = ScrollbarAdapter(readerUiState.mainListState),
                    style = ScrollbarStyle(
                        minimalHeight = 16.dp,
                        thickness = 16.dp,
                        shape = RoundedCornerShape(8.dp),
                        hoverDurationMillis = 300,
                        unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                        hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                    )
                )

                HorizontalScrollbar(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .fillMaxWidth()
                        .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                        .align(Alignment.BottomStart)
                    ,
                    adapter = ScrollbarAdapter(horizontalScrollState),
                    style = ScrollbarStyle(
                        minimalHeight = 16.dp,
                        thickness = 16.dp,
                        shape = RoundedCornerShape(8.dp),
                        hoverDurationMillis = 300,
                        unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                        hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                    )
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
                            modifier = Modifier
                                .clickable { requestUiModeScroll(ScrollMode.SCROLL_UP_DOWN)}
                                .padding(start = 10.dp.halveIfNotPixelDoubled(), end = 10.dp.halveIfNotPixelDoubled())
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
                            modifier = Modifier
                                .clickable { requestUiModeScroll(ScrollMode.SCROLL_LEFT_RIGHT)}
                                .padding(start = 10.dp.halveIfNotPixelDoubled(), end = 10.dp.halveIfNotPixelDoubled()))
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
                            modifier = Modifier
                                .clickable { requestUiModeScroll(ScrollMode.SELECT) }
                                .padding(start = 10.dp, end = 10.dp)
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
                                autoCloseOnSelect = false,
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.W,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "\uD83D\uDCDC \uD83E\uDC59",
                                            onClicked = {
                                                //Fix weird race condition by pushing a coroutine
                                                //to run in a jiffy
                                                GlobalScope.launch {
                                                    modalMenuService.closeModalMenu()
                                                    requestUiModeScroll(ScrollMode.SCROLL_UP_DOWN)
                                                }
                                            }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "\uD83D\uDCDC \uD83E\uDC58",
                                            onClicked = {
                                                GlobalScope.launch {
                                                    modalMenuService.closeModalMenu()
                                                    requestUiModeScroll(ScrollMode.SCROLL_LEFT_RIGHT)
                                                }
                                            }
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

        }

    }


}