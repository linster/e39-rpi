package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

class GetConnectionTypeFromConnectionUseCase @Inject constructor(
    private val logger: Logger
){

    fun getConnectionTypeFromConnection(path : DBusPath) : String? {
        val dbus = DBusConnectionBuilder.forSystemBus().build()
        dbus.connect()

        return dbus.use {
            val connection = dbus.getRemoteObject(
                "org.freedesktop.NetworkManager",
                path,
                Connection::class.java
            )
            getConnectionTypeFromConnection(connection)
        }
    }

    fun getConnectionTypeFromConnection(connection: Connection) : String? {
        val settings = connection.GetSettings().toMap()
        return settings["connection"]?.getOrElse("type") { null }?.let {
            it.value as String
        }
    }
}