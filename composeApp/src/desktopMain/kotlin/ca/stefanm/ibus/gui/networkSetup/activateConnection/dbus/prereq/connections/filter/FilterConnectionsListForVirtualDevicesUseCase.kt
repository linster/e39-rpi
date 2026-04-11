package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import javax.inject.Inject
import kotlin.collections.listOf

class FilterConnectionsListForVirtualDevicesUseCase @Inject constructor(

) {

    companion object {
        val virtualDeviceTypes = mapOf<NMDeviceType, List<String>>(
            //TODO also check identifiers
            NMDeviceType.NM_DEVICE_TYPE_VLAN to listOf("vlan"),
            NMDeviceType.NM_DEVICE_TYPE_VETH to listOf("veth"),
            NMDeviceType.NM_DEVICE_TYPE_BOND to listOf("bond",
                //"bond-port"
            ),
            NMDeviceType.NM_DEVICE_TYPE_TEAM to listOf("team", /* "team-port" */),
            //TODO also does other checks
            NMDeviceType.NM_DEVICE_TYPE_BRIDGE to listOf("bridge", /* "bridge-port" */),

            NMDeviceType.NM_DEVICE_TYPE_TUN to listOf("tun"),
            NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL to listOf("ip-tunnel"),
            NMDeviceType.NM_DEVICE_TYPE_WIREGUARD to listOf("wireguard"),
            NMDeviceType.NM_DEVICE_TYPE_MACSEC to listOf("macsec"),
        )
    }
}