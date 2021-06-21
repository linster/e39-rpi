package ca.stefanm.ibus.gui.map.widget

import org.jxmapviewer.viewer.GeoPosition
import kotlin.math.pow


//TODO we need a function to calculate a new map center given
//TODO an old mapcenter, the current zoom, and a direction.


object ExtentCalculator {


    fun newMapCenterOnPan(
        oldMapCenter : GeoPosition,
        currentZoom : Int,
        positiveMovement : Boolean,
        horizontal : Boolean,
        stepsPerTile : Int = 8
    ) : GeoPosition {
        //If horizontal and clicks positive, moving right
        //If horizontal and clicks negative, moving left.


        //Calculate with one tile per step, then multiply by the step factor
        //to scale how much we've moved.



    }

    //TODO
    //TODO I need a method to find out, inside a tile, how far to scroll the tile
    //TODO to get to the coord I want.


    data class TileBounds(
        val topLeft : GeoPosition,
        val bottomRight : GeoPosition
    )

    fun tileBounds(x: Int, y: Int, zoom: Int) : TileBounds {
        return TileBounds(
            topLeft = GeoPosition(
                tile2lat(y, zoom),
                tile2lon(x, zoom)
            ),
            bottomRight = GeoPosition(
                tile2lat(y+1, zoom),
                tile2lon(x+1, zoom)
            )
        )
    }

    fun tileCenter(x : Int, y : Int, zoom: Int) : GeoPosition {
        val bounds = tileBounds(x, y, zoom)
        val avgLat = (bounds.topLeft.latitude + bounds.bottomRight.latitude) / 2.0
        val avgLng = (bounds.topLeft.longitude + bounds.bottomRight.longitude) / 2.0

        return GeoPosition(avgLat, avgLng)
    }

    fun tile2lon(x: Int, zoom: Int): Double {
        return x / 2.0.pow(zoom.toDouble()) * 360.0 - 180
    }

    fun tile2lat(y: Int, zoom: Int): Double {
        val n = Math.PI - 2.0 * Math.PI * y / 2.0.pow(zoom.toDouble())
        return Math.toDegrees(Math.atan(Math.sinh(n)))
    }



    fun getTileNumber(lat: Double, lon: Double, zoom: Int): Triple<Int, Int, Int> {
        var xtile = Math.floor((lon + 180) / 360 * (1 shl zoom)).toInt()
        var ytile =
            Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 shl zoom))
                .toInt()
        if (xtile < 0) xtile = 0
        if (xtile >= 1 shl zoom) xtile = (1 shl zoom) - 1
        if (ytile < 0) ytile = 0
        if (ytile >= 1 shl zoom) ytile = (1 shl zoom) - 1
        return Triple(xtile, ytile, zoom)
    }
}