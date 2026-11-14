package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.e39pim.v1.ListMessagesResponse
import ca.stefanm.e39pim.v1.listMessagesResponse
import ca.stefanm.e39pim.v1.messageSummary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** RemoteMailRepository's walk through the bridge's page tokens (offsets, like the bridge's). */
internal class PageWalkTest {

    private var total = 250
    private val pageSize = 100
    private val fetched = mutableListOf<String>()

    private fun bridge(token: String): ListMessagesResponse {
        fetched += token
        val offset = token.ifEmpty { "0" }.toInt()
        val count = (total - offset).coerceIn(0, pageSize)
        return listMessagesResponse {
            repeat(count) { i -> messages += messageSummary { id = (offset + i).toString() } }
            totalCount = total.toLong()
            if (offset + count < total) nextPageToken = (offset + count).toString()
        }
    }

    @Test
    fun walksForwardAndRemembersTokens() = runBlocking {
        val tokens = mutableListOf("")
        val (page, response) = RemoteMailRepository.walkToPage(tokens, 2) { bridge(it) }
        assertEquals(2, page)
        assertEquals(50, response.messagesCount)
        assertEquals(listOf("", "100", "200"), tokens)
        assertEquals(listOf("", "100", "200"), fetched)

        // Known pages: one call.
        fetched.clear()
        assertEquals(1, RemoteMailRepository.walkToPage(tokens, 1) { bridge(it) }.first)
        assertEquals(listOf("100"), fetched)
    }

    @Test
    fun pastTheEndGivesTheLastPage() = runBlocking {
        val tokens = mutableListOf("")
        assertEquals(2, RemoteMailRepository.walkToPage(tokens, 9) { bridge(it) }.first)
        total = 0
        val (page, response) = RemoteMailRepository.walkToPage(tokens, 0) { bridge(it) }
        assertEquals(0, page)
        assertEquals(0, response.messagesCount)
    }

    @Test
    fun folderShrankBelowAKnownPage() = runBlocking {
        val tokens = mutableListOf("")
        RemoteMailRepository.walkToPage(tokens, 2) { bridge(it) }
        total = 150
        val (page, response) = RemoteMailRepository.walkToPage(tokens, 2) { bridge(it) }
        assertEquals(1, page)
        assertEquals(50, response.messagesCount)
        assertEquals(listOf("", "100"), tokens)
    }
}
