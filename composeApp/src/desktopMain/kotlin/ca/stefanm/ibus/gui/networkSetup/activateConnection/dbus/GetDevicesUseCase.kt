package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

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
import org.freedesktop.dbus.matchrules.DBusMatchRuleBuilder
import org.freedesktop.dbus.messages.constants.MessageTypes
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.Settings
import javax.inject.Inject

// Get the devices from the network manager and subscribe to the singals
// to keep the flow up to date.
class GetDevicesUseCase @Inject constructor(
    private val logger: Logger
) {
    companion object {
        const val TAG = "GetDevicesUseCase"
    }

    fun getDevices() : Flow<List<Device>> {
        val connection = DBusConnectionBuilder.forSystemBus().build()

        return callbackFlow {

            connection.connect()

            //TODO STEFAN connection.use {} for autoClosable??
            val nmClient = connection.getRemoteObject(
                "org.freedesktop.NetworkManager",
                "/org/freedesktop/NetworkManager",
                NetworkManager::class.java
            )

            send(nmClient.GetDevices())

            //Now to set up a properties changed listener for
            //"org.freedesktop.NetworkManager", "Devices"

            val handler = object : DBusSigHandler<Properties.PropertiesChanged> {
                override fun handle(_signal: Properties.PropertiesChanged?) {
                    val propertiesChanged = _signal?.propertiesChanged
                    if (propertiesChanged != null && propertiesChanged.containsKey("Devices")) {
//                        trySend(propertiesChanged["Devices"]!!.value as List<DBusPath>)
                        trySend(nmClient.GetDevices())
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
                    Device::class.java
                )
            }
        }
    }

}