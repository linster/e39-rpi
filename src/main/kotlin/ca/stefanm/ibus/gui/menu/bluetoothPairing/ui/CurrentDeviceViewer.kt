package ca.stefanm.ibus.gui.menu.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import javax.inject.Inject

fun NavigationNodeTraverser.showCurrentDevice(
    pairableDevice: PairableDeviceChooser.PairableDevice
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
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<CurrentDeviceViewer.ViewResult> {

    class CurrentDeviceViewerParameters(
        val device : PairableDeviceChooser.PairableDevice
    )

    sealed class ViewResult {
        object Cancelled : ViewResult()
        class DisconnectDevice(val device: PairableDeviceChooser.PairableDevice) : ViewResult()
        class ForgetDevice(val device : PairableDeviceChooser.PairableDevice) : ViewResult()
    }

    override val thisClass: Class<out NavigationNode<ViewResult>>
        get() = CurrentDeviceViewer::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ {
        Column {

            val currentDevice = (it?.requestParameters as? CurrentDeviceViewerParameters)?.device ?: return@content

            FullScreenPrompts.OptionPrompt(
                header = "Current Device",
                options = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(this@CurrentDeviceViewer,
                                ViewResult.Cancelled
                            )
                        }),
                    TextMenuItem(
                        "Disconnect",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(this@CurrentDeviceViewer,
                                ViewResult.DisconnectDevice(currentDevice)
                            )
                        }
                    ),
                    TextMenuItem(
                        "Forget",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(this@CurrentDeviceViewer,
                                ViewResult.ForgetDevice(currentDevice)
                            )
                        }
                    )
                )
            ) {
                Column(
                    Modifier.background(ChipItemColors.MenuBackground)
                ) {
                    Text("", color = Color.White, fontSize = 28.sp)
                    Text("Alias: ${currentDevice.alias}", color = Color.White, fontSize = 28.sp)
                    Text("Paired: ${currentDevice.isPaired.toCheckBox()}", color = Color.White, fontSize = 28.sp)
                    Text("Connected: ${currentDevice.isConnected.toCheckBox()}", color = Color.White, fontSize = 28.sp)
                    Text("MAC: ${currentDevice.address}", color = Color.White, fontSize = 28.sp)
                }
            }

        }
    }
}