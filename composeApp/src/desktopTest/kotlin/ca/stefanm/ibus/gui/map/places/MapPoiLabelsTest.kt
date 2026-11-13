package ca.stefanm.ibus.gui.map.places

import ca.stefanm.e39maps.v1.PoiCategory
import ca.stefanm.e39maps.v1.latLng
import ca.stefanm.e39maps.v1.poi
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toMapPoi
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class MapPoiLabelsTest {

    private val parliament = LatLng(45.4236, -75.7009)

    private fun place(
        name: String = "Esso",
        category: MapPoiCategory? = MapPoiCategory.FUEL,
        details: Map<String, String> = emptyMap(),
        location: LatLng = parliament,
    ) = MapPoi(1, name, location, category, details)

    @Test
    fun `distances are rounded for reading at a glance`() {
        assertEquals("0 m", MapPoiLabels.distance(10.0))
        assertEquals("350 m", MapPoiLabels.distance(342.0))
        assertEquals("1.0 km", MapPoiLabels.distance(960.0))
        assertEquals("1.2 km", MapPoiLabels.distance(1_234.0))
        assertEquals("12 km", MapPoiLabels.distance(12_345.0))
        assertEquals("400 km", MapPoiLabels.distance(400_000.0))
    }

    @Test
    fun `compass points from the centre to the place`() {
        assertEquals("N", MapPoiLabels.compass(parliament, LatLng(45.50, -75.7009)))
        assertEquals("E", MapPoiLabels.compass(parliament, LatLng(45.4236, -75.60)))
        assertEquals("SW", MapPoiLabels.compass(parliament, LatLng(45.35, -75.80)))
        assertEquals("NW", MapPoiLabels.compass(parliament, LatLng(45.50, -75.81)))
    }

    @Test
    fun `list entries name the place, its distance and direction`() {
        val near = FoundMapPoi(place(location = LatLng(45.4336, -75.6909)), 1_350.0)
        assertEquals("Esso  1.4 km NE", MapPoiLabels.listEntry(near, parliament))
        val here = FoundMapPoi(place(name = ""), 10.0)
        assertEquals("Fuel station  0 m", MapPoiLabels.listEntry(here, parliament))
        assertEquals("Unnamed place", MapPoiLabels.name(place(name = "", category = null)))
    }

    @Test
    fun `details read like a description`() {
        val chips = place(
            name = "Chip Truck", category = MapPoiCategory.RESTAURANT,
            details = mapOf(
                "kind" to "amenity=fast_food",
                "cuisine" to "burger;fish_and_chips",
                "addr:housenumber" to "250", "addr:street" to "Bank St", "addr:city" to "Ottawa",
                "phone" to "+1 613 555 0100",
                "opening_hours" to "Mo-Su 11:00-21:00",
                "website" to "https://www.chiptruck.example/",
            )
        )
        assertEquals(
            listOf(
                "Restaurant: Fast food",
                "Cuisine: Burger, fish and chips",
                "250 Bank St, Ottawa",
                "Phone: +1 613 555 0100",
                "Hours: Mo-Su 11:00-21:00",
                "chiptruck.example",
            ),
            MapPoiLabels.detailLines(chips)
        )
    }

    @Test
    fun `details skip what the name already says and repeated kinds`() {
        val esso = place(name = "Esso Bank St", details = mapOf("kind" to "amenity=fuel", "brand" to "Esso", "operator" to "Imperial"))
        assertEquals(listOf("Fuel station", "Operator: Imperial"), MapPoiLabels.detailLines(esso))
        val museum = place(name = "Canadian War Museum", category = MapPoiCategory.MUSEUM, details = mapOf("kind" to "tourism=museum"))
        assertEquals(listOf("Museum"), MapPoiLabels.detailLines(museum))
        val airport = place(
            name = "Ottawa Macdonald-Cartier", category = MapPoiCategory.AIRPORT,
            details = mapOf("kind" to "aeroway=aerodrome", "iata" to "YOW", "icao" to "CYOW", "ele" to "114"),
        )
        assertEquals(listOf("Airport", "Code: YOW / CYOW", "Elevation: 114 m"), MapPoiLabels.detailLines(airport))
        assertNull(MapPoiLabels.address(place(details = mapOf("addr:postcode" to "K1P 1A1"))))
        val reserve = place(name = "Mer Bleue", category = MapPoiCategory.PARK, details = mapOf("kind" to "leisure=nature_reserve"))
        assertEquals(listOf("Park: Nature reserve"), MapPoiLabels.detailLines(reserve))
    }

    @Test
    fun `query titles`() {
        val q = PlacesQuery(parliament)
        assertEquals("All places", q.title)
        assertEquals("Fuel", q.copy(categories = setOf(MapPoiCategory.FUEL)).title)
        assertEquals("\"tim\"", q.copy(nameContains = "tim").title)
        assertEquals("Restaurants: \"tim\"", q.copy(categories = setOf(MapPoiCategory.RESTAURANT), nameContains = "tim").title)
    }

    @Test
    fun `the mapper reads category and details, unknown categories as none`() {
        val pb = poi {
            id = 42
            name = "Mont Cascades"
            location = latLng { latitude = 45.59; longitude = -75.86 }
            category = PoiCategory.POI_CATEGORY_SKI_AREA
            details.put("kind", "landuse=winter_sports")
        }
        val mapped = pb.toMapPoi()
        assertEquals(MapPoiCategory.SKI_AREA, mapped.category)
        assertEquals("landuse=winter_sports", mapped.details["kind"])
        assertEquals(45.59, mapped.location.latitude, 1e-9)
        assertNull(pb.toBuilder().setCategoryValue(99).build().toMapPoi().category)
    }
}
