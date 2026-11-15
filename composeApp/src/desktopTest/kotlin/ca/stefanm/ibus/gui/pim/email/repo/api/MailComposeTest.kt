package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import ca.stefanm.ibus.gui.pim.email.repo.MailMessage
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import kotlin.time.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
internal class MailComposeTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val mail = FakeMailRepository()
    private val mailbox = MailboxListRepo(mail)
    private val senders = FakeMailSenders(mail)
    private val recent = RecentCorrespondents(mail, senders)
    private val suggestions = ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactRecipientSuggestions(
        ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository(), recent)

    private val me = MailAddress("Stefan", "stefan@example.com")
    private val anna = MailAddress("Anna Kowalski", "anna@example.com")
    private val bob = MailAddress("Bob", "bob@example.com")

    private fun message(from: MailAddress, to: List<MailAddress>, cc: List<MailAddress> = emptyList(), replyTo: List<MailAddress> = emptyList(), subject: String = "Dinner") =
        MailMessage(
            summary = MailSummary("9", "1", subject, from, to, Instant.fromEpochSeconds(1_700_000_000), seen = true, flagged = false,
                answered = false, forwarded = false, hasAttachments = false, sizeBytes = 10),
            cc = cc, replyTo = replyTo, messageId = "<x@y>", text = "Friday?\n\nAt 7.", textWasHtml = false, attachments = emptyList(),
        )

    @Test
    fun replies() {
        val own = setOf(me.email)
        val m = message(anna, listOf(me, bob), cc = listOf(MailAddress("", "carl@example.com")))
        val reply = MailDraft.reply(m, "acc", all = false, own = own)
        assertEquals(listOf(anna), reply.to)
        assertTrue(reply.cc.isEmpty())
        assertEquals("Re: Dinner", reply.subject)
        assertTrue(reply.quote.endsWith("wrote:\n> Friday?\n>\n> At 7."))
        assertEquals(MailDraft.Mode.REPLY, reply.mode)

        val all = MailDraft.reply(m, "acc", all = true, own = own)
        assertEquals(listOf(bob.email, "carl@example.com"), all.cc.map { it.email })

        // Reply-To wins; one of my own messages goes to its recipients.
        assertEquals(listOf(bob), MailDraft.reply(message(anna, listOf(me), replyTo = listOf(bob)), null, false, own).to)
        assertEquals(listOf(anna), MailDraft.reply(message(me, listOf(anna)), null, false, own).to)

        assertEquals("RE: Dinner", MailDraft.prefixed("Re:", "RE: Dinner"))
        assertEquals("AW: x", MailDraft.prefixed("Re:", "AW: x"))
        assertEquals("Fwd: Dinner", MailDraft.forward(m, null).subject)

        val outgoing = all.copy(text = "Yes!").toOutgoing()
        assertEquals("Yes!\n\n${all.quote}", outgoing.text)
        assertEquals(m.summary, outgoing.inReplyTo)
        assertNull(outgoing.forwardOf)
        assertEquals("Yes!", all.copy(text = "Yes!", includeQuote = false).body)
    }

    @Test
    fun addresses() {
        assertEquals(MailAddress("", "anna@example.com"), MailAddresses.parse(" anna@example.com "))
        assertEquals(MailAddress("Anna K", "anna@example.com"), MailAddresses.parse("\"Anna K\" <anna@example.com>"))
        assertNull(MailAddresses.parse("anna"))
        assertNull(MailAddresses.parse("anna@example"))
        assertNull(MailAddresses.parse("an na@example.com"))
    }

    @Test
    fun suggestionsFromRecentMail() = runBlocking {
        val all = recent.recent()
        assertTrue(all.any { it.email == "anna@example.com" })
        assertTrue(all.none { it.email == FakeMailRepository.ME_EMAIL || it.email == FakeMailRepository.WORK_EMAIL })
        assertEquals(all.size, all.map { it.email.lowercase() }.distinct().size)
        assertEquals(listOf("Dana Whitford"), recent.recent("dana").map { it.name })
        assertEquals(listOf("anna@example.com"), recent.recent("KOWAL").map { it.email })
    }

    @Test
    fun composeAndSend() = runBlocking {
        val repo = MailComposeRepo(mail, mailbox, senders, MutableStateFlow(listOf("On my way.")), suggestions)
        assertEquals(listOf("On my way."), repo.quickReplies.value)
        val inbox = mail.folders().first().first { it.accountName == "Home" && it.role == MailFolderRole.INBOX }
        val trip = mail.messages(inbox.id, 10).first().messages.first { it.subject == "Road trip plans" }
        val message = mail.getMessage(trip.id).getOrThrow()

        repo.startReply(message, all = false)
        assertEquals("fake_home", repo.draft.value!!.accountId)
        assertFalse(repo.hasChanges)
        repo.setText("Sounds good")
        assertTrue(repo.hasChanges)

        // A pick counts once; a recipient once.
        assertTrue(repo.applyPick(1L, MailAddress("Bob", "bob@example.com")))
        assertFalse(repo.applyPick(1L, MailAddress("Bob", "bob@example.com")))
        assertFalse(repo.addRecipient(RecipientField.CC, MailAddress("", "BOB@example.com")))
        repo.pickTarget = RecipientField.CC
        assertTrue(repo.applyPick(2L, MailAddress("Carl", "carl@example.com")))
        assertEquals(listOf("anna@example.com", "bob@example.com"), repo.draft.value!!.to.map { it.email })
        assertEquals(listOf("carl@example.com"), repo.draft.value!!.cc.map { it.email })
        repo.removeRecipient(RecipientField.TO, MailAddress("", "bob@example.com"))

        repo.send().getOrThrow()
        assertNull(repo.draft.value)
        val sent = mail.folders().first().first { it.accountName == "Home" && it.role == MailFolderRole.SENT }
        val out = mail.messages(sent.id, 10).first().messages.first { it.subject == "Re: Road trip plans" }
        assertEquals(listOf("anna@example.com"), out.to.map { it.email })
        assertTrue(mail.getMessage(out.id).getOrThrow().text.startsWith("Sounds good\n\nOn "))
        assertTrue(mail.messages(inbox.id, 10).first().messages.first { it.id == trip.id }.answered)

        // No recipients: kept, not sent.
        repo.startNew()
        assertTrue(repo.send().isFailure)
        assertTrue(repo.draft.value != null)
        repo.discard()
        assertNull(repo.draft.value)
    }

    @Test
    fun fakeSenders() = runBlocking {
        val list = senders.senders()
        assertEquals(listOf("Home", "Work"), list.map { it.accountName })
        assertEquals(setOf(FakeMailRepository.ME_EMAIL, FakeMailRepository.WORK_EMAIL), senders.ownAddresses())
    }
}
