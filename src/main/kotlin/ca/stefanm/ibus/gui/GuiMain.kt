package ca.stefanm.ibus.gui

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.debug.KeyEventSimulator
import ca.stefanm.ibus.gui.debug.MenuDebug
import ca.stefanm.ibus.gui.map.MapDebug
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