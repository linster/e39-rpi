package ca.stefanm.ibus.gui.bluetoothPairing.stateMachine

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.dbus.DBusConnectionOwner
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.dbus.DeviceListProvider
import ca.stefanm.ibus.gui.bluetoothPairing.ui.*
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.lib.logging.Logger
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import org.bluez.Agent1
import org.bluez.AgentManager1
import org.bluez.Device1
import org.bluez.exceptions.BluezDoesNotExistException
import org.bluez.exceptions.BluezRejectedException
import org.freedesktop.DBus
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.UInt32
import javax.inject.Inject


typealias Passkey = UInt32
typealias Entered = UInt16

@ApplicationScope
class PairingManager @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val dBusConnectionOwningComponent: DBusConnectionOwner,
    private val deviceListProvider: DeviceListProvider,
    private val logger: Logger
) {

    private companion object {
        const val TAG = "PairingManager"
        const val agentPath = "/pairing/agent"
    }

    private val pairingManagerScope = CoroutineScope(
        CoroutineName("PairingManagerScope") + Dispatchers.IO
    )

    private var isDBusSetup = false
    private fun setupDBus() {
        if (!isDBusSetup) {
            listOf(
                dBusConnectionOwningComponent,
                deviceListProvider
            ).forEach { it.onSetup() }
            setupAgent()
            isDBusSetup = true
        }
    }

    private fun cleanupDBus() {
        if (isDBusSetup) {
            listOf(
                dBusConnectionOwningComponent,
                deviceListProvider
            ).asReversed().forEach { it.onCleanup() }
            isDBusSetup = false
        }
    }

    private lateinit var agentManager: AgentManager1

    private fun setupAgent() {
        dBusConnectionOwningComponent.getSystemBusConnection().exportObject(agentPath, agent)

        //dBusConnectionOwningComponent.getSystemBusConnection().e
        //dBusConnectionOwningComponent.getSessionBusConnection()

        agentManager = dBusConnectionOwningComponent.getSystemBusConnection().getAgentManager() ?: error("Couldn't get the AgentManager")

        val localBus = dBusConnectionOwningComponent.getSessionBusConnection().getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java).GetNameOwner("ca.stefanm.e39")
        agentManager.RegisterAgent(
            DBusPath("" + agent.objectPath),
            "DisplayYesNo"
        )
        agentManager.RequestDefaultAgent(DBusPath(agent.objectPath))
    }

    private fun cleanupAgent() {
        logger.d(TAG, "Cleaning up agent() : $agentPath")
        try {
            agentManager.UnregisterAgent(DBusPath(agentPath))
        } catch (e : BluezDoesNotExistException) {
            logger.d(TAG, "Tried to unregister agent that didn't exist at ${agentPath}")
        }
        dBusConnectionOwningComponent.getSessionBusConnection().unExportObject(agentPath)
    }

    private fun DBusSystemConnection.getAgentManager() : AgentManager1? {
        val bluezUniqueBusId : String? = this.getRemoteObject(
            "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
        )?.GetNameOwner("org.bluez")

        return this.getRemoteObject(
            bluezUniqueBusId, //"org.bluez",
            "/org/bluez",
            AgentManager1::class.java
        )
    }

    //Keep track of which passkeys are okay to show, validate for connecting devices.
    private val passKeyMutex = Mutex()
    private val passKeyMap : MutableMap<DBusPath, Pair<Passkey, Entered>> = mutableMapOf()

    private val agent = object : Agent1 {

        override fun isRemote(): Boolean = false
        override fun getObjectPath(): String = agentPath

        override fun Release() {
            cleanupAgent()
        }

        override fun RequestPinCode(p0: DBusPath?): String {
            TODO("Not yet implemented")
        }

        override fun DisplayPinCode(p0: DBusPath?, p1: String?) {
            TODO("Not yet implemented")
        }

        override fun RequestPasskey(p0: DBusPath?): UInt32 {
            TODO("Not yet implemented")
        }

        override fun DisplayPasskey(_device: DBusPath?, _passkey: UInt32?, _entered: UInt16?) {
            logger.d(TAG, "Display PassKey: ${_passkey}, $_entered")
        }

        override fun RequestConfirmation(_device: DBusPath, p1: UInt32?) {
            logger.d("Agent1", "Request Confirmation: _device: $_device, passCode: ${p1?.toString()}")
            val device = dBusConnectionOwningComponent.getSystemBusConnection().getRemoteObject("org.bluez", _device.path) as Device1
            val btDevice = BluetoothDevice(device, dBusConnectionOwningComponent.getDeviceManager().adapter, _device.path, dBusConnectionOwningComponent.getSystemBusConnection())

            runBlocking {
                var result : Boolean? = null

                pinKeyApprove = { result = true }
                pinKeyDeny = { result = false }

                navigationNodeTraverser.navigateToNodeWithParameters(
                    BluetoothPinConfirmationScreen::class.java,
                    BluetoothPinConfirmationScreen.PinConfirmationParameters(
                        phoneName = btDevice.alias,
                        pin = p1?.toInt()?.toString(10)?.padStart(6, '0') ?: "null"
                    )
                )

                while (result == null) {
                    yield()
                }

                logger.d(TAG, "Pin Confirmation result: $result")
                if (result != true) {
                    throw BluezRejectedException("Rejected $_device with code ${p1?.toString()}")
                }
            }
            btDevice.isTrusted = true
            //btDevice.connect()
            logger.d("Agent1", "RequestConfirmation Connected.")
        }

        override fun RequestAuthorization(p0: DBusPath?) {
            TODO("Not yet implemented")
        }

        override fun AuthorizeService(p0: DBusPath?, p1: String?) {
            TODO("Not yet implemented")
        }

        override fun Cancel() {
            TODO("Not yet implemented")
        }
    }

    private var pinKeyApprove : () -> Unit = {}
    private var pinKeyDeny : () -> Unit = {}

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
                    }.filter { it.isConnected || it.isPaired }
                }
                .shareIn(GlobalScope, SharingStarted.WhileSubscribed())
        )
    }

    fun requestPairNewDevice() {
        navigationNodeTraverser.showPairableDevices(
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

    fun onUiResult(uiResult: UiResult) {
        when (uiResult) {
            CurrentDeviceChooser.CurrentDeviceChooserResult.Cancelled -> navigationNodeTraverser.goBack()
            is CurrentDeviceChooser.CurrentDeviceChooserResult.RequestViewDevice -> {
                navigationNodeTraverser.showCurrentDevice(
                    deviceListProvider.getLiveDevice(uiResult.device.address)
                        .map { device ->
                            CurrentDeviceViewer.CurrentDevice(
                                address = device.address,
                                alias = device.alias,
                                isPaired = device.isPaired,
                                isConnected = device.isConnected
                            )
                        }
                )
            }
            PairableDeviceChooser.PairableDeviceChooserResult.Cancelled -> {
                navigationNodeTraverser.goBack()
            }
            is PairableDeviceChooser.PairableDeviceChooserResult.RequestPairToDevice -> {
                val deviceMac = uiResult.device.address
                val device = deviceListProvider.getDeviceForMac(deviceMac)
                if (!device.isTrusted) {
                    try {
                        device.pair()
                    } catch (e: DBusExecutionException) {
                        logger.e(TAG, "RequestPairToDevice exception: $e", e)
                    }
                } else {
                    if (!device.isConnected) {
                        try {
                            device.connect()
                        } catch (e : DBusExecutionException) {
                            logger.e(TAG, "Request Pair to Device exception", e)
                        }
                    }
                }
            }
            is BluetoothPinConfirmationScreen.PinConfirmationResult -> {
                if (uiResult.isApproved) {
                    pinKeyApprove()
                } else {
                    pinKeyDeny()
                }
            }

            CurrentDeviceViewer.ViewResult.Cancelled -> {
                navigationNodeTraverser.goBack()
            }
            is CurrentDeviceViewer.ViewResult.DisconnectDevice -> {
                deviceListProvider.getDeviceForMac(uiResult.device.address).disconnect()
            }
            is CurrentDeviceViewer.ViewResult.ForgetDevice -> {
                deviceListProvider.getDeviceForMac(uiResult.device.address).isTrusted = false
            }
            is CurrentDeviceViewer.ViewResult.ConnectDevice -> {
                try {
                    deviceListProvider.getDeviceForMac(uiResult.device.address).connect()
                } catch (e: DBusExecutionException) {
                    logger.e(TAG, "Connect Device Exception", e)
                }
            }
            is CurrentDeviceViewer.ViewResult.SetDeviceForCarPlatform -> {
                TODO("Set this for Car Config")
            }
        }
    }
}