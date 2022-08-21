package ca.stefanm.ca.stefanm.ibus.gui.map.guidance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.map.Kanata_path
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject

class GuidanceDebugWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val guidanceService: GuidanceService,
    private val guidanceSessionStorage: GuidanceSessionStorage
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(1200.dp, 1200.dp)
    override val title = "HMI Navigator Debug"

    //Have a way to set the guidance state,
    //and to make fake guidance states.

    //The guidance state will also hold the route instructions.


    //buttons to do stuff on guidance service.
    //start, stop guidance
    //calculate route

    //Enable/disable Instruction consumers

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row {
            Column {
                ServiceOperations()
                InMemorySession()

            }
            Column {
                PersistedSession()
            }
        }
    }

    @Composable
    fun ServiceOperations() {
        NestingCard {
            NestingCardHeader("Setup Route")
            Button(onClick = {
                guidanceService.setStartPoint(Kanata_path.first())
            }) { Text("Set Start (Kanata_Path)")}
            Button(onClick = {
                guidanceService.setEndPoint(Kanata_path.last())
            }) { Text("Set End (Kanata_Path)")}
        }
        NestingCard {
            NestingCardHeader("Service Operations")
            Button(onClick = { guidanceService.calculateRoute()}) { Text("Calculate Route")}
            Button(onClick = { guidanceService.startGuidance()}) { Text("Start Guidance")}
            Button(onClick = { guidanceService.stopGuidance()}) { Text("Stop Guidance")}
        }

    }

    @Composable
    fun PersistedSession() {
        NestingCard {
            NestingCardHeader("Persisted Session")
            NestingCard {
                NestingCardHeader("Storage Operations")
                Button(onClick = { guidanceSessionStorage.updateWithCurrent() }) { Text("Update with current")}
            }
            NestingCard {
                val currentSessionInst = remember { mutableStateOf<GuidanceSession?>(null) }
                Text("Inst guidance session: ${currentSessionInst.value}")
                Button(onClick = {
                    currentSessionInst.value = guidanceSessionStorage.currentSession
                }) { Text("Re-read inst guidance session")}
            }
            NestingCard {
                NestingCardHeader("Subscribed storage")
                val currentSession = guidanceSessionStorage.getCurrentSessionFlow().collectAsState(null)
                Text(currentSession.value.toString())
            }
        }
    }

    @Composable
    fun InMemorySession() {
        NestingCard {
            NestingCardHeader("In-Memory Session")
        }
    }

    @Composable
    fun SessionView(guidanceSession: GuidanceSession) {
        //A view to make guidance sessions pretty. Borrow from the HMI navigator debug backstack viewer.
    }
}