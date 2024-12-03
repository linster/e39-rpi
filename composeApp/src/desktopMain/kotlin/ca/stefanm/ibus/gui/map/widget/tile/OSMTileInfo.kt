package ca.stefanm.ca.stefanm.ibus.gui.map.widget.tile

import androidx.compose.ui.unit.dp
import javax.inject.Inject
import kotlin.math.sin

class OSMTileServerInfo @Inject constructor() {

    fun getTileUrl(x : Int, y : Int, zoom : Int) : String {
        return "http://tile.openstreetmap.org/$zoom/$x/$y.png"
    }
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