package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
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
        //TODO Set each onClick to also close the menu after being run.
        return this.map { item ->
            item.copy(onClicked = { newOnclick(item.onClicked) })
        }
    }

    fun closeModalMenu() {
        _modalMenuOverlay.value = null
    }
}