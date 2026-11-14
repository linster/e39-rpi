package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.Account
import ca.stefanm.e39pim.v1.AccountServiceGrpcKt
import ca.stefanm.e39pim.v1.AuthEvent
import ca.stefanm.e39pim.v1.AuthServiceGrpcKt
import ca.stefanm.e39pim.v1.DiscoverMailSettingsResponse
import ca.stefanm.e39pim.v1.MailSettings
import ca.stefanm.e39pim.v1.StartLoginResponse
import ca.stefanm.e39pim.v1.addCalDavAccountRequest
import ca.stefanm.e39pim.v1.addGoogleAccountRequest
import ca.stefanm.e39pim.v1.addMailAccountRequest
import ca.stefanm.e39pim.v1.discoverMailSettingsRequest
import ca.stefanm.e39pim.v1.cancelAuthRequest
import ca.stefanm.e39pim.v1.listAccountsRequest
import ca.stefanm.e39pim.v1.removeAccountRequest
import ca.stefanm.e39pim.v1.setAccountOnlineRequest
import ca.stefanm.e39pim.v1.setContactsEnabledRequest
import ca.stefanm.e39pim.v1.startLoginRequest
import ca.stefanm.e39pim.v1.syncNowRequest
import ca.stefanm.e39pim.v1.watchAccountsRequest
import ca.stefanm.e39pim.v1.watchAuthRequestsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for accounts and logins: e39pim.v1 AccountService and
 * AuthService calls, with protobuf messages in and out.
 * [RemoteAccountRepository] builds on it. Every call throws
 * [PimBackendException] on failure.
 */
class AccountGrpcClient @Inject constructor(
    private val backendChannel: PimBackendChannel,
) {
    companion object {
        /** Adding a CalDAV account first tries the login on the server. */
        const val ADD_ACCOUNT_TIMEOUT_SECONDS = 45L
        /** Several lookups at once, then maybe connection attempts. */
        const val DISCOVER_MAIL_TIMEOUT_SECONDS = 40L
        /** Maybe discovery, then the IMAP and the SMTP login (up to 20 s each). */
        const val ADD_MAIL_ACCOUNT_TIMEOUT_SECONDS = 90L
    }

    private fun accountStub() = AccountServiceGrpcKt.AccountServiceCoroutineStub(backendChannel.channel())
    private fun authStub() = AuthServiceGrpcKt.AuthServiceCoroutineStub(backendChannel.channel())
    private fun <S : io.grpc.kotlin.AbstractCoroutineStub<S>> S.withTimeout(seconds: Long = PimBackendChannel.CALL_TIMEOUT_SECONDS): S =
        withDeadlineAfter(seconds, TimeUnit.SECONDS)

    suspend fun listAccounts(): List<Account> = pimCall {
        accountStub().withTimeout().listAccounts(listAccountsRequest { }).accountsList
    }

    /** The whole list, then again on every change. */
    fun watchAccounts(): Flow<List<Account>> =
        accountStub().watchAccounts(watchAccountsRequest { })
            .map { it.accountsList }
            .catch { throw PimBackendException.from(it) }

    /** [contacts]: also the server's address books (CardDAV). */
    suspend fun addCalDavAccount(name: String, url: String, username: String, password: String, refreshMinutes: Int = 0, contacts: Boolean = true): Account = pimCall {
        accountStub().withTimeout(ADD_ACCOUNT_TIMEOUT_SECONDS).addCalDavAccount(addCalDavAccountRequest {
            this.name = name
            this.url = url
            this.username = username
            this.password = password
            this.refreshIntervalMinutes = refreshMinutes
            this.contacts = contacts
        })
    }

    /** CalDAV accounts only; restarts the account's sync (slow-ish). */
    suspend fun setContactsEnabled(accountId: String, enabled: Boolean): Account = pimCall {
        accountStub().withTimeout(ADD_ACCOUNT_TIMEOUT_SECONDS).setContactsEnabled(setContactsEnabledRequest {
            this.accountId = accountId
            this.enabled = enabled
        })
    }

    suspend fun addGoogleAccount(name: String, loginHint: String): StartLoginResponse = pimCall {
        accountStub().withTimeout(ADD_ACCOUNT_TIMEOUT_SECONDS).addGoogleAccount(addGoogleAccountRequest {
            this.name = name
            this.loginHint = loginHint
        })
    }

    suspend fun discoverMailSettings(email: String): DiscoverMailSettingsResponse = pimCall {
        accountStub().withTimeout(DISCOVER_MAIL_TIMEOUT_SECONDS).discoverMailSettings(discoverMailSettingsRequest {
            this.email = email
        })
    }

    /** [settings] null: discovered by the bridge. */
    suspend fun addMailAccount(
        email: String,
        password: String,
        senderName: String,
        name: String,
        settings: MailSettings?,
        checkIntervalMinutes: Int = 0,
    ): Account = pimCall {
        accountStub().withTimeout(ADD_MAIL_ACCOUNT_TIMEOUT_SECONDS).addMailAccount(addMailAccountRequest {
            this.email = email
            this.password = password
            this.senderName = senderName
            this.name = name
            if (settings != null) this.settings = settings
            this.checkIntervalMinutes = checkIntervalMinutes
        })
    }

    suspend fun startLogin(accountId: String): StartLoginResponse = pimCall {
        accountStub().withTimeout().startLogin(startLoginRequest { this.accountId = accountId })
    }

    suspend fun removeAccount(accountId: String) {
        pimCall { accountStub().withTimeout().removeAccount(removeAccountRequest { this.accountId = accountId }) }
    }

    suspend fun setAccountOnline(accountId: String, online: Boolean): Account = pimCall {
        accountStub().withTimeout().setAccountOnline(setAccountOnlineRequest {
            this.accountId = accountId
            this.online = online
        })
    }

    /** [accountId] empty: all accounts. */
    suspend fun syncNow(accountId: String = "") {
        pimCall { accountStub().withTimeout().syncNow(syncNowRequest { this.accountId = accountId }) }
    }

    /** Logins waiting for the user first, then new ones and their results. */
    fun watchAuthRequests(): Flow<AuthEvent> =
        authStub().watchAuthRequests(watchAuthRequestsRequest { })
            .catch { throw PimBackendException.from(it) }

    suspend fun cancelAuth(requestId: String) {
        pimCall { authStub().withTimeout().cancelAuth(cancelAuthRequest { id = requestId }) }
    }
}
