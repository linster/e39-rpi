package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.HmiVersion
import ca.stefanm.ibus.lib.logging.Logger
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.inject.Inject

/**
 * Which map the maps backend has installed, for the About screen.
 *
 * Asked from the backend (its web port's /healthz: the map database's
 * facts), so it's current right after a live swap or USB import. When the
 * backend can't say, [mapVersion] falls back to version.conf's mapVersion,
 * which the device's map installer records on every install and at boot.
 */
class InstalledMapRepository @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "InstalledMapRepository"
        private val TIMEOUT: Duration = Duration.ofSeconds(3)

        /** "Ottawa (530 km), built 2026-11-13" from the map database's facts. */
        fun describe(meta: Map<String, String>): String? {
            val builtAt = meta["built_at"] ?: return null
            // Maps from before names: the toolkit's region name, without its
            // long "+530 km (London ON - Quebec City), Canada" tail.
            val label = meta["region_label"]?.takeIf { it.isNotBlank() }
                ?: meta["region"]?.substringBefore(" +")?.trim()?.takeIf { it.isNotBlank() }
                ?: "Map"
            val radius = meta["radius_km"]?.let { " ($it km)" } ?: ""
            return "$label$radius, built ${builtAt.take(10)}"
        }

        /** The map database's facts from a /healthz answer; null when it has no usable map. */
        fun mapFacts(healthz: JsonObject): Map<String, String>? {
            val mapDb = healthz.getAsJsonObject("map_db") ?: return null
            if (mapDb.get("available")?.asBoolean != true) return null
            val meta = mapDb.getAsJsonObject("meta") ?: return null
            return meta.entrySet().associate { (k, v) -> k to (if (v.isJsonPrimitive) v.asString else v.toString()) }
        }
    }

    private val http: HttpClient by lazy { HttpClient.newBuilder().connectTimeout(TIMEOUT).build() }

    /**
     * The About screen's line: from the backend, else what version.conf
     * recorded, saying so.
     */
    suspend fun mapVersion(): String = withContext(Dispatchers.IO) {
        try {
            val config = configurationStorage.config
            val url = "http://${config[E39Config.MapConfig.Backend.host]}:${config[E39Config.MapConfig.Backend.httpPort]}/healthz"
            val response = http.send(
                HttpRequest.newBuilder(URI(url)).timeout(TIMEOUT).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            )
            val facts = mapFacts(JsonParser.parseString(response.body()).asJsonObject)
            facts?.let(::describe) ?: "none installed"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(TAG, "Maps backend: ${e.message ?: e.javaClass.simpleName}")
            val recorded = configurationStorage.versionFileNow()[HmiVersion.mapVersion]
            "$recorded (backend not reachable)"
        }
    }
}
