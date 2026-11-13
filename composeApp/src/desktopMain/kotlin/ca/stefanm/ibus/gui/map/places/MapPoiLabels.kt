package ca.stefanm.ibus.gui.map.places

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import kotlin.math.roundToInt

/** Text for map POIs in lists and side panes (no Compose: unit-tested). */
object MapPoiLabels {

    private val COMPASS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    /**
     * The OSM kinds that say more than their category (the map toolkit's
     * categories.py): a restaurant that's fast food, a park that's a nature
     * reserve. The others ("aeroway=aerodrome" for an airport) would only
     * repeat it.
     */
    private val TELLING_KINDS = mapOf(
        "amenity=fast_food" to "Fast food",
        "leisure=nature_reserve" to "Nature reserve",
        "boundary=national_park" to "National park",
    )

    /** "350 m", "1.2 km", "12 km". */
    fun distance(meters: Double): String = when {
        meters < 950 -> "${(meters / 50).roundToInt() * 50} m"
        meters < 9_950 -> "%.1f km".format(meters / 1000)
        else -> "${(meters / 1000).roundToInt()} km"
    }

    /** "NE": the direction from [from] to [to], to the nearest of 8 points. */
    fun compass(from: LatLng, to: LatLng): String {
        val bearing = (LatLngTool.initialBearing(from, to) + 360) % 360
        return COMPASS[((bearing + 22.5) / 45).toInt() % 8]
    }

    /** "1.2 km NE"; just the distance when it's (nearly) here. */
    fun distanceAndDirection(found: FoundMapPoi, from: LatLng): String =
        if (found.distanceMeters < 25) distance(found.distanceMeters)
        else "${distance(found.distanceMeters)} ${compass(from, found.poi.location)}"

    /** A list entry: "Esso  1.2 km NE". Unnamed places are named by their category. */
    fun listEntry(found: FoundMapPoi, from: LatLng): String =
        "${name(found.poi)}  ${distanceAndDirection(found, from)}"

    fun name(poi: MapPoi): String = poi.name.ifBlank { poi.category?.singular ?: "Unnamed place" }

    /** "Fast food" for kind "amenity=fast_food"; null when the category says it all. */
    fun kind(poi: MapPoi): String? = poi.details["kind"]?.let { TELLING_KINDS[it] }

    /** "250 Bank St, Ottawa"; null without a street or city. */
    fun address(poi: MapPoi): String? {
        val d = poi.details
        val street = listOfNotNull(d["addr:housenumber"], d["addr:street"]).joinToString(" ").ifBlank { null }
        return listOfNotNull(street, d["addr:city"]).joinToString(", ").ifBlank { null }
    }

    /**
     * The side pane's lines after the name: what it is, address, phone,
     * hours, website and the category's own facts, as far as OSM knows them.
     */
    fun detailLines(poi: MapPoi): List<String> {
        val d = poi.details
        val what = listOfNotNull(poi.category?.singular, kind(poi)).joinToString(": ")
        return listOfNotNull(
            what.ifBlank { null },
            d["brand"]?.takeIf { !poi.name.contains(it, ignoreCase = true) }?.let { "Brand: $it" },
            d["operator"]?.takeIf { it != d["brand"] && !poi.name.contains(it, ignoreCase = true) }
                ?.let { "Operator: $it" },
            d["cuisine"]?.let { cuisine ->
                "Cuisine: " + cuisine.split(";").map { it.trim().replace('_', ' ') }.filter { it.isNotEmpty() }
                    .joinToString(", ").replaceFirstChar { it.uppercase() }
            },
            address(poi),
            d["phone"]?.let { "Phone: $it" },
            d["opening_hours"]?.let { "Hours: $it" },
            listOfNotNull(d["iata"], d["icao"]).joinToString(" / ").ifBlank { null }?.let { "Code: $it" },
            d["ele"]?.let { "Elevation: ${it.removeSuffix(" m").trim()} m" },
            d["website"]?.removePrefix("https://")?.removePrefix("http://")?.removePrefix("www.")
                ?.removeSuffix("/"),
        )
    }
}
