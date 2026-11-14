package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.Account
import ca.stefanm.e39pim.v1.AccountState
import ca.stefanm.e39pim.v1.AccountType
import ca.stefanm.e39pim.v1.AuthEvent
import ca.stefanm.e39pim.v1.MailAuthMethod
import ca.stefanm.e39pim.v1.MailSecurity
import ca.stefanm.e39pim.v1.MailSettingsSource
import ca.stefanm.e39pim.v1.mailServer
import ca.stefanm.e39pim.v1.mailSettings
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** An account (CalDAV server, Google account, mail account, ...) the PIM bridge syncs. */
data class PimAccount(
    val id: String,
    val type: Type,
    val name: String,
    val online: Boolean,
    val state: State,
    /** What it says it's doing or what's wrong, in its words. */
    val statusMessage: String,
    /** 0..100 while syncing. */
    val progressPercent: Int,
    /** Google: the login has to be done (again) in a browser. */
    val needsLogin: Boolean,
    /** The last error (e.g. a refused password), until a sync goes through. */
    val lastError: String,
    /** Mail accounts: the address mail is sent from; else empty. */
    val emailAddress: String = "",
    /** Mail accounts: the sender's name in sent mail. */
    val senderName: String = "",
) {
    enum class Type { CALDAV, GOOGLE, IMAP, ICAL_FILE, OTHER }
    enum class State { IDLE, SYNCING, OFFLINE, NOT_CONFIGURED, BROKEN }
}

/** A mail server (IMAP or SMTP) and how to log in to it. */
data class MailServerSettings(
    val host: String,
    val port: Int,
    val security: Security,
    /** Empty: the e-mail address. */
    val username: String = "",
    val auth: Auth = Auth.PASSWORD,
) {
    enum class Security { TLS, STARTTLS, NONE }
    /** OAUTH2 (a browser login) can't be used for mail accounts yet. */
    enum class Auth { PASSWORD, PASSWORD_ENCRYPTED, OAUTH2 }
}

data class MailAccountSettings(val imap: MailServerSettings, val smtp: MailServerSettings)

/** What the bridge found for an address (RemoteAccountRepository.discoverMailSettings). */
data class DiscoveredMailSettings(
    val settings: MailAccountSettings,
    val source: Source,
    /** E.g. "Fastmail"; empty if unknown. */
    val providerName: String,
    /** The provider prefers a browser login: ask for an app password. */
    val appPasswordNeeded: Boolean,
) {
    enum class Source { PROVIDER, ISPDB, MX, DNS_SRV, GUESS, UNKNOWN }
}

/** A web page (a login) the bridge wants shown in a browser. */
data class PendingLogin(
    val id: String,
    /** Empty if the bridge can't tie it to an account. */
    val accountId: String,
    val title: String,
    val url: String,
    /** The login is done when the browser gets here. */
    val redirectPrefix: String,
)

/** How a login (or other page) ended. */
data class LoginResult(val id: String, val accountId: String, val success: Boolean, val message: String)

/**
 * Accounts and logins from the PIM bridge, reached through
 * [AccountGrpcClient]. [accounts] and [pendingLogins] follow the bridge's
 * streams while collected, reconnecting after errors. Writes return
 * [Result]s with [PimBackendException]s.
 */
