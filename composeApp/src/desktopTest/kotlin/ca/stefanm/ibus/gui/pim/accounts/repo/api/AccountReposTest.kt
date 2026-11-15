package ca.stefanm.ibus.gui.pim.accounts.repo.api

import ca.stefanm.ibus.gui.pim.calendar.repo.backend.FakeAccountRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.LoginRedirect
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.MailServerSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

internal class AccountReposTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private val accounts = FakeAccountRepository()
    private val list = AccountListRepo(accounts)
    private val add = AddMailAccountRepo(accounts)

    @Test
    fun rows() = runBlocking {
        val rows = list.rows().first()!!
        assertEquals(listOf("Home mail", "Work", "Google", "Home server"), rows.map { it.name })
        assertEquals("Mail · stefan@example.com", rows[0].kind)
        assertEquals("CalDAV · contacts too", rows[3].kind)
        assertEquals("OK", rows[0].state)
        val caldav = rows[3].account
        list.setContactsEnabled(caldav, false).getOrThrow()
        assertEquals("CalDAV", list.rows().first()!!.first { it.name == "Home server" }.kind)
        assertTrue(list.setContactsEnabled(rows[0].account, true).exceptionOrNull() is PimBackendException.FailedPrecondition)
        list.setOnline(caldav, false).getOrThrow()
        assertEquals("offline", list.rows().first()!!.first { it.name == "Home server" }.state)
        list.remove(caldav).getOrThrow()
        assertEquals(3, list.rows().first()!!.size)
    }

    @Test
    fun addFoundAccount() = runBlocking {
        add.start()
        assertTrue(add.add().exceptionOrNull() is IllegalArgumentException)
        add.setEmail(" dana@fastmail.com ")
        val found = add.discover().getOrThrow()
        assertEquals("imap.fastmail.com", found.settings!!.imap.host)
        assertEquals("Fastmail: the provider's settings", found.sourceText)
        assertNull(found.appPasswordHint)
        // A password is needed; a wrong one is refused, the form stays.
        assertTrue(add.add().exceptionOrNull() is IllegalArgumentException)
        add.setPassword("wrong")
        val refused = add.add().exceptionOrNull()
        assertTrue(refused is PimBackendException.Unauthenticated)
        assertTrue(add.form.value != null)
        add.setPassword("secret")
        add.setSenderName(" Dana ")
        val account = add.add().getOrThrow()
        assertNull(add.form.value)
        assertEquals(PimAccount.Type.IMAP, account.type)
        assertEquals("dana@fastmail.com", account.name)
        assertEquals("Dana", account.senderName)
        assertTrue(accounts.accounts.value!!.any { it.emailAddress == "dana@fastmail.com" })
    }

    @Test
    fun gmailNeedsAnAppPassword() = runBlocking {
        add.start()
        add.setEmail("stefan.car@gmail.com")
        val f = add.discover().getOrThrow()
        assertTrue(f.appPasswordHint!!.contains("myaccount.google.com/apppasswords"))
        add.setPassword("hunter2")  // an ordinary password, not 16 letters
        val e = add.add().exceptionOrNull()!!
        assertTrue(e is PimBackendException.Unauthenticated)
        assertTrue(AddMailAccountRepo.describe(e, add.form.value!!).contains("app password"))
        add.setPassword("abcd efgh ijkl mnop")
        assertEquals("stefan.car@gmail.com", add.add().getOrThrow().emailAddress)
    }

    @Test
    fun nothingFoundThenTyped() = runBlocking {
        add.start()
        add.setEmail("me@nowhere.invalid")
        val f = add.discover().getOrThrow()
        assertTrue(f.manual)
        assertFalse(f.serversKnown)
        assertEquals(993, f.settings!!.imap.port)
        assertEquals(465, f.settings!!.smtp.port)
        add.editServer(MailServerRole.INCOMING) { it.copy(host = "mail.example.org") }
        add.editServer(MailServerRole.OUTGOING) { it.copy(host = "mail.example.org") }
        add.setSecurity(MailServerRole.OUTGOING, MailServerSettings.Security.STARTTLS)
        assertEquals(587, add.form.value!!.settings!!.smtp.port)
        assertTrue(add.form.value!!.serversKnown)
        add.setPassword("secret")
        add.add().getOrThrow()
        // A new address forgets the servers.
        add.start()
        add.setEmail("x@example.com")
        add.discover().getOrThrow()
        add.setEmail("y@example.com")
        assertNull(add.form.value!!.settings)
        assertTrue(add.discover().getOrThrow().sourceText.contains("a guess"))
        // Not an address.
        add.setEmail("nobody")
        assertTrue(add.discover().exceptionOrNull() is PimBackendException.InvalidArgument)
    }

    @Test
    fun loginRedirect() {
        val prefix = "http://127.0.0.1:41234/"
        val google = "http://127.0.0.1:41234/?state=abc&code=4/0AanRRrv-long&scope=email"
        assertEquals(google, LoginRedirect.extract(google, prefix))
        // Copied without the scheme, or with text around it.
        assertEquals(google, LoginRedirect.extract("127.0.0.1:41234/?state=abc&code=4/0AanRRrv-long&scope=email", prefix))
        assertEquals(google, LoginRedirect.extract("Site can't be reached $google \n", prefix))
        // Refused on Google's page: passed on too, so the login ends.
        assertEquals("http://127.0.0.1:41234/?error=access_denied", LoginRedirect.extract("http://127.0.0.1:41234/?error=access_denied", prefix))
        // Another port, another host, no answer in it.
        assertNull(LoginRedirect.extract("http://127.0.0.1:412345/?code=x", prefix))
        assertNull(LoginRedirect.extract("http://evil.example/?code=x", prefix))
        assertNull(LoginRedirect.extract("http://127.0.0.1:41234/", prefix))
    }

    @Test
    fun finishLogin() = runBlocking {
        list.addGoogleAccount().getOrThrow()
        val login = accounts.pendingLogins.first().single()
        assertTrue(accounts.finishLogin(login, "https://www.google.com/").exceptionOrNull() is PimBackendException.InvalidArgument)
        accounts.finishLogin(login, "${FakeAccountRepository.REDIRECT}?code=abc").getOrThrow()
        assertTrue(accounts.pendingLogins.first().isEmpty())
        assertFalse(accounts.accounts.value!!.first { it.id == login.accountId }.needsLogin)
        // Ended: nothing waits any more.
        assertTrue(accounts.finishLogin(login, "${FakeAccountRepository.REDIRECT}?code=abc").exceptionOrNull() is PimBackendException.Unavailable)
    }
}
