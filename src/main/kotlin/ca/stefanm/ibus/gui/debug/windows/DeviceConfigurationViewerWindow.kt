package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.desktop.Window
//import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import javax.inject.Inject

class DeviceConfigurationViewerWindow @Inject constructor() {

    fun show(deviceConfiguration: CarPlatformConfiguration?) {
        Window(
            title = "Device Configuration Viewer",
            size = IntSize(300, 600),
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
//                scrollState = stateVertical
            ) {
                Column {
                    deviceConfiguration.parseConfiguration().forEach {
                        it.widget()
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }

    }

    data class ConfigurationItem(
        val fieldName : String,
        val fieldValue : String
    ) {
        @Composable
        fun widget() {
            Box(
                modifier = Modifier.background(Color.LightGray)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = fieldName)
                    Text(text = fieldValue)
                }
            }
        }
    }

    private fun CarPlatformConfiguration?.parseConfiguration() : List<ConfigurationItem> {
        if (this == null) return listOf()
        return listOf(
            ConfigurationItem("isPi", isPi.toString()),
            ConfigurationItem("iBusInterfaceUri", iBusInterfaceUri),
            ConfigurationItem("displayDriver", displayDriver.toString()),
            ConfigurationItem("pairedPhone", pairedPhone.toString()),
            ConfigurationItem("serialPortReadMode", serialPortReadMode.toString()),
            ConfigurationItem("serialPortWriteMode", serialPortWriteMode.toString()),
            ConfigurationItem("trackInfoPrinter", trackInfoPrinter.toString())
        )
    }
}