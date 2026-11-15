package ca.stefanm.ibus.gui.pim.email.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

internal class QuickReplyEditsTest {

    private val list = listOf("On my way.", "Yes.", "No.")

    @Test
    fun adding() {
        assertEquals(list + "Thanks!", QuickReplyEdits.add(list, "  Thanks! "))
        assertEquals(list, QuickReplyEdits.add(list, "Yes."))
        assertThrows(IllegalArgumentException::class.java) { QuickReplyEdits.add(list, "   ") }
    }

    @Test
    fun replacing() {
        assertEquals(listOf("On my way.", "Yes!", "No."), QuickReplyEdits.replace(list, 1, " Yes! "))
        assertEquals(list, QuickReplyEdits.replace(list, 1, "Yes."))
        assertThrows(IllegalArgumentException::class.java) { QuickReplyEdits.replace(list, 1, "No.") }
        assertThrows(IllegalArgumentException::class.java) { QuickReplyEdits.replace(list, 1, "") }
        assertThrows(IllegalArgumentException::class.java) { QuickReplyEdits.replace(list, 5, "x") }
    }

    @Test
    fun moving() {
        assertEquals(listOf("Yes.", "On my way.", "No."), QuickReplyEdits.move(list, 1, 0))
        assertEquals(listOf("On my way.", "No.", "Yes."), QuickReplyEdits.move(list, 1, 2))
        assertEquals(listOf("Yes.", "No.", "On my way."), QuickReplyEdits.move(list, 0, 9))
        assertEquals(list, QuickReplyEdits.move(list, 7, 0))
    }
}
