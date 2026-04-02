package ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem

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

// For use with DynamicKnobListener.... for use with SmoothScroll
// Who even names this stuff?
fun List<TextMenuItem>.toDynamicLambdas(
) : List<@Composable KnobObserverBuilderScope.(Int, Int) -> Unit> {
    return this.map {
        { allocatedIndex, currentIndex ->
            MenuItem(
                label = it.title,
                chipOrientation = ItemChipOrientation.W,
                isSelected = allocatedIndex == currentIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                    it.onClicked()
                }
            )
        }
    }
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