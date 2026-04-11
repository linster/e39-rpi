package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.settings.Connection

//From the connection settings object
//{'bridge': {'interface-name': 'docker0', 'stp': False, 'vlans': []},
//    'connection': {'autoconnect': False,
//    'id': 'docker0',
//    'interface-name': 'docker0', <*** get this one
//    'permissions': [],

//Not all connections will specify this.
class GetInterfaceNameFromConnectionUseCase {

    fun getInterfaceNameFromConnection(path : DBusPath) : String? {
        val dbus = DBusConnectionBuilder.forSystemBus().build()
        dbus.connect()

        return dbus.use {
            val connection = dbus.getRemoteObject(
                "org.freedesktop.NetworkManager",
                path,
                Connection::class.java
            )
            getInterfaceNameFromConnection(connection)
        }
    }

    fun getInterfaceNameFromConnection(connection: Connection) : String? {
        val settings = connection.GetSettings().toMap()
        return settings["connection"]?.getOrElse("interface-name") { null }?.let {
            it.value as String
        }
    }
}