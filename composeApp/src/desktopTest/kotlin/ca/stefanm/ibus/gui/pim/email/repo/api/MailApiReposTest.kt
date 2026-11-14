package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailFolderRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

@OptIn(kotlin.time.ExperimentalTime::class)
internal class MailApiReposTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val mail = FakeMailRepository()
    private val mailbox = MailboxListRepo(mail)
    private val lists = MessageListRepo(mail, mailbox)
    private val reader = MessageReaderRepo(mail, mailbox)

    private suspend fun folder(account: String, name: String) =
        mail.folders().first().first { it.accountName == account && it.name == name }

    @Test
    fun formatting() {
        val zone = TimeZone.UTC
        val now = LocalDateTime(2026, 11, 14, 15, 0).toInstant(zone)
        assertEquals("09:05", MailFormat.listDate(LocalDateTime(2026, 11, 14, 9, 5).toInstant(zone), now, zone))
        assertEquals("Tue", MailFormat.listDate(LocalDateTime(2026, 11, 10, 9, 5).toInstant(zone), now, zone))
        assertEquals("Nov 3", MailFormat.listDate(LocalDateTime(2026, 11, 3, 9, 5).toInstant(zone), now, zone))
        assertEquals("2025-12-24", MailFormat.listDate(LocalDateTime(2025, 12, 24, 9, 5).toInstant(zone), now, zone))
        assertEquals("Sat Nov 14 2026, 15:00", MailFormat.fullDate(now, zone))
        assertEquals("820 B", MailFormat.size(820))
        assertEquals("176 KB", MailFormat.size(180_000))
        assertEquals("1.5 MB", MailFormat.size(1_572_864))
        val people = listOf(MailAddress("Anna", "a@x"), MailAddress("", "b@x"), MailAddress("C", "c@x"), MailAddress("D", "d@x"))
        assertEquals("Anna, b@x", MailFormat.addresses(people.take(2)))
        assertEquals("Anna, b@x and 2 more", MailFormat.addresses(people, max = 2))
        assertEquals("", MailFormat.unreadBadge(0))
        assertEquals("999+", MailFormat.unreadBadge(1500))
        assertEquals("101–200 of 250", MailFormat.range(101, 100, 250))
        assertEquals("No messages", MailFormat.range(1, 0, 0))
    }

    @Test
    fun accountsAndFolders() = runBlocking {
        val accounts = mailbox.accounts().first()
        assertEquals(listOf("Home", "Work"), accounts.map { it.accountName })
        val home = accounts.first()
        assertEquals(MailFolderRole.INBOX, home.inbox?.role)
        assertEquals(home.inbox, home.folders.first())
        // Inbox 2 + Car club 1 + E39 list 3; not the trash.
        assertEquals(6L, home.unreadCount)
        assertEquals(3L, mailbox.unreadInboxCount().first())
    }

    @Test
    fun pagerPagesAndRemembersItsPlace() = runBlocking {
        val list = folder("Home", "E39 list")
        val pager = lists.pager(list.id)
        var state = pager.state.first()
        assertEquals(100, state.rows.size)
        assertEquals("1–100 of 250", state.rangeLabel)
        assertEquals("1/3", state.pageLabel)
        assertTrue(state.rows.first().unread)
        assertEquals("E39 list", state.rows.first().correspondent)

        pager.next(); pager.next(); pager.next()
        state = pager.state.first()
        assertEquals("201–250 of 250", state.rangeLabel)
        assertEquals(2, pager.position.value.page)

        // Coming back to the folder: the same page.
        assertEquals(2, lists.pager(list.id).state.first().page.page)
        lists.resetPosition(list.id)
        assertEquals(0, lists.pager(list.id).state.first().page.page)

        pager.setUnreadOnly(true)
        state = pager.state.first()
        assertEquals(3, state.rows.size)
        pager.markRead(state).getOrThrow()
        assertTrue(pager.state.first().rows.isEmpty())
        assertEquals(0L, folder("Home", "E39 list").unreadCount)
    }

    @Test
    fun sentMailShowsRecipients() = runBlocking {
        val sent = mail.folders().first().first { it.accountName == "Home" && it.role == MailFolderRole.SENT }
        val row = lists.pager(sent.id).state.first().rows.single()
        assertEquals("To: Anna Kowalski", row.correspondent)
    }

    @Test
    fun readerAndActions() = runBlocking {
        val inbox = folder("Home", "Inbox")
        val state = lists.pager(inbox.id).state.first()
        val sgi = state.rows.first { it.subject == "Insurance documents" }

        val content = reader.open(sgi.id).getOrThrow()
        assertEquals("Insurance documents", content.subject)
        assertEquals(listOf("From", "To", "Date"), content.headers.map { it.first })
        assertEquals(1, content.attachments.size)
        assertFalse(content.inTrash)

        val oil = state.rows.first { it.subject == "Oil change reminder" }
        assertTrue(oil.unread)
        reader.open(oil.id).getOrThrow()
        assertFalse(lists.pager(inbox.id).state.first().rows.first { it.id == oil.id }.unread)
        val unread = reader.setSeen(oil.summary, false).getOrThrow()
        assertFalse(unread.seen)
        assertTrue(lists.pager(inbox.id).state.first().rows.first { it.id == oil.id }.unread)

        assertTrue(reader.setFlagged(oil.summary, true).getOrThrow().flagged)

        val targets = reader.moveTargets(oil.summary)
        assertTrue(targets.none { it.id == inbox.id || it.accountName != "Home" })
        val club = targets.first { it.name == "Car club" }
        reader.move(oil.summary, club).getOrThrow()
        assertTrue(lists.pager(club.id).state.first().rows.any { it.id == oil.id })

        // Delete: to the trash, then for good.
        reader.delete(oil.summary).getOrThrow()
        val inTrash = reader.open(oil.id).getOrThrow()
        assertTrue(inTrash.inTrash)
        reader.delete(inTrash.summary).getOrThrow()
        assertTrue(reader.open(oil.id).isFailure)
    }

    @Test
    fun neighbours() {
        val ids = listOf("a", "b", "c")
        assertEquals("c", MessageReaderRepo.neighbour(ids, "b", forward = true))
        assertEquals("a", MessageReaderRepo.neighbour(ids, "b", forward = false))
        assertNull(MessageReaderRepo.neighbour(ids, "c", forward = true))
        assertNull(MessageReaderRepo.neighbour(ids, "x", forward = true))
    }
}
