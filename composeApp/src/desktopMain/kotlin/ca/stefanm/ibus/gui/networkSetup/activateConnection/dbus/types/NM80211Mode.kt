package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

object NM80211Mode {

    /**
     * NM80211Mode:
     * @NM_802_11_MODE_UNKNOWN: the device or access point mode is unknown
     * @NM_802_11_MODE_ADHOC: for both devices and access point objects, indicates
     *   the object is part of an Ad-Hoc 802.11 network without a central
     *   coordinating access point.
     * @NM_802_11_MODE_INFRA: the device or access point is in infrastructure mode.
     *   For devices, this indicates the device is an 802.11 client/station.  For
     *   access point objects, this indicates the object is an access point that
     *   provides connectivity to clients.
     * @NM_802_11_MODE_AP: the device is an access point/hotspot.  Not valid for
     *   access point objects; used only for hotspot mode on the local machine.
     * @NM_802_11_MODE_MESH: the device is a 802.11s mesh point. Since: 1.20.
     *
     * Indicates the 802.11 mode an access point or device is currently in.
     **/
    enum class Mode(val raw : Int) {
        NM_802_11_MODE_UNKNOWN (raw = 0),
        NM_802_11_MODE_ADHOC   (raw = 1),
        NM_802_11_MODE_INFRA   (raw = 2),
        NM_802_11_MODE_AP      (raw = 3),
        NM_802_11_MODE_MESH    (raw = 4),
    }

    fun toSidebarList(mode : Int) : String {
        return when (Mode.entries.find { it.raw == mode }) {
            Mode.NM_802_11_MODE_UNKNOWN -> "Unknown"
            Mode.NM_802_11_MODE_ADHOC -> "Adhoc"
            Mode.NM_802_11_MODE_INFRA -> "Infrastructure (For device object, device is client/station. For AP object, means connectivity provided to clients)"
            Mode.NM_802_11_MODE_AP -> "Access point. (Local machine is hotspot)"
            Mode.NM_802_11_MODE_MESH -> "Mesh"
            null -> TODO()
        }
    }
}