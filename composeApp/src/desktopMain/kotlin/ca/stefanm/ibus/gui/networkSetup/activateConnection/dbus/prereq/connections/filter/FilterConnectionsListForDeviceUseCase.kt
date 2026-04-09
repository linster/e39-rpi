package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

class FilterConnectionsListForDeviceUseCase @Inject constructor(
    private val logger: Logger,
    private val getConnectionTypeFromConnectionUseCase: GetConnectionTypeFromConnectionUseCase
) {

    fun filter(devices : List<Device>, allConnections : List<Connection>) : Map<Device, List<Connection>> {

    }


    fun connectionMatchesDevice(device: Device, connection: Connection) : Boolean {
        return connectionMatchesDevice(device, getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection))
    }

    @VisibleForTesting
    private fun connectionMatchesDevice(device: Device, connectionType : String?) : Boolean {

    }

    companion object {
        val matchMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_UNKNOWN to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_WIFI to listOf("802-1x"),
            NMDeviceType.NM_DEVICE_TYPE_UNUSED1 to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_UNUSED2 to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_BT to listOf("bluetooth"),
            NMDeviceType.NM_DEVICE_TYPE_OLPC_MESH to listOf("802-11-olpc-mesh"),
            NMDeviceType.NM_DEVICE_TYPE_WIMAX to listOf("wimax"),
            NMDeviceType.NM_DEVICE_TYPE_MODEM to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND to listOf("infiniband"),
            NMDeviceType.NM_DEVICE_TYPE_BOND to listOf("bond", "bond-port"),
            NMDeviceType.NM_DEVICE_TYPE_VLAN to listOf("vlan"),
            NMDeviceType.NM_DEVICE_TYPE_ADSL to listOf("adsl"),
            NMDeviceType.NM_DEVICE_TYPE_BRIDGE to listOf("bridge", "bridge-port"),
            NMDeviceType.NM_DEVICE_TYPE_GENERIC to listOf("generic"),
            NMDeviceType.NM_DEVICE_TYPE_TEAM to listOf("team",
               // "team-port"
            ),
            NMDeviceType.NM_DEVICE_TYPE_TUN to listOf("tun"),
            NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL to listOf("ip-tunnel"),
            NMDeviceType.NM_DEVICE_TYPE_MACVLAN to listOf("macvlan"),
            NMDeviceType.NM_DEVICE_TYPE_VXLAN to listOf("vxlan"),
            NMDeviceType.NM_DEVICE_TYPE_VETH to listOf("veth"),
            NMDeviceType.NM_DEVICE_TYPE_MACSEC to listOf("macsec"),
            NMDeviceType.NM_DEVICE_TYPE_DUMMY to listOf("dummy"),


            NMDeviceType.NM_DEVICE_TYPE_PPP to listOf("ppp"),

            //Verified ok
            NMDeviceType.NM_DEVICE_TYPE_OVS_INTERFACE to listOf("ovs-interface"),
            //Verified ok
            NMDeviceType.NM_DEVICE_TYPE_OVS_PORT to listOf("ovs-port"),
            //Verified ok
            NMDeviceType.NM_DEVICE_TYPE_OVS_BRIDGE to listOf("ovs-bridge"),
            //Verified ok.
            NMDeviceType.NM_DEVICE_TYPE_WPAN to listOf("wpan"),

            //TODO STEFAN not sure yet
            NMDeviceType.NM_DEVICE_TYPE_6LOWPAN to listOf("6lowpan"),

            //TODO STEFAN not sure yet
            NMDeviceType.NM_DEVICE_TYPE_WIREGUARD to listOf("wireguard"),

            //TODO STEFAN not sure yet.
            NMDeviceType.NM_DEVICE_TYPE_WIFI_P2P to listOf("wifi-p2p"),

            //Verified ok.
            NMDeviceType.NM_DEVICE_TYPE_VRF to listOf("vrf"),

            //Verified ok.
            NMDeviceType.NM_DEVICE_TYPE_LOOPBACK to listOf("loopback"),

            //TODO STEFAN Not sure yet.
            NMDeviceType.NM_DEVICE_TYPE_HSR to listOf("hsr"),

            //TODO STEFAN Not sure yet.
            NMDeviceType.NM_DEVICE_TYPE_IPVLAN to listOf("ipvlan"),

            //Verified ok
            NMDeviceType.NM_DEVICE_TYPE_GENEVE to listOf("geneve"),
        )
    }
}