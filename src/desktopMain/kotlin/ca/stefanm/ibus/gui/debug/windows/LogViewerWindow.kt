package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.menu.HmiLogViewerScreen
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject

class LogViewerWindow @Inject constructor(
    private val hmiLogViewerScreen: HmiLogViewerScreen
): WindowManager.E39Window {
    override val title: String = "Log Viewer"
    override val tag: Any
        get() = this
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE
    override val size = DpSize(640.dp, 480.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        hmiLogViewerScreen.provideMainContent()(null)
    }

}