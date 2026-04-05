package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.GetActiveConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.GetConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.freedesktop.dbus.DBusPath
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.connection.Active
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

typealias DevicePath = DBusPath

class GetConnectionListUseCase @Inject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getDisambiguatedDeviceNameUseCase: GetDisambiguatedDeviceNameUseCase,

    private val getConnectionsUseCase: GetConnectionsUseCase,
    private val getActiveConnectionsUseCase: GetActiveConnectionsUseCase,

    private val getConnectionsForDeviceUseCase: GetConnectionsForDeviceUseCase,

    private val logger: Logger
) {

    sealed interface ConnectionListItem {
        data class DeviceHeader(
            val name : String
        ) : ConnectionListItem
        data class WifiAccessPoint(
            val ssid : String,
            val isConnected : Boolean,
            val strength : Int
        ) : ConnectionListItem
        data class OtherConnection(
            val name : String,
            val isConnected : Boolean
        ) : ConnectionListItem
    }



    fun getConnectionItems(
        scope : CoroutineScope
    ) : Flow<List<Device?>> {
        val devicesFlow = getDevicesUseCase.getDevices().distinctUntilChanged()
        return scope.launchMolecule(
            mode = RecompositionMode.Immediate
        ) {
//            combobulateItems()
//            combobulate2(devices = devicesFlow)

            devicesFlow.collectAsState(null).value?.firstOrNull()


        }.onEach { logger.d("WAT", "EMITTING") }
            .map { listOf(it) }
    }

// This is the connection that each NmtDevice has on it!
//    typedef struct {
//        const char *name;
//        char       *ssid;
//
//        NMConnection       *conn;
//        NMAccessPoint      *ap;
//        NMDevice           *device;
//        NMActiveConnection *active;
//    } NmtConnectConnection;

    @Composable
    fun combobulateItems() : List<Device> {

        //Jeez, I hope this is Compose-stable?
        val devices = getDevicesUseCase
            .getDevices()
            .distinctUntilChanged()
            .collectAsState(emptyList())

//        val activeConnections = getActiveConnectionsUseCase
//            .getActiveConnections()
//            .distinctUntilChanged()
//            .collectAsState(emptyList())
//
//        val connections = getConnectionsUseCase
//            .getConnections()
//            .distinctUntilChanged()
//            .collectAsState(emptyList())
//
//        val disambiguatedNames : Map<DevicePath, String> =
//            getDisambiguatedDeviceNameUseCase
//            .getDisambiguatedNames(devices.value)
//            .mapKeys { DBusPath(it.key.objectPath) }

        // Get a Map<DevicePath, ActiveConnection>,
        // starting from List<Connection(List<Path>))
//        val devicePathToActiveConnection: Map<DevicePath, List<Active>> =
//            activeConnections.value
//                .map {
//                    it to it.devices.toList()
//                }
//                .map { (connection, paths) ->
//                    paths.map { path -> connection to path }
//                }
//                .flatten()
//                .groupBy { (connection, path) -> path }
//                .mapValues { it.value.map { it.first } }


//        return key(devices.value) {
//            devices.value.map { device ->
//                Nmt.NmtConnectDevice(
//                    name = disambiguatedNames[DBusPath(device.objectPath)] ?: "No name ",
//                    device = device,
//                    sortOrder = -1,
//                    connections = listOf()
//                )
//            }
//        }


        return devices.value
        //return emptyList()

    }

    @Composable
    fun combobulate2(
        devices : Flow<List<Device>>
    ) : List<Device> {
        val list = devices.collectAsState(emptyList())
        return list.value
    }
}