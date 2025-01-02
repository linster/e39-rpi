package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

fun NavigationNodeTraverser.showCurrentDeviceList(
    currentDevices : SharedFlow<List<PairableDeviceChooser.PairableDevice>>
) {
    //TODO we're going to pass a flow to this screen so that when the agent
    //TODO updates the list of devices we can pair to, the UI updates
    //TODO live.
    this.navigateToNodeWithParameters(CurrentDeviceChooser::class.java,
        CurrentDeviceChooser.CurrentDeviceChooserParameters(
            currentDevices = currentDevices
        )
    )
}

@AutoDiscover
class CurrentDeviceChooser @Inject constructor(
    private val pairingManager: PairingManager
) : NavigationNode<CurrentDeviceChooser.CurrentDeviceChooserResult> {

    data class CurrentDeviceChooserParameters(
        val currentDevices : SharedFlow<List<PairableDeviceChooser.PairableDevice>>
    )

    sealed class CurrentDeviceChooserResult : UiResult(){
        object Cancelled : CurrentDeviceChooserResult()
        data class RequestViewDevice(val device: PairableDeviceChooser.PairableDevice) : CurrentDeviceChooserResult()
    }

    override val thisClass: Class<out NavigationNode<CurrentDeviceChooserResult>>
        get() = CurrentDeviceChooser::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {

            BmwSingleLineHeader("Select Device to Inspect")

            //Unpack the Flow<Devices> here and make it a UI state.
            val pairableDevices = (it?.requestParameters as CurrentDeviceChooserParameters)
                .currentDevices
                .collectAsState(listOf())

            ScrollMenu.OneColumnScroll(
                items = pairableDevices.value.map { device ->
                    TextMenuItem(
                        title =
                            "${device.alias} ; isConnected ${device.isConnected.toCheckBox()} ; isPaired ${device.isPaired.toCheckBox()}",
                        onClicked = {
                            pairingManager.onUiResult(CurrentDeviceChooserResult.RequestViewDevice(device))
                        }
                    )
                },
                onScrollListExitSelected = {
                    pairingManager.onUiResult(CurrentDeviceChooserResult.Cancelled)
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