package ca.stefanm.ibus.gui.menu.widgets.knobListener

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationModule.Companion.KNOB_LISTENER_MODAL
import ca.stefanm.ibus.di.ApplicationModule.Companion.KNOB_LISTENER_MAIN
import ca.stefanm.ibus.di.ApplicationModule.Companion.KNOB_LISTENER_MAIN_AUX
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import co.touchlab.stately.concurrency.value
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider


//Silly, but a way for the knob listener service to know
//which one (of three) it is at run time
@ApplicationScope
class KnobListenerServiceIdentifier @Inject constructor(
    @Named(KNOB_LISTENER_MAIN) private val main : Provider<KnobListenerService>,
    @Named(KNOB_LISTENER_MAIN_AUX) private val mainAux : Provider<KnobListenerService>,
    @Named(KNOB_LISTENER_MODAL) private val modal : Provider<KnobListenerService>
) {

    fun getNameFromHashcode(hashCode : Int) : String {
        if (hashCode == main.get().hashCode()) {
            return "KnobListenerMain"
        }
        if (hashCode == mainAux.get().hashCode()) {
            return "KnobListenerMainAux"
        }
        if (hashCode == modal.get().hashCode()) {
            return "KnobListenerModal"
        }
        return hashCode.toString()
    }
}


/** Inject this into any control that needs to listen to scroll wheel state. */
@ExperimentalCoroutinesApi
@ApplicationScope
@Stable
class KnobListenerService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) val inputEvents : SharedFlow<InputEvent>,
    private val identifier: KnobListenerServiceIdentifier,
    //Used by the inline listenForKnob, so it can't be private.
    @PublishedApi internal val logger: Logger
) {

    val tag
        get() = identifier.getNameFromHashcode(this.hashCode())

    /** The input events, only while this listener is enabled.
     *
     * Disabling unsubscribes from the input stream and enabling subscribes
     * again, so a collector lasts through a modal menu (or other overlay)
     * taking the knob. The old takeWhile gate completed the flow on the first
     * event while disabled, and the screen never got the knob back.
     *
     * Subscribing anew on enable (instead of filtering on the flag) also means
     * the press that closes a modal is not seen by the screen underneath: the
     * modal's click handler re-enables this listener while that press is
     * still being delivered. inputEvents has no replay, so only later events
     * reach it. The filter covers the other edge: flatMapLatest switches
     * asynchronously, so an event delivered just after disableListener()
     * is dropped here.
     */
    fun enabledInputEvents() : Flow<InputEvent> =
        listenerEnabled.flatMapLatest { enabled ->
            if (enabled) inputEvents.filter { listenerEnabled.value } else emptyFlow()
        }

    fun knobTurnEvents(filterEvents : Boolean = true) : Flow<InputEvent> {
        return enabledInputEvents().filter {
            if (filterEvents) {
                it is InputEvent.NavKnobPressed || it is InputEvent.NavKnobTurned
            } else {
                true
            }
        }.onEach {
            logger.d(tag, "I am ${tag} and enable is ${listenerEnabled.value}")
        }
            .transform {
            if (it is InputEvent.NavKnobPressed) {
                emit(it)
                return@transform
            }

            if (it is InputEvent.NavKnobTurned) {
                repeat(it.clicks) { _ ->
                    emit(InputEvent.NavKnobTurned(1, it.direction))
                }
                return@transform
            }
            if (!filterEvents) {
                emit(it)
            }
        }
    }

    val listenerEnabled : MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun enableListener() {
        logger.d("KnobListenerService", "$tag enableListener")
        listenerEnabled.value = true
    }

    fun disableListener() {
        logger.d("KnobListenerService", "$tag disableListener")
        listenerEnabled.value = false
    }

    @Composable
    inline fun <reified T> listenForKnob(
        /** This is the list data the knob will scroll through */
        listData : List<T>,

        /** This is a lambda that is applied to each item in the list
         *  marking that is selected. Return the item with the appropriate
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

        val stateListOf = mutableStateListOf(*listData.toTypedArray())

        return produceState(initialValue = stateListOf, listData) {
            val selectedListIndices = stateListOf
                .mapIndexed { index, t -> index to t }
                .filter { isSelectableAdapter(it.second) }
                .map { it.first }
                .circular()
            //Position in selectedListIndices (the selectable rows), not in stateListOf.
            var selectedIndex : Int = 0

            //Pre-select the first selectable item. It is selectedListIndices[0];
            //selectedIndex stays 0. Setting it to the row's position in the
            //whole list made the knob act on a different row than the
            //highlighted one whenever non-selectable rows came first.
            if (selectedListIndices.isEmpty()) {
                //Nothing to select: ignore the knob. Indexing the empty
                //circular list on the first knob event would throw.
                return@produceState
            }
            selectedListIndices[0].let { index ->
                stateListOf[index] = onSelectAdapter(stateListOf[index], true)
                value = stateListOf
            }

            // See enabledInputEvents(): the old takeWhile gate ended this
            // menu's knob handling for good the first time it was disabled.
            enabledInputEvents()
                .onEach {
                    logger.d("WAT", "I am ${tag} and enable is ${listenerEnabled.value}")
                }
                .collect { event ->

                //logger.d("WAT", "EVENT WAT: $event")

                val offset = if (event !is InputEvent.NavKnobTurned) { 0 } else {
                    event.clicks * (if (event.direction == InputEvent.NavKnobTurned.Direction.RIGHT) 1 else -1)
                }

                val oldSelectedIndex = selectedIndex
                stateListOf[selectedListIndices[oldSelectedIndex]] =
                    onSelectAdapter(stateListOf[selectedListIndices[oldSelectedIndex]], false)

                selectedIndex = oldSelectedIndex + offset
                stateListOf[selectedListIndices[selectedIndex]] =
                    onSelectAdapter(stateListOf[selectedListIndices[selectedIndex]], true)

//                logger.d("Scroll", "Offset: $offset")
//                logger.d("Scroll", "SelectedIndex: (old, new) : $oldSelectedIndex, $selectedIndex")
//                logger.d("Scroll", "Selectable indices: ${selectedListIndices}")

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

@ApplicationScope
class DebugKnobService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS_WRITER) private val inputEventsWriter : MutableSharedFlow<InputEvent>,
    private val logger: Logger
) {
    fun knobClick() {
        GlobalScope.launch {
            logger.d("DebugKnobService", "Sending event: NavKnobPressed")
            inputEventsWriter.emit(InputEvent.NavKnobPressed)
            logger.d("DebugKnobService", "Sent event: NavKnobPressed")
        }
    }
}