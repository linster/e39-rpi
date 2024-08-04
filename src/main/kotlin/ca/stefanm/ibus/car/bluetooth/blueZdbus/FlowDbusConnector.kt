package ca.stefanm.ca.stefanm.ibus.car.bluetooth.blueZdbus

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.lib.logging.Logger
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject

@PlatformServiceInfo(
    name = "FlowDbusConnector",
    description = "Provides a Flow of DBus objects that are used in other bluetooth components."
)
@BluetoothServiceGroup
@ConfiguredCarScope
class FlowDbusConnector @Inject constructor(
    private val deviceConfiguration: CarPlatformConfiguration,
    private val logger : Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    private val _connection = MutableStateFlow<DBusConnection?>(null)
    val connection : StateFlow<DBusConnection?>
        get() = _connection.asStateFlow()

    private val deviceManager = MutableStateFlow<DeviceManager?>(null)

    // Public so that other components can set the current macAddress and everything just updates
    val macAddress = MutableStateFlow<List<Int>?>(null)

    override suspend fun doWork() {

        _connection.value = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)

        _connection
            .filter { it != null }
            .map { it as DBusConnection }
            .filter { it?.isConnected == true }
            .collect {
            logger.d("DBUS CONN", "Connected to DBus.")
            DeviceManager.createInstance(it.address.rawAddress)
            deviceManager.value = DeviceManager.getInstance()
            logger.d("DBUS CONN", "Created device manager.")
        }
    }

    override fun onShutdown() {
        super.onShutdown()

        if (deviceManager.value != null) {
            deviceManager.value?.closeConnection()
        } else {
            logger.w("DbusConnector", "onShutdown() ,no deviceManager??")
        }
        _connection.value?.disconnect()
    }


    private fun Flow<DeviceManager>.getDevice(macAddress: List<Int>) : Flow<BluetoothDevice?> {
        return this.map {
            it.getDevices(true)
                .filter { it.isConnected }
                .firstOrNull { it.address == macAddress
                    .joinToString(separator = ":") { it.toString(16) }.toUpperCase()
                }
        }
    }

    private fun DeviceManager.getDevice(macAddress: List<Int>) : BluetoothDevice? {
        return this.getDevices(true)
                .filter { it.isConnected }
                .firstOrNull { it.address == macAddress
                    .joinToString(separator = ":") { it.toString(16) }.toUpperCase()
                }
    }

    private fun Flow<BluetoothDevice?>.getPlayer() : Flow<MediaPlayer1?> {
        return filter { it != null }.map { it as BluetoothDevice }.map {
            it.dbusConnection?.getRemoteObject("org.bluez", it.dbusPath + "/player0", MediaPlayer1::class.java)
        }
    }

    private fun getDevice(macAddress: List<Int>) : Flow<BluetoothDevice> {
        // Unrolled flatMapConcat
        // https://flowmarbles.com/#flatMapConcat
        return deviceManager.filterNotNull().map { it.getDevice(macAddress) }.filterNotNull()
    }

    private fun getPlayer(macAddress: List<Int>) : Flow<MediaPlayer1?> {
        return getDevice(macAddress).getPlayer()
    }

    fun getDevice() : Flow<BluetoothDevice> {
        return macAddress.filter { it != null }.map { it as List<Int> }.map { getDevice(it) }.flattenConcat()
    }

    fun getPlayer() : Flow<MediaPlayer1?> {
        return macAddress.filter { it != null }.map { it as List<Int> }.map { getPlayer(it) }.flattenConcat()
    }

}