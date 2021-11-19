package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

//This is the screen that lets us pick devices to pair with

fun NavigationNodeTraverser.showPairableDevices(
    pairableDevices: SharedFlow<List<PairableDeviceChooser.PairableDevice>>
) {
    //TODO we're going to pass a flow to this screen so that when the agent
    //TODO updates the list of devices we can pair to, the UI updates
    //TODO live.
    this.navigateToNodeWithParameters(PairableDeviceChooser::class.java,
        PairableDeviceChooser.PairableDeviceChooserParameters(
            pairableDevices = pairableDevices
        )
    )
}

@AutoDiscover
class PairableDeviceChooser @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val pairingManager: PairingManager
) : NavigationNode<PairableDeviceChooser.PairableDeviceChooserResult> {

    data class PairableDevice(
        val address : String, //MAC Address as string
        val alias : String, //Name of device
        val isPaired : Boolean,
        val isConnected : Boolean
    ) {
        companion object {
            val EMPTY = PairableDevice(
                "<INVALID>",
                "<INVALID>",
                false,
                false
            )
        }
    }

    data class PairableDeviceChooserParameters(
        val pairableDevices : SharedFlow<List<PairableDevice>>
    )

    sealed class PairableDeviceChooserResult : UiResult() {
        object Cancelled : PairableDeviceChooserResult()
        data class RequestPairToDevice(val device: PairableDevice) : PairableDeviceChooserResult()
    }

    override val thisClass: Class<out NavigationNode<PairableDeviceChooserResult>>
        get() = PairableDeviceChooser::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {

            BmwSingleLineHeader("Select Device to Pair With")

            if (it == null) {
                pairingManager.onGoToBtMainMenu()
            }

            //Unpack the Flow<Devices> here and make it a UI state.
            val pairableDevices = (it?.requestParameters as PairableDeviceChooserParameters)
                .pairableDevices
                .collectAsState(listOf())

            ScrollMenu.OneColumnScroll(
                items = pairableDevices.value.map { device ->
                    TextMenuItem(
                        title = "${device.alias} ; isConnected ${device.isConnected.toCheckBox()} ; isPaired ${device.isPaired.toCheckBox()}",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(
                                this@PairableDeviceChooser,
                                PairableDeviceChooserResult.RequestPairToDevice(device)
                            )
                        }
                    )
                },
                onScrollListExitSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this@PairableDeviceChooser, PairableDeviceChooserResult.Cancelled)
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 3,
                    isExitItemOnEveryPage = true,
                    isPageCountItemVisible = true,
                    showSpacerRow = false
                )
            )
        }
    }
}