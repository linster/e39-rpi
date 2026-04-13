package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter.FilterConnectionsListForDeviceUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter.GetConnectionIdFromConnectionUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

// After getting a device, we need to populate it
// into a fatter device by adding the connections for the device

class GetConnectionsForDeviceUseCase @Inject constructor(
    private val getConnectionsUseCase: GetConnectionsUseCase,
    private val filterConnectionsListForDeviceUseCase: FilterConnectionsListForDeviceUseCase,
    private val getConnectionIdFromConnectionUseCase: GetConnectionIdFromConnectionUseCase
) {


    fun getConnectionsForDevices(
        devices : Flow<List<Device>>
    ) : Flow<Map<Device, List<Nmt.NmtConnectConnection>>> {

        return devices.combine(getConnectionsUseCase.getConnections()) { devices, connections ->
            //TODO also do the virtual devices from this usecase too. Use the FilterConnectionListForVirtualDevicesUseCase
            filterConnectionsListForDeviceUseCase.filter(devices, connections).mapValues {
                it.value.map { conn ->
                    Nmt.NmtConnectConnection(
                        name = getConnectionIdFromConnectionUseCase.getConnectionIdFromConnection(conn),
                        device = it.key,
                        conn = conn,

                    )
                }
            }
        }
    }

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


}