package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.io.path.Path


//Think about how observable this will be with Compose?

//Keep the directory status in another class.
@Stable //TODO read the PdfReaderState
class FileManagerViewState {


    //Are previews enabled
    var showPreview by mutableStateOf(false)
        private set

    @JvmName("setShowPreviewFun")
    fun setShowPreview(new : Boolean) {
        showPreview = new
        if (itemStyle == ItemStyle.List || itemStyle == ItemStyle.ListWithPreviews) {
            itemStyle = if (new) {
                ItemStyle.ListWithPreviews
            } else {
                ItemStyle.List
            }
        }
    }


    enum class ItemStyle(val label : String) {
        /** Just a single column list of file names */
        List(label = "List"),
        /** A grid of files */
        Grid(label = "Grid"),
        /** A single column list of files with names and previews */
        //A separate view mode because the control is different, but
        //Selected by choosing list and enabling previews.
        ListWithPreviews(label = "List with Previews")
    }

    var itemStyle : ItemStyle by mutableStateOf(ItemStyle.List)
        private set

    @JvmName("setItemStyleFun")
    fun setItemStyle(itemStyle: ItemStyle) {
        this.itemStyle = if (itemStyle == ItemStyle.List && showPreview) {
            ItemStyle.ListWithPreviews
        } else {
            itemStyle
        }
    }

    // Item height (if not List viewMode)
    var previewItemHeightPx : Int = 100
        private set

    fun setPreviewItemHeightPx(height : Int) {
        this.previewItemHeightPx = height
    }

}