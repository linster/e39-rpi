package ca.stefanm.ibus.car.bluetooth.blueZdbus

import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.Service
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject

@ConfiguredCarScope
class DbusConnector @Inject constructor(
    private val logger: Logger
) : Service {

    lateinit var connection : DBusConnection
    lateinit var deviceManager: DeviceManager //TODO we sometimes get "lateinit property device manager has noot been initalized"

    override fun onCreate() {
        runBlocking {
            connection = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)

            logger.d("DBUS CONN", "Connected to DBus.")
            DeviceManager.createInstance(connection.address.rawAddress)
            deviceManager = DeviceManager.getInstance()

            logger.d("DBUS CONN", "Created device manager.")

            //We need to have already paired to the device and connected to it
            //before we can call getDevice.
            //deviceManager.findBtDevicesByIntrospection(deviceManager.adapter)
        }
    }

    override fun onShutdown() {
        if (::deviceManager.isInitialized) {
            deviceManager.closeConnection()
        } else {
            logger.w("DbusConnector", "onShutdown() ,no deviceManager??")
        }
        connection.disconnect()
    }


    fun getDevice(macAddress: List<Int>) : BluetoothDevice? {

        if (!::deviceManager.isInitialized) {
            logger.w("DbusConnector", "getDevice() has no deviceManager")
            return null
        }

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

