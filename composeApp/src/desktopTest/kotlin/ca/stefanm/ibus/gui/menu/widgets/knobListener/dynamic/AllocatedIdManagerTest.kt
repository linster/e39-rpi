package ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic

import ca.stefanm.ibus.lib.logging.StdOutLogger
import org.junit.Assert.assertEquals
import org.junit.Test

internal class AllocatedIdManagerTest {

    private val ids = AllocatedIdManager(StdOutLogger())
    private fun current() = ids.getCurrentId().value

    @Test
    fun removingOtherItemsKeepsTheCurrentOne() {
        repeat(5) { ids.addId() }      // 0..4
        ids.incrementPointer(); ids.incrementPointer()
        assertEquals(2, current())
        ids.removeId(0)
        ids.removeId(4)
        assertEquals(2, current())
    }

    @Test
    fun removingTheCurrentItemMovesToTheNext() {
        repeat(4) { ids.addId() }      // 0..3
        ids.incrementPointer()
        ids.removeId(1)
        assertEquals(2, current())
        ids.removeId(3)
        ids.removeId(2)
        assertEquals(0, current())
    }

    @Test
    fun aRebuiltListGetsAnItem() {
        // A toolbar (0, 1) and rows (2..4); the knob on a row; the rows are
        // replaced (new ones first, then the old ones go, one by one).
        repeat(5) { ids.addId() }
        repeat(3) { ids.incrementPointer() }
        assertEquals(3, current())
        repeat(3) { ids.addId() }      // 5..7
        listOf(2, 3, 4).forEach { ids.removeId(it) }
        assertEquals(5, current())
        // Old ones first, then the new: still an item.
        repeat(3) { ids.incrementPointer() }
        val before = current()
        listOf(5, 6, 7).forEach { ids.removeId(it) }
        repeat(2) { ids.addId() }
        assert(current() in listOf(0, 1, 8, 9)) { "current ${current()} (was $before)" }
        ids.incrementPointer()
    }

    @Test
    fun turningWrapsAround() {
        repeat(3) { ids.addId() }
        ids.decrementPointer()
        assertEquals(2, current())
        ids.incrementPointer()
        assertEquals(0, current())
    }
}
