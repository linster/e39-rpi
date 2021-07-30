package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ca.stefanm.ibus.gui.menu.widgets.BmwChipMenu
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem


data class ModalMenu(
    val items : List<ModalMenuItem>,
    val chipOrientation: ItemChipOrientation,
    val onOpen : suspend () -> Unit,
    val onClose : () -> Unit,
) {

    data class ModalMenuItem(
        val title : String,
        val isSelectable : Boolean = true,
        val isSelected : Boolean = false,
        val onClicked : () -> Unit
    )


    companion object {
        val EMPTY = ModalMenu(
            items = listOf(),
            chipOrientation = ItemChipOrientation.E,
            onOpen = {},
            onClose = {}
        )
    }
}

@Composable
fun ModalChipMenu(
    modifier : Modifier = Modifier,
    modalMenu: ModalMenu
) {
    Box(
        modifier = modifier.then(
            Modifier.background(ChipItemColors.MenuBackground)
        )
    ) {
        Column {
            LaunchedEffect(modalMenu) {
                modalMenu.onOpen
            }

            //TODO USE THE POSITION OF THE DATA TO REGISTER A SELECTION INDEX.

            DisposableEffect(modalMenu) {
                onDispose {
                    modalMenu.onClose
                }
            }

            for (item in modalMenu.items) {
                MenuItem(
                    label = item.title,
                    chipOrientation = if (item.isSelectable) {
                        modalMenu.chipOrientation
                    } else {
                           ItemChipOrientation.NONE
                    },
                    isSelected = item.isSelected,
                    isSmallSize = true,
                    onClicked = item.onClicked
                )
            }

        }
    }
}

@Composable
fun ModalChipMenuWindowOverlay(
    menuTopLeft : IntOffset,
    menuWidth : Int, //The height can be automatically calculated.
    menuData : ModalMenu
) {
    Layout(
        modifier = Modifier.fillMaxSize(),
        content = { ModalChipMenu(modalMenu = menuData)}
    ) { measurables, constraints ->

        val placeable = measurables[0].measure(Constraints.fixedWidth(menuWidth))

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(position = menuTopLeft)
        }
    }
}