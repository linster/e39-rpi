package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject

@ApplicationScope
class DeviceConfigurationViewerWindow @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform
) : WindowManager.E39Window {

    override val title: String = "Device Configuration Viewer"
    override val size = DpSize(300.dp, 600.dp)
    override val tag: Any get() = this
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {

        val deviceConfiguration = configurablePlatform.currentConfigurationFlow.collectAsState(null)

        Column(modifier = Modifier.fillMaxSize()) {
            Column {
                deviceConfiguration.value.parseConfiguration().forEach {
                    it.widget()
                    Spacer(modifier = Modifier.height(5.dp))
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