package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

object NM80211ApSecurityFlags {

    /**
     * NM80211ApSecurityFlags:
     * @NM_802_11_AP_SEC_NONE: the access point has no special security requirements
     * @NM_802_11_AP_SEC_PAIR_WEP40: 40/64-bit WEP is supported for
     * pairwise/unicast encryption
     * @NM_802_11_AP_SEC_PAIR_WEP104: 104/128-bit WEP is supported for
     * pairwise/unicast encryption
     * @NM_802_11_AP_SEC_PAIR_TKIP: TKIP is supported for pairwise/unicast encryption
     * @NM_802_11_AP_SEC_PAIR_CCMP: AES/CCMP is supported for pairwise/unicast encryption
     * @NM_802_11_AP_SEC_GROUP_WEP40: 40/64-bit WEP is supported for group/broadcast
     * encryption
     * @NM_802_11_AP_SEC_GROUP_WEP104: 104/128-bit WEP is supported for
     * group/broadcast encryption
     * @NM_802_11_AP_SEC_GROUP_TKIP: TKIP is supported for group/broadcast encryption
     * @NM_802_11_AP_SEC_GROUP_CCMP: AES/CCMP is supported for group/broadcast
     * encryption
     * @NM_802_11_AP_SEC_KEY_MGMT_PSK: WPA/RSN Pre-Shared Key encryption is
     * supported
     * @NM_802_11_AP_SEC_KEY_MGMT_802_1X: 802.1x authentication and key management
     * is supported
     * @NM_802_11_AP_SEC_KEY_MGMT_SAE: WPA/RSN Simultaneous Authentication of Equals is
     * supported
     * @NM_802_11_AP_SEC_KEY_MGMT_OWE: WPA/RSN Opportunistic Wireless Encryption is
     * supported
     * @NM_802_11_AP_SEC_KEY_MGMT_OWE_TM: WPA/RSN Opportunistic Wireless Encryption
     * transition mode is supported. Since: 1.26.
     * @NM_802_11_AP_SEC_KEY_MGMT_EAP_SUITE_B_192: WPA3 Enterprise Suite-B 192 bit mode
     * is supported. Since: 1.30.
     *
     * 802.11 access point security and authentication flags.  These flags describe
     * the current security requirements of an access point as determined from the
     * access point's beacon.
     **/
    enum class Flag(val raw : Int) {
        //typedef enum /*< underscore_name=nm_802_11_ap_security_flags, flags >*/ {
        NM_802_11_AP_SEC_NONE                     (raw = 0x00000000),
        NM_802_11_AP_SEC_PAIR_WEP40               (raw = 0x00000001),
        NM_802_11_AP_SEC_PAIR_WEP104              (raw = 0x00000002),
        NM_802_11_AP_SEC_PAIR_TKIP                (raw = 0x00000004),
        NM_802_11_AP_SEC_PAIR_CCMP                (raw = 0x00000008),
        NM_802_11_AP_SEC_GROUP_WEP40              (raw = 0x00000010),
        NM_802_11_AP_SEC_GROUP_WEP104             (raw = 0x00000020),
        NM_802_11_AP_SEC_GROUP_TKIP               (raw = 0x00000040),
        NM_802_11_AP_SEC_GROUP_CCMP               (raw = 0x00000080),
        NM_802_11_AP_SEC_KEY_MGMT_PSK             (raw = 0x00000100),
        NM_802_11_AP_SEC_KEY_MGMT_802_1X          (raw = 0x00000200),
        NM_802_11_AP_SEC_KEY_MGMT_SAE             (raw = 0x00000400),
        NM_802_11_AP_SEC_KEY_MGMT_OWE             (raw = 0x00000800),
        NM_802_11_AP_SEC_KEY_MGMT_OWE_TM          (raw = 0x00001000),
        NM_802_11_AP_SEC_KEY_MGMT_EAP_SUITE_B_192 (raw = 0x00002000),
    }

    fun flagsFromInt(raw : Int) : List<Flag> {
        val result = mutableListOf<Flag>()
        Flag.entries.forEach { flag ->
            if ((raw and flag.raw) != 0) result.add(flag)
        }
        return result
    }

    fun prettyPrint(flag : Flag) : String {
        return when (flag) {
            Flag.NM_802_11_AP_SEC_NONE -> "No security"
            Flag.NM_802_11_AP_SEC_PAIR_WEP40 -> "WEP 40/64-bit"
            Flag.NM_802_11_AP_SEC_PAIR_WEP104 -> "WEP 104/128-bit"
            Flag.NM_802_11_AP_SEC_PAIR_TKIP -> "TKIP"
            Flag.NM_802_11_AP_SEC_PAIR_CCMP -> "AES/CCMP"
            Flag.NM_802_11_AP_SEC_GROUP_WEP40 -> "WEP 40/64-bit"
            Flag.NM_802_11_AP_SEC_GROUP_WEP104 -> "WEP 104/128-bit"
            Flag.NM_802_11_AP_SEC_GROUP_TKIP -> "TKIP"
            Flag.NM_802_11_AP_SEC_GROUP_CCMP -> "AES/CCMP"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_PSK -> "WPA PSK (Pre-shared key)"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_802_1X -> "802.1X encryption/auth"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_SAE -> "WPA SAE (Simultaneous Authentication of Equals)"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_OWE -> "WPA Opportunistic Wireless Encryption"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_OWE_TM -> "WPA Opportunistic Wireless Encryption transition mode"
            Flag.NM_802_11_AP_SEC_KEY_MGMT_EAP_SUITE_B_192 -> "WPA3 Enterprise Suite-B 192-bit mode"
        }
    }
}