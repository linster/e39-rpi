package ca.stefanm.ibus.gui.platformConfig

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject

class WindowManagerConfigSetupWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage
) : WindowManager.E39Window {
    override val title: String
        get() = "Window manager Config Setup Window"
    override val size: DpSize = DpSize(1024.dp, 768.dp)
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        Text("WindowManagerConfig")
    }

}