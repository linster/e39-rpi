package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic

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
        allocatedIds.add(newId)
        return newId
    }

    fun removeId(id : AllocatedId) {
        if (true) {
            logger.d(TAG, "Removing the current selected id. Setting the current id to minimum allowed first.")
            var proposedCurrentId = allocatedIds.minOrNull()
            if (proposedCurrentId == null) {
                logger.w(TAG, "We removed an ID $id and now there is no valid id to be the current Id because allocatedIDs is empty: $allocatedIds")
                if (allocatedIds.isEmpty()) {
                    logger.w(TAG, "allocatedIDs was empty, adding an entry so that there's something for the pointer to point at.")
                    proposedCurrentId = addId()
                }
            }
            currentId.value = proposedCurrentId!!
        }
        val result = allocatedIds.remove(id)
        if (!result) {
            logger.d(TAG, "Tried to remove an allocatedId $id which wasn't allocated")
        }
    }


    private var currentId : MutableStateFlow<CurrentId> = MutableStateFlow(0)

    fun incrementPointer() {
        val currentIdIndex = allocatedIds.indexOf(currentId.value)
        currentId.value = allocatedIds[currentIdIndex + 1]
    }

    fun decrementPointer() {
        val currentIdIndex = allocatedIds.indexOf(currentId.value)
        currentId.value = allocatedIds[currentIdIndex - 1]
    }

    fun getCurrentId() : StateFlow<CurrentId> {
        return currentId.asStateFlow()
    }

    fun hasAllocatedId(allocatedId: AllocatedId) : Boolean = allocatedIds.contains(allocatedId)

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


    fun getCurrentSelectedItem(): StateFlow<Int> = allocatedIdManager.getCurrentId()

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

    suspend fun subscribeEvents() {
        logger.d("WAT", "subscribeEvents hash ${this.hashCode()}")
        service.knobTurnEvents()
            .map { event ->
                logger.d(TAG, "got an event in subscribeEvnents: $event")
                when (event) {
                    is InputEvent.NavKnobPressed -> {
                        logger.d(TAG, "NavKnobPressed on callback with getCurrentSelectedItem(): ${getCurrentSelectedItem().value}")
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
            }.collect()
    }


    fun getScope(): KnobObserverBuilderScope {
        return object : KnobObserverBuilderScope {
            override fun getState() = this@KnobObserverBuilderState
        }
    }

    companion object {
        const val TAG = "KnobListenerService"
    }
}



