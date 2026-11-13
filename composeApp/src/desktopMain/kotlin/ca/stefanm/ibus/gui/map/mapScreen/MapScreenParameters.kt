package ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import com.javadocmd.simplelatlng.LatLng

data class MapScreenParameters(
    /* If true, store the map zoom and center state so it can be reused on the next open */
    val persistUiStateOnClose : Boolean = false,
    /* If true, use the stored map zoom and center state when opening */
    val usePersistedStateOnOpen : Boolean = false,

    val openMode : MapScreenOpenMode,

    /* Drawn on the map along with the user's visible POIs, for this opening
     * only (e.g. the places a search found); not saved anywhere. */
    val markers : List<PoiRepository.Poi> = emptyList(),
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