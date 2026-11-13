package ca.stefanm.ibus.gui.generalSettings

import ca.stefanm.ibus.lib.system.ZoneTab
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class TimeZoneLabelsTest {

    // Lines as in tzdata's zone1970.tab (tab-separated).
    private val zones = ZoneTab.parse(
        listOf(
            "# comment",
            "CA\t+4734-05243\tAmerica/St_Johns\tNewfoundland, Labrador (SE)",
            "CA,BS\t+4339-07923\tAmerica/Toronto\tEastern - ON & QC (most areas)",
            "CA\t+5333-11328\tAmerica/Edmonton\tMountain - AB, BC(E), NT(E), SK(W)",
            "CA\t+5024-10439\tAmerica/Regina\tCST - SK (most areas)",
            "US\t+404251-0740023\tAmerica/New_York\tEastern (most areas)",
            "AR\t-3436-05827\tAmerica/Argentina/Buenos_Aires\tBuenos Aires (BA, CF)",
            "GB,GG,IM,JE\t+513030-0000731\tEurope/London",
            "",
        ).joinToString("\n")
    )

    @Test
    fun `zone1970 tab is parsed, comments and blank lines skipped`() {
        assertEquals(7, zones.size)
        val toronto = zones.single { it.id == "America/Toronto" }
        assertEquals(listOf("CA", "BS"), toronto.countryCodes)
        assertEquals("Eastern - ON & QC (most areas)", toronto.comment)
        assertEquals("Toronto", toronto.city)
        assertEquals("America", toronto.region)
        assertEquals("", zones.single { it.id == "Europe/London" }.comment)
        assertEquals("Buenos Aires", zones.single { it.id.endsWith("Buenos_Aires") }.city)
    }

    @Test
    fun `a country's zones in the file's order, a region's by city`() {
        assertEquals(
            listOf("America/St_Johns", "America/Toronto", "America/Edmonton", "America/Regina"),
            TimeZoneLabels.countryZones(zones, "CA").map { it.id }
        )
        assertEquals(listOf("America/New_York"), TimeZoneLabels.countryZones(zones, "US").map { it.id })
        assertEquals(
            listOf("Buenos Aires", "Edmonton", "New York", "Regina", "St Johns", "Toronto"),
            TimeZoneLabels.regionZones(zones, "America").map { it.city }
        )
    }

    @Test
    fun labels() {
        val toronto = zones.single { it.id == "America/Toronto" }
        assertEquals("Eastern - ON & QC (most areas) (Toronto)", TimeZoneLabels.label(toronto, byComment = true))
        assertEquals("Toronto (Eastern - ON & QC (most areas))", TimeZoneLabels.label(toronto, byComment = false))
        assertEquals("London", TimeZoneLabels.label(zones.single { it.id == "Europe/London" }, byComment = true))
        assertEquals("\u2713 Canada", TimeZoneLabels.mark(true, "Canada"))
        assertEquals("   Canada", TimeZoneLabels.mark(false, "Canada"))
    }

    @Test
    fun `the current zone reads like the clock, with its offset`() {
        val toronto = ZoneId.of("America/Toronto")
        assertEquals("America/Toronto, 4:26 PM, UTC-5",
            TimeZoneLabels.describe(toronto, ZonedDateTime.of(2026, 11, 13, 16, 26, 0, 0, toronto)))
        assertEquals("America/Toronto, 9:00 AM, UTC-4",
            TimeZoneLabels.describe(toronto, ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, toronto)))
        val regina = ZoneId.of("America/Regina")   // no daylight saving time
        assertEquals("America/Regina, 9:00 AM, UTC-6",
            TimeZoneLabels.describe(regina, ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, regina)))
        assertEquals("UTC+5:30", TimeZoneLabels.utcOffset(java.time.ZoneOffset.ofHoursMinutes(5, 30)))
        assertEquals("UTC", TimeZoneLabels.utcOffset(java.time.ZoneOffset.UTC))
    }
}