@ApplicationScope
class RemoteAccountRepository @Inject constructor(
    private val client: AccountGrpcClient,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    coroutineScope: CoroutineScope,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "RemoteAccountRepository"
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    /** Reconnects [stream] (to the current settings) after it ends or fails. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> reconnecting(what: String, stream: () -> Flow<T>): Flow<T> = settingsRepository.settings
        .flatMapLatest {
            flow {
                while (true) {
                    try {
                        stream().collect { emit(it) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.d(TAG, "$what stream ended: ${e.message}")
                    }
                    delay(RETRY_INTERVAL)
                    backendChannel.resetConnectBackoff()
                }
            }
        }
        .flowOn(Dispatchers.IO)

    /** Every account, by name; null until the bridge has answered. */
    val accounts: StateFlow<List<PimAccount>?> = reconnecting("Accounts") { client.watchAccounts() }
        .map { list -> list.map { it.toPimAccount() } }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), null)

    private val authEvents: SharedFlow<AuthEvent> = reconnecting("Logins") { client.watchAuthRequests() }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000))

    /** Logins waiting for the user, oldest first. */
    val pendingLogins: Flow<List<PendingLogin>> = flow {
        val open = linkedMapOf<String, PendingLogin>()
        emit(emptyList())
        authEvents.collect { event ->
            when {
                event.hasOpened() -> event.opened.let {
                    open[it.id] = PendingLogin(it.id, it.accountId, it.title, it.url, it.redirectPrefix)
                }
                event.hasFinished() -> open.remove(event.finished.id)
            }
            emit(open.values.toList())
        }
    }

    /** Logins as they end. */
    val loginResults: Flow<LoginResult> = flow {
        authEvents.collect { event ->
            if (event.hasFinished()) {
                event.finished.let { emit(LoginResult(it.id, it.accountId, it.success, it.message)) }
            }
        }
    }

    /**
     * Adds a CalDAV account; the bridge tries the login first:
     * [PimBackendException.Unauthenticated] if the server refuses it,
     * [PimBackendException.Unavailable] if it can't be reached.
     */
    suspend fun addCalDavAccount(name: String, url: String, username: String, password: String): Result<PimAccount> = result {
        client.addCalDavAccount(name.trim(), url.trim(), username.trim(), password).toPimAccount()
    }

    /** Adds a Google account and starts its login; the page shows up in [pendingLogins]. */
    suspend fun addGoogleAccount(loginHint: String = ""): Result<PimAccount> = result {
        client.addGoogleAccount("", loginHint.trim()).account.toPimAccount()
    }

    /**
     * Finds [email]'s IMAP and SMTP servers. [PimBackendException.NotFound]:
     * nothing found (ask for them); InvalidArgument: not an address.
     */
    suspend fun discoverMailSettings(email: String): Result<DiscoveredMailSettings> = result {
        val found = client.discoverMailSettings(email.trim())
        DiscoveredMailSettings(
            settings = MailAccountSettings(found.settings.imap.toServer(), found.settings.smtp.toServer()),
            source = when (found.source) {
                MailSettingsSource.MAIL_SETTINGS_SOURCE_PROVIDER -> DiscoveredMailSettings.Source.PROVIDER
                MailSettingsSource.MAIL_SETTINGS_SOURCE_ISPDB -> DiscoveredMailSettings.Source.ISPDB
                MailSettingsSource.MAIL_SETTINGS_SOURCE_MX -> DiscoveredMailSettings.Source.MX
                MailSettingsSource.MAIL_SETTINGS_SOURCE_DNS_SRV -> DiscoveredMailSettings.Source.DNS_SRV
                MailSettingsSource.MAIL_SETTINGS_SOURCE_GUESS -> DiscoveredMailSettings.Source.GUESS
                else -> DiscoveredMailSettings.Source.UNKNOWN
            },
            providerName = found.providerName,
            appPasswordNeeded = found.appPasswordNeeded,
        )
    }

    /**
     * Adds a mail account (IMAP, SMTP and the sender identity). [settings]
     * null: the bridge discovers them. Both logins are tried first:
     * [PimBackendException.Unauthenticated] if a server refuses the
     * password, Unavailable if it can't be reached, FailedPrecondition if
     * it only takes a browser login. [name] empty: the address.
     */
    suspend fun addMailAccount(
        email: String,
        password: String,
        senderName: String,
        name: String = "",
        settings: MailAccountSettings? = null,
    ): Result<PimAccount> = result {
        client.addMailAccount(
            email = email.trim(),
            password = password,
            senderName = senderName.trim(),
            name = name.trim(),
            settings = settings?.let { mailSettings {
                imap = it.imap.toProto()
                smtp = it.smtp.toProto()
            } },
        ).toPimAccount()
    }

    suspend fun startLogin(account: PimAccount): Result<Unit> = result { client.startLogin(account.id) }

    suspend fun cancelLogin(login: PendingLogin): Result<Unit> = result { client.cancelAuth(login.id) }

    /** Also deletes its password and its local copy of the calendars (not the server's). */
    suspend fun removeAccount(account: PimAccount): Result<Unit> = result { client.removeAccount(account.id) }

    suspend fun setOnline(account: PimAccount, online: Boolean): Result<Unit> = result {
        client.setAccountOnline(account.id, online)
    }

    /** [account] null: all accounts. */
    suspend fun syncNow(account: PimAccount? = null): Result<Unit> = result { client.syncNow(account?.id ?: "") }

    private suspend fun <T> result(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        logger.w(TAG, "PIM bridge call failed: ${e.message}")
        Result.failure(e)
    }

    private fun Account.toPimAccount() = PimAccount(
        id = id,
        type = when (type) {
            AccountType.ACCOUNT_TYPE_CALDAV -> PimAccount.Type.CALDAV
            AccountType.ACCOUNT_TYPE_GOOGLE -> PimAccount.Type.GOOGLE
            AccountType.ACCOUNT_TYPE_IMAP -> PimAccount.Type.IMAP
            AccountType.ACCOUNT_TYPE_ICAL_FILE -> PimAccount.Type.ICAL_FILE
            else -> PimAccount.Type.OTHER
        },
        name = name,
        online = online,
        state = when (state) {
            AccountState.ACCOUNT_STATE_SYNCING -> PimAccount.State.SYNCING
            AccountState.ACCOUNT_STATE_OFFLINE -> PimAccount.State.OFFLINE
            AccountState.ACCOUNT_STATE_NOT_CONFIGURED -> PimAccount.State.NOT_CONFIGURED
            AccountState.ACCOUNT_STATE_BROKEN -> PimAccount.State.BROKEN
            else -> PimAccount.State.IDLE
        },
        statusMessage = statusMessage,
        progressPercent = progressPercent,
        needsLogin = needsLogin,
        lastError = lastError,
        emailAddress = emailAddress,
        senderName = senderName,
    )

    private fun ca.stefanm.e39pim.v1.MailServer.toServer() = MailServerSettings(
        host = host,
        port = port,
        security = when (security) {
            MailSecurity.MAIL_SECURITY_STARTTLS -> MailServerSettings.Security.STARTTLS
            MailSecurity.MAIL_SECURITY_NONE -> MailServerSettings.Security.NONE
            else -> MailServerSettings.Security.TLS
        },
        username = username,
        auth = when (auth) {
            MailAuthMethod.MAIL_AUTH_METHOD_PASSWORD_ENCRYPTED -> MailServerSettings.Auth.PASSWORD_ENCRYPTED
            MailAuthMethod.MAIL_AUTH_METHOD_OAUTH2 -> MailServerSettings.Auth.OAUTH2
            else -> MailServerSettings.Auth.PASSWORD
        },
    )

    private fun MailServerSettings.toProto() = mailServer {
        host = this@toProto.host
        port = this@toProto.port
        security = when (this@toProto.security) {
            MailServerSettings.Security.TLS -> MailSecurity.MAIL_SECURITY_TLS
            MailServerSettings.Security.STARTTLS -> MailSecurity.MAIL_SECURITY_STARTTLS
            MailServerSettings.Security.NONE -> MailSecurity.MAIL_SECURITY_NONE
        }
        username = this@toProto.username
        auth = when (this@toProto.auth) {
            MailServerSettings.Auth.PASSWORD -> MailAuthMethod.MAIL_AUTH_METHOD_PASSWORD
            MailServerSettings.Auth.PASSWORD_ENCRYPTED -> MailAuthMethod.MAIL_AUTH_METHOD_PASSWORD_ENCRYPTED
            MailServerSettings.Auth.OAUTH2 -> MailAuthMethod.MAIL_AUTH_METHOD_OAUTH2
        }
    }
}
