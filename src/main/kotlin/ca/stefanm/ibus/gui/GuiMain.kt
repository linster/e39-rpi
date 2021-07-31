package ca.stefanm.ibus.gui

import androidx.compose.desktop.AppManager
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
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

    init {
        DaggerApplicationComponent.builder().build().inject(this)

        AppManager.setEvents(onAppStart = {
//            platform.run()
        })
    }

    fun main() {
        windowManager.get().runApplication()
    }
}