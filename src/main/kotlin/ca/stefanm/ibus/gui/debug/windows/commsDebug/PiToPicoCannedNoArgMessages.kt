package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ibus.lib.messages.IBusMessage

enum class PiToPicoNoArgCannedMessageType(val message: PiToPicoMessageFactory.() -> IBusMessage) {
    HeartbeatRequest({heartbeatRequest()}),
    HeartbeatResponse({heartbeatResponse()}),

    ConfigStatusRequest({configStatusRequest()}), ///Instruct the pico to tell us what it's config object is


    PicoVideoRequestUpstream({ videoSourceRequest(PiToPicoMessageFactory.PicoVideoRequestSource.Upstream)}),
    PicoVideoRequestPico({ videoSourceRequest(PiToPicoMessageFactory.PicoVideoRequestSource.Pico) }),
    PicoVideoRequestRpi({ videoSourceRequest(PiToPicoMessageFactory.PicoVideoRequestSource.Rpi)}),
    PicoVideoRequestRVC({ videoSourceRequest(PiToPicoMessageFactory.PicoVideoRequestSource.RVC)}),

    //For test purposes to toggle the power switch for the RPI power supply.
    PicoPowerRequestOn({ piHardPowerSwitch(true) }),
    PicoPowerRequestOff({ piHardPowerSwitch(false) }),

    SimulatedIgnitionKey0({ simulateIgnition(PiToPicoMessageFactory.IgnitionPosition.POSITION_0)}),
    SimulatedIgnitionKey1({ simulateIgnition(PiToPicoMessageFactory.IgnitionPosition.POSITION_1)}),
    SimulatedIgnitionKey2({ simulateIgnition(PiToPicoMessageFactory.IgnitionPosition.POSITION_2)}),
    SimulatedIgnitionKey3({ simulateIgnition(PiToPicoMessageFactory.IgnitionPosition.POSITION_3)}),
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

