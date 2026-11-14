package ca.stefanm.ibus.gui.menu.widgets.bottombar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

internal class ClockTextTest {
    private fun at(hour: Int, minute: Int) = clockText(LocalDateTime.of(2026, 11, 13, hour, minute))

    @Test
    fun `twelve-hour clock`() {
        assertEquals("12:05 AM", at(0, 5))
        assertEquals("01:00 AM", at(1, 0))
        assertEquals("11:59 AM", at(11, 59))
        assertEquals("12:00 PM", at(12, 0))
        assertEquals("02:36 PM", at(14, 36))
        assertEquals("11:59 PM", at(23, 59))
    }
}
