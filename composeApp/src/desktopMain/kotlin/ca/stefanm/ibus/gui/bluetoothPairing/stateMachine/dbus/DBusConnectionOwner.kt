package ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.DBusConnectionOwningComponent
import com.github.hypfvieh.bluetooth.DeviceManager
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject

@ApplicationScope
class DBusConnectionOwner @Inject constructor() : DBusConnectionOwningComponent {


    //https://dbus.freedesktop.org/doc/dbus-java/dbus-java.pdf


    private lateinit var systemConnection : DBusConnection
    private lateinit var sessionConnection : DBusConnection

    private lateinit var deviceManager : DeviceManager

    override fun onSetup() {
        systemConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)
        sessionConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)
        sessionConnection.requestBusName("ca.stefanm.e39")
        DeviceManager.createInstance(systemConnection.address.rawAddress)
        deviceManager = DeviceManager.getInstance()
    }

    override fun onCleanup() {
        sessionConnection.releaseBusName("ca.stefanm.e39")
        sessionConnection.disconnect()

        deviceManager.closeConnection()
        systemConnection.disconnect()
    }

    override fun getSystemBusConnection() = systemConnection
    override fun getSessionBusConnection() = sessionConnection
    override fun getDeviceManager() = deviceManager
}