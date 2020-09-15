package ca.stefanm.ibus.lib.bluetooth.blueZdbus

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import ca.stefanm.ibus.lib.platform.Service
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.delay
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbusConnector @Inject constructor(
    private val logger: Logger
) : Service {

    lateinit var connection : DBusConnection
    lateinit var deviceManager: DeviceManager

    override fun onCreate() {
        connection = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)
        logger.d("DBUS CONN", "Connected to DBus.")
        DeviceManager.createInstance(connection.address.rawAddress)
        deviceManager = DeviceManager.getInstance()

        logger.d("DBUS CONN", "Created device manager.")

        //We need to have already paired to the device and connected to it
        //before we can call getDevice.
        deviceManager.findBtDevicesByIntrospection(deviceManager.adapter)
    }

    override fun onShutdown() {
        deviceManager.closeConnection()
        connection.disconnect()
    }

    fun reScanBluetoothDevices() {
        deviceManager.findBtDevicesByIntrospection(deviceManager.adapter)
    }

    fun getDevice(macAddress: List<Int>) : BluetoothDevice? {
        return deviceManager.getDevices(true)
            .filter { it.isConnected }
            .firstOrNull { it.address == macAddress
                        .joinToString(separator = ":") { it.toString(16) }.toUpperCase()
            }
    }

    fun getPlayer(device : BluetoothDevice?) : MediaPlayer1? {
        return device?.dbusConnection?.getRemoteObject("org.bluez", device.dbusPath + "/player0", MediaPlayer1::class.java)
    }
}

//This is a hack because I haven't thought through the design for the multiple-phone pairing yet.
@Singleton
class DbusReconnector @Inject constructor(
    private val deviceConfiguration: DeviceConfiguration,
    private val dbusConnector: DbusConnector,
    private val logger: Logger
) {

    var previouslyPairedPhone : DeviceConfiguration.PairedPhone? = null

    @Synchronized
    fun reconnect() : Pair<DBusConnection?, MediaPlayer1?>{
        logger.d("RECONNECTOR", "Attempting DBus Reconnect.")
        with (dbusConnector) {
            return Pair(connection, getPlayer(getDevice(deviceConfiguration.pairedPhone.macAddress)))
        }

    }
}