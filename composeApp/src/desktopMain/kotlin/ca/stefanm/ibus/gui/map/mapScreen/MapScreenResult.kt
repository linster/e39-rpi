package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen

import com.javadocmd.simplelatlng.LatLng

sealed class MapScreenResult {
    data class PointSelectedResult(val point : LatLng?) : MapScreenResult()
}