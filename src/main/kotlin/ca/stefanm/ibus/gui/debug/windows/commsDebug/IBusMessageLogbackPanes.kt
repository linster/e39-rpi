package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.lib.logging.StaticLogger
import ca.stefanm.ibus.lib.messages.IBusMessage
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@Composable
fun IbusMessageLogbackPane(
    ibusMessages : Flow<IbusCommsDebugMessage>
) {

    val messages = remember { mutableStateListOf<IbusCommsDebugMessage>() }


    LaunchedEffect(ibusMessages) {
        launch {
            ibusMessages.collect {
                StaticLogger.d("WAT","Adding message")
                messages.add(it)
            }
        }
    }

    val stateVertical = rememberScrollState(0)

    LaunchedEffect(messages.size) {
        stateVertical.scrollTo(stateVertical.maxValue)
    }



    NestingCard {
        Row {
            Button(onClick = { messages.clear() }) { Text("Clear Logback") }

            Text("Logback size: ${messages.size}")
        }

        Box(Modifier.fillMaxSize().verticalScroll(stateVertical).padding(16.dp)) {
            Column(Modifier.wrapContentHeight()) {
                for (message in messages) {
                    IbusMessageView(message)
                }
            }
        }
    }
}


@Composable
fun IbusMessageView(message: IbusCommsDebugMessage) {
    Column(
        modifier = Modifier.border(2.dp, Color.Black).padding(10.dp)
    ) {
        //TODO we can dress up these messages.

        when (message) {
            is IbusCommsDebugMessage.IncomingMessage.RawMessage -> {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = message.message.toString(), modifier = Modifier.padding(10.dp))
                    Text(text = "Created at: ${message.createdAt.toHttpDateString()}", modifier = Modifier)
                    Text(text = "Received at: ${message.recievedAt.toHttpDateString()}", modifier = Modifier)
                }
            }
            is IbusCommsDebugMessage.IncomingMessage.InputEventMessage -> {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = message.inputEvent.toString(), modifier = Modifier.padding(10.dp).background(Color.Green))
                    Text(text = message.message.toString(), modifier = Modifier.padding(10.dp))
                    Text(text = "Created at: ${message.createdAt.toHttpDateString()}", modifier = Modifier)
                    Text(text = "Received at: ${message.recievedAt.toHttpDateString()}", modifier = Modifier)
                }
            }
            is IbusCommsDebugMessage.OutgoingMessage.RawMessage -> {
                Text(text = message.outgoingMessage.toString(), modifier = Modifier.padding(10.dp))
            }
            else -> {}
        }
    }
}