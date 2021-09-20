package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.ConnectionOwningComponent
import org.freedesktop.dbus.connections.impl.DBusConnection
import javax.inject.Inject

class DBusConnectionOwner @Inject constructor() : ConnectionOwningComponent {


    //https://dbus.freedesktop.org/doc/dbus-java/dbus-java.pdf


    private lateinit var systemConnection : DBusConnection
    private lateinit var sessionConnection : DBusConnection

    override fun onSetup() {
        systemConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SYSTEM)
        sessionConnection = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)
        sessionConnection.requestBusName("ca.stefanm.e39")
    }

    override fun onCleanup() {
        systemConnection.disconnect()
        sessionConnection.releaseBusName("ca.stefanm.e39")
        sessionConnection.disconnect()
    }

    override fun getSystemBusConnection() = systemConnection
    override fun getSessionBusConnection() = sessionConnection
}