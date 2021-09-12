package ca.stefanm.ibus.gui.menu.bluetoothPairing.ui

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

//This is the screen that lets us pick devices to pair with

fun NavigationNodeTraverser.showPairableDevices(
    pairableDevices: SharedFlow<PairableDeviceChooser.PairableDevice>
) {
    //TODO we're going to pass a flow to this screen so that when the agent
    //TODO updates the list of devices we can pair to, the UI updates
    //TODO live.
}

@AutoDiscover
class PairableDeviceChooser @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<PairableDeviceChooser.PairableDeviceChooserResult> {

    data class PairableDevice(
        val address : String, //MAC Address as string
        val alias : String, //Name of device
        val isPaired : Boolean,
        val isConnected : Boolean
    )

    data class PairableDeviceChooserParameters(
        val pairableDevices : List<PairableDevice>
    )

    sealed class PairableDeviceChooserResult {
        object Cancelled : PairableDeviceChooserResult()
        data class Error(val rootCause : Throwable) : PairableDeviceChooserResult()
        data class PairedToDevice(val device: PairableDevice) : PairableDeviceChooserResult()
    }

    override val thisClass: Class<out NavigationNode<PairableDeviceChooserResult>>
        get() = PairableDeviceChooser::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        //TODO we gotta unpack the Flow<Devices> here and make it a UI state.
        it?.requestParameters

        //TODO on Menu cancel needs to return a Cancelled result.
//        ScrollMenu.OneColumnScroll(
//
//        )
    }
}