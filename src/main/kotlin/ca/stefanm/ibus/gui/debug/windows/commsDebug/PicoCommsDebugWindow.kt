package ca.stefanm.ca.stefanm.ibus.gui.debug.windows.commsDebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.ServiceStatusViewer
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.hardwareDrivers.SunroofOpener
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class PicoCommsDebugWindow @Inject constructor(
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger,
    private val sunroofOpener: SunroofOpener
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Pico Comms Debug"
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(1280.dp, 1024.dp)

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(0.6f)) {
                PicoLogViewerWindow()
            }
            Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.Start) {
                PlatformControls(configurablePlatform)
                MessageSendCard()
            }
        }
    }

    @Composable
    fun MessageSendCard() {
        NestingCard {
            NestingCardHeader("Send Pico Message")

            CannedMessageTypes(modifier = Modifier, onMessageTypeSelected = {})

            SendTestMessages()
        }
    }

    @Composable
    fun PicoLogViewerWindow() {
        HorizontalServiceControlStrip(modifier = Modifier, configurablePlatform)

        Text("commsDebugChannel: ${commsDebugChannel.hashCode()}")
        IbusMessageLogbackPane(commsDebugChannel)
    }

    @Composable
    fun SendTestMessages() {
        NestingCard {
            NestingCardHeader("Send Test Messages")

            val scope = rememberCoroutineScope()
            Button(onClick = { scope.launch { sunroofOpener.openSunroof() } }) {
                Text("Open Sunroof")
            }
        }
    }


}