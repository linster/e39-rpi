package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.DBusConnectionOwningComponent
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
        sessionConnection.disconnect()
//        //TODO
//        Exception in thread "main" org.freedesktop.dbus.exceptions.DBusException: Not Connected
//        at org.freedesktop.dbus.connections.impl.DBusConnection.releaseBusName(DBusConnection.java:453)
//        at ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus.DBusConnectionOwner.onCleanup(DBusConnectionOwner.kt:31)
//        at ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.PairingManager.cleanupDBus(PairingManager.kt:42)
//        at ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.PairingManager.onRequestNavigatorRoot(PairingManager.kt:63)
        sessionConnection.releaseBusName("ca.stefanm.e39")

        deviceManager.closeConnection()
        systemConnection.disconnect()
    }

    override fun getSystemBusConnection() = systemConnection
    override fun getSessionBusConnection() = sessionConnection
    override fun getDeviceManager() = deviceManager
}