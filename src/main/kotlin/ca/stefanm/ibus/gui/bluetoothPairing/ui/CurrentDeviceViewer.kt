package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

fun NavigationNodeTraverser.showCurrentDevice(
    currentDevice: Flow<CurrentDeviceViewer.CurrentDevice>
) {
    this.navigateToNodeWithParameters(
        CurrentDeviceViewer::class.java,
        CurrentDeviceViewer.CurrentDeviceViewerParameters(
            currentDevice
        )
    )
}

@AutoDiscover
class CurrentDeviceViewer @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val pairingManager: PairingManager
) : NavigationNode<CurrentDeviceViewer.ViewResult> {

    data class CurrentDevice(
        val address : String, //MAC Address as string
        val alias : String, //Name of device
        val isPaired : Boolean,
        val isConnected : Boolean
    ) {
        companion object {
            val EMPTY = CurrentDevice(
                "<INVALID>",
                "<INVALID>",
                false,
                false
            )
        }
    }

    class CurrentDeviceViewerParameters(
        val device : Flow<CurrentDevice>
    )

    sealed class ViewResult : UiResult() {
        object Cancelled : ViewResult()
        class ConnectDevice(val device : CurrentDevice) : ViewResult()
        class DisconnectDevice(val device: CurrentDevice) : ViewResult()
        class ForgetDevice(val device : CurrentDevice) : ViewResult()
        class SetDeviceForCarPlatform(val device : CurrentDevice) : ViewResult()
    }

    override val thisClass: Class<out NavigationNode<ViewResult>>
        get() = CurrentDeviceViewer::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ {
        Column {

            val currentDevice = (it?.requestParameters as? CurrentDeviceViewerParameters)
                ?.device
                ?.collectAsState(
                    CurrentDevice.EMPTY
                ) ?: return@content

            Column(
                Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BmwSingleLineHeader("Current Device")

                //Contents
                Box(
                    Modifier.wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                        .weight(0.8F)
                        .fillMaxWidth(0.8F)
                ) {
                    Column(
                        Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    ) {
                        Text("Alias: ${currentDevice.value.alias}", color = Color.White, fontSize = 28.sp.halveIfNotPixelDoubled())
                        Text("Paired: ${currentDevice.value.isPaired.toCheckBox()}", color = Color.White, fontSize = 28.sp.halveIfNotPixelDoubled())
                        Text("Connected: ${currentDevice.value.isConnected.toCheckBox()}", color = Color.White, fontSize = 28.sp.halveIfNotPixelDoubled())
                        Text("MAC: ${currentDevice.value.address}", color = Color.White, fontSize = 28.sp.halveIfNotPixelDoubled())
                    }
                }

                HalfScreenMenu.BottomHalfTwoColumn(
                    leftItems = listOf(
                        TextMenuItem(
                            title = "Connect",
                            onClicked = {
                                pairingManager.onUiResult(ViewResult.ConnectDevice(currentDevice.value))
                            }
                        ),
                        TextMenuItem(
                            title = "Use device for BMBT",
                            onClicked = {
                                pairingManager.onUiResult(ViewResult.SetDeviceForCarPlatform(currentDevice.value))
                            }
                        )
                    ),
                    rightItems = listOf(
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
                )
            }
        }
    }
}