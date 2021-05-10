package ca.stefanm.ibus.gui.menu.widgets

import androidx.compose.desktop.AppManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.lib.logging.Logger
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@ExperimentalCoroutinesApi
class ScrollListener @Inject constructor(
//    @Named(ConfiguredCarModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

    maxIndex : Int
) {

    //TODO make a bridge between modules for this kinda stuff.
    private val inputEvents : SharedFlow<InputEvent> = MutableSharedFlow()

    private var _currentSelectedIndex = mutableStateOf(0)
    val currentSelectedIndex : State<Int> = _currentSelectedIndex

    private val validIndices = (0..maxIndex).toList().circular()

    private val _isClickProcessing = MutableValue(false)

    init {
        GlobalScope.launch {
            listenForKnob()
        }
    }

    private suspend fun listenForKnob() {
        println("KNOB SCROLL : INIT")
        inputEvents.collect {
            println("KNOB SCROLL : $it")
            if (it == InputEvent.NavKnobPressed) {
                _isClickProcessing.value = true
                actions.getOrDefault(_currentSelectedIndex.value, {}).invoke()
                _isClickProcessing.value = false
            }

            if (it is InputEvent.NavKnobTurned) {
                //https://github.com/tginsberg/cirkle
                when (it.direction) {
                    InputEvent.NavKnobTurned.Direction.LEFT -> {
                        _currentSelectedIndex.value = validIndices[_currentSelectedIndex.value - it.clicks]
                    }
                    InputEvent.NavKnobTurned.Direction.RIGHT -> {
                        println("KNOB SCROLL : OLD :${currentSelectedIndex.value}")
                        _currentSelectedIndex.value = validIndices[_currentSelectedIndex.value + it.clicks]
                        println("KNOB SCROLL : NEW :${currentSelectedIndex.value}")
                    }
                }
            }
        }
    }

    private val actions = mutableMapOf<Int, ScrollListenerOnClickListener>()

    fun ScrollListenerOnClick(id : Int, action : () -> Unit = {}) : ScrollListenerOnClickListener {
        if (!actions.containsKey(id)) {
            actions[id] = action
        }
        return action
    }
}

typealias ScrollListenerOnClickListener = () -> Unit

//TODO in a ChipMenu, set the isSelected = scrollListener.currentSelectedIndex == <some number>
//TODO then that will set the border color correctly.

//TODO ScrollListenerOnClick(id = 4) { /* do stuff here */ }

//TODO foo(isSelected = scrollListener.currentSelectedIndex == 4, onClicked = ScrollListenerOnClick(4) { /* stuff */})
//TODO then inside a widget, take the onClicked, and jam it in a Modifier.onClick.


class MenuKnobListenerService @Inject constructor(
    private val logger: Logger,
//    @Named(ConfiguredCarModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent?>,
) {

    fun toScrollListener(maxIndex : Int) : ScrollListener {
        TODO()
//        return ScrollListener(
//            inputEvents,
//            maxIndex
//        )
    }
}