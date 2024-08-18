package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ImageMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MenuItem {
    val isSelectable : Boolean
    val isSelected : Boolean
    val onClicked : () -> Unit

    fun toView(
        boxModifier: Modifier = Modifier.fillMaxWidth(),
        chipOrientation: ItemChipOrientation
    ): @Composable () -> Unit

    fun copyAndSetIsSelected(isSelected: Boolean): MenuItem {
        return when (this) {
            is TextMenuItem -> this.copy(isSelected = isSelected)
            is CheckBoxMenuItem -> this.copy(isSelected = isSelected)
            is CheckBoxFlowMenuItem -> this.copy(isSelected = isSelected)
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
                is CheckBoxFlowMenuItem -> it.copy(onClicked = newOnClick)
                is ImageMenuItem -> it.copy(onClicked = newOnClick)
                else -> error("Unsupported.")
            }
        }
    }
}


data class TextMenuItem(
    val title : String,
    val labelColor : Color? = null,
    override val isSelectable : Boolean = true,
    override val isSelected : Boolean = false,
    override val onClicked : () -> Unit
) : MenuItem {

    companion object {
        const val CHECKBOX_CHECKED = "\uD83D\uDDF9"
        const val CHECKBOX_UNCHECKED = "\u2610"

        fun Boolean.toCheckBox() : String = if (this) CHECKBOX_CHECKED else CHECKBOX_UNCHECKED
    }

    override fun toView(boxModifier : Modifier, chipOrientation: ItemChipOrientation): @Composable () -> Unit = {
        MenuItem(
            boxModifier = boxModifier,
            label = title,
            chipOrientation = chipOrientation,
            labelColor = labelColor ?: ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            isSelected = isSelected,
            onClicked = onClicked
        )
    }
}

data class CheckBoxMenuItem(
    val title: String,
    val isChecked : Boolean,
    val labelColor: Color? = null,
    override val isSelectable : Boolean = true,
    override val isSelected : Boolean = false,
    override val onClicked : () -> Unit
) : MenuItem {
    override fun toView(boxModifier: Modifier, chipOrientation: ItemChipOrientation): @Composable () -> Unit = {
        MenuItem(
            boxModifier = boxModifier,
            label = " ${if (isChecked) "[X]" else "[ ]"} $title",
            chipOrientation = chipOrientation,
            labelColor = labelColor ?: ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            isSelected = isSelected,
            onClicked = onClicked
        )
    }
}
data class CheckBoxFlowMenuItem(
    val title: String,
    val isChecked : SharedFlow<Boolean>,
    val isCheckedInitial : Boolean = false,
    val labelColor: Color? = null,
    override val isSelectable : Boolean = true,
    override val isSelected : Boolean = false,
    override val onClicked : () -> Unit
) : MenuItem {
    override fun toView(boxModifier: Modifier, chipOrientation: ItemChipOrientation): @Composable () -> Unit = {

        val isCheckedState = isChecked.collectAsState(isCheckedInitial)
        MenuItem(
            boxModifier = boxModifier,
            label = " ${if (isCheckedState.value) "[X]" else "[ ]"} $title",
            chipOrientation = chipOrientation,
            labelColor = labelColor ?: ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            isSelected = isSelected,
            onClicked = onClicked
        )
    }
}

data class ImageMenuItem(
    val image : Painter,
    val imageModifier : Modifier = Modifier,
    val tintColor : Color? = null,
    override val isSelectable : Boolean = true,
    override val isSelected : Boolean = false,
    override val onClicked : () -> Unit
) : MenuItem {
    override fun toView(boxModifier: Modifier, chipOrientation: ItemChipOrientation): @Composable () -> Unit = {
        ImageMenuItem(
            boxModifier = boxModifier,
            painter = image,
            imageModifier = imageModifier,
            alignment = Alignment.Center,
            tintColor = tintColor,
            chipOrientation = chipOrientation,
            isSelected = isSelected,
            onClicked = onClicked
        )
    }
}

