package ca.stefanm.ibus.gui.menu.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

fun NavigationNodeTraverser.showCurrentDevice(
    pairableDevice: Flow<PairableDeviceChooser.PairableDevice>
) {
    this.navigateToNodeWithParameters(
        CurrentDeviceViewer::class.java,
        CurrentDeviceViewer.CurrentDeviceViewerParameters(
            pairableDevice
        )
    )
}

@AutoDiscover
class CurrentDeviceViewer @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val pairingManager: PairingManager
) : NavigationNode<CurrentDeviceViewer.ViewResult> {

    class CurrentDeviceViewerParameters(
        val device : Flow<PairableDeviceChooser.PairableDevice>
    )

    sealed class ViewResult : UiResult() {
        object Cancelled : ViewResult()
        class DisconnectDevice(val device: PairableDeviceChooser.PairableDevice) : ViewResult()
        class ForgetDevice(val device : PairableDeviceChooser.PairableDevice) : ViewResult()
    }

    override val thisClass: Class<out NavigationNode<ViewResult>>
        get() = CurrentDeviceViewer::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ {
        Column {

            val currentDevice = (it?.requestParameters as? CurrentDeviceViewerParameters)
                ?.device
                ?.collectAsState(
                    PairableDeviceChooser.PairableDevice.EMPTY
                ) ?: return@content

            FullScreenPrompts.OptionPrompt(
                header = "Current Device",
                options = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = {
                            pairingManager.onUiResult(ViewResult.Cancelled)
                        }),
                    TextMenuItem(
                        "Disconnect",
                        onClicked = {
                            pairingManager.onUiResult(ViewResult.DisconnectDevice(currentDevice.value))
                        }
                    ),
                    TextMenuItem(
                        "Forget",
                        onClicked = {
                            pairingManager.onUiResult(ViewResult.ForgetDevice(currentDevice.value))
                        }
                    )
                )
            ) {
                Column(
                    Modifier.background(ChipItemColors.MenuBackground)
                ) {
                    Text("", color = Color.White, fontSize = 28.sp)
                    Text("Alias: ${currentDevice.value.alias}", color = Color.White, fontSize = 28.sp)
                    Text("Paired: ${currentDevice.value.isPaired.toCheckBox()}", color = Color.White, fontSize = 28.sp)
                    Text("Connected: ${currentDevice.value.isConnected.toCheckBox()}", color = Color.White, fontSize = 28.sp)
                    Text("MAC: ${currentDevice.value.address}", color = Color.White, fontSize = 28.sp)
                }
            }

        }
    }
}