package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfPageSelectorScreen.PageSelectorResult
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.KeyboardViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import dev.nucleusframework.pdfium.PageSize
import dev.nucleusframework.pdfium.PageTextLayout
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Stable
internal class SearchState(
    private val reader : PdfReaderState,
    private val scope : CoroutineScope,
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val readerUiState: ReaderUiState
) {
    companion object {
        @Composable
        fun rememberSearchState(
            reader: PdfReaderState,
            rootScope : CoroutineScope,
            modalMenuService: ModalMenuService,
            logger: Logger,
            navigationNodeTraverser: NavigationNodeTraverser,
            readerUiState: ReaderUiState
        ): SearchState {
            return remember(reader, rootScope, modalMenuService, logger, readerUiState) {
                SearchState(reader, rootScope, modalMenuService, logger, navigationNodeTraverser, readerUiState)
            }
        }
    }


    //UI for this should have a few options.
    // Scroll to prev, next search Hit
    // but maybe also summarize them on the modal menu sidebar for where they all are?
    // Yes. Let's do a sidebar list where each page with a result has a preview, and you can smooth-scroll
    // that sidebar
    ///
    ///  { Side Bar}
    ///    [page 1]
    ///   | result 1
    ///   | result 2
    ///    [page n]
    //    | result 3
    ///   / Go Back

    data class SearchHit(
        val page: Int,
        val rect: Rect,
        val text: String
    )

    private fun doSearch(
        query: String,
        ignoreCase : Boolean = true
    ): List<SearchHit> {
        //if (query.length < 2) return emptyList()
        return buildList {
            //TODO let the UI specify which pages to search.
            //TODO the ibus.pdf seems to break search for anything past page 1?
            for (page in 0 until reader.pageCount) {
                val layout = layouts[page]
                if (layout == null) {
                    logger.d(PdfViewerScreen.TAG, "No Layout for page $page for query $query")
                    continue
                }
                for (i in 0 until layout.rectCount) {
                    val run = layout.text(i)
                    if (run.contains(query, ignoreCase)) {
                        val pageH = layout.pageSize.heightPoints
                        val rect = Rect(
                            left = layout.left(i),
                            top = pageH - layout.top(i),
                            right = layout.right(i),
                            bottom = pageH - layout.bottom(i)
                        )
                        add(SearchHit(page = page, rect = rect, text = run))
                    }
                }
            }
        }
    }

    sealed interface SearchSession {
        object EmptySession : SearchSession
        open class TextEntered(open val query : String) : SearchSession
        data class SearchFinished(
            override val query : String,
            val results : List<SearchHit>
        ) : SearchSession, TextEntered(query = query)
    }

    //This might have to be a stateFlow that is collected...
    val sessionState : MutableStateFlow<SearchSession> =
        MutableStateFlow(SearchSession.EmptySession)

    fun openSearchUi() {

        scope.launch {

            modalMenuService.showModalWaitDialog(
                throbber = true,
                headerText = "Fetching Page Text Layouts",
                isCancellable = true,
                autoCloseTimeout = Duration.INFINITE,
                onCancel = { modalMenuService.closeModalMenu() ; navigationNodeTraverser.navigateToRoot() }
            )
            // Fetch the page text layouts ahead of time before opening the toolbar.
            fetchPageTextLayouts()

            modalMenuService.closeModalMenu()

            openSessionToolbar()

        }

    }

    private lateinit var layouts : Map<Int, PageTextLayout>

    private suspend fun fetchPageTextLayouts() {
        val layouts = buildMap {
            for (page in 0 until reader.pageCount) {
                val layout = reader.pageTextLayout(page)
                if (layout == null) {
                    logger.d(PdfViewerScreen.TAG, "No Layout for page $page")
                    continue
                }
                put(page, layout)
            }
        }
        this.layouts = layouts
    }

    //TODO selecting a result needs to do something.

    private fun openSessionToolbar() {
        val tag = "SearchToolbar"
        modalMenuService.showArbitraryToolbar(tag) { knobListenerServiceModal, logger ->

            val knobState = KnobObserverBuilderState.setupListener(
                knobListenerServiceModal,
                logger,
                tag
            )

            val state = sessionState.collectAsState(
                SearchSession.EmptySession
            )
            val searchQuery by derivedStateOf {
                val value = state.value
                if (value is SearchSession.TextEntered) {
                    value.query
                } else {
                    ""
                }
            }
            val resultCount by derivedStateOf {
                val value = state.value
                if (value is SearchSession.SearchFinished) {
                    value.results.size
                } else {
                    0
                }
            }

            KeyboardViews.KeyboardPane(
                maxHeight =  0.32F
            ) {
                val theme = ThemeWrapper.ThemeHandle.current
                Column(
                    Modifier.Companion,
                    Arrangement.Bottom
                ) {

                    Row {

                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Close",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.N,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    modalMenuService.closeModalMenu()
                                }
                            )
                        }

                        Box(
                            Modifier
                                .weight(1.22F, true)
                                .border(
                                    width = if (theme.isPixelDoubled) 2.dp else 1.dp,
                                    color = theme.colors.menuBackground
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        theme.centerGradientWithEdgeHighlight.backgroundGradientColorList
                                    )
                                )
                        ) {
                            Text(
                                text = searchQuery,
                                color = theme.colors.TEXT_WHITE,
                                fontSize = theme.smallItem.fontSize,
                                modifier = Modifier.padding(
                                    top = (theme.smallItem.chipWidth).dp.halveIfNotPixelDoubled(),
                                    bottom = theme.smallItem.highlightWidth.dp.halveIfNotPixelDoubled(),
                                    start = (theme.smallItem.chipWidth).dp.halveIfNotPixelDoubled(),
                                    end = (theme.smallItem.chipWidth).dp.halveIfNotPixelDoubled(),
                                )
                            )

                        }
                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "✐",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.N,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    enterQueryText()
                                }
                            )
                        }
                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Run Search",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.N,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    scope.launch {
                                        val results = doSearch(searchQuery)
                                        logger.d(PdfViewerScreen.TAG, "Query: $searchQuery, Results: $results")
                                        logger.d(PdfViewerScreen.TAG, "reader: ${reader.isLoading}")
                                        logger.d(PdfViewerScreen.TAG, "reader: ${reader.pageCount}")
                                        logger.d(PdfViewerScreen.TAG, "reader: ${reader.error}")
