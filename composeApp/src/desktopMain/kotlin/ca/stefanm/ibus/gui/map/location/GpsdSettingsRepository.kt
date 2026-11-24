package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Where gpsd listens: host and port. */
data class GpsdSettings(
    val host: String,
    val port: Int,
) {
    val target: String get() = "$host:$port"

    companion object {
        val DEFAULT = GpsdSettings(
            host = E39Config.GpsConfig.gpsdHost.default,
            port = E39Config.GpsConfig.gpsdPort.default,
        )
    }
}

/**
 * gpsd's host and port, stored in config.conf (E39Config.GpsConfig).
 * GpsdService connects to whatever [settings] holds, and reconnects when it
 * changes.
 */
@ApplicationScope
class GpsdSettingsRepository @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "GpsdSettings"
    }

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<GpsdSettings> = _settings.asStateFlow()

    private fun read() = GpsdSettings(
        host = configurationStorage.config[E39Config.GpsConfig.gpsdHost],
        port = configurationStorage.config[E39Config.GpsConfig.gpsdPort],
    )

    /** @throws IllegalArgumentException for an empty host or a port outside 1..65535. */
    fun update(newSettings: GpsdSettings) {
        val cleaned = newSettings.copy(host = newSettings.host.trim())
        require(cleaned.host.isNotEmpty() && cleaned.host.none { it.isWhitespace() }) {
            "Invalid host '${newSettings.host}'"
        }
        require(cleaned.port in 1..65535) { "Invalid port ${newSettings.port}" }
        if (cleaned == _settings.value) return

        logger.i(TAG, "gpsd: ${_settings.value.target} -> ${cleaned.target}")
        configurationStorage.config[E39Config.GpsConfig.gpsdHost] = cleaned.host
        configurationStorage.config[E39Config.GpsConfig.gpsdPort] = cleaned.port
        _settings.value = cleaned
    }

    fun setHost(host: String) = update(_settings.value.copy(host = host))

    fun setPort(port: Int) = update(_settings.value.copy(port = port))

    fun resetToDefaults() = update(GpsdSettings.DEFAULT)
}
