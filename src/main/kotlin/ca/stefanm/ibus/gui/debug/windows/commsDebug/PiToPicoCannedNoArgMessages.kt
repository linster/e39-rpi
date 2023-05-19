package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

enum class PiToPicoNoArgCannedMessageType {
    HeartbeatRequest,
    HeartbeatResponse,

    ConfigStatusRequest, //Instruct the pico to tell us what it's config object is


    PicoVideoRequestUpstream, //Ask the pico to show upstream (for Back to BMW function)
    PicoVideoRequestPico, //Ask the pico to show the debug menu
    PicoVideoRequestRpi, //Ask the pico to show the RPi.
    PicoVideoRequestRVC,

    //For test purposes to toggle the power switch for the RPI power supply.
    PicoPowerRequestOn,
    PicoPowerRequestOff,
}

@Composable
fun CannedMessageTypes(
    modifier: Modifier,
    onMessageTypeSelected : (PiToPicoNoArgCannedMessageType) -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        PiToPicoNoArgCannedMessageType.values().forEach {
            Button(onClick = {
                onMessageTypeSelected(it)
            }) {
                Text(it.name)
            }
        }
    }
}

