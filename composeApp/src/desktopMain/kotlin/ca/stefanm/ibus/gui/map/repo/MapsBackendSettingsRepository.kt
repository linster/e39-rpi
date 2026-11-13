package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Where the maps backend's gRPC API is: host and port. */
data class MapsBackendSettings(
    val host: String,
    val port: Int,
) {
    val target: String get() = "$host:$port"

    companion object {
        val DEFAULT = MapsBackendSettings(
            host = E39Config.MapConfig.Backend.host.default,
            port = E39Config.MapConfig.Backend.port.default,
        )
    }
}

/**
 * The maps backend client settings, stored in config.conf
 * (E39Config.MapConfig.Backend). [MapsBackendChannel] connects to whatever
 * [settings] currently holds, so changing them reconnects every client.
 */
@ApplicationScope
class MapsBackendSettingsRepository @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "MapsBackendSettings"
    }

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<MapsBackendSettings> = _settings.asStateFlow()

    private fun read() = MapsBackendSettings(
        host = configurationStorage.config[E39Config.MapConfig.Backend.host],
        port = configurationStorage.config[E39Config.MapConfig.Backend.port],
    )

    /** @throws IllegalArgumentException for an empty host or a port outside 1..65535. */
    fun update(newSettings: MapsBackendSettings) {
        val cleaned = newSettings.copy(host = newSettings.host.trim())
        require(cleaned.host.isNotEmpty() && cleaned.host.none { it.isWhitespace() }) {
            "Invalid host '${newSettings.host}'"
        }
        require(cleaned.port in 1..65535) { "Invalid port ${newSettings.port}" }
        if (cleaned == _settings.value) return

        logger.i(TAG, "Maps backend: ${_settings.value.target} -> ${cleaned.target}")
        configurationStorage.config[E39Config.MapConfig.Backend.host] = cleaned.host
        configurationStorage.config[E39Config.MapConfig.Backend.port] = cleaned.port
        _settings.value = cleaned
    }

    fun setHost(host: String) = update(_settings.value.copy(host = host))

    fun setPort(port: Int) = update(_settings.value.copy(port = port))

    fun resetToDefaults() = update(MapsBackendSettings.DEFAULT)
}
