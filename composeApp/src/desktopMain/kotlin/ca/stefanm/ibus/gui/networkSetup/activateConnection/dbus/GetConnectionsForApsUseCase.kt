package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

class GetConnectionsForApsUseCase @Inject constructor(
    private val logger: Logger
) {

    fun getConnectionsForAps(
        devices : List<Device>
    ) : Map<Device, List<Connection>> {
        return devices.map { device ->
            device to getConnectionsForDevice(device)
        }.associate { (device, connections) ->
            device to connections
        }
    }

    fun getConnectionsForDevice(
        device: Device
    ) : List<Connection> {
        //TODO
        return emptyList()
    }
}