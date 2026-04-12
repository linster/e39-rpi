package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetActiveConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all.GetDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate.GetConnectionsForDeviceUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate.GetDisambiguatedDeviceNameUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.freedesktop.dbus.DBusPath
import org.freedesktop.networkmanager.Device
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
            val name : String,
            val device: Device
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

}