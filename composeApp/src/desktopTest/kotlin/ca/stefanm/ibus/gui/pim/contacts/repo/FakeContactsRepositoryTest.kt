package ca.stefanm.ibus.gui.pim.contacts.repo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import javax.imageio.ImageIO

@OptIn(kotlin.time.ExperimentalTime::class)
internal class FakeContactsRepositoryTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val repo = FakeContactsRepository()

    private suspend fun idOf(name: String) =
        repo.contacts(ContactsRepository.MAX_CONTACTS).first().contacts.first { it.displayName == name }.id

    @Test
    fun addressBooksAndOrder() = runBlocking {
        val books = repo.addressBooks().first()
        assertEquals(listOf(4L, 4L), books.map { it.contactCount })
        val all = repo.contacts(100).first()
        assertEquals(8L, all.totalCount)
        assertEquals(
            listOf("Zoë Adams", "Carl Carlson", "Anna Kowalski", "René Lévesque", "Bob Ng",
                "Prairie Euro Garage", "someone@example.org", "Dana Whitford"),
            all.contacts.map { it.displayName },
        )
        assertEquals('A', all.contacts.first().initial)
        val page = repo.contacts(3, books[0].id).first()
        assertEquals(3, page.contacts.size)
        assertEquals(4L, page.totalCount)
        assertTrue(page.hasMore)
    }

    @Test
    fun details() = runBlocking {
        val anna = repo.getContact(idOf("Anna Kowalski")).getOrThrow()
        assertEquals("+13065550123", anna.phones[0].e164)
        assertEquals(PhoneNumber.Type.MOBILE, anna.phones[0].type)
        assertEquals("Regina", anna.addresses.single().locality)
        assertEquals(1990, anna.birthday?.year)
        assertTrue(repo.getContact("nope").isFailure)
    }

    @Test
    fun search() = runBlocking {
        assertEquals(listOf("René Lévesque"), repo.search("levesque").getOrThrow().contacts.map { it.displayName })
        assertEquals(listOf("René Lévesque", "Prairie Euro Garage"), repo.search("garage").getOrThrow().contacts.map { it.displayName })
        assertEquals(listOf("Anna Kowalski", "Bob Ng"), repo.search("555 0123").getOrThrow().contacts.map { it.displayName })
        assertTrue(repo.search(" ").isFailure)
    }

    @Test
    fun phoneNumbers() = runBlocking {
        val both = repo.findByPhoneNumber("(306) 555-0123").getOrThrow()
        assertEquals(listOf("Anna Kowalski", "Bob Ng"), both.map { it.contact.displayName })
        assertTrue(both.all { it.exact })
        val garage = repo.findByPhoneNumber("+1 306 555 0199").getOrThrow().single()
        assertEquals("Prairie Euro Garage", garage.contact.displayName)
        assertFalse(garage.exact)
        assertTrue(repo.findByPhoneNumber("+1 306 555 9999").getOrThrow().isEmpty())
        assertTrue(repo.findByPhoneNumber("private").isFailure)
    }

    @Test
    fun photo() = runBlocking {
        val photo = repo.getPhoto(idOf("Anna Kowalski"), 64).getOrThrow()
        val image = ImageIO.read(photo.jpeg.inputStream())
        assertEquals(64, image.width)
        assertTrue(repo.getPhoto(idOf("Bob Ng")).isFailure)
        assertTrue(repo.getPhoto(idOf("Anna Kowalski"), 8).isFailure)
    }

    @Test
    fun callerId() = runBlocking {
        val lookup = CallerIdLookup(repo)
        val anna = lookup.lookup("+13065550123")!!
        assertEquals("Anna Kowalski", anna.name)
        assertEquals("Mobile", anna.phoneLabel)
        assertTrue(anna.exact)
        assertTrue(anna.hasPhoto)
        assertFalse(lookup.lookup("306-555-0199")!!.exact)
        assertNull(lookup.lookup("+1 306 555 9999"))
        assertNull(lookup.lookup("withheld"))
    }
}
