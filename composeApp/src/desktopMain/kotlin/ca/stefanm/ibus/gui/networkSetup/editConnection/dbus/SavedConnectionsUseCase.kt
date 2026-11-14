package ca.stefanm.ibus.gui.networkSetup.editConnection.dbus

import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant
import org.freedesktop.networkmanager.Settings
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

typealias ConnectionSettings = MutableMap<String, MutableMap<String, Variant<*>>>

//Reads and edits the connection profiles NetworkManager has saved (org.freedesktop.NetworkManager.Settings).
class SavedConnectionsUseCase @Inject constructor(
    private val logger: Logger,
    private val getConnectionsUseCase: GetConnectionsUseCase
) {

    companion object {
        const val TAG = "SavedConnectionsUseCase"

        const val NM_BUS_NAME = "org.freedesktop.NetworkManager"
        const val NM_SETTINGS_PATH = "/org/freedesktop/NetworkManager/Settings"

        const val SETTING_CONNECTION = "connection"
        const val SETTING_WIFI = "802-11-wireless"
        const val SETTING_WIFI_SECURITY = "802-11-wireless-security"

        const val TYPE_WIFI = "802-11-wireless"
        const val TYPE_ETHERNET = "802-3-ethernet"
        const val TYPE_BLUETOOTH = "bluetooth"
        const val TYPE_VPN = "vpn"
        const val TYPE_WIREGUARD = "wireguard"

        val VPN_TYPES = setOf(TYPE_VPN, TYPE_WIREGUARD)

        fun prettyType(type : String) : String = when (type) {
            TYPE_WIFI -> "Wi-Fi"
            TYPE_ETHERNET -> "Ethernet"
            TYPE_BLUETOOTH -> "Bluetooth"
            TYPE_VPN -> "VPN"
            TYPE_WIREGUARD -> "WireGuard"
            "gsm" -> "Mobile (GSM)"
            "cdma" -> "Mobile (CDMA)"
            "bridge" -> "Bridge"
            "bond" -> "Bond"
            "vlan" -> "VLAN"
            "loopback" -> "Loopback"
            else -> type
        }

        fun ssidFromVariantValue(value : Any?) : String? {
            return when (value) {
                is ByteArray -> String(value)
                is List<*> -> String(value.filterIsInstance<Byte>().toByteArray())
                else -> null
            }
        }
    }

    data class SavedConnection(
        val path : DBusPath,
        val id : String,
        val uuid : String?,
        val type : String,
        val interfaceName : String?,
        val autoconnect : Boolean,
        val ssid : String?,
        val keyMgmt : String?
    ) {
        val isWifi get() = type == TYPE_WIFI
        val isVpn get() = type in VPN_TYPES
        val hasWifiPassword get() = isWifi && keyMgmt in setOf("wpa-psk", "sae")
    }

    private val refreshTrigger = MutableStateFlow(0)

    //Edits made through this use case don't change the Settings.Connections list,
    //so poke the flow to re-read the settings afterwards.
    private fun refresh() {
        refreshTrigger.value += 1
    }

    fun getSavedConnections() : Flow<List<SavedConnection>> {
        return getConnectionsUseCase.getConnections()
            .combine(refreshTrigger) { connections, _ ->
                connections.mapNotNull { connection ->
                    try {
                        toSavedConnection(DBusPath(connection.objectPath), connection.GetSettings())
                    } catch (e : Throwable) {
                        //The connection can be deleted between listing and reading it.
                        logger.w(TAG, "Could not read settings for ${connection.objectPath}: ${e.message}")
                        null
                    }
                }.sortedWith(compareBy<SavedConnection> { prettyType(it.type) }.thenBy { it.id.lowercase() })
            }
            .catch {
                logger.e(TAG, "Error listing saved connections", it)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    private fun toSavedConnection(path : DBusPath, settings : Map<String, Map<String, Variant<*>>>) : SavedConnection? {
        val connection = settings[SETTING_CONNECTION] ?: return null
        val type = connection["type"]?.value as? String ?: return null
        return SavedConnection(
            path = path,
            id = connection["id"]?.value as? String ?: path.path,
            uuid = connection["uuid"]?.value as? String,
            type = type,
            interfaceName = connection["interface-name"]?.value as? String,
            //NetworkManager leaves autoconnect out of the settings when it's the default (true).
            autoconnect = connection["autoconnect"]?.value as? Boolean ?: true,
            ssid = ssidFromVariantValue(settings[SETTING_WIFI]?.get("ssid")?.value),
            keyMgmt = settings[SETTING_WIFI_SECURITY]?.get("key-mgmt")?.value as? String
        )
    }

    private fun <T> withSystemBus(block : (DBusConnection) -> T) : T {
        val dbus = DBusConnectionBuilder.forSystemBus().build()
        dbus.connect()
        return dbus.use { block(it) }
    }

    private fun DBusConnection.getConnection(path : DBusPath) : Connection {
        return getRemoteObject(NM_BUS_NAME, path.path, Connection::class.java)
    }

    fun findSavedWifiConnections(ssid : String) : List<DBusPath> {
        return withSystemBus { dbus ->
            val settings = dbus.getRemoteObject(NM_BUS_NAME, NM_SETTINGS_PATH, Settings::class.java)
            settings.ListConnections().filter { path ->
                try {
                    val saved = toSavedConnection(path, dbus.getConnection(path).GetSettings())
                    saved != null && saved.isWifi && saved.ssid == ssid
                } catch (e : Throwable) {
                    logger.w(TAG, "Could not read settings for ${path.path}: ${e.message}")
                    false
                }
            }
        }
    }

    fun findSavedWifiConnection(ssid : String) : SavedConnection? {
        val path = findSavedWifiConnections(ssid).firstOrNull() ?: return null
        return withSystemBus { dbus -> toSavedConnection(path, dbus.getConnection(path).GetSettings()) }
    }

    fun rename(path : DBusPath, newId : String) {
        update(path) { settings ->
            settings.getOrPut(SETTING_CONNECTION) { mutableMapOf() }["id"] = Variant(newId)
        }
    }

    fun setAutoconnect(path : DBusPath, autoconnect : Boolean) {
        update(path) { settings ->
            settings.getOrPut(SETTING_CONNECTION) { mutableMapOf() }["autoconnect"] = Variant(autoconnect)
        }
    }

    fun setWifiPassword(path : DBusPath, password : String) {
        update(path) { settings ->
            val security = settings.getOrPut(SETTING_WIFI_SECURITY) { mutableMapOf() }
            if (security["key-mgmt"] == null) {
                security["key-mgmt"] = Variant("wpa-psk")
            }
            security["psk"] = Variant(password)
            //Make sure NetworkManager stores the new password instead of asking an agent for it.
            security["psk-flags"] = Variant(UInt32(0))
        }
    }

    fun delete(path : DBusPath) {
        withSystemBus { dbus -> dbus.getConnection(path).Delete() }
        refresh()
    }

    //Returns how many saved profiles were deleted.
    fun forgetWifi(ssid : String) : Int {
        val paths = findSavedWifiConnections(ssid)
        withSystemBus { dbus ->
            paths.forEach { dbus.getConnection(it).Delete() }
        }
        refresh()
        return paths.size
    }

    //Update() replaces every setting of the profile, so read the current settings (and the wifi secrets,
    //which GetSettings() leaves out) and send them back with the change applied.
    private fun update(path : DBusPath, mutate : (ConnectionSettings) -> Unit) {
        withSystemBus { dbus ->
            val connection = dbus.getConnection(path)
            val settings : ConnectionSettings = connection.GetSettings()
                .mapValues { it.value.toMutableMap() }
                .toMutableMap()

            if (settings.containsKey(SETTING_WIFI_SECURITY)) {
                try {
                    connection.GetSecrets(SETTING_WIFI_SECURITY)[SETTING_WIFI_SECURITY]?.let { secrets ->
                        settings[SETTING_WIFI_SECURITY]?.putAll(secrets)
                    }
                } catch (e : Throwable) {
                    logger.w(TAG, "Could not read wifi secrets for ${path.path}: ${e.message}")
                }
            }

            mutate(settings)
            connection.Update(settings)
        }
        refresh()
    }
}
