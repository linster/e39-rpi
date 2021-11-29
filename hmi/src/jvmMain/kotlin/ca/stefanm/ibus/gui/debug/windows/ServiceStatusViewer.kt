package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.logger.Logger
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

class ServiceStatusViewer @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Service Status"
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(800.dp, 1024.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        val list = configurablePlatform.servicesRunning.collectAsState(GlobalScope.coroutineContext)

        logger.d("VIEWER", list.value.toString())

        ScrollableStatusList(list.value)
    }

    @Composable
    fun ScrollableStatusList(
        list : List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
                .background(color = Color(180, 180, 180))
                .padding(10.dp)
        ) {
            val stateVertical = rememberScrollState(0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .padding(end = 12.dp, bottom = 12.dp)
            ) {
                ServiceStatusList(list)


            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
        }
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