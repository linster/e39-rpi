package ca.stefanm.ibus.gui.pim.accounts.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.AccountRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.DiscoveredMailSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.MailAccountSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.MailServerSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Which of a mail account's two servers. */
enum class MailServerRole { INCOMING, OUTGOING }

/** The "Add Email Account" form. */
data class MailAccountForm(
    val email: String = "",
    val password: String = "",
    val senderName: String = "",
    /** Empty: the address. */
    val name: String = "",
    /** What the bridge found for [email]; null before looking (or nothing found). */
    val discovered: DiscoveredMailSettings? = null,
    /** The servers to use (what was found, or typed); null before looking. */
    val settings: MailAccountSettings? = null,
    /** Nothing was found: the servers are typed (or edited by hand). */
    val manual: Boolean = false,
) {
    /** The servers are known (found or typed). */
    val serversKnown: Boolean get() = settings != null && settings.imap.host.isNotBlank() && settings.smtp.host.isNotBlank()

    /** The provider wants an app password: what to tell the user. */
    val appPasswordHint: String? get() = discovered?.takeIf { it.appPasswordNeeded }?.let { d ->
        val provider = d.providerName.ifBlank { "This provider" }
        if (d.providerName.equals("Gmail", ignoreCase = true)) {
            "Gmail needs an app password, not your Google password: turn on 2-Step Verification, then make one at myaccount.google.com/apppasswords."
        } else {
            "$provider needs an app password, not your usual one: make one in its account settings."
        }
    }

    /** Where the servers came from, in words. */
    val sourceText: String get() = when {
        manual -> "Typed in"
        discovered == null -> ""
        else -> listOf(discovered.providerName, when (discovered.source) {
            DiscoveredMailSettings.Source.PROVIDER -> "the provider's settings"
            DiscoveredMailSettings.Source.ISPDB -> "Thunderbird's list of providers"
            DiscoveredMailSettings.Source.MX -> "the domain's mail servers"
            DiscoveredMailSettings.Source.DNS_SRV -> "the domain's DNS"
            DiscoveredMailSettings.Source.GUESS -> "a guess: check them"
            DiscoveredMailSettings.Source.UNKNOWN -> "found"
        }).filter { it.isNotBlank() }.joinToString(": ")
    }

    fun server(role: MailServerRole): MailServerSettings? = settings?.let { if (role == MailServerRole.INCOMING) it.imap else it.smtp }
}

/**
 * Adding a mail account: the address, then the bridge looks for its servers
 * (shown to confirm, editable; typed when nothing is found), the password
 * (an app password where the provider wants one), the sender's name. Kept
 * here so the form survives the keyboard.
 */
@ApplicationScope
class AddMailAccountRepo @Inject constructor(
    private val accounts: AccountRepository,
) {
    companion object {
        fun serverText(s: MailServerSettings): String =
            if (s.host.isBlank()) "Enter..." else "${s.host}:${s.port} · ${securityText(s.security)}"

        fun securityText(s: MailServerSettings.Security) = when (s) {
            MailServerSettings.Security.TLS -> "TLS"
            MailServerSettings.Security.STARTTLS -> "STARTTLS"
            MailServerSettings.Security.NONE -> "no encryption"
        }

        /** Usual ports: IMAP 993 (TLS) / 143; SMTP 465 (TLS) / 587. */
        fun defaultPort(role: MailServerRole, security: MailServerSettings.Security) = when (role) {
            MailServerRole.INCOMING -> if (security == MailServerSettings.Security.TLS) 993 else 143
            MailServerRole.OUTGOING -> if (security == MailServerSettings.Security.TLS) 465 else 587
        }

        /** What went wrong adding, in words. */
        fun describe(e: Throwable, form: MailAccountForm): String = when (e) {
            is PimBackendException.Unauthenticated ->
                listOfNotNull(e.message, form.appPasswordHint).joinToString(" ")
            is PimBackendException.Unavailable -> "Can't reach the server: ${e.message}"
            is PimBackendException.FailedPrecondition -> "${e.message} (this account only takes a browser login: try an app password)"
            else -> e.message ?: e.toString()
        }
    }

    private val _form = MutableStateFlow<MailAccountForm?>(null)
    /** Null when no account is being added. */
    val form: StateFlow<MailAccountForm?> = _form.asStateFlow()

    fun start(): MailAccountForm = MailAccountForm().also { _form.value = it }

    fun discard() {
        _form.value = null
    }

    private fun change(f: (MailAccountForm) -> MailAccountForm) = _form.update { it?.let(f) }

    /** A new address: the servers found for the old one no longer apply. */
    fun setEmail(email: String) = change { it.copy(email = email.trim(), discovered = null, settings = null, manual = false) }

    fun setPassword(password: String) = change { it.copy(password = password) }
    fun setSenderName(name: String) = change { it.copy(senderName = name.trim()) }
    fun setName(name: String) = change { it.copy(name = name.trim()) }

    /**
     * Looks for the address's servers. Nothing found: the form switches to
     * typing them (manual), with the address as the user name and the usual
     * ports. Fails for a bad address or an unreachable bridge.
     */
    suspend fun discover(): Result<MailAccountForm> {
        val f = _form.value ?: return Result.failure(IllegalStateException("No form"))
        if (f.email.isBlank()) return Result.failure(IllegalArgumentException("Enter the e-mail address first"))
        return accounts.discoverMailSettings(f.email).fold(
            onSuccess = { found ->
                change { it.copy(discovered = found, settings = found.settings, manual = false) }
                Result.success(_form.value!!)
            },
            onFailure = { e ->
                if (e is PimBackendException.NotFound) {
                    val sec = MailServerSettings.Security.TLS
                    change {
                        it.copy(discovered = null, manual = true, settings = MailAccountSettings(
                            MailServerSettings("", defaultPort(MailServerRole.INCOMING, sec), sec, f.email),
                            MailServerSettings("", defaultPort(MailServerRole.OUTGOING, sec), sec, f.email),
                        ))
                    }
                    Result.success(_form.value!!)
                } else {
                    Result.failure(e)
                }
            },
        )
    }

    /** Edits a server (found or typed): the form uses those settings from now on. */
    fun editServer(role: MailServerRole, f: (MailServerSettings) -> MailServerSettings) = change { form ->
        val s = form.settings ?: return@change form
        form.copy(settings = if (role == MailServerRole.INCOMING) s.copy(imap = f(s.imap)) else s.copy(smtp = f(s.smtp)))
    }

    /** A security choice also sets the usual port for it. */
    fun setSecurity(role: MailServerRole, security: MailServerSettings.Security) =
        editServer(role) { it.copy(security = security, port = defaultPort(role, security)) }

    /**
     * Adds the account with the servers shown (the bridge tries both logins
     * first). The form is kept on failure (see [describe]).
     */
    suspend fun add(): Result<PimAccount> {
        val f = _form.value ?: return Result.failure(IllegalStateException("No form"))
        when {
            f.email.isBlank() -> return Result.failure(IllegalArgumentException("Enter the e-mail address"))
            !f.serversKnown -> return Result.failure(IllegalArgumentException("Find the servers first (or enter them)"))
            f.password.isEmpty() -> return Result.failure(IllegalArgumentException("Enter the password"))
        }
        return accounts.addMailAccount(f.email, f.password, f.senderName, f.name, f.settings)
            .onSuccess { if (_form.value == f) discard() }
    }
}
