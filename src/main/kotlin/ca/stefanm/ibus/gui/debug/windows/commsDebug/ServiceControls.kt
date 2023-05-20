package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.car.platform.SerialInterfaceServiceDebugGroup
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import kotlinx.coroutines.GlobalScope


@Composable
fun HorizontalServiceControlStrip(
    modifier: Modifier,
    configurablePlatform: ConfigurablePlatform
) {
    val servicesRunning = configurablePlatform.servicesRunning.collectAsState(GlobalScope.coroutineContext)

    val serialListenerService = servicesRunning.value
        .firstOrNull { it.name == "SerialInterface"}
        ?.children?.firstOrNull { it.name == "SerialListenerService" }
    val serialPublisherService = servicesRunning.value
        .firstOrNull { it.name == "SerialInterface"}
        ?.children?.firstOrNull { it.name == "SerialPublisherService" }

    val serialListenerDebugService = servicesRunning.value
        .firstOrNull { it.name == "SerialInterfaceDebug"}
        ?.children?.firstOrNull { it.name == "SerialListenerDebugService" }
    val serialWriterDebugService = servicesRunning.value
        .firstOrNull { it.name == "SerialInterfaceDebug"}
        ?.children?.firstOrNull { it.name == "SerialWriterDebugService" }

    val syntheticIBusInputEventDebugLoggerService = servicesRunning.value
        .firstOrNull { it.name == "SerialInterfaceDebug"}
        ?.children?.firstOrNull { it.name == "SyntheticIBusInputEventDebugLoggerService" }

    val services = listOf(
        serialListenerService,
        serialPublisherService,
        serialListenerDebugService,
        serialWriterDebugService,
        syntheticIBusInputEventDebugLoggerService
    )

    Row(modifier, horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
        if (services.any { it == null }) {
            Text("Platform is not running?")
        } else {
            Column {
                services.forEach { CheckboxForService(it!!) }
            }
        }
    }
}

@Composable
private fun CheckboxForService(
    service : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordService
) {
    NestingCard {
        Row {
            Text("Name: ${service.name}")
            Text("RunStatus: ${service.runStatus.collectAsState(PlatformService.RunStatus.ZOMBIE).value}")
            Button(onClick = service.startService) { Text("Start") }
            Button(onClick = service.stopService) { Text("Stop") }
        }
    }
}