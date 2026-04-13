package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

class SortDevicesUseCase @Inject constructor(
    private val logger: Logger
){

    fun sortDevices(devices : Set<Nmt.NmtConnectDevice>) : List<Nmt.NmtConnectDevice> {
        return devices.sortedBy { getSortOrderForDevice(it.device) }
    }

    fun getSortOrderForDevice(device: Device) : Int {
        return when (device.deviceType.toNMDeviceType()!!) {
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET    -> 0
            NMDeviceType.NM_DEVICE_TYPE_VETH        -> 1
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND  -> 2
            NMDeviceType.NM_DEVICE_TYPE_WIFI        -> 3
            NMDeviceType.NM_DEVICE_TYPE_LOOPBACK    -> 4
            NMDeviceType.NM_DEVICE_TYPE_MODEM       -> 14
            NMDeviceType.NM_DEVICE_TYPE_BT          -> 15


            NMDeviceType.NM_DEVICE_TYPE_UNKNOWN,
            NMDeviceType.NM_DEVICE_TYPE_UNUSED1,
            NMDeviceType.NM_DEVICE_TYPE_UNUSED2,
            NMDeviceType.NM_DEVICE_TYPE_OLPC_MESH,
            NMDeviceType.NM_DEVICE_TYPE_WIMAX,
            NMDeviceType.NM_DEVICE_TYPE_ADSL,
            NMDeviceType.NM_DEVICE_TYPE_GENERIC,
            NMDeviceType.NM_DEVICE_TYPE_MACVLAN,
            NMDeviceType.NM_DEVICE_TYPE_VXLAN,
            NMDeviceType.NM_DEVICE_TYPE_DUMMY,
            NMDeviceType.NM_DEVICE_TYPE_PPP,
            NMDeviceType.NM_DEVICE_TYPE_OVS_INTERFACE,
            NMDeviceType.NM_DEVICE_TYPE_OVS_PORT,
            NMDeviceType.NM_DEVICE_TYPE_OVS_BRIDGE,
            NMDeviceType.NM_DEVICE_TYPE_WPAN,
            NMDeviceType.NM_DEVICE_TYPE_6LOWPAN,
            NMDeviceType.NM_DEVICE_TYPE_WIFI_P2P,
            NMDeviceType.NM_DEVICE_TYPE_VRF,
            NMDeviceType.NM_DEVICE_TYPE_HSR,
            NMDeviceType.NM_DEVICE_TYPE_IPVLAN,
            NMDeviceType.NM_DEVICE_TYPE_GENEVE,
            NMDeviceType.NM_DEVICE_TYPE_BOND,
            NMDeviceType.NM_DEVICE_TYPE_VLAN,
            NMDeviceType.NM_DEVICE_TYPE_BRIDGE,
            NMDeviceType.NM_DEVICE_TYPE_TEAM,
            NMDeviceType.NM_DEVICE_TYPE_TUN,
            NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL,
            NMDeviceType.NM_DEVICE_TYPE_MACSEC,
            NMDeviceType.NM_DEVICE_TYPE_WIREGUARD -> 99
        }
    }



}