package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.networkmanager.connection.Active
import javax.inject.Inject

/** The analog to
 *  acs         = nm_client_get_active_connections(nm_client);
 *  from nmt_connect_connection_list_rebuild
 *  */
class GetActiveConnectionsUseCase @Inject constructor(
    private val logger : Logger
) {

    companion object {
        const val TAG = "GetActiveConnectionsUseCase"
    }

    fun getActiveConnections() : Flow<List<Active>> {
        val connection = DBusConnectionBuilder.forSystemBus().build()
        return callbackFlow {
            connection.connect()

            val nmClient = connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )
            send(nmClient.activeConnections)

            //Now to set up a properties changed listener for
            //"org.freedesktop.NetworkManager", "ActiveConnections"

            val handler = object : DBusSigHandler<Properties.PropertiesChanged> {
                override fun handle(_signal: Properties.PropertiesChanged?) {
                    val propertiesChanged = _signal?.propertiesChanged
                    if (propertiesChanged != null && propertiesChanged.containsKey("Devices")) {
//                        trySend(propertiesChanged["Devices"]!!.value as List<DBusPath>)
                        trySend(nmClient.activeConnections)
                    }
                }
            }
            connection.addSigHandler(
                Properties.PropertiesChanged::class.java,
                nmClient,
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
            logger.d(TAG, "Got list of paths: ${it.map { it.path }}")
        }.map { pathList ->
            if (!connection.connect()) {
                logger.w(TAG, "Connection was dropped, reconnected.")
            }
            pathList.map { path ->
                connection.getRemoteObject(
                    "org.freedesktop.NetworkManager",
                    path,
                    Active::class.java
                )
            }
        }
    }
}