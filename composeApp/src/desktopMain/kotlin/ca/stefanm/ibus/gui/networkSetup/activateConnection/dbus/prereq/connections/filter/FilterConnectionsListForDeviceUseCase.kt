package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.filter

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.convert.ConvertDeviceToBluetoothUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

class FilterConnectionsListForDeviceUseCase @Inject constructor(
    private val logger: Logger,
    private val getConnectionTypeFromConnectionUseCase: GetConnectionTypeFromConnectionUseCase,
    private val getInterfaceNameFromConnectionUseCase: GetInterfaceNameFromConnectionUseCase,
    private val convertDeviceToBluetoothUseCase: ConvertDeviceToBluetoothUseCase,
    private val notificationHub: NotificationHub
) {

    fun filter(devices : List<Device>, allConnections : List<Connection>) : Map<Device, List<Connection>> {
        return devices
            .associateWith { device ->
                allConnections.filter { connection ->
                    connectionMatchesDevice(device, connection)
                }
            }
    }


    @VisibleForTesting
    private fun connectionMatchesDevice(device: Device, connection : Connection) : Boolean {

        val connectionType = getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection)

        //Filter out virtual connection types for this use case.
        if (device.deviceType.toNMDeviceType() in setOf(
                NMDeviceType.NM_DEVICE_TYPE_VLAN,
                NMDeviceType.NM_DEVICE_TYPE_VETH,
                NMDeviceType.NM_DEVICE_TYPE_BOND,
                NMDeviceType.NM_DEVICE_TYPE_TEAM,
                NMDeviceType.NM_DEVICE_TYPE_BRIDGE,
                NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL,
                NMDeviceType.NM_DEVICE_TYPE_MACSEC,
                NMDeviceType.NM_DEVICE_TYPE_WIREGUARD,
                NMDeviceType.NM_DEVICE_TYPE_TUN,

        )) {
            return false
        }

        //Silently reject connections in the editor for these types.
        if (device.deviceType.toNMDeviceType() in setOf(

                // Related to docker
                // https://medium.com/@dyavanapellisujal7/docker-macvlan-and-ipvlan-explained-advanced-networking-guide-b3ba20bc22e4
                NMDeviceType.NM_DEVICE_TYPE_MACVLAN,


                NMDeviceType.NM_DEVICE_TYPE_WIFI_P2P,

                )) {
            return false
        }

        val wifiCheck = processWifiDevice(device, connection)
        if (wifiCheck != null) {
            return wifiCheck
        }

        val btCheck = processBtDevice(device, connection)
        if (btCheck != null) {
            return btCheck
        }

        val ethCheck = processEthernetDevice(device, connection)
        if (ethCheck != null) {
            return ethCheck
        }

        val simpleCheck = simpleCheckConnectionMatchesDevices(device, connectionType)
        if (simpleCheck != null) {
            return simpleCheck
        }

        val hasInterfaceCheck = checkInterfaceNameExistsCheckConnectionMatchesDevices(device, connection)
        if (hasInterfaceCheck != null) {
            return hasInterfaceCheck
        }


        val rejectUnsupportedCheck = rejectNotSupportedNetworks(device, connection)
        if (rejectUnsupportedCheck != null) {
            return rejectUnsupportedCheck
        }

        logger.w(TAG, "Unhandled device of type ${device.deviceType.toNMDeviceType()} fell through")
        return false
    }

    @VisibleForTesting
    private fun simpleCheckConnectionMatchesDevices(device: Device, connectionType : String?) : Boolean? {
        //This works for the device types that have no further checks in their connection_compatible
        //methods.

        val simpleCheckMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_UNKNOWN to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_UNUSED1 to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_UNUSED2 to listOf(),
            NMDeviceType.NM_DEVICE_TYPE_ADSL to listOf("adsl"),
            NMDeviceType.NM_DEVICE_TYPE_WPAN to listOf("wpan"),
            NMDeviceType.NM_DEVICE_TYPE_OLPC_MESH to listOf("802-11-olpc-mesh"),
            NMDeviceType.NM_DEVICE_TYPE_TEAM to listOf("team", /* "team-port" */),
            NMDeviceType.NM_DEVICE_TYPE_BOND to listOf("bond",
                //"bond-port"
            ),
            )

        return simpleCheckMap[device.deviceType.toNMDeviceType()]?.contains(connectionType)
    }

    @VisibleForTesting
    private fun checkInterfaceNameExistsCheckConnectionMatchesDevices(device: Device, connection: Connection) : Boolean? {
        //These types just check that there is an interface name in their connection settings

        //First check the connectionType is ok
        val typeCheckMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_DUMMY to listOf("dummy"),
            NMDeviceType.NM_DEVICE_TYPE_GENERIC to listOf("generic"),
            NMDeviceType.NM_DEVICE_TYPE_OVS_INTERFACE to listOf("ovs-interface"),
            NMDeviceType.NM_DEVICE_TYPE_OVS_PORT to listOf("ovs-port"),
            NMDeviceType.NM_DEVICE_TYPE_OVS_BRIDGE to listOf("ovs-bridge"),
            NMDeviceType.NM_DEVICE_TYPE_LOOPBACK to listOf("loopback"),
            )

        if (device.deviceType.toNMDeviceType() !in typeCheckMap.keys) {
            return null
        }

        return if (typeCheckMap[device.deviceType.toNMDeviceType()]?.contains(
                getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection)
            ) == true) {
            //Then check the interface type is ok
            //These types only check that there is an interface name, they don't actually check that it's matching anything.
            getInterfaceNameFromConnectionUseCase.getInterfaceNameFromConnection(connection) != null
        } else {
            false
        }
    }

    private fun rejectNotSupportedNetworks(device: Device, connection: Connection) : Boolean? {
        //This function explicitly rejects network types I don't want to support.
        //My BMW, nor my phone will ever have an infiniband network, for example.

        val notSupportedMatchMap = mapOf<NMDeviceType, List<String>>(
            // In Server racks for SANs
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND to listOf("infiniband"),

            // Industrial ethernet
            // https://en.wikipedia.org/wiki/High-availability_Seamless_Redundancy
            NMDeviceType.NM_DEVICE_TYPE_HSR to listOf("hsr"),

            NMDeviceType.NM_DEVICE_TYPE_6LOWPAN to listOf("6lowpan"),

            NMDeviceType.NM_DEVICE_TYPE_WIMAX to listOf("wimax"),

            )

        if (device.deviceType.toNMDeviceType() in notSupportedMatchMap.keys) {
            logger.w(TAG, "Device type ${device.deviceType.toNMDeviceType()} is unsupported.")
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                topText = "Unsupported Network Device found",
                contentText = "Cannot setup devices of type ${device.deviceType.toNMDeviceType()}"
            ))
            return false
        }
        return null
    }

    private fun processWifiDevice(device: Device, connection: Connection) : Boolean? {

        //First, check that the connection type is wifi

        val typeCheckMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_WIFI to listOf("802-11-wireless"),
        )

        if (device.deviceType.toNMDeviceType() !in typeCheckMap.keys) {
            return null
        }

        if (typeCheckMap[device.deviceType.toNMDeviceType()]?.contains(
                getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection)
            ) == false) {
            return false
        }

        //Now check the MAC address


        //Check the MAC address is valid
        val connectionMac = connection.GetSettings().toMap()?.get("wireless")?.get("mac-address")?.let { it.value as String }
        if (connectionMac != null) {
            val deviceMac = device.hwAddress
            if (connectionMac != deviceMac) {
                logger.d(TAG, "The MACs of the device and connection didn't match")
                return false
            }
        }
        //Check the device and connection have a matching mac address
        //Check that the security type of the connection matches the security types supported by the device
        //TODO STEFAN Who cares? The CM5 has a wpa2 capable card and won't have a wep-only device in it.
        return true
    }

    private fun processEthernetDevice(device: Device, connection: Connection) : Boolean? {
        val typeCheckMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET to listOf(
                "pppoe",
                "802-3-ethernet",
                "veth"
            ),
        )

        if (device.deviceType.toNMDeviceType() !in typeCheckMap.keys) {
            return null
        }

        if (typeCheckMap[device.deviceType.toNMDeviceType()]?.contains(
                getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection)
            ) == false) {
            return false
        }

        //There's a bunch of checks here for PPoE, s390 subchannels that I skipped.

        return true
    }

    private fun processBtDevice(device: Device, connection: Connection) : Boolean? {
        val typeCheckMap = mapOf<NMDeviceType, List<String>>(
            NMDeviceType.NM_DEVICE_TYPE_BT to listOf(
                "bluetooth"
            ),
        )

        if (device.deviceType.toNMDeviceType() !in typeCheckMap.keys) {
            return null
        }

        if (typeCheckMap[device.deviceType.toNMDeviceType()]?.contains(
                getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection)
            ) == false) {
            return false
        }

        //Now we have to check that the connection isn't a BT NAP

        if (getConnectionTypeFromConnectionUseCase.getConnectionTypeFromConnection(connection) == "nap") {
            return false
        }

        // Now check that the bt address is valid
        // TODO STEFAN just assume it's fine.

        // Now check the connection bdaddr matches the device mac
        val deviceMac = device.hwAddress

        val connectionMacRaw = connection.GetSettings().toMap()["bluetooth"]?.get("bdaddr")?.value as? List<Int>?

        //Convert the bdaddr to a mac string so it can be compared
        val connectionMac = connectionMacRaw
            ?.fold("") { acc: String, i: Int -> acc + i.toString(16) }
            ?.windowed(2, 2, true) { "$it:" }
            ?.fold("") { acc: String, s: String -> acc + s }
            ?.removeSuffix(":")
            ?: "None"

        if (deviceMac != connectionMac) {
            logger.d(TAG, "Rejecting bt connection where connectioMac is ${connectionMac} and device mac is ${deviceMac}")
            return false
        }


        val bluetooth = convertDeviceToBluetoothUseCase.deviceToBluetooth(device)

        // Check device capabilities
