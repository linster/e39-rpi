package ca.stefanm.ibus.gui.pim.email.repo

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

internal class FakeMailRepositoryTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val repo = FakeMailRepository()

    private suspend fun folder(account: String, role: MailFolderRole) =
        repo.folders().first().first { it.accountName == account && it.role == role }

    private suspend fun subjects(folder: MailFolder) =
        repo.messages(folder.id, MailRepository.MAX_MESSAGES).first().messages.map { it.subject }

    @Test
    fun foldersAndCounts() = runBlocking {
        val folders = repo.folders().first()
        assertEquals(listOf("Home", "Work"), folders.map { it.accountName }.distinct())
        val inbox = folder("Home", MailFolderRole.INBOX)
        assertEquals(5L, inbox.totalCount)
        assertEquals(2L, inbox.unreadCount)
        assertEquals(1, folders.first { it.name == "Car club" }.depth)
        // Home 2 + Work 1
        assertEquals(3L, repo.unreadInboxCount().first())
    }

    @Test
    fun messagesNewestFirstWithLimit() = runBlocking {
        val inbox = folder("Home", MailFolderRole.INBOX)
        val page = repo.messages(inbox.id, 2).first()
        assertEquals(listOf("Road trip plans", "Oil change reminder"), page.messages.map { it.subject })
        assertEquals(5L, page.totalCount)
        assertTrue(page.hasMore)
        assertEquals(2, repo.messages(inbox.id, 10, unreadOnly = true).first().messages.size)
    }

    @Test
    fun paging() = runBlocking {
        val list = repo.folders().first().first { it.name == "E39 list" }
        assertEquals(250L, list.totalCount)
        val first = repo.messagePage(list.id, 0).first()
        assertEquals(100, first.messages.size)
        assertEquals(3, first.pageCount)
        assertFalse(first.hasPrevious)
        assertTrue(first.hasNext)
        assertEquals("[e39] VANOS seals (#250)", first.messages.first().subject)
        val last = repo.messagePage(list.id, 2).first()
        assertEquals(50, last.messages.size)
        assertEquals(201L, last.firstNumber)
        assertFalse(last.hasNext)
        // Past the end: the last page.
        assertEquals(2, repo.messagePage(list.id, 7).first().page)
        assertEquals(3, repo.messagePage(list.id, 0, unreadOnly = true).first().messages.size)
        // Empty folder: one empty page.
        val drafts = folder("Home", MailFolderRole.DRAFTS)
        val empty = repo.messagePage(drafts.id, 0).first()
        assertEquals(1, empty.pageCount)
        assertTrue(empty.messages.isEmpty())
    }

    @Test
    fun openingMarksRead() = runBlocking {
        val inbox = folder("Home", MailFolderRole.INBOX)
        val trip = repo.messages(inbox.id, 10).first().messages.first { it.subject == "Road trip plans" }
        val html = repo.getMessageHtml(trip.id, markSeen = false).getOrThrow()
        assertTrue(html.html.contains("<b>Saturday</b>"))
        assertFalse(html.message.summary.seen)
        assertEquals(2L, folder("Home", MailFolderRole.INBOX).unreadCount)

        val message = repo.getMessage(trip.id).getOrThrow()
        assertTrue(message.summary.seen)
        assertTrue(message.text.startsWith("Leaving Saturday"))
        assertEquals(1L, folder("Home", MailFolderRole.INBOX).unreadCount)

        assertTrue(repo.getMessage("nope").isFailure)
    }

    @Test
    fun flagsMoveTrash() = runBlocking {
        val inbox = folder("Home", MailFolderRole.INBOX)
        val club = repo.folders().first().first { it.name == "Car club" }
        val oil = repo.messages(inbox.id, 10).first().messages.first { it.subject == "Oil change reminder" }

        repo.setFlagged(listOf(oil.id), true).getOrThrow()
        assertTrue(repo.messages(inbox.id, 10).first().messages.first { it.id == oil.id }.flagged)

        repo.move(listOf(oil.id), club.id).getOrThrow()
        assertTrue("Oil change reminder" in subjects(club))
        assertFalse("Oil change reminder" in subjects(inbox))

        // Not across accounts.
        assertTrue(repo.move(listOf(oil.id), folder("Work", MailFolderRole.INBOX).id).isFailure)

        val trash = folder("Home", MailFolderRole.TRASH)
        repo.trash(listOf(oil.id)).getOrThrow()
        assertTrue("Oil change reminder" in subjects(trash))
        // Trashing from the trash deletes.
        repo.trash(listOf(oil.id)).getOrThrow()
        assertFalse("Oil change reminder" in subjects(trash))
    }

    @Test
    fun sendReply() = runBlocking {
        val inbox = folder("Home", MailFolderRole.INBOX)
        val trip = repo.messages(inbox.id, 10).first().messages.first { it.subject == "Road trip plans" }
        val event = async(start = CoroutineStart.UNDISPATCHED) { repo.events.first() }
        val outboxId = repo.send(
            OutgoingMail(to = listOf(trip.from), subject = "Re: Road trip plans", text = "On my way.", inReplyTo = trip)
        ).getOrThrow()
        assertEquals(MailEvent.Sent(outboxId, "Re: Road trip plans"), event.await())
        assertTrue("Re: Road trip plans" in subjects(folder("Home", MailFolderRole.SENT)))
        assertTrue(repo.messages(inbox.id, 10).first().messages.first { it.id == trip.id }.answered)

        assertTrue(repo.send(OutgoingMail(to = emptyList(), subject = "x", text = "y")).isFailure)
    }

    @Test
    fun search() = runBlocking {
        assertEquals(
            listOf("Road trip plans", "Re: Dinner on Friday", "Dinner on Friday"),
            repo.search("anna").getOrThrow().messages.map { it.subject }
        )
        val work = folder("Work", MailFolderRole.INBOX)
        assertEquals(listOf("Q3 report draft"), repo.search("q3", work.id).getOrThrow().messages.map { it.subject })
        assertTrue(repo.search(" ").isFailure)
    }

    @Test
    fun newMail() = runBlocking {
        val event = async(start = CoroutineStart.UNDISPATCHED) { repo.events.first() }
        repo.simulateNewMail()
        val newMail = event.await() as MailEvent.NewMail
        assertEquals("Are you close?", newMail.latest.subject)
        assertEquals(3L, folder("Home", MailFolderRole.INBOX).unreadCount)
    }
}
