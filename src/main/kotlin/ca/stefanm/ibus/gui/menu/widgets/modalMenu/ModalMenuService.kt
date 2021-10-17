package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

object SidePanelMenu {
    @Composable
    fun SidePanelMenu(
        title : String? = null,
        text : @Composable () -> Unit,
        buttons : List<TextMenuItem>
    ) {
        SidePanelMenu(title) {
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                    text()
                }
                HalfScreenMenu.OneColumn(
                    alignment = Alignment.End,
                    fullWidth = true,
                    items = buttons
                )
            }
        }
    }

    @Composable
    fun SidePanelMenu(title : String? = null, contents : @Composable () -> Unit) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ChipItemColors.MenuBackground)
                .border(width = 4.dp, color = Color(61, 112, 176, 255))
                .shadow(4.dp, RectangleShape)
        ) {
            if (title != null) {
                BmwSingleLineHeader(title)
            }
            contents()
        }
    }

    @Composable
    fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ChipItemColors.TEXT_WHITE,
            fontSize = 18.sp,
            fontWeight = weight
        )
    }
}


/** Inject this anywhere you want to show a modal menu */
@ExperimentalCoroutinesApi
@ApplicationScope
class ModalMenuService @Inject constructor(
    private val knobListenerService: KnobListenerService
) {
    private val _modalMenuOverlay = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val modalMenuOverlay = _modalMenuOverlay.asStateFlow()

    private val _sidePaneOverlay = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val sidePaneOverlay = _sidePaneOverlay.asStateFlow()

    fun showModalMenu(
        menuTopLeft : IntOffset,
        menuWidth : Int, //The height can be automatically calculated.
        menuData : ModalMenu,
        autoCloseOnSelect : Boolean = true
    ) {
        _modalMenuOverlay.value = {
            ModalChipMenuWindowOverlay(
                menuTopLeft = menuTopLeft,
                menuWidth = menuWidth,
                //TODO, put in an operator to manage the scroll button control state.
                menuData = menuData.copy(
                    items = menuData.items
                        .reduceUpdateOnClick { existingOnClick ->
                            existingOnClick()
                            if (autoCloseOnSelect) {
                                closeModalMenu()
                            }
                        }.let {
                            knobListenerService
                                .listenForKnob(it,
                                    onSelectAdapter = { item, isNowSelected ->
                                        item.copy(isSelected = isNowSelected)
                                    },
                                    isSelectableAdapter = { item -> item.isSelectable },
                                    onItemClickAdapter = { item -> item.onClicked() }
                                ).value
                    }
                )
            )
        }
    }

    //This is a mix of `reduce` and `copy` that allows the caller to update
    //the onClick lambda in place.
    private fun List<ModalMenu.ModalMenuItem>.reduceUpdateOnClick(
        newOnclick : (existingOnClick : () -> Unit) -> Unit
    ) : List<ModalMenu.ModalMenuItem> {
        return this.map { item ->
            item.copy(onClicked = { newOnclick(item.onClicked) })
        }
    }

    fun closeModalMenu() {
        _modalMenuOverlay.value = null
    }

    fun showKeyboard(
        type : Keyboard.KeyboardType,
        prefilled : String = "",
        onTextEntered : (entered : String) -> Unit
    ) {
        _modalMenuOverlay.value = {
            Keyboard.showKeyboard(
                type = type,
                prefilled = prefilled,
                knobListenerService = knobListenerService,
                onTextEntered = { onTextEntered(it); closeModalMenu() },
                closeWithoutEntry = this::closeModalMenu
            )()
        }
    }

    fun showSidePaneOverlay(
        darkenBackground : Boolean = false,
        contents : @Composable () -> Unit
    ) {
        if (darkenBackground) {
            _modalMenuOverlay.value = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(
                            listOf(Color.Transparent,
                            Color(0F, 0F, 0F, alpha = 0.6F)),
                            startX = 0F,
                            endX = 0.25F
                        )
                        )
                ) { }
            }
        }
        _sidePaneOverlay.value = contents
    }

    fun closeSidePaneOverlay(clearModalOverlay : Boolean = false) {
        if (clearModalOverlay) {
            closeModalMenu()
        }
        _sidePaneOverlay.value = null
    }
}