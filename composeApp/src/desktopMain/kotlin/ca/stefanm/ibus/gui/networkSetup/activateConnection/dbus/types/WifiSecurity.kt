package ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NM80211ApSecurityFlags.Flag
import org.freedesktop.networkmanager.AccessPoint

//The kind of security a new wifi connection needs, worked out from the access point's beacon flags.
//keyMgmt is the value for "802-11-wireless-security"."key-mgmt" in the connection settings.
enum class WifiSecurity(
    val label : String,
    val keyMgmt : String?,
    val needsPassword : Boolean,
    val isSupported : Boolean
) {
    OPEN(label = "Open", keyMgmt = null, needsPassword = false, isSupported = true),
    OWE(label = "Enhanced Open (OWE)", keyMgmt = "owe", needsPassword = false, isSupported = true),
    WPA_PSK(label = "WPA/WPA2 Personal", keyMgmt = "wpa-psk", needsPassword = true, isSupported = true),
    SAE(label = "WPA3 Personal", keyMgmt = "sae", needsPassword = true, isSupported = true),
    WEP(label = "WEP", keyMgmt = null, needsPassword = true, isSupported = false),
    ENTERPRISE(label = "WPA Enterprise (802.1X)", keyMgmt = null, needsPassword = true, isSupported = false);

    fun isPasswordValid(password : String) : Boolean {
        return when (this) {
            //WPA passphrases are 8..63 characters, or exactly 64 hex digits.
            WPA_PSK -> password.length in 8..63
                    || (password.length == 64 && password.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })
            SAE -> password.isNotEmpty()
            else -> true
        }
    }

    companion object {
        //NM80211ApFlags
        private const val NM_802_11_AP_FLAGS_PRIVACY = 0x1

        fun fromAccessPoint(ap : AccessPoint) : WifiSecurity {
            return fromFlags(
                apFlags = ap.flags.toInt(),
                wpaFlags = ap.wpaFlags.toInt(),
                rsnFlags = ap.rsnFlags.toInt()
            )
        }

        fun fromFlags(apFlags : Int, wpaFlags : Int, rsnFlags : Int) : WifiSecurity {
            val flags = NM80211ApSecurityFlags.flagsFromInt(wpaFlags or rsnFlags).toSet()

            return when {
                Flag.NM_802_11_AP_SEC_KEY_MGMT_802_1X in flags -> ENTERPRISE
                Flag.NM_802_11_AP_SEC_KEY_MGMT_EAP_SUITE_B_192 in flags -> ENTERPRISE
                //WPA2/WPA3 transition mode networks accept wpa-psk.
                Flag.NM_802_11_AP_SEC_KEY_MGMT_PSK in flags -> WPA_PSK
                Flag.NM_802_11_AP_SEC_KEY_MGMT_SAE in flags -> SAE
                Flag.NM_802_11_AP_SEC_KEY_MGMT_OWE in flags -> OWE
                flags.isEmpty() && (apFlags and NM_802_11_AP_FLAGS_PRIVACY) != 0 -> WEP
                else -> OPEN
            }
        }
    }
}
