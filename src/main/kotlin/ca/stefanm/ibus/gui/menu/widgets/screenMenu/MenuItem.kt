package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem

sealed class MenuItem(
    open val isSelectable : Boolean = true,
    open val isSelected : Boolean = false,
    open val onClicked : () -> Unit
) {

    abstract fun toView(
        chipOrientation: ItemChipOrientation
    ) : @Composable () -> Unit

    fun copyAndSetIsSelected(isSelected: Boolean) : MenuItem {
        return when (this) {
            is TextMenuItem -> this.copy(isSelected = isSelected)
            is CheckBoxMenuItem -> this.copy(isSelected = isSelected)
            is ImageMenuItem -> this.copy(isSelected = isSelected)
        }
    }

    data class TextMenuItem(
        val title : String,
        val labelColor : Color = ChipItemColors.TEXT_WHITE,
        override val isSelectable : Boolean = true,
        override val isSelected : Boolean = false,
        override val onClicked : () -> Unit
    ) : MenuItem(
        isSelectable, isSelected, onClicked
    ) {
        override fun toView(chipOrientation: ItemChipOrientation): @Composable () -> Unit = {
            MenuItem(
                label = title,
                chipOrientation = chipOrientation,
                labelColor = labelColor,
                isSelected = isSelected,
                onClicked = onClicked
            )
        }
    }

    data class CheckBoxMenuItem(
        val title: String,
        val isChecked : Boolean,
        val labelColor: Color = ChipItemColors.TEXT_WHITE,
        override val isSelectable : Boolean = true,
        override val isSelected : Boolean = false,
        override val onClicked : () -> Unit
    ) : MenuItem(
        isSelectable, isSelected, onClicked
    ) {
        override fun toView(chipOrientation: ItemChipOrientation): @Composable () -> Unit = {
            MenuItem(
                label = " ${if (isChecked) "[X]" else "[ ]"} $title",
                chipOrientation = chipOrientation,
                labelColor = labelColor,
                isSelected = isSelected,
                onClicked = onClicked
            )
        }
    }

    data class ImageMenuItem(
        val image : ImageBitmap,
        override val isSelectable : Boolean = true,
        override val isSelected : Boolean = false,
        override val onClicked : () -> Unit
    ) : MenuItem(
        isSelectable, isSelected, onClicked
    ) {

        override fun toView(chipOrientation: ItemChipOrientation): () -> Unit {
            TODO("Not yet implemented")
        }

    }
    companion object {
        val SPACER = TextMenuItem(
            title = "",
            isSelectable = false,
            isSelected = false,
            onClicked = {}
        )
    }
}