package ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import com.javadocmd.simplelatlng.LatLng

data class MapScreenParameters(
    /* If true, store the map zoom and center state so it can be reused on the next open */
    val persistUiStateOnClose : Boolean = false,
    /* If true, use the stored map zoom and center state when opening */
    val usePersistedStateOnOpen : Boolean = false,

    val openMode : MapScreenOpenMode
) {
    sealed class MapScreenOpenMode(
        open val center : LatLng
    ) {
        /* The user is just browsing around on the map */
        data class BrowsingMode(
            override val center : LatLng,
            val persistedState: MapScreen.BrowsingState? = null,
        ) : MapScreenOpenMode(center = center)
        data class LocationSelection(override val center : LatLng) : MapScreenOpenMode(center = center)
    }
}