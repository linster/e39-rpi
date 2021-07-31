package ca.stefanm.ibus.gui.menu.widgets.knobListener

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider


/** Inject this into any control that needs to listen to scroll wheel state. */
@ExperimentalCoroutinesApi
@ApplicationScope
@Stable
class KnobListenerService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) val inputEvents : SharedFlow<InputEvent>,
) {


    @Composable
    inline fun <reified T> listenForKnob(
        /** This is the list data the knob will scroll through */
        listData : List<T>,

        /** This is a lambda that is applied to each item in the list
         *  marking that is is selected. Return the item with the appropriate
         *  flag set so that the UI can know it's selected.
         */
        crossinline onSelectAdapter : (item : T, isNowSelected : Boolean) -> T,

        /** This is a lambda that is applied to the item if we need to ask it
         *  if it is selectable.
         */
        crossinline isSelectableAdapter : (item : T) -> Boolean,

        /** This is a lambda that's called with an item when we've decided it's
         * been clicked. Users of the listenForKnob should call the onClicked function
         * for the item, whatever it's called.
         */
        crossinline onItemClickAdapter : (item : T) -> Unit

        ) : State<SnapshotStateList<T>> {

        val logger = DaggerApplicationComponent.create().logger()

        val stateListOf = mutableStateListOf(*listData.toTypedArray())

        return produceState(initialValue = stateListOf, listData) {
            val selectedListIndices = stateListOf
                .mapIndexed { index, t -> index to t }
                .filter { isSelectableAdapter(it.second) }
                .map { it.first }
                .circular()
            var selectedIndex : Int = 0

            //Pre-select the first selectable item
            stateListOf.indexOfFirst { isSelectableAdapter(it) }.let { index ->
                if (index != -1) {
                    selectedIndex = index
                    stateListOf[index] = onSelectAdapter(stateListOf[index], true)
                    value = stateListOf
                }
            }

            inputEvents
                .onSubscription { logger.d("BAZBAT", "onSubscription") }
                .onEmpty { logger.d("BAZBAT", "onEmpty") }
                .onEach { logger.d("BAZBAT", "onEach") }
                .onCompletion { logger.d("BAZBAT", "onCompletion") }.collect { event ->

                logger.d("WAT", "EVENT WAT: $event")

                val offset = if (event !is InputEvent.NavKnobTurned) { 0 } else {
                    event.clicks * (if (event.direction == InputEvent.NavKnobTurned.Direction.RIGHT) 1 else -1)
                }

                val oldSelectedIndex = selectedIndex
                stateListOf[selectedListIndices[oldSelectedIndex]] =
                    onSelectAdapter(stateListOf[selectedListIndices[oldSelectedIndex]], false)

                selectedIndex = oldSelectedIndex + offset
                stateListOf[selectedListIndices[selectedIndex]] =
                    onSelectAdapter(stateListOf[selectedListIndices[selectedIndex]], true)

                logger.d("Scroll", "Offset: $offset")
                logger.d("Scroll", "SelectedIndex: (old, new) : $oldSelectedIndex, $selectedIndex")
                logger.d("Scroll", "Selectable indices: ${selectedListIndices}")

                value = stateListOf

                //Listen for clicks
                if (event is InputEvent.NavKnobPressed) {
                    logger.d("Scroll:Click:", "selectedIndex: ${selectedIndex}")
                    logger.d("Scroll:Click:", "selectedListIndices: ${selectedListIndices}")
                    logger.d("Scroll:Click:", "selectedListIndices[selectedIndex]: ${selectedListIndices[selectedIndex]}")
                    logger.d("Scroll:Click:", "item: ${stateListOf[selectedListIndices[selectedIndex]]}")
                    onItemClickAdapter(stateListOf[selectedListIndices[selectedIndex]])
                }
            }

        }

    }

}