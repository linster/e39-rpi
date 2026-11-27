package ca.stefanm.ibus.gui.crash

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DefaultWindowExceptionHandlerFactory
import ca.stefanm.ibus.gui.crash.HmiCrashHandler.CrashedWindow
import ca.stefanm.ibus.gui.crash.HmiCrashHandler.Then
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Rectangle

class HmiCrashHandlerTest {

    private object Silent : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    private class Screen : NavigationNode<Unit> {
        override val thisClass: Class<out NavigationNode<Unit>> get() = javaClass
        override fun provideMainContent(): @Composable (Navigator.IncomingResult?) -> Unit = {}
    }

    private val root = Screen()
    private val navigator = Navigator(root, Silent)
    private var now = 1_000_000L
    private val exits = mutableListOf<Int>()

    private fun handler(enabled: Boolean = true) = HmiCrashHandler(
        { navigator }, Silent, enabled = { enabled }, countdown = { 10 }, clock = { now }, exit = { exits += it },
    )

    private val bounds = Rectangle(10, 20, 800, 468)

    @Test
    fun aCrashReopensTheHmiOnTheRootScreen() {
        val h = handler()
        navigator.navigateToNode(Screen())
        h.onCrash(CrashedWindow.HMI, IllegalStateException("boom"), bounds)

        val crash = h.crash.value!!
        assertEquals(Then.REOPEN_HMI, crash.then)
        assertEquals(bounds, crash.bounds)
        assertEquals("boom", crash.throwable.message)

        h.finish()
        assertNull(h.crash.value)
        assertSame(root, navigator.mainContentScreen.value.node)
        assertEquals(emptyList<Int>(), exits)
    }

    @Test
    fun aCrashSoonAfterReopeningExits() {
        val h = handler()
        h.onCrash(CrashedWindow.HMI, IllegalStateException("one"), bounds)
        h.finish()

        now += HmiCrashHandler.REPEAT_MS - 1
        h.onCrash(CrashedWindow.HMI, IllegalStateException("two"), bounds)
        assertEquals(Then.EXIT, h.crash.value!!.then)
        h.finish()
        assertEquals(listOf(HmiCrashHandler.EXIT_CODE), exits)
    }

    @Test
    fun aCrashLongAfterReopeningReopensAgain() {
        val h = handler()
        h.onCrash(CrashedWindow.HMI, IllegalStateException("one"), bounds)
        h.finish()

        now += HmiCrashHandler.REPEAT_MS
        h.onCrash(CrashedWindow.HMI, IllegalStateException("two"), bounds)
        assertEquals(Then.REOPEN_HMI, h.crash.value!!.then)
    }

    @Test
    fun theLoadingWindowCrashingExits() {
        val h = handler()
        h.onCrash(CrashedWindow.LOADING, IllegalStateException("boom"), bounds)
        assertEquals(Then.EXIT, h.crash.value!!.then)
        h.finish()
        assertEquals(listOf(HmiCrashHandler.EXIT_CODE), exits)
    }

    @Test
    fun theSameWindowThrowingAgainIsTheSameCrash() {
        val h = handler()
        h.onCrash(CrashedWindow.HMI, IllegalStateException("first"), bounds)
        h.onCrash(CrashedWindow.HMI, IllegalStateException("next frame"), bounds)
        val crash = h.crash.value!!
        assertEquals("first", crash.throwable.message)
        assertEquals(Then.REOPEN_HMI, crash.then)
    }

    @Test
    fun theOtherWindowCrashingTooExits() {
        val h = handler()
        h.onCrash(CrashedWindow.HMI, IllegalStateException("hmi"), bounds)
        h.onCrash(CrashedWindow.LOADING, IllegalStateException("loading"), bounds)
        val crash = h.crash.value!!
        assertEquals("hmi", crash.throwable.message)
        assertEquals(Then.EXIT, crash.then)
    }

    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Test
    fun offItIsComposesErrorDialog() {
        assertSame(DefaultWindowExceptionHandlerFactory, handler(enabled = false).exceptionHandlerFactory(CrashedWindow.HMI))
    }

    @Test
    fun theCrashButtonThrowsOnce() {
        val h = handler()
        h.throwIfRequested()
        h.requestCrash()
        assertTrue(runCatching { h.throwIfRequested() }.exceptionOrNull() is IllegalStateException)
        h.throwIfRequested()
    }
}
