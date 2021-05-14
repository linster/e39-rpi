package ca.stefanm.ibus.gui.debug

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

class ServiceStatusViewer @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger
) {

    fun showWindow() {

        Window(
            title = "Service Status",
        ) {
            val list = configurablePlatform.servicesRunning.collectAsState(GlobalScope.coroutineContext)

            logger.d("VIEWER", list.value.toString())
            ServiceStatusList(list.value)
//            list.value.forEach {
//                Row {
//                    ServiceGroup(it)
//                    Spacer(Modifier.height(10.dp))
//                }
//            }
        }
    }

    @Composable
    fun ScrollableStatusList(
        content : @Composable () -> Unit
    ) {

    }

    @Composable
    fun ServiceStatusList(
        list : List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>
    ) {
        Column {
            list.forEach {
                ServiceGroup(it)
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    @Composable
    fun ServiceGroup(
        group : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup
    ) {
        Column(Modifier.background(Color.LightGray).fillMaxWidth().wrapContentHeight().padding(10.dp)){
            Text("Name: ${group.name}")
            Text("Description: ${group.description}")
            Column(Modifier.padding(start = 32.dp)) {
                group.children.forEach { Service(it) }
            }
        }
    }

    @Composable
    fun Service(
        service : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordService
    ) {
        val status = service.runStatus.collectAsState(PlatformService.RunStatus.STOPPED)
        Column(Modifier.padding(10.dp)) {
            Text("Name: ${service.name}")
            Text("Description: ${service.description}")
            Spacer(Modifier.height(5.dp))
            Row {
                Text("Status: ${status.value}")
                Spacer(Modifier.width(32.dp))
                Button(onClick = {service.startService()}) { Text("Start")}
                Button(onClick = {service.stopService()}) { Text("Stop")}
            }
        }
    }
}