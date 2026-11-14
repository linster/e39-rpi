package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteAccountRepository
import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** A mail account to send with; [address] null when the bridge doesn't say. */
data class MailSender(val accountId: String, val accountName: String, val address: MailAddress?) {
    val label: String get() = address?.let { "$accountName <${it.email}>" } ?: accountName
}

/**
 * The user's own mail accounts and addresses: "From" when writing, and who
 * to leave out of a reply to all. [MailModule] picks the bridge's or the
 * fake data's.
 */
interface MailSenders {
    /** Every mail account, in the folder list's order. */
    suspend fun senders(): List<MailSender>

    /** The user's addresses, lower case. */
    suspend fun ownAddresses(): Set<String> = senders().mapNotNull { it.address?.email?.lowercase() }.toSet()
}

/** Accounts from the mail folders; addresses from the bridge's account list (IMAP accounts). */
class AccountMailSenders @Inject constructor(
    private val mail: MailRepository,
    private val accountRepository: RemoteAccountRepository,
) : MailSenders {
    override suspend fun senders(): List<MailSender> {
        val accounts = withTimeoutOrNull(3_000) { accountRepository.accounts.filterNotNull().first() }.orEmpty()
            .filter { it.type == PimAccount.Type.IMAP && it.emailAddress.isNotBlank() }
            .associateBy { it.id }
        return mail.folders().first().distinctBy { it.accountId }.map { folder ->
            val account = accounts[folder.accountId]
            MailSender(folder.accountId, folder.accountName, account?.let { MailAddress(it.senderName, it.emailAddress) })
        }
    }
}

/** The fake data's two accounts. */
class FakeMailSenders @Inject constructor(
    private val mail: MailRepository,
) : MailSenders {
    override suspend fun senders(): List<MailSender> = mail.folders().first().distinctBy { it.accountId }.map { folder ->
        val email = if (folder.accountId == "fake_work") FakeMailRepository.WORK_EMAIL else FakeMailRepository.ME_EMAIL
        MailSender(folder.accountId, folder.accountName, MailAddress(FakeMailRepository.ME_NAME, email))
    }
}
