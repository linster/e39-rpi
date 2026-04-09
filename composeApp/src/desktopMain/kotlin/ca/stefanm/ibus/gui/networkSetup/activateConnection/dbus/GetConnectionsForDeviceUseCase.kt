package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
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

    //Aha. It's GObject doing dynamic dispatch.
    //Each Device has a connection_compatible. Then, that (manually, because GObject is full of boilerplate)
    //dispatches to a connection_compatible in every C file. Each one of those checks the type of the connection
    //against a fixed list the device supports.

    //Ok, so you need to GetSettings()
    //Then you get somthing like this (here's one for a wired connection:)
    //
//    'connection': {'autoconnect-priority': -100,
//        'id': 'Wired connection 1',
//        'permissions': [],
//        'timestamp': 1775606101,
//        'type': '802-3-ethernet',

    //Here's one for the docker0 bridge:
//    'connection': {'autoconnect': False,
//        'id': 'docker0',
//        'interface-name': 'docker0',
//        'permissions': [],
//        'timestamp': 1775606044,
//        'type': 'bridge',
    // And the type is what matches.
    // So here we need to build a map of DeviceType to accepted type strings. (Grab it from the nm-device subclasses)

    // Then, we take the list of devices that comes in, and filter the whole list of the connections only to the ones
    // that are ok for the device type.

    //TODO where's the list of things we can filter on come from?
    //TODO does that come from each device's availableConnections?
    //TODO nope, it's nmSettings.getConnections() (getConnections() on NetworkManagerSettings object).

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