package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.GetConnectionsUseCase
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

// After getting a device, we need to populate it
// into a fatter device by adding the connections for the device

class GetConnectionsForDeviceUseCase @Inject constructor(
    private val getConnectionsUseCase: GetConnectionsUseCase
) {

    //This thing actually needs to loop over all the devices
    //and then see which one is compatible for each device.
    //(except wifi? vpn? virtual?)
    //nm-device.c:2924 for connection_compatible

    fun getConnectionsForDevices(
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
//        getConnectionsUseCase.
        return emptyList()
    }
}