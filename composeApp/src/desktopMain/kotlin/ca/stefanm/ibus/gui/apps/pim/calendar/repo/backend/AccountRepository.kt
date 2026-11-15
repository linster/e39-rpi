package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The accounts the PIM bridge syncs (calendars, contacts, mail) and their
 * logins: [RemoteAccountRepository] (the bridge) or [FakeAccountRepository]
 * (sample accounts), chosen by E39Config.PimConfig.Accounts.useFakeData
 * (AccountModule). Writes return [Result]s with [PimBackendException]s.
 */
interface AccountRepository {
    /** Every account, by name; null until the bridge has answered. */
    val accounts: StateFlow<List<PimAccount>?>

    /** Logins waiting for the user (a browser page), oldest first. */
    val pendingLogins: Flow<List<PendingLogin>>

    /** Logins as they end. */
    val loginResults: Flow<LoginResult>

    /**
     * Adds a CalDAV account; the bridge tries the login first:
     * Unauthenticated if the server refuses it, Unavailable if it can't be
     * reached. [contacts]: also sync the server's address books (CardDAV).
     */
    suspend fun addCalDavAccount(name: String, url: String, username: String, password: String, contacts: Boolean = true): Result<PimAccount>

    /** CalDAV accounts: sync the address books too, or stop. FailedPrecondition for others. */
    suspend fun setContactsEnabled(account: PimAccount, enabled: Boolean): Result<PimAccount>

    /** Adds a Google account and starts its login (in [pendingLogins]). */
    suspend fun addGoogleAccount(loginHint: String = ""): Result<PimAccount>

    /** [email]'s IMAP and SMTP servers. NotFound: nothing found; InvalidArgument: not an address. */
    suspend fun discoverMailSettings(email: String): Result<DiscoveredMailSettings>

    /**
     * Adds a mail account (IMAP, SMTP, sender identity); [settings] null: the
     * bridge discovers them. Both logins are tried first: Unauthenticated
     * (a refused password), Unavailable, FailedPrecondition (browser login
     * only). [name] empty: the address.
     */
    suspend fun addMailAccount(email: String, password: String, senderName: String, name: String = "",
                               settings: MailAccountSettings? = null): Result<PimAccount>

    suspend fun startLogin(account: PimAccount): Result<Unit>

    suspend fun cancelLogin(login: PendingLogin): Result<Unit>

    /**
     * Finishes [login] with the address its browser ended on (Google's
     * redirect to the bridge's 127.0.0.1 listener, opened on a phone where
     * 127.0.0.1 is the phone): sends it to the listener. InvalidArgument: not
     * that login's address; FailedPrecondition: the bridge runs on another
     * computer; Unavailable: the listener didn't answer (the login ended).
     * The result arrives on [loginResults].
     */
    suspend fun finishLogin(login: PendingLogin, pastedAddress: String): Result<Unit>

    /** Also deletes its password and its local copy (not the server's). */
    suspend fun removeAccount(account: PimAccount): Result<Unit>

    suspend fun setOnline(account: PimAccount, online: Boolean): Result<Unit>

    /** [account] null: all accounts. */
    suspend fun syncNow(account: PimAccount? = null): Result<Unit>
}

/** The address a login's browser ended on, found in what was pasted. */
object LoginRedirect {
    /**
     * The address in [pasted] (with or without "http://", maybe with text
     * around it) that belongs to a login waiting at [redirectPrefix]
     * (e.g. "http://127.0.0.1:41234/") and carries Google's answer
     * ("code=" or "error="); null if there's none.
     */
    fun extract(pasted: String, redirectPrefix: String): String? {
        val hostPort = redirectPrefix.trim().removePrefix("http://").trimEnd('/')
        if (hostPort.isEmpty()) return null
        val at = pasted.indexOf(hostPort)
        if (at < 0) return null
        val rest = pasted.substring(at).takeWhile { !it.isWhitespace() && it != '"' && it != '<' }
        val after = rest.removePrefix(hostPort)
        if (after.isNotEmpty() && after[0] !in "/?#") return null // another port
        if (!rest.contains("code=") && !rest.contains("error=")) return null
        return "http://$rest"
    }
}
