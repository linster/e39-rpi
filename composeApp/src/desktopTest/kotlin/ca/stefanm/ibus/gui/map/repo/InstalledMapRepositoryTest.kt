package ca.stefanm.ibus.gui.map.repo

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class InstalledMapRepositoryTest {

    private fun healthz(meta: String, available: Boolean = true) = JsonParser.parseString(
        """{"status": "ok", "map_db": {"available": $available, "meta": $meta}, "tiles": {}}"""
    ).asJsonObject

    @Test
    fun `a named map`() {
        val facts = InstalledMapRepository.mapFacts(healthz(
            """{"built_at": "2026-11-13T04:42:15Z", "region": "Ottawa +530 km (London ON - Quebec City), Canada",
               "region_label": "Ottawa", "radius_km": "530", "poi_count": "55881"}"""
        ))!!
        assertEquals("Ottawa (530 km), built 2026-11-13", InstalledMapRepository.describe(facts))
    }

    @Test
    fun `a map from before names uses the start of its region`() {
        val facts = InstalledMapRepository.mapFacts(healthz(
            """{"built_at": "2026-11-13T04:02:54Z", "region": "Ottawa +530 km (London ON - Quebec City), Canada",
               "radius_km": "530"}"""
        ))!!
        assertEquals("Ottawa (530 km), built 2026-11-13", InstalledMapRepository.describe(facts))
    }

    @Test
    fun `no map`() {
        assertNull(InstalledMapRepository.mapFacts(healthz("{}", available = false)))
        assertNull(InstalledMapRepository.mapFacts(JsonParser.parseString("""{"status": "ok"}""").asJsonObject))
        assertNull(InstalledMapRepository.describe(mapOf("region" to "x")))
    }
}