//
//        /**
//         * NMBluetoothCapabilities:
//         * @NM_BT_CAPABILITY_NONE: device has no usable capabilities
//         * @NM_BT_CAPABILITY_DUN: device provides Dial-Up Networking capability
//         * @NM_BT_CAPABILITY_NAP: device provides Network Access Point capability
//         *
//         * #NMBluetoothCapabilities values indicate the usable capabilities of a
//         * Bluetooth device.
//         **/
//        typedef enum /*< flags >*/ {
//            NM_BT_CAPABILITY_NONE = 0x00000000,
//            NM_BT_CAPABILITY_DUN  = 0x00000001,
//            NM_BT_CAPABILITY_NAP  = 0x00000002,
//        } NMBluetoothCapabilities;

        //Will return a UInt32
        val deviceCapabilities = bluetooth.btCapabilities
        // Check conn capabilities
        // Will return a string like "panu"
        val connectionCapabilites = connection.GetSettings()
            .toMap()
            .get("bluetooth")
            ?.get("type")
            ?.value
            ?.let { it as? String }
            ?.let {
                when (it) {
                    "panu" -> {
                        // Personal Area Networking connections can connect to devices that
                        // support the Network Access (NAP) profile
                        UInt32(2L)
                    }
                    "nap" -> {
                        //Already filtered out above
                        //I think this is for making new bt hotspots that share some other connection over bt
                        // like how my iPhone shares it's gsm via bt
                        // We can't connect to our own Network Access Point (self-referential)
                        // so return 0L here so that there's no overlap
                        UInt32(0L)
                    }
                    "dun-gsm" -> {
                        UInt32(1L)
                    }
                    "dun-cdma" -> {
                        UInt32(1L)
                    }
                    else -> UInt32(0L)
                }
            }
            ?: UInt32(0L)

        // Bit-wise AND them and make sure the result isn't falsey
        return deviceCapabilities.toInt().and(connectionCapabilites.toInt()) != 0

    }

    companion object {
        const val TAG = "FilterConnectionsListForDeviceUseCase"
        val matchMap = mapOf<NMDeviceType, List<String>>(


                //TODO how do the types that don't have connections do stuff?
                NMDeviceType.NM_DEVICE_TYPE_MODEM to listOf(),

            // TODO ^^^^ actually finish these, they're important.

            //?
            NMDeviceType.NM_DEVICE_TYPE_VXLAN to listOf("vxlan"),

            NMDeviceType.NM_DEVICE_TYPE_PPP to listOf("ppp"),


            //TODO needs some othe processing
            NMDeviceType.NM_DEVICE_TYPE_VRF to listOf("vrf"),


            //TODO STEFAN Not sure yet.
            NMDeviceType.NM_DEVICE_TYPE_IPVLAN to listOf("ipvlan"),

            //TODO needs further check.
            NMDeviceType.NM_DEVICE_TYPE_GENEVE to listOf("geneve"),
        )
    }
}