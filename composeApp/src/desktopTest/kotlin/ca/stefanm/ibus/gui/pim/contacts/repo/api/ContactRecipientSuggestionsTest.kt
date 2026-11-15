package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.gui.pim.contacts.repo.ContactField
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactInput
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository
import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.api.FakeMailSenders
import ca.stefanm.ibus.gui.pim.email.repo.api.RecentCorrespondents
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

internal class ContactRecipientSuggestionsTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val contacts = FakeContactsRepository()
    private val mail = FakeMailRepository()
    private val suggestions = ContactRecipientSuggestions(contacts, RecentCorrespondents(mail, FakeMailSenders(mail)))

    @Test
    fun recentFirstThenContacts() = runBlocking {
        val s = suggestions.suggest(limit = 100)
        val recent = s.recipients.takeWhile { it.recent }
        assertEquals("Anna Kowalski", recent.first().address.name)
        // Anna is a contact too: listed once, as recent, with her address book.
        assertEquals("Recent · Personal", recent.first().detail)
        assertEquals(1, s.recipients.count { it.address.email == "anna@example.com" })
        // Recent people who aren't contacts: just "Recent".
        assertTrue(recent.any { it.address.email == "service@prairie-euro.example.com" && it.detail == "Recent" })

        val others = s.recipients.dropWhile { it.recent }
        assertTrue(others.isNotEmpty() && others.none { it.recent })
        // By name, with their address book.
        assertEquals("Alex Abbott", others.first().address.name)
        assertEquals("Members", others.first().detail)
        assertTrue(others.any { it.address.name == "Zoë Adams" && it.detail == "Personal" })
        // 255 contacts with addresses + recent people not in contacts, beyond the 100.
        assertEquals(s.recipients.size, 100)
        assertTrue(s.more > 150)
        assertEquals(s.more + 100, suggestions.suggest(limit = 1000).recipients.size)
    }

    @Test
    fun search() = runBlocking {
        // A contact who isn't in recent mail.
        val zoe = suggestions.suggest("adams").recipients
        assertEquals(listOf("zoe@example.co.uk"), zoe.map { it.address.email })
        assertEquals("Personal", zoe.single().detail)
        // Accents and case don't matter; recent people match too.
        assertEquals("rene@example.fr", suggestions.suggest("LEVESQUE").recipients.single().address.email)
        assertTrue(suggestions.suggest("garage").recipients.any { it.recent && it.address.email == "service@prairie-euro.example.com" })
        assertTrue(suggestions.suggest("nobody-like-this").recipients.isEmpty())

        // Every address of a contact found.
        val anna = contacts.allContacts().first().contacts.first { it.displayName == "Anna Kowalski" }
        contacts.updateContact(anna, ContactInput(emails = listOf(
            EmailAddress("anna@example.com", EmailAddress.Type.HOME, true),
            EmailAddress("anna@work.example.com", EmailAddress.Type.WORK, false),
        )), setOf(ContactField.EMAILS)).getOrThrow()
        val found = suggestions.suggest("kowalski").recipients
        assertEquals(listOf("anna@example.com", "anna@work.example.com"), found.map { it.address.email })
        assertEquals(listOf(true, false), found.map { it.recent })
    }

    @Test
    fun noAddressBookAnswer() = runBlocking {
        // The bridge unreachable: the contacts flows never emit.
        val silent = object : ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository by contacts {
            override fun addressBooks() = kotlinx.coroutines.flow.flow<List<ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook>> { kotlinx.coroutines.awaitCancellation() }
        }
        val s = ContactRecipientSuggestions(silent, RecentCorrespondents(mail, FakeMailSenders(mail))).suggest()
        assertTrue(s.recipients.isNotEmpty() && s.recipients.all { it.recent && it.detail == "Recent" })
    }
}
