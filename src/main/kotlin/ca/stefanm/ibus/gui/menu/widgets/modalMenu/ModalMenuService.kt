package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Inject this anywhere you want to show a modal menu */
@ExperimentalCoroutinesApi
@ApplicationScope
class ModalMenuService @Inject constructor(
    private val knobListenerService: KnobListenerService
) {

    private val _modalMenuOverlay = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val modalMenuOverlay = _modalMenuOverlay.asStateFlow()

    fun showModalMenu(
        menuTopLeft : IntOffset,
        menuWidth : Int, //The height can be automatically calculated.
        menuData : ModalMenu
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
                            closeModalMenu()
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
        onTextEntered : (entered : String) -> Unit
    ) {
        _modalMenuOverlay.value = {
            Keyboard.showKeyboard(
                type = type,
                onTextEntered = { onTextEntered(it); closeModalMenu() },
                closeWithoutEntry = this::closeModalMenu
            )()
        }
    }
}