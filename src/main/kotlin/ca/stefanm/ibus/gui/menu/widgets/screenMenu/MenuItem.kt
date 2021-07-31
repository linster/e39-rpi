package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem

interface MenuItem {
    val isSelectable : Boolean
    val isSelected : Boolean
    val onClicked : () -> Unit

    fun toView(
        chipOrientation: ItemChipOrientation
    ): @Composable () -> Unit

    fun copyAndSetIsSelected(isSelected: Boolean): MenuItem {
        return when (this) {
            is TextMenuItem -> this.copy(isSelected = isSelected)
            is CheckBoxMenuItem -> this.copy(isSelected = isSelected)
            is ImageMenuItem -> this.copy(isSelected = isSelected)
            else -> error("Unsupported type")
        }
    }

    companion object {
        val SPACER = TextMenuItem(
            title = "",
            isSelectable = false,
            isSelected = false,
            onClicked = {}
        )

        fun List<MenuItem>.reduceUpdateOnClick(
            newOnClick : (existingOnClick : () -> Unit) -> Unit
        ) : List<MenuItem> = this.map {
            val newOnClick = { newOnClick(it.onClicked)}
            when (it) {
                is TextMenuItem -> it.copy(onClicked = newOnClick)
                is CheckBoxMenuItem -> it.copy(onClicked = newOnClick)
                is ImageMenuItem -> it.copy(onClicked = newOnClick)
                else -> error("Unsupported.")
            }
        }
    }
}



data class TextMenuItem(
    val title : String,
    val labelColor : Color = ChipItemColors.TEXT_WHITE,
    override val isSelectable : Boolean = true,
    override val isSelected : Boolean = false,
    override val onClicked : () -> Unit
) : MenuItem {

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
) : MenuItem {
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
) : MenuItem {
    override fun toView(chipOrientation: ItemChipOrientation): () -> Unit {
        TODO("Not yet implemented")
    }
}

