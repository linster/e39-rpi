package ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import kotlinx.coroutines.flow.map

@Composable
fun PlatformControls(
    configurablePlatform: ConfigurablePlatform
) {
    NestingCard {
        NestingCardHeader("Platform Controls")
        val isRunning = configurablePlatform.servicesRunning.map { it.isNotEmpty() }.collectAsState(false)
        Text("Is Running: ${isRunning.value}")
        Row {
            Button(onClick = { configurablePlatform.stop() }) { Text("Stop Platform") }
            Button(onClick = {
                configurablePlatform.run()
            }) { Text("Start Platform") }
        }
    }
}