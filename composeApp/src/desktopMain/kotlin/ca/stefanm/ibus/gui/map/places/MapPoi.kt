package ca.stefanm.ibus.gui.map.places

import androidx.compose.ui.graphics.Color
import ca.stefanm.e39maps.v1.PoiCategory
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import com.javadocmd.simplelatlng.LatLng

/**
 * A place from the map data (OpenStreetMap, built into the map database by
 * e39-rpi-maps' toolkit): read-only, unlike the user's own POIs
 * ([PoiRepository.Poi]). Found with [ca.stefanm.ibus.gui.map.repo.RemoteMapPoiRepository].
 *
 * [details] are OpenStreetMap facts when known, under OSM's keys: kind (the
 * matching tag, e.g. "amenity=fast_food"), brand, operator, cuisine, phone,
 * website, opening_hours, iata, icao, ele, addr:housenumber/street/city/postcode.
 */
data class MapPoi(
    /** Stable across map updates, but separate from the user's POI ids. */
    val id: Long,
    val name: String,
    val location: LatLng,
    /** Null for a category this HMI doesn't know (a newer backend). */
    val category: MapPoiCategory?,
    val details: Map<String, String>,
) {
    /** As one of the user's POIs: for the map overlay, or to save in the address book. */
    fun toUserPoi(): PoiRepository.Poi = PoiRepository.Poi(
        name = name,
        location = location,
        icon = PoiRepository.Poi.PoiIcon.ColoredCircle(category?.color ?: Color.White),
        isVisible = true,
    )
}

data class FoundMapPoi(val poi: MapPoi, val distanceMeters: Double)

/** The map's POI categories (the API's PoiCategory), with how the HMI shows them. */
enum class MapPoiCategory(
    val pb: PoiCategory,
    /** For lists of categories: "Restaurants". */
    val title: String,
    /** For one place: "Restaurant". */
    val singular: String,
    /** Its circle on the map. */
    val color: Color,
) {
    FUEL(PoiCategory.POI_CATEGORY_FUEL, "Fuel", "Fuel station", Color(0xFFFF9800)),
    RESTAURANT(PoiCategory.POI_CATEGORY_RESTAURANT, "Restaurants", "Restaurant", Color(0xFFE53935)),
    SKI_AREA(PoiCategory.POI_CATEGORY_SKI_AREA, "Ski Hills", "Ski hill", Color(0xFF81D4FA)),
    MUSEUM(PoiCategory.POI_CATEGORY_MUSEUM, "Museums", "Museum", Color(0xFFAB47BC)),
    AIRPORT(PoiCategory.POI_CATEGORY_AIRPORT, "Airports", "Airport", Color(0xFF90A4AE)),
    PARK(PoiCategory.POI_CATEGORY_PARK, "Parks", "Park", Color(0xFF43A047)),
    TRAILHEAD(PoiCategory.POI_CATEGORY_TRAILHEAD, "Trailheads", "Trailhead", Color(0xFF8D6E63)),
    CAR_PARTS(PoiCategory.POI_CATEGORY_CAR_PARTS, "Car Parts", "Car parts", Color(0xFFFDD835)),
    JUNKYARD(PoiCategory.POI_CATEGORY_JUNKYARD, "Junkyards", "Junkyard", Color(0xFF757575));

    companion object {
        fun fromPb(pb: PoiCategory): MapPoiCategory? = entries.firstOrNull { it.pb == pb }
    }
}

/**
 * A search for places: near [center], in [categories] (empty: all), with
 * [nameContains] in the name (empty: any). [centerLabel] says what the
 * centre is, for headers ("map centre").
 */
data class PlacesQuery(
    val center: LatLng,
    val categories: Set<MapPoiCategory> = emptySet(),
    val nameContains: String = "",
    val centerLabel: String = "map centre",
) {
    /** "Fuel", "All places", "\"tim\"", "Restaurants: \"tim\"". */
    val title: String
        get() {
            val what = when (categories.size) {
                0 -> null
                1 -> categories.first().title
                else -> categories.joinToString(", ") { it.title }
            }
            val name = nameContains.takeIf { it.isNotBlank() }?.let { "\"$it\"" }
            return listOfNotNull(what, name).joinToString(": ").ifEmpty { "All places" }
        }
}
