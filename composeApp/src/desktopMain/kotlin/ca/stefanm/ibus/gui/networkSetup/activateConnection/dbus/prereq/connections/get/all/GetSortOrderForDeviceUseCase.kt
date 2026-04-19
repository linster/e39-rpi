package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all

import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

class GetSortOrderForDeviceUseCase @Inject constructor(
    private val logger: Logger
) {

    //TODO STEFAN remove this use-case. Have one that's like SortDevices, and another one that's like SortConnectionsForDevice
    //TODO STEFAN then have a usecase that depends on those that sorts connection list.

    //ONLY DO the devices, not the virtual devices.
    //
    fun getSortOrderForDevice(device: Device) : Int { return 0
//        when (device.deviceType.toNMDeviceType()!!) {
//            NMDeviceType.NM_DEVICE_TYPE_ETHERNET    -> 0
//            NMDeviceType.NM_DEVICE_TYPE_VETH        -> 1
//            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND -> 2
//            NMDeviceType.NM_DEVICE_TYPE_WIFI -> 3
//            NMDeviceType.NM_DEVICE_TYPE_LOOPBACK -> 4
//
//            NMDeviceType.NM_DEVICE_TYPE_VLAN -> 5
//            NMDeviceType.NM_DEVICE_TYPE_VETH -> 6
//            NMDeviceType.NM_DEVICE_TYPE_BOND -> 7
//            NMDeviceType.NM_DEVICE_TYPE_TEAM -> 8
//            NMDeviceType.NM_DEVICE_TYPE_BRIDGE -> 9
//            NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL -> 10
//            NMDeviceType.NM_DEVICE_TYPE_MACSEC -> 11
//            NMDeviceType.NM_DEVICE_TYPE_WIREGUARD -> 12
//            NMDeviceType.NM_DEVICE_TYPE_TUN -> 13
//
//            NMDeviceType.NM_DEVICE_TYPE_MODEM -> 14
//            NMDeviceType.NM_DEVICE_TYPE_BT -> 15
//        }
    }
}