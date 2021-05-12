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
import javax.inject.Inject

fun main() {
    GuiMain().main()
}

class GuiMain {


    @Inject
    lateinit var loadingWindow: LoadingWindow

    @Inject
    lateinit var keyEventSimulator: KeyEventSimulator

    @Inject
    lateinit var mapDebug: MapDebug

    @Inject
    lateinit var menuDebug: MenuDebug

    init {
        DaggerApplicationComponent.builder().build().inject(this)

        AppManager.setEvents(onAppStart = {
//            platform.run()
        })
    }

    fun main() {
        Window(title = "Compose for Desktop", size = IntSize(300, 300)) {
            val count = remember { mutableStateOf(0) }
            MaterialTheme {
                Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            count.value++
                        }) {
                        Text(if (count.value == 0) "Hello World" else "Clicked ${count.value}!")
                    }
                    Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            count.value = 0
                        }) {
                        Text("Reset")
                    }
                    Button(
                        onClick = {
                            keyEventSimulator.show()
                        }
                    ) {
                        Text("Simulator")
                    }

                    Button(
                        onClick = {
                            loadingWindow.show()
                        }
                    ) {
                        Text("Loading Window")
                    }

                    Button(onClick = { mapDebug.show() }) {
                        Text("Map Debug")
                    }

                    Button(onClick = { menuDebug.show()}) {
                        Text("Menu Debug")
                    }
                }
            }
        }
    }
}