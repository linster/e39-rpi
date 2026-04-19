package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.DevicePath
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import net.folivo.trixnity.client.flatten
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBus
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

    fun getAllActiveConnections() : Flow<List<Active>> {
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
                    if (propertiesChanged != null && propertiesChanged.containsKey("ActiveConnections")) {
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
            .updateListWhenAnyConnectionStateChanges()
    }

    internal fun Flow<List<Active>>.updateListWhenAnyConnectionStateChanges() : Flow<List<Active>> {
        return this.map {
            it.getReactiveList()
        }.flatten() //Thank you Trixnity
    }

    internal fun List<Active>.getReactiveList() : List<Flow<Active>> {
        return this.map { it.getReactiveActive() }
    }

    internal fun Active.getReactiveActive() : Flow<Active> {
        return flowOf(this).combineTransform(this.getStateChanged()) { active, stateChanged ->
            emit(active)
        }
    }

    internal fun Active.getStateChanged() : Flow<Active.StateChanged?> {
        val active = this
        return callbackFlow {
            val connection = DBusConnectionBuilder.forSystemBus().build()
            connection.connect()

            val handler = object : DBusSigHandler<Active.StateChanged> {
                override fun handle(_signal: Active.StateChanged?) {
                    if (_signal != null) {
                        trySend(_signal)
                    }
                }
            }

            trySend(null)

            connection.addSigHandler(
                Active.StateChanged::class.java,
                connection.getRemoteObject(
                    "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
                )!!.GetNameOwner("org.freedesktop.NetworkManager")!!,
                active,
                handler
            )

            awaitClose {
                connection.removeSigHandler(
                    Active.StateChanged::class.java,
                    handler
                )
                connection.close()
            }
        }
    }
}