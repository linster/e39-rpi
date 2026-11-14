package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.e39pim.v1.MailServiceGrpcKt
import ca.stefanm.e39pim.v1.getMessageRequest
import ca.stefanm.e39pim.v1.listMailFoldersRequest
import ca.stefanm.e39pim.v1.listMessagesRequest
import ca.stefanm.e39pim.v1.watchMailRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

/**
 * The generated client and [MailMapper] against a running PIM bridge on
 * localhost:50052 (e39-rpi-pim scripts/run-dev.sh) that has mail in a
 * folder named "inbox" (e.g. e39-rpi-pim's testdata/mail/eml in the dev
 * data's local-mail/inbox/cur). Skipped when there's no such bridge.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
internal class MailBridgeLiveTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(60)

    private lateinit var channel: ManagedChannel
    private lateinit var stub: MailServiceGrpcKt.MailServiceCoroutineStub

    @Before
    fun connect() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build()
        stub = MailServiceGrpcKt.MailServiceCoroutineStub(channel).withDeadlineAfter(15, TimeUnit.SECONDS)
        val reachable = try {
            runBlocking { stub.listMailFolders(listMailFoldersRequest { }) }
            true
        } catch (e: StatusException) {
            false
        }
        assumeTrue("No PIM bridge on localhost:50052", reachable)
    }

    @After
    fun disconnect() {
        channel.shutdownNow()
    }

    @Test
    fun foldersMessagesAndHtml() = runBlocking {
        val folders = stub.listMailFolders(listMailFoldersRequest { }).foldersList.map { it.toMailFolder() }
        val inbox = folders.firstOrNull { it.name == "inbox" && it.totalCount > 0 }
        assumeTrue("No mail in an \"inbox\" folder", inbox != null)
        inbox!!
        assertEquals(MailFolderRole.INBOX, inbox.role)

        val list = stub.listMessages(listMessagesRequest {
            folderId = inbox.id
            pageSize = 3
        }).toMessageList()
        assertEquals(3, list.messages.size)
        assertEquals(inbox.totalCount, list.totalCount)
        assertTrue(list.hasMore)
        val dates = list.messages.map { it.date.epochSeconds }
        assertEquals(dates.sortedDescending(), dates)

        val all = stub.listMessages(listMessagesRequest {
            folderId = inbox.id
            pageSize = MailRepository.MAX_MESSAGES
        }).toMessageList().messages
        val trip = all.first { it.subject == "Road trip plans" }
        assertEquals("anna@example.com", trip.from.email)

        val html = stub.getMessageHtml(getMessageRequest { messageId = trip.id }).toMailMessageHtml()
        assertEquals("Leaving Saturday at 8. Bring the cooler.", html.message.text)
        assertEquals("<p>Leaving <b>Saturday</b> at 8. Bring the cooler.</p>", html.html)
        assertEquals("carol@example.com", html.message.cc.single().email)

        val insurance = all.first { it.subject == "Insurance documents" }
        val withAttachment = stub.getMessage(getMessageRequest { messageId = insurance.id }).toMailMessage()
        assertEquals(1, withAttachment.attachments.size)
        assertTrue(withAttachment.summary.hasAttachments)
    }

    @Test
    fun watchStartsWithReady() = runBlocking {
        val first = MailServiceGrpcKt.MailServiceCoroutineStub(channel)
            .withDeadlineAfter(15, TimeUnit.SECONDS)
            .watchMail(watchMailRequest { })
            .first()
        assertTrue(first.hasReady())
        assertEquals(null, first.toMailEvent())
    }
}