//                                            logger.d(TAG, "reader: ${reader.pageText(0)}")
                                        sessionState.value = SearchSession.SearchFinished(
                                            query = searchQuery,
                                            results = results
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Row {

                        MenuItem(
                            label = if (resultCount == 1) "$resultCount result" else "$resultCount results",
                            boxModifier = Modifier.weight(1F, true),
                            isSmallSize = true,
                            isSelected = false,
                            chipOrientation = ItemChipOrientation.NONE,
                            onClicked = {}
                        )

                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "🚫💡 ",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.S,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    scope.launch { clearHighlights() }
                                }
                            )
                        }

                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "💡 Current Page",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.S,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    scope.launch {
                                        highlightResultsCurrentPage()
                                    }
                                }
                            )
                        }

                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "💡 All Pages",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.S,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    scope.launch {
                                        highlightResultsAllPages()
                                    }
                                }
                            )
                        }

                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Explore 💡...",
                                boxModifier = Modifier.Companion,
                                isSmallSize = true,
                                isSelected = (allocatedIndex == currentIndex),
                                chipOrientation = ItemChipOrientation.S,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    scope.launch {
                                        showExploreResultsPane()
                                    }
                                }
                            )
                        }


                        //TODO buttons for "Draw selector box on current page"
                        //TODO button for "Draw selector box on all pages"

                        //TODO explore results -> open side menu.
                    }
                }
            }
        }
    }

    private fun enterQueryText() {
        //Called from an onClick handler in openSessionToolbar

        modalMenuService.closeModalMenu()
        modalMenuService.showKeyboard(
            Keyboard.KeyboardType.FULL,
            prefilled = if (sessionState is SearchSession.TextEntered) {
                sessionState.query
            } else { "" },
            onCloseWithoutEntry = {
                scope.launch { openSessionToolbar() }
            },
            onTextEntered = { new ->
                scope.launch {
                    sessionState.value = if (new.isBlank()) {
                        SearchSession.EmptySession
                    } else {
                        SearchSession.TextEntered(query = new)
                    }
                    openSessionToolbar()
                }
            }
        )
    }

    private val currentReaderPage = MutableStateFlow(0)
    fun setCurrentReaderPage(page : Int) {
        currentReaderPage.value = page
    }
    val highlights : MutableStateFlow<List<SearchHit>> = MutableStateFlow(emptyList())

    private fun highlightResultsAllPages() {
        val state = sessionState.value
        if (state is SearchSession.SearchFinished) {
            highlights.value = state.results
        }
    }

    private fun highlightResultsCurrentPage() {
        val state = sessionState.value
        if (state is SearchSession.SearchFinished) {
            highlights.value = state.results.filter { it.page == currentReaderPage.value }
        }
    }

    private fun clearHighlights() {
        highlights.value = emptyList()
    }

    private fun showExploreResultsPane() {
        modalMenuService.closeSidePaneOverlay(true)
        modalMenuService.closeModalMenu()
        reader.renderScale = 0.58F
        modalMenuService.showSidePaneOverlayWithKnobListener(false) { knobListenerServiceModal ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .border(width = 4.dp.halveIfNotPixelDoubled(), color = ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                    .shadow(4.dp.halveIfNotPixelDoubled(), RectangleShape),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                var resultIsOnPage by remember { mutableStateOf<Int?>(null) }

                CenterGradientWithEdgeHighlight {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Page ${readerUiState.currentPage} / ${reader.pageCount}".uppercase(),
                            fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor,
                        )
                    }
                }

                val results : Map<Int, List<SearchHit>> = (sessionState.value as? SearchSession.SearchFinished)?.let {
                    it.results
                        .groupBy { it.page }
                        .mapValues { entry -> entry.value.sortedBy { it.rect.top } }
                } ?: emptyMap()

                val tag = "SearchExploreResults"

                SmoothScroll.SmoothScroll(
                    modifier = Modifier.fillMaxWidth(),
                    knobListenerService = knobListenerServiceModal,
                    tag = tag,
                    logger = logger,
                    navigationNodeTraverser = navigationNodeTraverser,
                    prependGoBackEntry = false,
                    items = buildList {
                        add(@Composable { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Go Back",
                                chipOrientation = ItemChipOrientation.W,
                                isSelected = allocatedIndex == currentIndex,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    modalMenuService.closeSidePaneOverlay(true)
                                    openSearchUi()
                                }
                            )
                        })
                        results.keys.map { pageKey ->
                            //Draw a page with highlights, when selected, scroll to the page
                            add(@Composable { allocatedIndex, currentIndex ->
                                Box(
                                    Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth()
                                ) {
                                    PdfPage(
                                        state = reader,
                                        pageIndex = pageKey,
                                        modifier = Modifier
                                        //.matchParentSize()
                                    )
                                    val items = results[pageKey]
                                    if (items?.isNotEmpty() == true) {
                                        var pageSize by remember { mutableStateOf<PageSize?>(null) }
                                        LaunchedEffect(pageKey) { pageSize = reader.pageSize(pageKey) }
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

                            })
                            //For each result for a page, add a plain old MenuItem
                            //with the whole text run.
                            results[pageKey]?.map { hit ->
                                add(@Composable { allocatedIndex, currentIndex ->
                                    LaunchedEffect(allocatedIndex, currentIndex) {
                                        if (allocatedIndex == currentIndex) {
                                            resultIsOnPage = pageKey
                                        }
                                    }
                                    MenuItem(
                                        label = hit.text,
                                        chipOrientation = ItemChipOrientation.E,
                                        isSelected = allocatedIndex == currentIndex,
                                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                            readerUiState.jumpToPage(hit.page)
                                        }
                                    )
                                })
                            }
                        }
                    }
                )

//                PdfPage(
//                    state = reader,
//                    pageIndex = readerUiState.currentPage,
//                    contentScale = ContentScale.Inside,
//                    modifier = Modifier.weight(2F, true).align(Alignment.CenterHorizontally)
//                )

//                Box(Modifier.weight(1F), contentAlignment = Alignment.BottomStart) {
//                    HalfScreenMenu.OneColumn(
//                        alignment = Alignment.End,
//                        fullWidth = true,
//                        items = listOf(
//                            TextMenuItem("Select Page", onClicked = {
//                                modalMenuService.closeSidePaneOverlay(true)
//                                navigationNodeTraverser.setResultAndGoBack(
//                                    thisClass,
//                                    PageSelectorResult.PageSelected(
//                                        selectedPageNumber = pageNumber,
//                                        lastPageNumber = lastPageNumber,
//                                        screenParameters.fileName,
//                                        screenParameters.fileBytes
//                                    )
//                                )
//                            }),
//                            TextMenuItem("Close Menu", onClicked = {
//                                modalMenuService.closeSidePaneOverlay(true)
//                            })
//                        )
//
//                    )
//                }
            }
        }
    }



}