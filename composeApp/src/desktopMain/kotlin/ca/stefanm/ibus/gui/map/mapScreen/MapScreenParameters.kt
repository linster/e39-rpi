package ca.stefanm.ibus.gui.map.mapScreen

import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.places.FoundMapPoi
import ca.stefanm.ibus.gui.map.places.MapPoi
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
    @Deprecated("Start using the PoiBrowser openMode.")
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

        /* Select one point on a map */
        data class LocationSelection(override val center : LatLng) : MapScreenOpenMode(center = center)

        /* Give the map a list of POIs to select from, with each POI optionally having information about it */
        data class PoiBrowser(
            override val center: LatLng,
            val poiById : Map<Long, MapPoi>,
            val poiMode: PoiMode,
            val preSelected: FoundMapPoi? = null
        ) : MapScreenOpenMode(center) {
            enum class PoiMode {
                /** We're showing users a list of POIs to select one (the MapScree nreturns a LocationSelection result) */
                Select,
                /** We're showing users a listof POIs for aimless browsing. */
                Browse
            }
        }
    }

    companion object {
        fun PoiBrowser(
            center: LatLng,
            pois : List<FoundMapPoi>,
            poiMode: MapScreenOpenMode.PoiBrowser.PoiMode,
            preSelected: FoundMapPoi? = null,
        ) : MapScreenParameters {
            //MapPoi has information that can be displayed in a side pane.
            val openMode = MapScreenOpenMode.PoiBrowser(
                center = center,
                poiById = pois.map { it.poi }.associateBy { it.id },
                poiMode = poiMode,
                preSelected = preSelected
            )
            return MapScreenParameters(
                persistUiStateOnClose = false,
                usePersistedStateOnOpen = false,
                openMode = openMode,
                markers = emptyList()
            )
        }
    }
}