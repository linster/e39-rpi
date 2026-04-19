package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.populate

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.convert.ConvertDeviceToWirelessUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all.GetDeviceFromDevicePathUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMActiveConnectionState
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.DevicePath
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetActiveConnectionsUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.retryWhen
import org.freedesktop.dbus.DBusPath
import org.freedesktop.networkmanager.connection.Active
import javax.inject.Inject

class MarkActiveConnectionsUseCase @Inject constructor(
    private val logger: Logger,
    private val getActiveConnectionsUseCase: GetActiveConnectionsUseCase,
    private val getDeviceFromDevicePathUseCase: GetDeviceFromDevicePathUseCase,
    private val convertDeviceToWirelessUseCase: ConvertDeviceToWirelessUseCase
) {

    companion object {
        const val TAG = "MarkActiveConnectionsUseCase"

        data class MultiActiveLogInfo(
            val activeConnectionObjectPath : DBusPath,
            val devicesList : List<DBusPath>,
            val state : NMActiveConnectionState.State
        )
    }

    fun markActiveConnections(upstream : Flow<List<Nmt.NmtConnectDevice>>) : Flow<List<Nmt.NmtConnectDevice>> {
        //TODO also subscribe to the StateChanged signal
        //TODO and only for ACTIVATED, DEACTIVATED signals
        //TODO however, that signal is on each ActiveConnection....

        return upstream.combine(getActiveConnectionsUseCase.getAllActiveConnections()) { upstream, active ->
            markActive(upstream, active)
        }
    }

    internal fun markActive(devices : List<Nmt.NmtConnectDevice>, active : List<Active>) : List<Nmt.NmtConnectDevice> {
        return devices.map { device ->
            device.copy(
                connections = if (device.connections?.all { it.deviceIsWifi == true } == true) {
                    markActiveForWifi(device, active)
                } else {
                    markActive(device, active)
                }
            )
        }
    }

    internal fun markActive(device: Nmt.NmtConnectDevice, activeConns : List<Active>) : List<Nmt.NmtConnectConnection>? {
        //Get all the device paths from the active list
        //We have List<Active>, and each Active has a List<Device>
        //Do the old reverse the map trick to get Map<Device, Active>

        val devicePath = DBusPath(device.device.objectPath)

        //https://networkmanager.dev/docs/api/latest/gdbus-org.freedesktop.NetworkManager.Connection.Active.html
        //At any time a settings-connection can only be activated on one device and
        // vice versa. However, during activation and deactivation multiple
        // active-connections can reference the same device or settings-connection
        // as they are waiting to be activated or to be deactivated.
        val deviceToActive = mutableMapOf<String, MutableList<Active>>()
        activeConns.filter {
            try {
                NMActiveConnectionState.fromInt(it.state) == NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED
            } catch(e : Throwable) {
                false
            }
        }.forEach { active ->
            active.devices.forEach { devicePath ->
                val device = getDeviceFromDevicePathUseCase.getDevice(devicePath)
                if (!deviceToActive.containsKey(devicePath.path)) {
                    deviceToActive[devicePath.path] = mutableListOf<Active>()
                }
                deviceToActive[devicePath.path]?.add(active)
            }
        }

        logger.d(TAG, "DeviceToActive: $deviceToActive")

        //If any devices have more than one Active, print them out
        deviceToActive.mapValues {
            (it.value.size > 1) to it.value
        }.filter {
            it.value.first
        }.mapValues {
            it.value.second
        }.mapValues {
            it.value.map { MultiActiveLogInfo(DBusPath(it.objectPath), it.devices, NMActiveConnectionState.fromInt(it.state)) }
        }.let {
            if (it.isNotEmpty()) {
                logger.i(TAG, "Cardinality of ActiveConnections to Devices is not currently 1:1: $it")
            }
        }

        if (devicePath.path in deviceToActive.keys) {
            //For each one, check the State
            //https://networkmanager.dev/docs/api/latest/nm-dbus-types.html#NMActiveConnectionState
            //and only take the one that is ACTIVATED

            val allActiveConnectionsForDevice = deviceToActive.getOrDefault(devicePath.path, emptyList<Active>())
            logger.d(TAG, "allActiveConnectionsForDevice $devicePath: ${allActiveConnectionsForDevice.map { it.objectPath }}")

            val activatedForDeviceList = allActiveConnectionsForDevice.filter { NMActiveConnectionState.fromInt(it.state) == NMActiveConnectionState.State.NM_ACTIVE_CONNECTION_STATE_ACTIVATED }
            if (activatedForDeviceList.size > 1) {
                logger.w(TAG, "Device $devicePath, has more than one ACTIVATED connection $activatedForDeviceList")
            }
            val activatedForDevice = activatedForDeviceList.firstOrNull()
            return if (activatedForDevice == null) {
                //Return the connection list the device already had
                device.connections
            } else {
                //Update the list by finding the matching connection path
                //also preserve the connections the device already had.

                val connections = device.connections
                if (connections == null) {
                    //Nothing to update
                    return null
                }

                val (activeConnections, others) = connections.partition { it.conn?.objectPath == activatedForDevice.connection.path }

                //Update the matching active connections
                val updatedActive = activeConnections.map {
                    it.copy(
                        hasActiveConnection = true,
                        active = activatedForDevice
                    )
                }

                return updatedActive + others
            }
        } else {
            //The device doesn't belong to any active connections.
            //Return the device's connection list it already had
            return device.connections
        }
    }

    internal fun markActiveForWifi(device: Nmt.NmtConnectDevice, activeConns : List<Active>) : List<Nmt.NmtConnectConnection>? {

        //Run the other markActive so that the NmtConnection.active gets set, but then also
        // Do a match on the APs.

        return markActive(device, activeConns)?.map { conn ->
            val ap = conn.ap?.objectPath
            val wireless = convertDeviceToWirelessUseCase.deviceToWireless(device.device)
            val apIsActive = wireless.activeAccessPoint.path == ap

            conn.copy(
                apIsactive = apIsActive
            )
        }
    }
}