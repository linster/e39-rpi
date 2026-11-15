package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactField
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactInput
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

internal class ContactEditRepoTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val contacts = FakeContactsRepository()
    private val edit = ContactEditRepo(contacts)

    private suspend fun idOf(name: String) = contacts.allContacts().first().contacts.first { it.displayName == name }.id

    @Test
    fun newContact() = runBlocking {
        edit.startNew()
        assertFalse(edit.hasChanges)
        // Nothing to call it by.
        assertTrue(edit.save().exceptionOrNull() is IllegalArgumentException)
        edit.change { it.copy(givenName = " Tow ", familyName = "Truck") }
        edit.addPhone("3065550144")
        edit.addPhone("306 555 0145", PhoneNumber.Type.WORK)
        edit.preferPhone(1)
        assertEquals(listOf(false, true), edit.draft.value!!.input.phones.map { it.preferred })
        edit.removePhone(1)
        assertEquals(listOf(true), edit.draft.value!!.input.phones.map { it.preferred })
        edit.setBirthday("1980-02-29")
        assertThrows(IllegalArgumentException::class.java) { edit.setBirthday("Feb 29") }
        assertTrue(edit.hasChanges)

        val saved = edit.save().getOrThrow()
        assertNull(edit.draft.value)
        assertEquals("Tow Truck", saved.summary.displayName)
        assertEquals("b1", saved.summary.addressBookId)  // the first writable
        assertEquals("+13065550144", saved.phones.single().e164)
        assertEquals(LocalDate(1980, 2, 29), saved.birthday)

        // Into another address book.
        edit.startNew("b4", ContactInput(organization = "Club Shop"))
        assertEquals("b4", edit.save().getOrThrow().summary.addressBookId)
        assertEquals(listOf("Personal", "Contacts", "Members"), edit.writableAddressBooks().first().map { it.name })
    }

    @Test
    fun editingSendsOnlyWhatChanged() = runBlocking {
        val draft = edit.startEdit(idOf("Anna Kowalski")).getOrThrow()
        assertEquals(emptySet<ContactField>(), draft.changedFields())
        edit.change { it.copy(nickname = "Ania") }
        edit.updatePhone(1) { it.copy(type = PhoneNumber.Type.WORK) }
        assertEquals(setOf(ContactField.NICKNAME, ContactField.PHONES), edit.draft.value!!.changedFields())
        val saved = edit.save().getOrThrow()
        assertEquals("Ania", saved.nickname)
        assertEquals(PhoneNumber.Type.WORK, saved.phones[1].type)
        assertEquals("anna@example.com", saved.emails.single().address)
        assertTrue(saved.summary.hasPhoto)
        assertEquals(2L, saved.summary.revision)

        // Nothing changed: nothing sent, the draft goes.
        edit.startEdit(saved.summary.id).getOrThrow()
        assertEquals(saved, edit.save().getOrThrow())

        // Changed meanwhile: a conflict, the draft stays.
        edit.startEdit(saved.summary.id).getOrThrow()
        contacts.updateContact(saved.summary, ContactInput(nickname = "Annie"), setOf(ContactField.NICKNAME)).getOrThrow()
        edit.change { it.copy(title = "Pilot") }
        assertTrue(edit.save().exceptionOrNull() is PimBackendException.Conflict)
        assertTrue(edit.draft.value != null)
    }

    @Test
    fun fromMail() = runBlocking {
        val d = edit.startFromMail("Saskatchewan BMW Club", "news@skbmwclub.example.com")
        assertEquals("Saskatchewan BMW", d.input.givenName)
        assertEquals("Club", d.input.familyName)
        assertEquals(listOf(EmailAddress("news@skbmwclub.example.com", EmailAddress.Type.OTHER, true)), d.input.emails)
        assertEquals("Anna", edit.startFromMail("Anna", "anna@example.com").input.givenName)
        assertEquals("", edit.startFromMail("", "x@example.com").input.givenName)
        assertEquals("x@example.com", edit.draft.value!!.displayName)
    }

    @Test
    fun deleteAndReadOnly() = runBlocking {
        val bob = contacts.allContacts().first().contacts.first { it.displayName == "Bob Ng" }
        assertTrue(edit.canEdit(bob))
        val help = contacts.allContacts().first().contacts.first { it.displayName == "IT Help Desk" }
        assertFalse(edit.canEdit(help))
        edit.delete(bob).getOrThrow()
        assertTrue(contacts.getContact(bob.id).isFailure)
        assertTrue(edit.delete(help).isFailure)
    }
}
