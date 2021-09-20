package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import com.github.hypfvieh.bluetooth.DeviceManager
import org.bluez.Agent1
import org.bluez.AgentManager1
import org.freedesktop.DBus
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.UInt32
import javax.inject.Inject

@ApplicationScope
class BluetoothPairingManager @Inject constructor(
    private val logger: Logger
) {

    companion object {
        const val TAG = "BluetoothPairingManager"
    }

    interface BluetoothPairingManagerClient {

        /* This is the 6-digit pairing pin the user needs to match on
         * the phone and the car.
         */
        suspend fun askToApprovePin(pin : String) : Boolean
    }

    enum class PairingState {
        //We have no clue about this device
        NOT_PAIRED,
        //We can see the device is in the dbus tree
        PAIRED_INTROSPECTABLE
    }

    enum class PairingProcessState {
        STARTED,

    }


    private var dbusSessionConnection : DBusConnection? = null
    private var dbusSystemConnection : DBusConnection? = null
    private val bluzUniqueBusId : String?
        get() = dbusSystemConnection?.getRemoteObject(
            "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
        )?.GetNameOwner("org.bluez")

    private fun getAgentManager(dBusConnection: DBusConnection) : AgentManager1? {
        return dBusConnection.getRemoteObject(
            "org.bluez",
            "/org/bluez",
            AgentManager1::class.java
        )
    }

    fun startPairing() {
        dbusSystemConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)
        dbusSessionConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)

        dbusSessionConnection?.requestBusName("ca.stefanm.e39")
        //We have to export our agent so that when we register it, DBus
        //can hook it up.
        dbusSessionConnection?.exportObject(
            "/pairing/agent",
            agent
        )
        val agentManager = getAgentManager(dbusSystemConnection!!)

        agentManager?.RegisterAgent(
            DBusPath("/pairing/agent"),
            "DisplayYesNo"
        )

        DeviceManager.createInstance(dbusSystemConnection!!.address.rawAddress)
        val deviceManager = DeviceManager.getInstance()

        deviceManager.adapter.let {
            it.isDiscoverable = true
            it.startDiscovery()
        }

        deviceManager.scanForBluetoothDevices(50*1000).map {
            logger.d(TAG, "devices: ${it.name} ${it.address}")
        }
    }

    fun cleanup() {

        dbusSessionConnection?.releaseBusName("ca.stefanm.e39")
    }

    //https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/agent-api.txt
    val agent = object : Agent1 {

        override fun isRemote(): Boolean = false

        override fun getObjectPath(): String {
            return "/pairing/agent"
        }

        override fun Release() {
            TODO("Not yet implemented")
        }

        override fun RequestPinCode(_device: DBusPath?): String {
            TODO("Not yet implemented")
        }

        override fun DisplayPinCode(_device: DBusPath?, _pincode: String?) {
            logger.d(TAG, "PinCode: $_pincode")
        }

        override fun RequestPasskey(_device: DBusPath?): UInt32 {
            TODO("Not yet implemented")
        }

        override fun DisplayPasskey(_device: DBusPath?, _passkey: UInt32?, _entered: UInt16?) {
            logger.d(TAG, "Display PassKey: ${_passkey}, $_entered")
        }

        override fun RequestConfirmation(_device: DBusPath?, _passkey: UInt32?) {
            TODO("Not yet implemented")
        }

        override fun RequestAuthorization(_device: DBusPath?) {
            TODO("Not yet implemented")
        }

        override fun AuthorizeService(_device: DBusPath?, _uuid: String?) {
            TODO("Not yet implemented")
        }

        override fun Cancel() {
            TODO("Not yet implemented")
        }

    }

}