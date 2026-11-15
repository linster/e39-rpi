package ca.stefanm.ibus.gui

import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.testprobe.TestProbe
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    GuiMain().main()
}

@ExperimentalTime
class GuiMain {

    @Inject
    lateinit var windowManager: Provider<WindowManager>

    @Inject
    lateinit var testProbe: Provider<TestProbe>

    init {
        DaggerApplicationComponent.builder().build().inject(this)
    }

    fun main() {
        // UI tests (uitest/): only when E39_TEST_PROBE names a port.
        TestProbe.portFromEnvironment()?.let { testProbe.get().start(it) }
        windowManager.get().runApplication()
    }
}