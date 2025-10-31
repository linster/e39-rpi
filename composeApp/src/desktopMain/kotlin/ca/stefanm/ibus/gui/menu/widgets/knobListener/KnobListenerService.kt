package ca.stefanm.ibus.gui.menu.widgets.knobListener

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import co.touchlab.stately.concurrency.value
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger
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

    fun knobTurnEvents() : Flow<InputEvent> {
        return inputEvents.filter {
            it is InputEvent.NavKnobPressed || it is InputEvent.NavKnobTurned
        }.transform {
            if (it is InputEvent.NavKnobPressed) {
                emit(it)
            }

            if (it is InputEvent.NavKnobTurned) {
                repeat(it.clicks) { _ ->
                    emit(InputEvent.NavKnobTurned(1, it.direction))
                }
            }
        }
    }

    val listenerEnabled : MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun enableListener() {
        listenerEnabled.value = true
    }

    fun disableListener() {
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
                .takeWhile {
                    logger.d("WAT", "I am ${this@KnobListenerService.hashCode()} and enable is ${listenerEnabled.value}")
                    listenerEnabled.value
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

    companion object {


//        KnobObserverBuilder(builderState) { allocatedIndex, current index ->
//
//            TextViewComposable(
//                isSelected = currentIndex == allocatedIndex,
//                onClicked = CallWhen(currentIndexIs = allocatedIndex) { … }
//
//        }
//
//        The CallWhen is a function in the lambda scope for the KnobObserverBuilder
//        that registers the observer in a Map<Int, suspend ()->Unit>.
//
//        The builderState is a class passed in. It is remember(Unit){} where the screen is made.
//
//        The builderState state takes in in the Flow<InputEvents> as a constructor parameter
//        and that’s what does the dispatches for the allocated Index.
//
//        KnobObserverBuilder is made so that it runs only once? Remember updated?
//        We don’t want recompositions to reregister. Maybe a key() around the call to contents().
//

        class KnobObserverBuilderState(
            private val service : KnobListenerService,
            private val logger: Logger
        ) {

            private val currentAllocatedIndex = AtomicInteger()

            fun allocateIndex() : Int {
                return currentAllocatedIndex.incrementAndGet()
            }

            private val currentIndex = MutableStateFlow(1)
            fun getCurrentIndex() : StateFlow<Int> = currentIndex.asStateFlow()

            private fun incrementCurrentIndex() {
                if (currentIndex.value + 1 > currentAllocatedIndex.value) {
                    currentIndex.value = 0
                    return
                }
                currentIndex.value = currentIndex.value + 1
            }

            private fun decrementCurrentIndex() {
                if (currentIndex.value - 1 < 1) {
                    currentIndex.value = currentAllocatedIndex.value
                    return
                }
                currentIndex.value = currentIndex.value -1
            }

            private val callbacks : MutableMap<Int, () -> Unit> = mutableMapOf()
            fun registerCallback(index : Int, callback : () -> Unit) {
                callbacks[index] = callback
            }

            fun unregisterCallback(index: Int) {
                callbacks.remove(index)
            }

            fun logState() {
                logger.d(TAG, "currentIndex: ${currentIndex.value}")
//                logger.d(TAG, "callbacks: $callbacks")

            }

            suspend fun subscribeEvents() {
                service.knobTurnEvents()
                    .onEach { logState() }
                    .map { event ->
                        when (event) {
                            is InputEvent.NavKnobPressed -> {
                                logger.d(TAG,"NavKnobPressed")
                                callbacks.getOrDefault(currentIndex.value, { logger.w(TAG, "Index $currentIndex clicked but has no callback")}).invoke()
                            }
                            is InputEvent.NavKnobTurned -> {
                                when (event.direction) {
                                    InputEvent.NavKnobTurned.Direction.RIGHT -> incrementCurrentIndex()
                                    InputEvent.NavKnobTurned.Direction.LEFT -> decrementCurrentIndex()
                                }
                            }
                            else -> {}
                        }
                    }.collect()
            }


            fun getScope() : KnobObserverBuilderScope {
                return object : KnobObserverBuilderScope {
                    override fun getState() = this@KnobObserverBuilderState
                }
            }

            companion object {
                const val TAG = "KnobListenerService"
            }
        }

        interface KnobObserverBuilderScope {
            fun getState() : KnobObserverBuilderState
            @Composable fun CallWhen(currentIndexIs : Int, call : () -> Unit) : () -> Unit {
                DisposableEffect(currentIndexIs) {
                    getState().registerCallback(currentIndexIs, call)
                    onDispose {
                        getState().unregisterCallback(currentIndexIs)
                    }
                }

                return call
            }
        }

        @Composable
        fun KnobObserverBuilder(
            state : KnobObserverBuilderState,
            contents : @Composable KnobObserverBuilderScope.(allocatedIndex : Int, currentIndex : Int) -> Unit
        ) {


            val allocatedIndex = remember(Unit) { state.allocateIndex() }

            contents.invoke(state.getScope(),
                allocatedIndex,
                state.getCurrentIndex().collectAsState(0).value )

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