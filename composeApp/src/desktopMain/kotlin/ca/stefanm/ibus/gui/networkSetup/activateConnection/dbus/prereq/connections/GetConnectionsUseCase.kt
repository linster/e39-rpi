package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.networkmanager.Settings
import org.freedesktop.networkmanager.connection.Active
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject
import kotlin.collections.map

class GetConnectionsUseCase @Inject constructor(
    private val logger: Logger
) {
    companion object {
        const val TAG = "GetConnectionsUseCase"
    }

    fun getConnections() : Flow<List<Connection>> {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        return callbackFlow {
            connection.connect()

            val settings = connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager/Settings",
                Settings::class.java
            )
            send(settings.connections)

            val handler = object : DBusSigHandler<Properties.PropertiesChanged> {
                override fun handle(_signal: Properties.PropertiesChanged?) {
                    val propertiesChanged = _signal?.propertiesChanged
                    if (propertiesChanged != null && propertiesChanged.containsKey("Connections")) {
                        trySend(settings.connections)
                    }
                }
            }

            connection.addSigHandler(
                Properties.PropertiesChanged::class.java,
                settings,
                handler
            )

            awaitClose {
                connection.removeSigHandler(
                    Properties.PropertiesChanged::class.java,
                    handler
                )
                connection.close()
            }
        }.map {
            it as List<DBusPath>
        }.onEach {
            logger.d(GetActiveConnectionsUseCase.Companion.TAG, "Got list of paths: ${it.map { it.path }}")
        }.map { pathList ->
            if (!connection.connect()) {
                logger.w(GetActiveConnectionsUseCase.Companion.TAG, "Connection was dropped, reconnected.")
            }
            pathList.map { path ->
                connection.getRemoteObject(
                    "org.freedesktop.NetworkManager",
                    path,
                    Connection::class.java
                )
            }
        }
    }
}