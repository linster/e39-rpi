package ca.stefanm.ibus.gui.map.widget.tile

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.MapConfig.TileSource
import javax.inject.Inject
import kotlin.math.sin

/**
 * Where map tiles come from (E39Config.MapConfig.tileSource): OpenStreetMap's
 * tile server, or the maps backend (e39-rpi-maps), whose /tiles serves its
 * own cache (offline too) and fetches misses from OpenStreetMap.
 *
 * Read from the configuration on every call: TileView builds a new component
 * per tile, so a change applies to the next tile fetched.
 */
class OSMTileServerInfo @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
) {
    data class Server(
        val source: TileSource,
        /** With {z}, {x}, {y}. */
        val urlTemplate: String,
        /** The local cache's folder for this server (tiles differ per server). */
        val cacheFolder: String,
    )

    fun current(): Server {
        val config = configurationStorage.config
        return when (config[E39Config.MapConfig.tileSource]) {
            TileSource.OPENSTREETMAP -> Server(
                TileSource.OPENSTREETMAP, "https://tile.openstreetmap.org/{z}/{x}/{y}.png", "openstreetmap")
            TileSource.MAPS_BACKEND -> {
                val host = config[E39Config.MapConfig.Backend.host]
                val port = config[E39Config.MapConfig.Backend.httpPort]
                Server(TileSource.MAPS_BACKEND, "http://$host:$port/tiles/{z}/{x}/{y}.png",
                    "maps-backend-${host.replace(Regex("[^A-Za-z0-9.-]"), "_")}-$port")
            }
        }
    }

    fun getTileUrl(x : Int, y : Int, zoom : Int) : String =
        current().urlTemplate.replace("{z}", "$zoom").replace("{x}", "$x").replace("{y}", "$y")
}

object MercartorCalc {

    /**
     * @param longitudeDegrees the longitude in degrees
     * @param radius the world radius in pixels
     * @return the x value
     */
    fun longToX(longitudeDegrees: Double, radius: Double): Int {
        val longitude = Math.toRadians(longitudeDegrees)
        return (radius * longitude).toInt()
    }

    /**
     * @param latitudeDegrees the latitude in degrees
     * @param radius the world radius in pixels
     * @return the y value
     */
    fun latToY(latitudeDegrees: Double, radius: Double): Int {
        val latitude = Math.toRadians(latitudeDegrees)
        val y = radius / 2.0 * Math.log((1.0 + sin(latitude)) / (1.0 - Math.sin(latitude)))
        return y.toInt()
    }

    /**
     * @param x the x value
     * @param radius the world radius in pixels
     * @return the longitude in degrees
     */
    fun xToLong(x: Int, radius: Double): Double {
        val longRadians = x / radius
        val longDegrees = Math.toDegrees(longRadians)
        /*
         * The user could have panned around the world a lot of times. Lat long goes from -180 to 180. So every time a
         * user gets to 181 we want to subtract 360 degrees. Every time a user gets to -181 we want to add 360 degrees.
         */
        val rotations = Math.floor((longDegrees + 180) / 360).toInt()
        return longDegrees - rotations * 360
    }

    /**
     * @param y the y value
     * @param radius the world radius in pixels
     * @return the latitude in degrees
     */
    fun yToLat(y: Int, radius: Double): Double {
        val latitude = Math.PI / 2 - 2 * Math.atan(Math.exp(-1.0 * y / radius))
        return Math.toDegrees(latitude)
    }

}