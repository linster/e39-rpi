package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.populate

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.device.Bluetooth
import javax.inject.Inject

class GetDisambiguatedDeviceNameUseCase @Inject constructor(

) {

    //Lib NM has a method to disambiguate device names based on
    //DeviceType. It's not exported via DBus, but I can reimplement it here.

    //From nmt-connect-connection-list.c:
    // Line 470: names       = nm_device_disambiguate_names((NMDevice **) devices->pdata, devices->len);

    // Goes to nm_device_disambiguate_names in nm-device.c, Line 2210

    data class DeviceWithDisambiguatedName(
        val device: Device,
        val disambiguatedName: String
    )

    fun getDisambiguatedNamesList(devices : List<Device>) : List<DeviceWithDisambiguatedName> {
        return getDisambiguatedNames(devices).entries.map { (device, name) ->
            DeviceWithDisambiguatedName(
                device = device,
                disambiguatedName = name
            )
        }
    }

    fun getDisambiguatedNames(devices : List<Device>) : Map<Device, String> {
        /* Generic Device name */
        //Loop over the devices and for each one call
        //r = get_device_generic_type_name_with_iface()
        //If no duplicates, return a listOf(device to r).associate()

        val step1 = devices.associate { device ->
            device to getDeviceGenericTypeNameWithIFace(device)
        }
        if (findDuplicateKeys(step1).isEmpty()) {
            //No duplicates! Good to go!
            return step1
        }

        // Find out which ones are duplicated and replace only the duplicates
        // with getDeviceTypeNameWithIFace()
        val step2 = step1.toMutableMap()
        findDuplicateKeys(step1).forEach {
            step2[it] = getDeviceTypeNameWithIFace(it)
        }
        if (findDuplicateKeys(step2).isEmpty()) {
            return step2
        }

        /* Try prefixing bus name (eg, "PCI Ethernet" vs "USB Ethernet") */

        // TODO STEFAN this uses udev to find the bus name
        // skip step 3
        /* Try prefixing vendor name */

        // TODO STEFAN this uses udev to find the vendor name
        // skip step 4

        /* If dealing with Bluetooth devices, try to distinguish them by
         * device name.
         */
        val step5 = step2.toMutableMap()
        findDuplicateKeys(step2).forEach {
            if (it.deviceType.toNMDeviceType() == NMDeviceType.NM_DEVICE_TYPE_BT) {
                it as Bluetooth
                step5[it] = "${getDeviceTypeNameWithIFace(it)} (${it.name})"
            }
        }
        if (findDuplicateKeys(step5).isEmpty()) {
            return step5
        }

        /* We have multiple identical network cards, so we have to differentiate
         * them by interface name.
         */

        val step6 = step5.toMutableMap()
        findDuplicateKeys(step5).forEach {
            val interfaceName = it.getInterface()?: ""

            if (interfaceName.isNullOrBlank())
                return@forEach

            step6[it] = "${getTypeName(it)} (${interfaceName})"
        }

        return step6
    }

    fun <K, V> findDuplicateKeys(map: Map<K, V>) : Set<K> {
        //Optimization if there are no duplicate keys
        if (map.values.toList().size == map.values.toSet().size) {
            //No duplicates! Good to go!
            return emptySet()
        }

        val duplicatedKeys = mutableSetOf<K>()
        val seenValues = mutableSetOf<V>()

        for ((key, value) in map.entries) {
            if (seenValues.contains(value)) {
                duplicatedKeys.add(key)
            }
            seenValues.add(value)
        }
        return duplicatedKeys
    }

    /* get_device_generic_type_name_with_iface(NMDevice *device)
     * https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-client-impl/nm-device.c#L1916
     */
    private fun getDeviceGenericTypeNameWithIFace(device: Device) : String {
        return when (device.deviceType.toNMDeviceType()!!) {
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET,
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND -> "Wired"
            else -> getDeviceTypeNameWithIFace(device)
        }
    }

    /*
     * https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-client-impl/nm-device.c#L1900
     * get_device_type_name_with_iface(NMDevice *device)
     */
    private fun getDeviceTypeNameWithIFace(device : Device) : String {
        val typeName = getTypeName(device)

        return when (device.deviceType.toNMDeviceType()!!) {
            NMDeviceType.NM_DEVICE_TYPE_BOND,
            NMDeviceType.NM_DEVICE_TYPE_TEAM,
            NMDeviceType.NM_DEVICE_TYPE_BRIDGE,
            NMDeviceType.NM_DEVICE_TYPE_VLAN -> {
                "$typeName ${device.getInterface()}"
            }
            else -> typeName
        }
    }

    /* get_type_name(NMDevice *device)
     * https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-client-impl/nm-device.c#L1823
     */
    fun getTypeName(device: Device) : String {
        // The _ macros in the source are GLib translation macros:
        // https://docs.gtk.org/glib/i18n.html#macros
        // I'm going to keep the original names so that some day the .po files
        // from network manager can be brought in. (If I ever internationalize this lol)
        return when (device.deviceType.toNMDeviceType()!!) {
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET -> "Ethernet"
            NMDeviceType.NM_DEVICE_TYPE_WIFI -> "Wi-Fi"
            NMDeviceType.NM_DEVICE_TYPE_BT -> "Bluetooth"
            NMDeviceType.NM_DEVICE_TYPE_OLPC_MESH -> "OLPC Mesh"
            NMDeviceType.NM_DEVICE_TYPE_OVS_INTERFACE -> "Open vSwitch Interface"
            NMDeviceType.NM_DEVICE_TYPE_OVS_PORT -> "Open vSwitch Port"
            NMDeviceType.NM_DEVICE_TYPE_OVS_BRIDGE -> "Open vSwitch Bridge"
            NMDeviceType.NM_DEVICE_TYPE_WIMAX -> "WiMAX"
            NMDeviceType.NM_DEVICE_TYPE_MODEM -> "Mobile Broadband"
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND -> "InfiniBand"
            NMDeviceType.NM_DEVICE_TYPE_BOND -> "Bond"
            NMDeviceType.NM_DEVICE_TYPE_TEAM -> "Team"
            NMDeviceType.NM_DEVICE_TYPE_BRIDGE -> "Bridge"
            NMDeviceType.NM_DEVICE_TYPE_VLAN -> "VLAN"
            NMDeviceType.NM_DEVICE_TYPE_ADSL -> "ADSL"
            NMDeviceType.NM_DEVICE_TYPE_MACVLAN -> "MACVLAN"
            NMDeviceType.NM_DEVICE_TYPE_VXLAN -> "VXLAN"
            NMDeviceType.NM_DEVICE_TYPE_GENEVE -> "GENEVE"
            NMDeviceType.NM_DEVICE_TYPE_IP_TUNNEL -> "IPTunnel"
            NMDeviceType.NM_DEVICE_TYPE_TUN -> "Tun"
            NMDeviceType.NM_DEVICE_TYPE_VETH -> "Veth"
            NMDeviceType.NM_DEVICE_TYPE_MACSEC -> "MACsec"
            NMDeviceType.NM_DEVICE_TYPE_DUMMY -> "Dummy"
            NMDeviceType.NM_DEVICE_TYPE_PPP -> "PPP"
            NMDeviceType.NM_DEVICE_TYPE_WPAN -> "IEEE 802.15.4"
            NMDeviceType.NM_DEVICE_TYPE_6LOWPAN -> "6LoWPAN"
            NMDeviceType.NM_DEVICE_TYPE_WIREGUARD -> "WireGuard"
            NMDeviceType.NM_DEVICE_TYPE_WIFI_P2P -> "Wi-Fi P2P"
            NMDeviceType.NM_DEVICE_TYPE_VRF -> "VRF"
            NMDeviceType.NM_DEVICE_TYPE_LOOPBACK -> "Loopback"
            NMDeviceType.NM_DEVICE_TYPE_HSR -> "HSR"
            NMDeviceType.NM_DEVICE_TYPE_IPVLAN -> "IPVLAN"
            NMDeviceType.NM_DEVICE_TYPE_GENERIC,
            NMDeviceType.NM_DEVICE_TYPE_UNUSED1,
            NMDeviceType.NM_DEVICE_TYPE_UNUSED2,
            NMDeviceType.NM_DEVICE_TYPE_UNKNOWN -> "Unknown"
        }
    }
}