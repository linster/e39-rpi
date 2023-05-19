package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.ServiceStatusViewer
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

class PicoCommsDebugWindow @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Pico Comms"
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(800.dp, 1024.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row {
            Column {
                PicoLogViewerWindow()
            }
            Column {
                MessageSendCard()
            }
        }
    }

    @Composable
    fun MessageSendCard() {
        NestingCard {
            NestingCardHeader("Send Pico Message")

        }
    }

    @Composable
    fun PicoLogViewerWindow() {

    }


}