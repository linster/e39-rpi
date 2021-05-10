package ca.stefanm.ibus.car.bordmonitor.menu

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.menu.painter.ScreenPainter
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarModuleScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationModule.Companion.INPUT_EVENTS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Named

@ConfiguredCarModuleScope
class ScreenManager(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

//    private val iBusInputMessageParser: IBusInputMessageParser,
    /* The screen we show when entering our menu system */
    private val entryPointScreen : Screen,
    private val screenPainter: ScreenPainter,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        inputEvents.collect {
            processInputEvent(it)
        }
    }

    private fun processInputEvent(event : InputEvent) {
        val currentScreen = screenStack.peekOrNull() ?: return

        when (event) {
            is InputEvent.NavKnobTurned -> {
                when (event.direction) {
                    InputEvent.NavKnobTurned.Direction.LEFT ->
                        currentScreen.onKnobLeft(event.clicks)
                    InputEvent.NavKnobTurned.Direction.RIGHT ->
                        currentScreen.onKnobRight(event.clicks)
                }
            }
            InputEvent.NavKnobPressed -> {
                currentScreen.onKnobClick()
            }
            InputEvent.BMBTMenuPressed -> {
                clearBackStack()
            }
            InputEvent.BMBTPhonePressed -> {
                clearBackStack(entryPointScreen)
            }
        }
    }

    private val screenStack : Stack<Screen> =
        Stack()
    private fun <T> Stack<T>.peekOrNull() : T?
            = if (this.empty()) { null } else { this.peek() }


    fun showScreen(screen: Screen) {
        screenStack.add(screen)
        screenPainter.paint(screen)
    }

    fun goBack() {
        screenStack.pop()
        screenStack.peekOrNull()?.let { screenPainter.paint(it) }
    }

    fun clearBackStack(newScreen: Screen? = null) {
        screenStack.clear()
        if (newScreen != null) {
            showScreen(newScreen)
        }
    }
}