package ca.stefanm.ibus.lib.bordmonitor.menu

import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.ScreenPainter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ScreenManager(
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEvents : Channel<InputEvent>,
    /* The screen we show when entering our menu system */
    private val entryPointScreen : Screen,
    private val screenPainter: ScreenPainter
){

    init {
        GlobalScope.launch {
            while (true) {
                val event = inputEvents.receive()
                processInputEvent(event)
            }
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