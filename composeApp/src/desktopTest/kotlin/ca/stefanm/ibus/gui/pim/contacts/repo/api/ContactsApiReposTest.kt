package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

internal class ContactsApiReposTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val contacts = FakeContactsRepository()
    private val lists = ContactListRepo(contacts)
    private val details = ContactDetailsRepo(contacts)

    @Test
    fun pagesAndLetters() = runBlocking {
        val pager = lists.pager(null)
        var s = pager.state.first()
        assertEquals("1–100 of 260", s.rangeLabel)
        assertEquals("All", s.bookLabel)
        assertFalse(s.hasPrevious)
        assertTrue(s.hasNext)
        assertEquals("Alex Abbott", s.rows.first().name)
        assertTrue(s.rows.first().firstOfInitial)
        assertEquals('A', s.rows.first().initial)
        // A caption on each letter's first row only.
        assertEquals(s.rows.map { it.initial }.distinct().size, s.rows.count { it.firstOfInitial })
        // (Zoë Adams files under A: nobody under Z.)
        assertEquals(('A'..'Y').toList(), s.initials.map { it.letter })

        pager.next(); pager.next()
        s = pager.state.first()
        assertEquals("201–260 of 260", s.rangeLabel)
        pager.next()
        assertEquals(200, pager.state.first().offset)

        // A letter: the page starts at its first contact.
        pager.jumpTo('K')
        s = pager.state.first()
        assertEquals('K', s.rows.first().initial)
        assertEquals("Alex Klein", s.rows.first().name)
        assertEquals(s.initials.first { it.letter == 'K' }.offset, s.offset)
        // A letter nobody has: the next one's.
        val club = lists.pager("b4")
        club.jumpTo('Z')
        assertEquals(ContactListRepo.lastPageStart(250, 100), club.state.first().offset)

        // The place is kept.
        assertEquals(s.offset, lists.pager(null).state.first().offset)
        lists.clearPositions()
        assertEquals(0, lists.pager(null).state.first().offset)
    }

    @Test
    fun addressBooksAndRows() = runBlocking {
        val personal = lists.pager("b1").state.first()
        assertEquals("1–4 of 4", personal.rangeLabel)
        assertEquals("Personal", personal.bookLabel)
        assertEquals(listOf("Zoë Adams", "Anna Kowalski", "René Lévesque", "Prairie Euro Garage"), personal.rows.map { it.name })
        // The organization for people, the number otherwise.
        assertEquals("Garage Lévesque", personal.rows[2].detail)
        assertEquals("555-0199", personal.rows[3].detail)
        assertEquals("+1 306 555 0123", personal.rows[1].detail)

        lists.selectAddressBook("b2")
        assertEquals("b2", lists.selectedAddressBookId.value)
        lists.clearPositions()
        assertNull(lists.selectedAddressBookId.value)
    }

    @Test
    fun details() = runBlocking {
        val annaId = lists.pager("b1").state.first().rows.first { it.name == "Anna Kowalski" }.id
        val anna = details.open(annaId).getOrThrow()
        assertEquals("Anna Kowalski", anna.name)
        assertEquals("Personal", anna.addressBook?.name)
        assertEquals(
            listOf("Mobile ★" to "+1 306 555 0123", "Home" to "(306) 555-0177", "E-mail (home) ★" to "anna@example.com",
                "Home address" to "123 Main St\nRegina SK S4P 1A1\nCanada", "Birthday" to "May 17, 1990", "Nickname" to "Annie"),
            anna.rows.map { it.label to it.value },
        )
        assertTrue(details.photo(anna)!!.isNotEmpty())
        assertEquals("anna@example.com", details.mailRecipient(anna, anna.rows.first { it.email != null }.email!!).email)

        val rene = details.open(lists.pager("b1").state.first().rows.first { it.name == "René Lévesque" }.id).getOrThrow()
        assertEquals("Mechanic, Garage Lévesque", rene.subtitle)
        assertNull(details.photo(rene))
        assertTrue(details.open("nope").isFailure)
    }
}
