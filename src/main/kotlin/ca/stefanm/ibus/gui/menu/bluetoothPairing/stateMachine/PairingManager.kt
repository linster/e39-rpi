package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus.AgentIncomingEventAdapter
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus.DBusConnectionOwner
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus.DeviceListProvider
import ca.stefanm.ibus.gui.menu.bluetoothPairing.ui.*
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@ApplicationScope
class PairingManager @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val dBusConnectionOwningComponent: DBusConnectionOwner,
    private val agentIncomingEventAdapter: AgentIncomingEventAdapter,
    private val deviceListProvider: DeviceListProvider
) {

    private var isDBusSetup = false
    private fun setupDBus() {
        if (!isDBusSetup) {
            listOf(
                dBusConnectionOwningComponent,
                agentIncomingEventAdapter,
                deviceListProvider
            ).forEach { it.onSetup() }
            isDBusSetup = true
        }
    }

    private fun cleanupDBus() {
        if (isDBusSetup) {
            listOf(
                dBusConnectionOwningComponent,
                agentIncomingEventAdapter,
                deviceListProvider
            ).asReversed().forEach { it.onCleanup() }
            isDBusSetup = false
        }
    }


    //This is the main class for pairing. It will contain a flow of an enum
    //that will specify which screen the user should be seeing.
    //We'll use an empty activity (no UI) and use the incoming results
    //to drive the state machine.




    fun onLoadEmptyBtMenu() {
        //Setup all the things
        setupDBus()
        navigationNodeTraverser.navigateToNode(MainBtMenu::class.java)
    }
    fun onRequestNavigatorRoot() {
        //Shut down all the things
        cleanupDBus()
        navigationNodeTraverser.navigateToRoot()
    }
    fun onGoToBtMainMenu() {
        //Need to clean up everything
        navigationNodeTraverser.cleanupBackStackDescendentsOf(BtEmptyMenu::class.java)
        navigationNodeTraverser.navigateToNode(MainBtMenu::class.java)
    }

    fun requestCurrentConnectedDevicesViewer() {
        //This is where we need to navigate to the current device screen with
        //info.
        navigationNodeTraverser.showCurrentDeviceList(
            deviceListProvider.getDevices()
                .map { list ->
                    list.map { device ->
                        PairableDeviceChooser.PairableDevice(
                            address = device.address,
                            alias = device.alias,
                            isPaired = device.isPaired,
                            isConnected = device.isConnected
                        )
                    }
                }
                .shareIn(GlobalScope, SharingStarted.WhileSubscribed())
        )
    }

    fun requestPairNewDevice() {

    }

    fun onUiResult(uiResult: UiResult) {
        when (uiResult) {
            CurrentDeviceChooser.CurrentDeviceChooserResult.Cancelled -> navigationNodeTraverser.goBack()
            is CurrentDeviceChooser.CurrentDeviceChooserResult.RequestViewDevice -> {
                navigationNodeTraverser.showCurrentDevice(
                    //TODO ask the device manager helper for a flow
                    //TODO of the device so we can listen to it's status while
                    //TODO we look at it.
                    deviceListProvider.getLiveDevice(uiResult.device.address)
                        .map { device ->
                            PairableDeviceChooser.PairableDevice(
                                address = device.address,
                                alias = device.alias,
                                isPaired = device.isPaired,
                                isConnected = device.isConnected
                            )
                        }
                )
            }
            PairableDeviceChooser.PairableDeviceChooserResult.Cancelled -> navigationNodeTraverser.goBack()
            is PairableDeviceChooser.PairableDeviceChooserResult.RequestPairToDevice -> TODO()
            is BluetoothPinConfirmationScreen.PinConfirmationResult -> TODO()
            CurrentDeviceViewer.ViewResult.Cancelled -> navigationNodeTraverser.goBack()
            is CurrentDeviceViewer.ViewResult.DisconnectDevice -> TODO()
            is CurrentDeviceViewer.ViewResult.ForgetDevice -> TODO()
        }
    }
}