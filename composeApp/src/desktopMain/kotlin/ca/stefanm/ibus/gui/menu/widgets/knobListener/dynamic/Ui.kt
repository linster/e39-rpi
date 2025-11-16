package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
fun KnobObserverBuilder(
    state: KnobObserverBuilderState,
    contents: @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit
) {


    val allocatedIndex = remember(Unit) { state.allocateIndex() }

    DisposableEffect(Unit) {
        onDispose {
            state.deallocateIndex(allocatedIndex)
        }
    }

    contents.invoke(
        state.getScope(),
        allocatedIndex,
        state.getCurrentSelectedItem().collectAsState(0).value
    )

}


interface KnobObserverBuilderScope {
    fun getState(): KnobObserverBuilderState

    @Composable
    fun CallWhen(currentIndexIs: Int, call: () -> Unit): () -> Unit {
        DisposableEffect(currentIndexIs) {
            getState().registerCallback(currentIndexIs, call)
            onDispose {
                getState().unregisterCallback(currentIndexIs)
            }
        }

        return call
    }
}