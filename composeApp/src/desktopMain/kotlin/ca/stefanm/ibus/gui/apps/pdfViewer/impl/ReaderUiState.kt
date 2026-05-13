package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import dev.nucleusframework.pdfium.PdfReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

// Basically this https://github.com/kdroidFilter/ComposePdf/blob/master/example/src/commonMain/kotlin/dev/nucleusframework/pdf/reader/ReaderScreenState.kt
// but without the two-up view. It's a 400*234 screen, no one needs a 2-up view.
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
        fun rememberReaderUiState(
            reader: PdfReaderState,
            scope: CoroutineScope,
        ) : ReaderUiState {
            val mainListState = rememberLazyListState()
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
//                mainListState.animateScrollToItem(row)
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