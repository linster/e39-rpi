package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.ibus.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Sample accounts, for building the accounts screens without the PIM
 * bridge (E39Config.PimConfig.Accounts.useFakeData): a CalDAV server, a
 * Google account, two mail accounts. Adding works until the HMI restarts,
 * with the bridge's checks imitated:
 *
 * - discovering servers: fastmail.com (the provider's settings), gmail.com
 *   (an app password needed), a domain ending in .invalid (nothing found),
 *   any other domain (a guess: imap./smtp.<domain>);
 * - adding: the password "wrong" is refused; Gmail takes only an app
 *   password (16 letters); a domain "unreachable.example" can't be reached.
 */
@ApplicationScope
class FakeAccountRepository @Inject constructor() : AccountRepository {

    companion object {
        val SYNC_TIME = 2.seconds
        /** Where the sample logins end (as libkgapi's listener on the bridge). */
        const val REDIRECT = "http://127.0.0.1:48123/"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var nextId = 1

    private val _accounts = MutableStateFlow<List<PimAccount>?>(sample())
    override val accounts: StateFlow<List<PimAccount>?> = _accounts.asStateFlow()

    private val _logins = MutableStateFlow<List<PendingLogin>>(emptyList())
    override val pendingLogins: Flow<List<PendingLogin>> = _logins.asStateFlow()

    private val _results = MutableSharedFlow<LoginResult>(extraBufferCapacity = 8)
    override val loginResults: Flow<LoginResult> = _results.asSharedFlow()

    /** Back to the sample accounts (the UI test probe, between tests). */
    fun reset() {
        _accounts.value = sample()
        _logins.value = emptyList()
    }

    private fun account(id: String, type: PimAccount.Type, name: String, email: String = "", sender: String = "",
                        contacts: Boolean = false, state: PimAccount.State = PimAccount.State.IDLE) =
        PimAccount(id, type, name, online = true, state = state, statusMessage = if (state == PimAccount.State.IDLE) "Ready" else "",
            progressPercent = 0, needsLogin = false, lastError = "", emailAddress = email, senderName = sender, contactsEnabled = contacts)

    private fun sample() = listOf(
        account("fake_caldav", PimAccount.Type.CALDAV, "Home server", contacts = true),
        account("fake_google", PimAccount.Type.GOOGLE, "Google", contacts = true),
        account("fake_home", PimAccount.Type.IMAP, "Home mail", "stefan@example.com", "Stefan"),
        account("fake_work", PimAccount.Type.IMAP, "Work", "stefan@work.example.com", "Stefan"),
    )

    private fun all() = _accounts.value.orEmpty()

    private fun find(id: String) = all().firstOrNull { it.id == id } ?: throw PimBackendException.NotFound("No account $id", null)

    private fun add(account: PimAccount): PimAccount {
        _accounts.update { list -> (list.orEmpty() + account).sortedBy { it.name.lowercase() } }
        sync(account.id)
        return account
    }

    private fun change(id: String, f: (PimAccount) -> PimAccount) = _accounts.update { list -> list?.map { if (it.id == id) f(it) else it } }

    /** Syncing for a moment, then idle (as the bridge reports it). */
    private fun sync(id: String) {
        change(id) { it.copy(state = PimAccount.State.SYNCING, statusMessage = "Syncing", progressPercent = 0) }
        scope.launch {
            delay(SYNC_TIME)
            change(id) { if (it.online) it.copy(state = PimAccount.State.IDLE, statusMessage = "Ready", progressPercent = 0) else it }
        }
    }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        Result.failure(e)
    }

    private fun checkPassword(password: String) {
        if (password == "wrong") throw PimBackendException.Unauthenticated("The server refused the user name or password", null)
    }

    override suspend fun addCalDavAccount(name: String, url: String, username: String, password: String, contacts: Boolean) = attempt {
        if (!url.startsWith("http")) throw PimBackendException.InvalidArgument("Not a web address: $url", null)
        checkPassword(password)
        add(account("fake_caldav_${nextId++}", PimAccount.Type.CALDAV, name.ifBlank { url }, contacts = contacts))
    }

    override suspend fun setContactsEnabled(account: PimAccount, enabled: Boolean) = attempt {
        val current = find(account.id)
        if (current.type != PimAccount.Type.CALDAV) throw PimBackendException.FailedPrecondition("Only CalDAV accounts' contacts can be turned on or off", null)
        change(account.id) { it.copy(contactsEnabled = enabled) }
        find(account.id)
    }

    override suspend fun addGoogleAccount(loginHint: String) = attempt {
        val account = account("fake_google_${nextId++}", PimAccount.Type.GOOGLE, "Google").copy(needsLogin = true, state = PimAccount.State.NOT_CONFIGURED)
        _accounts.update { list -> (list.orEmpty() + account).sortedBy { it.name.lowercase() } }
        _logins.update { it + PendingLogin("login_${account.id}", account.id, "Log in to Google", "https://accounts.google.com/o/oauth2/auth?sample", REDIRECT) }
        account
    }

    override suspend fun discoverMailSettings(email: String) = attempt {
        val address = email.trim()
        val domain = address.substringAfter('@', "").lowercase()
        if (domain.isEmpty() || address.substringBefore('@').isEmpty()) throw PimBackendException.InvalidArgument("Not an e-mail address: $address", null)
        fun server(host: String, port: Int, security: MailServerSettings.Security) = MailServerSettings(host, port, security, address)
        when {
            domain.endsWith(".invalid") -> throw PimBackendException.NotFound("No mail servers found for $domain", null)
            domain == "fastmail.com" -> DiscoveredMailSettings(
                MailAccountSettings(server("imap.fastmail.com", 993, MailServerSettings.Security.TLS), server("smtp.fastmail.com", 465, MailServerSettings.Security.TLS)),
                DiscoveredMailSettings.Source.PROVIDER, "Fastmail", appPasswordNeeded = false)
            domain == "gmail.com" -> DiscoveredMailSettings(
                MailAccountSettings(server("imap.gmail.com", 993, MailServerSettings.Security.TLS), server("smtp.gmail.com", 465, MailServerSettings.Security.TLS)),
                DiscoveredMailSettings.Source.ISPDB, "Gmail", appPasswordNeeded = true)
            else -> DiscoveredMailSettings(
                MailAccountSettings(server("imap.$domain", 993, MailServerSettings.Security.TLS), server("smtp.$domain", 587, MailServerSettings.Security.STARTTLS)),
                DiscoveredMailSettings.Source.GUESS, "", appPasswordNeeded = false)
        }
    }

    override suspend fun addMailAccount(email: String, password: String, senderName: String, name: String, settings: MailAccountSettings?) = attempt {
        val address = email.trim()
        val domain = address.substringAfter('@', "").lowercase()
        val servers = settings ?: discoverMailSettings(address).getOrThrow().settings
        if (domain == "unreachable.example" || servers.imap.host.endsWith(".invalid")) {
            throw PimBackendException.Unavailable("Can't reach ${servers.imap.host}:${servers.imap.port}", null)
        }
        checkPassword(password)
        if (domain == "gmail.com" && !Regex("[a-zA-Z]{16}").matches(password.replace(" ", ""))) {
            throw PimBackendException.Unauthenticated("Gmail refused the password: it takes an app password (16 letters) from myaccount.google.com/apppasswords", null)
        }
        if (all().any { it.emailAddress.equals(address, ignoreCase = true) }) {
            throw PimBackendException.InvalidArgument("There's an account for $address already", null)
        }
        add(account("fake_imap_${nextId++}", PimAccount.Type.IMAP, name.trim().ifBlank { address }, address, senderName.trim()))
    }

    override suspend fun startLogin(account: PimAccount) = attempt {
        find(account.id)
        _logins.update { it + PendingLogin("login_${account.id}_${nextId++}", account.id, "Log in to ${account.name}", "https://accounts.google.com/o/oauth2/auth?sample", REDIRECT) }
        Unit
    }

    override suspend fun cancelLogin(login: PendingLogin) = attempt {
        _logins.update { list -> list.filterNot { it.id == login.id } }
        _results.tryEmit(LoginResult(login.id, login.accountId, success = false, message = "Cancelled"))
        Unit
    }

    override suspend fun finishLogin(login: PendingLogin, pastedAddress: String) = attempt {
        val current = _logins.value.firstOrNull { it.id == login.id }
            ?: throw PimBackendException.Unavailable("That login has ended", null)
        val address = LoginRedirect.extract(pastedAddress, current.redirectPrefix)
            ?: throw PimBackendException.InvalidArgument("That's not the address Google's last page ended on", null)
        _logins.update { list -> list.filterNot { it.id == login.id } }
        if (address.contains("error=")) {
            _results.tryEmit(LoginResult(login.id, login.accountId, success = false, message = "Google said no: ${address.substringAfter("error=").substringBefore('&')}"))
        } else {
            change(login.accountId) { it.copy(needsLogin = false) }
            sync(login.accountId)
            _results.tryEmit(LoginResult(login.id, login.accountId, success = true, message = ""))
        }
        Unit
    }

    override suspend fun removeAccount(account: PimAccount) = attempt {
        find(account.id)
        _accounts.update { list -> list?.filterNot { it.id == account.id } }
        _logins.update { list -> list.filterNot { it.accountId == account.id } }
        Unit
    }

    override suspend fun setOnline(account: PimAccount, online: Boolean) = attempt {
        change(account.id) {
            it.copy(online = online, state = if (online) PimAccount.State.IDLE else PimAccount.State.OFFLINE,
                statusMessage = if (online) "Ready" else "Offline")
        }
        if (online) sync(account.id)
        Unit
    }

    override suspend fun syncNow(account: PimAccount?) = attempt {
        (if (account == null) all() else listOf(find(account.id))).filter { it.online }.forEach { sync(it.id) }
        Unit
    }
}
