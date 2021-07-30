package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.ui.graphics.ImageBitmap

sealed class MenuItem(
    val isSelectable : Boolean = true,
    val isSelected : Boolean = false,
    val onClicked : () -> Unit
) {
    class TextMenuItem(
        val title : String,
        isSelectable : Boolean = true,
        isSelected : Boolean = false,
        onClicked : () -> Unit
    ) : MenuItem(
        isSelectable, isSelected, onClicked
    )

    class ImageMenuItem(
        val image : ImageBitmap,
        isSelectable : Boolean = true,
        isSelected : Boolean = false,
        onClicked : () -> Unit
    ) : MenuItem(
        isSelectable, isSelected, onClicked
    )
    companion object {
        val SPACER = TextMenuItem(
            title = "",
            isSelectable = false,
            isSelected = false,
            onClicked = {}
        )
    }
}