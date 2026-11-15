package ca.stefanm.ibus.gui.generalSettings

import ca.stefanm.ibus.gui.generalSettings.DateTimeLabels.Field
import ca.stefanm.ibus.lib.system.ClockSettings
import ca.stefanm.ibus.lib.system.ClockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

internal class DateTimeLabelsTest {

    @Test
    fun parsesTimedatectlShow() {
        // As timedatectl show -p NTP -p NTPSynchronized prints it.
        assertEquals(ClockStatus(internetTime = true, synchronized = true), ClockStatus.parse("NTP=yes\nNTPSynchronized=yes\n"))
        assertEquals(ClockStatus(internetTime = false, synchronized = false), ClockStatus.parse("NTP=no\nNTPSynchronized=no\n"))
        assertNull(ClockStatus.parse("Failed to connect to bus"))
    }

    @Test
    fun internetTimeLine() {
        assertEquals("Internet time: synchronized", DateTimeLabels.internetTime(ClockStatus(true, true)))
        assertEquals("Internet time: not synchronized yet", DateTimeLabels.internetTime(ClockStatus(true, false)))
        assertEquals("Internet time: off", DateTimeLabels.internetTime(ClockStatus(false, true)))
        assertEquals("Internet time: unknown", DateTimeLabels.internetTime(null))
    }

    @Test
    fun describesAndRequestsTheTime() {
        val t = LocalDateTime.of(2026, 11, 15, 9, 7, 42)
        assertEquals("Sun 15 Nov 2026, 9:07 AM", DateTimeLabels.describe(t))
        val draft = DateTimeLabels.startingDraft(t)
        assertEquals(0, draft.second)
        // What the image's helper (e39rpi-timezone) accepts.
        assertEquals("set-time 2026-11-15 09:07", ClockSettings.setTimeRequest(draft))
        assertEquals("set-time 2027-01-02 23:04", ClockSettings.setTimeRequest(LocalDateTime.of(2027, 1, 2, 23, 4)))
    }

    @Test
    fun theDayFitsTheMonth() {
        val jan31 = LocalDateTime.of(2026, 1, 31, 10, 0)
        assertEquals(LocalDateTime.of(2026, 2, 28, 10, 0), Field.MONTH.with(jan31, 2))
        assertEquals(LocalDateTime.of(2026, 3, 31, 10, 0), Field.MONTH.with(jan31, 3))
        val leapDay = LocalDateTime.of(2028, 2, 29, 10, 0)
        assertEquals(LocalDateTime.of(2027, 2, 28, 10, 0), Field.YEAR.with(leapDay, 2027))
        assertEquals(1..28, Field.DAY.range(LocalDateTime.of(2026, 2, 10, 0, 0)))
        assertEquals(1..29, Field.DAY.range(leapDay))
        assertEquals(LocalDateTime.of(2026, 1, 31, 23, 59), Field.MINUTE.with(Field.HOUR.with(jan31, 23), 59))
    }

    @Test
    fun rowLabels() {
        val t = LocalDateTime.of(2026, 11, 15, 9, 7)
        assertEquals(
            listOf("Year: 2026", "Month: November", "Day: 15 (Sunday)", "Hour: 09", "Minute: 07"),
            Field.entries.map { it.label(t) }
        )
    }
}
