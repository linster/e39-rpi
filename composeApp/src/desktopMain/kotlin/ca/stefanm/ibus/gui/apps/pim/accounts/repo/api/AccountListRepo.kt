package ca.stefanm.ibus.gui.pim.accounts.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.AccountRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** An account as the accounts screen lists it. */
data class AccountRow(
    val account: PimAccount,
    /** "Mail · stefan@example.com", "CalDAV · contacts too", "Google". */
    val kind: String,
    /** One short word or two: "OK", "syncing 40%", "log in needed". */
    val state: String,
) {
    val name: String get() = account.name

    /** A row's caption (shown in capitals): "Mail · OK", "CalDAV + contacts · syncing". */
    val caption: String get() = when {
        account.type == PimAccount.Type.CALDAV && account.contactsEnabled -> "CalDAV + contacts"
        else -> AccountListRepo.typeLabel(account)
    } + " · " + state

    /** A row's value: the name, and a mail account's address if that's not the name. */
    val title: String get() = account.emailAddress.takeIf { it.isNotBlank() && !it.equals(account.name, ignoreCase = true) }
        ?.let { "${account.name} · $it" } ?: account.name
}

/**
 * The accounts screen's data: every account the PIM bridge syncs (mail,
 * calendars, contacts), mail accounts first, then the others by name.
 */
@ApplicationScope
class AccountListRepo @Inject constructor(
    private val accounts: AccountRepository,
) {
    companion object {
        fun typeLabel(a: PimAccount): String = when (a.type) {
            PimAccount.Type.CALDAV -> "CalDAV"
            PimAccount.Type.GOOGLE -> "Google"
            PimAccount.Type.IMAP -> "Mail"
            PimAccount.Type.ICAL_FILE -> "Calendar file"
            PimAccount.Type.OTHER -> "Other"
        }

        fun kindOf(a: PimAccount): String = when (a.type) {
            PimAccount.Type.IMAP -> listOf("Mail", a.emailAddress).filter { it.isNotBlank() }.joinToString(" · ")
            PimAccount.Type.CALDAV -> if (a.contactsEnabled) "CalDAV · contacts too" else "CalDAV"
            else -> typeLabel(a)
        }

        fun stateOf(a: PimAccount): String = when {
            a.needsLogin -> "log in needed"
            a.state == PimAccount.State.OFFLINE -> "offline"
            a.state == PimAccount.State.SYNCING -> if (a.progressPercent > 0) "syncing ${a.progressPercent}%" else "syncing"
            a.state == PimAccount.State.BROKEN -> "not working"
            a.state == PimAccount.State.NOT_CONFIGURED -> "setting up"
            a.lastError.isNotEmpty() -> "last sync failed"
            else -> "OK"
        }

        fun rowsOf(list: List<PimAccount>): List<AccountRow> =
            list.sortedWith(compareBy({ it.type != PimAccount.Type.IMAP }, { it.name.lowercase() }))
                .map { AccountRow(it, kindOf(it), stateOf(it)) }

        /** What went wrong, for a notification. */
        fun describe(e: Throwable): String = when (e) {
            is PimBackendException.Unavailable -> "Not reachable: ${e.message}"
            else -> e.message ?: e.toString()
        }
    }

    /** Null until the bridge has answered. */
    fun rows(): Flow<List<AccountRow>?> = accounts.accounts.map { it?.let(::rowsOf) }

    val pendingLogins get() = accounts.pendingLogins
    val loginResults get() = accounts.loginResults

    suspend fun syncNow(account: PimAccount? = null) = accounts.syncNow(account)
    suspend fun setOnline(account: PimAccount, online: Boolean) = accounts.setOnline(account, online)
    suspend fun setContactsEnabled(account: PimAccount, enabled: Boolean) = accounts.setContactsEnabled(account, enabled)
    suspend fun remove(account: PimAccount) = accounts.removeAccount(account)
    suspend fun addGoogleAccount() = accounts.addGoogleAccount()
    suspend fun startLogin(account: PimAccount) = accounts.startLogin(account)
    suspend fun cancelLogin(login: ca.stefanm.ibus.gui.pim.calendar.repo.backend.PendingLogin) = accounts.cancelLogin(login)
}
