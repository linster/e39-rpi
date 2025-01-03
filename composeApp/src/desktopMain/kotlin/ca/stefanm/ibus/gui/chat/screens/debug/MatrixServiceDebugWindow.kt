package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class MatrixServiceDebugWindow @Inject constructor(
    private val logger: Logger,
    private val matrixService: MatrixService
) : WindowManager.E39Window {


    override val tag: Any
        get() = this

    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(600.dp, 400.dp)
    override val title = "Matrix Service Debug Window"

    override fun content(): @Composable WindowScope.() -> Unit = {

        //Login pane

        // start/stop service

        // the logout/clear buttons.
    }
}