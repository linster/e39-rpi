package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetActiveConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.SortDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all.GetDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate.GetConnectionsForApsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate.GetConnectionsForDeviceUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate.GetDisambiguatedDeviceNameUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import io.ktor.network.sockets.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.freedesktop.dbus.DBusPath
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

typealias DevicePath = DBusPath

class GetConnectionListUseCase @Inject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getDisambiguatedDeviceNameUseCase: GetDisambiguatedDeviceNameUseCase,

    private val sortDevicesUseCase: SortDevicesUseCase,


    private val getConnectionsForApsUseCase: GetConnectionsForApsUseCase,

    private val getActiveConnectionsUseCase: GetActiveConnectionsUseCase,

    private val getConnectionsForDeviceUseCase: GetConnectionsForDeviceUseCase,

    private val logger: Logger
) {

    sealed interface ConnectionListItem {
        data class DeviceHeader(
            val name : String,
            val device: Device,
            val nmtConnectDevice: Nmt.NmtConnectDevice
        ) : ConnectionListItem

        sealed interface ConnectionListConnection : ConnectionListItem {
            data class WifiAccessPoint(
                val ssid: String,
                val isConnected: Boolean,
                val strength: Int,
                val nmtConnectConnection: Nmt.NmtConnectConnection
            ) : ConnectionListConnection

            data class OtherConnection(
                val name: String,
                val isConnected: Boolean,
                val nmtConnectConnection: Nmt.NmtConnectConnection
            ) : ConnectionListConnection
        }
    }



    fun getConnectionItems() : Flow<List<ConnectionListItem>> {

        val foo = getDevicesUseCase.getDevices()
            .let { upstream ->
                getConnectionsForDeviceUseCase.getConnectionsForDevices(upstream)
            }
            .let { upstream ->
                getConnectionsForApsUseCase.fillInNmtConnectConnectionForWirelessOnly(upstream)
            }
            .map {
                val allNames = it.keys
                val disambiguatedNames = getDisambiguatedDeviceNameUseCase.getDisambiguatedNames(allNames.toList())
                it.mapKeys { entry ->
                    Nmt.NmtConnectDevice(
                        device = entry.key,
                        name = disambiguatedNames[entry.key] ?: entry.key.objectPath
                    )
                }
            }
            .map {
                it.mapKeys {
                    it.key.copy(
                        connections = it.value
                    )
                }.keys
                //By now we should have a populated NmtConnectDevice
            }
            .map {
                //Sort the devices
                sortDevicesUseCase.sortDevices(it)
            }.map {
                //Sort the connections within each device
                //TODO this is where the multiple access points are merged together, I think?
                //TODO sort the wifi connections before merging them, then actually merge them.
                //TODO >1 AP will get smooshed.
                it
            }.map {
                // Set the isConnected flag on each connection by checking with the active connection use-case
                // This should also fill-in the Active? connection for the NmtConnectConnection
                it
            }.map {
                val result = mutableListOf<ConnectionListItem>()
                it.forEach { device ->
                    result.add(
                        ConnectionListItem.DeviceHeader(
                            name = device.name,
                            device = device.device,
                            nmtConnectDevice = device
                        )
                    )
                    device.connections?.forEach { connection ->
                        if (connection.deviceIsWifi == true) {
                            result.add(
                                ConnectionListItem.ConnectionListConnection.WifiAccessPoint(
                                    ssid = connection?.ssid ?: "<unknown ssid>",
                                    isConnected = connection?.apIsactive == true,
                                    strength = connection.ap?.strength?.toInt() ?: 0,
                                    nmtConnectConnection = connection
                                )
                            )
                        } else {
                            result.add(
                                ConnectionListItem.ConnectionListConnection.OtherConnection(
                                    name = connection.name ?: "<unknown connection>",
                                    isConnected = connection?.active != null,
                                    nmtConnectConnection = connection
                                )
                            )
                        }
                    }
                }
                result
            }




        return foo
    }

}