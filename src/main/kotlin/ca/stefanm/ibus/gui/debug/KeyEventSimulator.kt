package ca.stefanm.ibus.gui.debug

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class KeyEventSimulator @Inject constructor(
    private val logger: Logger,
    private val configurablePlatform: ConfigurablePlatform
) {

    fun show() {
        Window(
            title = "Key Event Simulator",
            size = IntSize(600, 400),
            onDismissRequest = {

            }
        ) {
            Row(modifier = Modifier.padding(5.dp)){
                Column(
                    modifier = Modifier.padding(5.dp)
                ) {
                    inputEventButton("Prev Track", InputEvent.PrevTrack)
                    inputEventButton("Next Track", InputEvent.NextTrack)
                    Spacer(Modifier.height(5.dp))
                    inputEventButton("RT Button", InputEvent.RTButton)
                    Spacer(Modifier.height(5.dp))
                    Text("BMBT")
                    inputEventButton("Menu", InputEvent.BMBTMenuPressed)
                    inputEventButton("Phone", InputEvent.BMBTPhonePressed)
                    Spacer(Modifier.height(10.dp))

                    inputEventButton("Radio Status", InputEvent.ShowRadioStatusScreen)
                }

                Column {
                    Text("Index Select")
                    Spacer(Modifier.height(5.dp))
                    Row {
                        Column {
                            inputEventButton("0", InputEvent.IndexSelectEvent(0))
                            inputEventButton("1", InputEvent.IndexSelectEvent(1))
                            inputEventButton("2", InputEvent.IndexSelectEvent(2))
                            inputEventButton("3", InputEvent.IndexSelectEvent(3))
                            inputEventButton("4", InputEvent.IndexSelectEvent(4))
                        }
                        Column {
                            inputEventButton("5", InputEvent.IndexSelectEvent(5))
                            inputEventButton("6", InputEvent.IndexSelectEvent(6))
                            inputEventButton("7", InputEvent.IndexSelectEvent(7))
                            inputEventButton("8", InputEvent.IndexSelectEvent(8))
                            inputEventButton("9", InputEvent.IndexSelectEvent(9))
                        }
                    }
                }

                Column {
                    Text("Knob")
                    Spacer(Modifier.height(5.dp))


                    val steps = remember { mutableStateOf(TextFieldValue("1")) }

                    OutlinedTextField(value = steps.value,
                        modifier = Modifier.padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = "Steps per click") },
                        placeholder = { Text(text = "1") },
                        onValueChange = {
                            steps.value = it
                        }
                    )

                    Row {
                        Button(
                            modifier = Modifier.padding(5.dp),
                            onClick = { sendInputEvent(InputEvent.NavKnobTurned(
                            direction = InputEvent.NavKnobTurned.Direction.LEFT,
                            clicks = steps.value.text.toIntOrNull() ?: 1))
                        }) { Text("<--") }
                        Button(
                            modifier = Modifier.padding(5.dp),
                            onClick = { sendInputEvent(InputEvent.NavKnobPressed)}
                        ) { Text("Click")}
                        Button(
                            modifier = Modifier.padding(5.dp),
                            onClick = { sendInputEvent(InputEvent.NavKnobTurned(
                            direction = InputEvent.NavKnobTurned.Direction.RIGHT,
                            clicks = steps.value.text.toIntOrNull() ?: 1))
                        }) { Text("-->") }
                    }
                }
            }
        }
    }

    @Composable
    private fun inputEventButton(label : String, event : InputEvent) {
        Button(
            modifier = Modifier.padding(5.dp),
            onClick = { sendInputEvent(event)}
        ) {
            Text(text = label)
        }
    }

    private fun sendInputEvent(inputEvent : InputEvent) {
        GlobalScope.launch {
            configurablePlatform
                .configuredCarComponent
                ?.ibusInputMessageParser()
                ?.debugSend(inputEvent)
            logger.d("KeyEventSimulator", "Sending event: $inputEvent")
        }
    }
}