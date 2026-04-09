package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import io.ktor.util.logging.Logger
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

class GetConnectionIdFromConnectionUseCase @Inject constructor(
    private val logger: Logger
) {

    fun getConnectionIdFromConnection(path : DBusPath) : String? {
        val dbus = DBusConnectionBuilder.forSystemBus().build()
        dbus.connect()

        return dbus.use {
            val connection = dbus.getRemoteObject(
                "org.freedesktop.NetworkManager",
                path,
                Connection::class.java
            )
            getConnectionIdFromConnection(connection)
        }
    }

    fun getConnectionIdFromConnection(connection: Connection) : String? {
        val settings = connection.GetSettings().toMap()
        return settings["connection"]?.getOrElse("id") { null }?.let {
            it.value as String
        }
    }
}