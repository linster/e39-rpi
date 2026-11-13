package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.Account
import ca.stefanm.e39pim.v1.AccountState
import ca.stefanm.e39pim.v1.AccountType
import ca.stefanm.e39pim.v1.AuthEvent
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

/** An account (CalDAV server, Google account, ...) the PIM bridge syncs calendars from. */
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
) {
    enum class Type { CALDAV, GOOGLE, ICAL_FILE, OTHER }
    enum class State { IDLE, SYNCING, OFFLINE, NOT_CONFIGURED, BROKEN }
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
    )
}
