package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

import org.freedesktop.dbus.types.UInt32

//https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-core-public/nm-dbus-interface.h#L217


/**
 * NMDeviceType:
 *
* #NMDeviceType values indicate the type of hardware represented by a
* device object.
**/
enum class NMDeviceType(val dbusValue: Int) {
    /* @NM_DEVICE_TYPE_UNKNOWN: unknown device */
    NM_DEVICE_TYPE_UNKNOWN(dbusValue = 0),

    /* * @NM_DEVICE_TYPE_ETHERNET: a wired ethernet device */
    NM_DEVICE_TYPE_ETHERNET(dbusValue = 1),

    /* * @NM_DEVICE_TYPE_WIFI: an 802.11 Wi-Fi device */
    NM_DEVICE_TYPE_WIFI(dbusValue = 2),

    /* * @NM_DEVICE_TYPE_UNUSED1: not used */
    NM_DEVICE_TYPE_UNUSED1(dbusValue = 3),

    /* * @NM_DEVICE_TYPE_UNUSED2: not used */
    NM_DEVICE_TYPE_UNUSED2(dbusValue = 4),

    /* * @NM_DEVICE_TYPE_BT: a Bluetooth device supporting PAN or DUN access protocols */
    NM_DEVICE_TYPE_BT(dbusValue = 5), /* Bluetooth */

    /* * @NM_DEVICE_TYPE_OLPC_MESH: an OLPC XO mesh networking device */
    NM_DEVICE_TYPE_OLPC_MESH(dbusValue = 6),

    /* * @NM_DEVICE_TYPE_WIMAX: an 802.16e Mobile WiMAX broadband device */
    NM_DEVICE_TYPE_WIMAX(dbusValue = 7),

    /* * @NM_DEVICE_TYPE_MODEM: a modem supporting analog telephone, CDMA/EVDO, GSM/UMTS, or LTE network access protocols */
    NM_DEVICE_TYPE_MODEM(dbusValue = 8),

    /* * @NM_DEVICE_TYPE_INFINIBAND: an IP-over-InfiniBand device */
    NM_DEVICE_TYPE_INFINIBAND(dbusValue = 9),

    /* * @NM_DEVICE_TYPE_BOND: a bond controller interface */
    NM_DEVICE_TYPE_BOND(dbusValue = 10),

    /* * @NM_DEVICE_TYPE_VLAN: an 802.1Q VLAN interface */
    NM_DEVICE_TYPE_VLAN(dbusValue = 11),

    /* * @NM_DEVICE_TYPE_ADSL: ADSL modem */
    NM_DEVICE_TYPE_ADSL(dbusValue = 12),

    /* * @NM_DEVICE_TYPE_BRIDGE: a bridge controller interface */
    NM_DEVICE_TYPE_BRIDGE(dbusValue = 13),

    /* * @NM_DEVICE_TYPE_GENERIC: generic support for unrecognized device types */
    NM_DEVICE_TYPE_GENERIC(dbusValue = 14),

    /* * @NM_DEVICE_TYPE_TEAM: a team controller interface */
    NM_DEVICE_TYPE_TEAM(dbusValue = 15),

    /* * @NM_DEVICE_TYPE_TUN: a TUN or TAP interface */
    NM_DEVICE_TYPE_TUN(dbusValue = 16),

    /* * @NM_DEVICE_TYPE_IP_TUNNEL: a IP tunnel interface */
    NM_DEVICE_TYPE_IP_TUNNEL(dbusValue = 17),

    /* * @NM_DEVICE_TYPE_MACVLAN: a MACVLAN interface */
    NM_DEVICE_TYPE_MACVLAN(dbusValue = 18),

    /* * @NM_DEVICE_TYPE_VXLAN: a VXLAN interface */
    NM_DEVICE_TYPE_VXLAN(dbusValue = 19),

    /* * @NM_DEVICE_TYPE_VETH: a VETH interface */
    NM_DEVICE_TYPE_VETH(dbusValue = 20),

    /* * @NM_DEVICE_TYPE_MACSEC: a MACsec interface */
    NM_DEVICE_TYPE_MACSEC(dbusValue = 21),

    /* * @NM_DEVICE_TYPE_DUMMY: a dummy interface */
    NM_DEVICE_TYPE_DUMMY(dbusValue = 22),

    /* * @NM_DEVICE_TYPE_PPP: a PPP interface */
    NM_DEVICE_TYPE_PPP(dbusValue = 23),

    /* * @NM_DEVICE_TYPE_OVS_INTERFACE: a Open vSwitch interface */
    NM_DEVICE_TYPE_OVS_INTERFACE(dbusValue = 24),

    /* * @NM_DEVICE_TYPE_OVS_PORT: a Open vSwitch port */
    NM_DEVICE_TYPE_OVS_PORT(dbusValue = 25),

    /* * @NM_DEVICE_TYPE_OVS_BRIDGE: a Open vSwitch bridge */
    NM_DEVICE_TYPE_OVS_BRIDGE(dbusValue = 26),

    /* * @NM_DEVICE_TYPE_WPAN: a IEEE 802.15.4 (WPAN) MAC Layer Device */
    NM_DEVICE_TYPE_WPAN(dbusValue = 27),

    /* * @NM_DEVICE_TYPE_6LOWPAN: 6LoWPAN interface */
    NM_DEVICE_TYPE_6LOWPAN(dbusValue = 28),

    /* * @NM_DEVICE_TYPE_WIREGUARD: a WireGuard interface */
    NM_DEVICE_TYPE_WIREGUARD(dbusValue = 29),

    /* * @NM_DEVICE_TYPE_WIFI_P2P: an 802.11 Wi-Fi P2P device. Since: 1.16. */
    NM_DEVICE_TYPE_WIFI_P2P(dbusValue = 30),

    /* * @NM_DEVICE_TYPE_VRF: A VRF (Virtual Routing and Forwarding) interface. Since: 1.24. */
    NM_DEVICE_TYPE_VRF(dbusValue = 31),

    /* * @NM_DEVICE_TYPE_LOOPBACK: a loopback interface. Since: 1.42. */
    NM_DEVICE_TYPE_LOOPBACK(dbusValue = 32),

    /* * @NM_DEVICE_TYPE_HSR: A HSR/PRP device. Since: 1.46. */
    NM_DEVICE_TYPE_HSR(dbusValue = 33),

    /* * @NM_DEVICE_TYPE_IPVLAN: A IPVLAN device. Since: 1.52. */
    NM_DEVICE_TYPE_IPVLAN(dbusValue = 34),

    /* * @NM_DEVICE_TYPE_GENEVE: A GENEVE device. Since: 1.58. */
    NM_DEVICE_TYPE_GENEVE(dbusValue = 35);

    companion object {
        fun UInt32.toNMDeviceType(): NMDeviceType? {
            //TODO this could one day just use a stored map
            return entries.find { it.dbusValue == this.toInt() }
        }
    }
}