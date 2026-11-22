package ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject


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

typealias AllocatedId = Int
typealias CurrentId = Int

class AllocatedIdManager @Inject constructor(
    private val logger: Logger
) {

    companion object {
        const val TAG = "AllocatedIdManager"
    }

    private val allocatedIds = mutableListOf<AllocatedId>().circular()

    fun addId() : AllocatedId {
        //Make the first allocated id be 0 if there isn't one allocated,
        //otherwise add the newId
        val newId = (allocatedIds.maxOrNull()?.let { it + 1 }) ?: 0
        val currentWasGone = currentId.value !in allocatedIds
        allocatedIds.add(newId)
        // The current item went away (e.g. its list was rebuilt): the first
        // item that comes is the current one.
        if (currentWasGone) {
            currentId.value = newId
        }
        return newId
    }

    fun removeId(id : AllocatedId) {
        val index = allocatedIds.indexOf(id)
        if (index < 0) {
            logger.d(TAG, "Tried to remove an allocatedId $id which wasn't allocated")
            return
        }
        allocatedIds.removeAt(index)
        // Only when the current item goes does the pointer move: to the item
        // that took its place in the order (or the last one). It used to go
        // to the lowest ID first, before removing: removing several items in
        // turn left it on an ID that was gone (no item highlighted).
        if (currentId.value == id && allocatedIds.isNotEmpty()) {
            currentId.value = allocatedIds[minOf(index, allocatedIds.size - 1)]
        }
    }


    private var currentId : MutableStateFlow<CurrentId> = MutableStateFlow(0)

    fun resetPointerToStart() {
        if (allocatedIds.isEmpty()) {
            return
        }
        currentId.value = allocatedIds.min()
    }
    fun incrementPointer() {
        if (allocatedIds.isEmpty()) {
            return
        }
        val currentIdIndex = allocatedIds.indexOf(currentId.value)
        currentId.value = allocatedIds[currentIdIndex + 1]
    }

    fun decrementPointer() {
        if (allocatedIds.isEmpty()) {
            return
        }
        val currentIdIndex = allocatedIds.indexOf(currentId.value)
        currentId.value = allocatedIds[currentIdIndex - 1]
    }

    fun getCurrentId() : StateFlow<CurrentId> {
        return currentId.asStateFlow()
    }

    fun hasAllocatedId(allocatedId: AllocatedId) : Boolean = allocatedIds.contains(allocatedId)

    fun getMaxAllocatedId() : Int {
        return allocatedIds.maxOrNull() ?: 0
    }
}



class KnobObserverBuilderState(
    private val service: KnobListenerService,
    private val logger: Logger
) {

    private val allocatedIdManager = AllocatedIdManager(logger)



    fun allocateIndex(): Int {
        return allocatedIdManager.addId()
    }

    fun deallocateIndex(allocatedId: Int) {
        allocatedIdManager.removeId(id = allocatedId)
    }

    /** Used in smooth scrolls when there's a toolbar above the smooth scroll
     *  that has already allocated entries.
     */
    fun getMaxAllocatedIndex() : Int {
        return allocatedIdManager.getMaxAllocatedId()
    }


    fun getCurrentSelectedItem(): StateFlow<Int> = allocatedIdManager.getCurrentId()

    fun resetIndexToStart() {
        allocatedIdManager.resetPointerToStart()
    }
    private fun incrementCurrentIndex() {
        //logState("beforeIncrement")
        allocatedIdManager.incrementPointer()
        //logState("afterIncrement")
    }

    private fun decrementCurrentIndex() {
        allocatedIdManager.decrementPointer()
    }

    //Map<AllocatedIndex, lambda>
    private val callbacks: MutableMap<Int, () -> Unit> = mutableMapOf()
    fun registerCallback(allocatedIndex: Int, callback: () -> Unit) {
        callbacks[allocatedIndex] = callback
    }

    fun unregisterCallback(allocatedIndex: Int) {
        callbacks.remove(allocatedIndex)
    }

    private fun logState(tagSuffix: String = "") {
        val computedTag = "$TAG logState $tagSuffix"
        logger.d(computedTag, "Current selected item: ${getCurrentSelectedItem().value}")
        logger.d(computedTag, "callbacks: ${callbacks.keys}")
    }

    suspend fun subscribeEvents(tag : String? = null) {
//        logger.d("WAT", "subscribeEvents hash ${this.hashCode()}")
        service.knobTurnEvents()
            .onStart {
                logger.d(TAG, "Subscription for ${tag ?: this@KnobObserverBuilderState.hashCode()} started")
            }
            .map { event ->
//                logger.d(TAG, "got an event in subscribeEvnents: $event")
                when (event) {
                    is InputEvent.NavKnobPressed -> {
//                        logger.d(TAG, "NavKnobPressed on callback with getCurrentSelectedItem(): ${getCurrentSelectedItem().value}")
                        callbacks.getOrDefault(
                            getCurrentSelectedItem().value,
                            { logger.w(TAG, "Index ${getCurrentSelectedItem().value} clicked but has no callback") }).invoke()
                    }

                    is InputEvent.NavKnobTurned -> {
                        when (event.direction) {
                            InputEvent.NavKnobTurned.Direction.RIGHT -> incrementCurrentIndex()
                            InputEvent.NavKnobTurned.Direction.LEFT -> decrementCurrentIndex()
                        }
                    }

                    else -> {}
                }
            }
            .onCompletion {

                logger.d(TAG, "Subscription for ${tag ?: this@KnobObserverBuilderState.hashCode()} ended. Cause: ${it}")

            }.collect()
    }


    fun getScope(): KnobObserverBuilderScope {
        return object : KnobObserverBuilderScope {
            override fun getState() = this@KnobObserverBuilderState
        }
    }

    companion object {
        const val TAG = "KnobObserverBuilderState"

        //This is a nice use-case for Metro.....
        @Composable
        fun setupListener(
            knobListenerService: KnobListenerService,
            logger: Logger,
            tag: String?
        ) : KnobObserverBuilderState {
            val knobState = remember(Unit) { KnobObserverBuilderState(knobListenerService, logger) }
            val mainListenerEnabled = knobListenerService.listenerEnabled.collectAsState(true)
            LaunchedEffect(mainListenerEnabled.value) {
                knobState.subscribeEvents(tag)
            }
            return knobState
        }

        // TODO this might be easily "double-enabled" with duplicated emissions based
        // TODO On how the LaunchedEffect is setup.
        @Composable
        fun setupEnableableListener(
            knobListenerService: KnobListenerService,
            isEnabled : Boolean,
            logger: Logger,
            tag: String?,
        ) : KnobObserverBuilderState {
            val knobState = remember(Unit) { KnobObserverBuilderState(knobListenerService, logger) }
            val mainListenerEnabled = knobListenerService.listenerEnabled.collectAsState(true)
            LaunchedEffect(mainListenerEnabled.value, isEnabled) {
                if (isEnabled) {
                    logger.d(tag ?: "enableableListener", "Calling subscribe events!! " +
                            "mainListenerEnabled: ${mainListenerEnabled.value} " +
                            "isEnabled $isEnabled")
                    knobState.subscribeEvents(tag)
                }
            }
            return knobState
        }
    }
}



