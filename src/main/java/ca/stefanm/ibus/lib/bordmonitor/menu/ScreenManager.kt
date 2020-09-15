package ca.stefanm.ibus.lib.bordmonitor.menu

import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.ScreenPainter
import ca.stefanm.ibus.lib.platform.IBusInputEventListenerService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ScreenManager(
    private val iBusInputMessageParser: IBusInputMessageParser,
    /* The screen we show when entering our menu system */
    private val entryPointScreen : Screen,
    private val screenPainter: ScreenPainter,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), IBusInputEventListenerService {

    override fun onCreate() {
        iBusInputMessageParser.addMailbox(this)
        super.onCreate()
    }

    override fun onShutdown() {
        super.onShutdown()
        iBusInputMessageParser.removeMailbox(this)
    }

    override val incomingIbusInputEvents: Channel<InputEvent> = Channel(capacity = Channel.UNLIMITED)

    override suspend fun doWork() {
        incomingIbusInputEvents.consumeEach {
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