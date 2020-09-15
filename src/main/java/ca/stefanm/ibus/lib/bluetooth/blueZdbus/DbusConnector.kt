package ca.stefanm.ibus.lib.bluetooth.blueZdbus

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.platform.Service
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject

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

    fun getDevice(macAddress: List<Int>) : BluetoothDevice? {
        return deviceManager.getDevices(true)
            .filter { it.isConnected }
            .firstOrNull { it.address == macAddress
                        .joinToString(separator = ":") { it.toString(16) }.toUpperCase()
            }
    }
}