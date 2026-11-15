package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailMessage
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * The message being written. One at a time, kept here (not in a screen) so
 * it survives going to the contact picker and back, or leaving the compose
 * screen by accident. Start one with [startNew], [startReply] or
 * [startForward]; change it; [send] or [discard] it.
 */
@ApplicationScope
class MailComposeRepo internal constructor(
    private val mail: MailRepository,
    private val mailbox: MailboxListRepo,
    private val senders: MailSenders,
    /** The canned texts (MailSettingsRepository's). */
    val quickReplies: StateFlow<List<String>>,
    val suggestions: RecipientSuggestions,
) {
    @Inject
    constructor(
        mail: MailRepository,
        mailbox: MailboxListRepo,
        senders: MailSenders,
        settings: MailSettingsRepository,
        suggestions: RecipientSuggestions,
    ) : this(mail, mailbox, senders, settings.quickReplies, suggestions)

    private val _draft = MutableStateFlow<MailDraft?>(null)
    /** Null when nothing is being written. */
    val draft: StateFlow<MailDraft?> = _draft.asStateFlow()

    // As started: what "unchanged" means for [hasChanges].
    private var started: MailDraft? = null

    /** Whether discarding the draft loses something the user did. */
    val hasChanges: Boolean get() = _draft.value != null && _draft.value != started

    suspend fun senders(): List<MailSender> = senders.senders()

    fun startNew(to: List<MailAddress> = emptyList(), accountId: String? = null): MailDraft = start(MailDraft.new(accountId, to))

    suspend fun startReply(message: MailMessage, all: Boolean): MailDraft =
        start(MailDraft.reply(message, mailbox.folderOf(message.summary)?.accountId, all, senders.ownAddresses()))

    suspend fun startForward(message: MailMessage): MailDraft =
        start(MailDraft.forward(message, mailbox.folderOf(message.summary)?.accountId))

    private fun start(draft: MailDraft): MailDraft {
        started = draft
        _draft.value = draft
        return draft
    }

    fun setSubject(subject: String) = change { it.copy(subject = subject) }

    fun setText(text: String) = change { it.copy(text = text) }

    fun setIncludeQuote(include: Boolean) = change { it.copy(includeQuote = include) }

    fun setAccount(accountId: String) = change { it.copy(accountId = accountId) }

    /** False if it's already there (in To or Cc). */
    fun addRecipient(field: RecipientField, address: MailAddress): Boolean {
        val d = _draft.value ?: return false
        if ((d.to + d.cc).any { it.email.equals(address.email, ignoreCase = true) }) return false
        change { it.withRecipients(field, it.recipients(field) + address) }
        return true
    }

    fun removeRecipient(field: RecipientField, address: MailAddress) =
        change { it.withRecipients(field, it.recipients(field).filterNot { r -> r.email.equals(address.email, ignoreCase = true) }) }

    /**
     * Where the next contact picked goes, and the picks already used: a
     * picker's result stays with the compose screen's back stack entry, and
     * must count once.
     */
    var pickTarget: RecipientField = RecipientField.TO
    private val usedPicks = mutableSetOf<Long>()

    /** Adds a picked contact once per [token] (true if added now). */
    fun applyPick(token: Long, address: MailAddress): Boolean =
        synchronized(usedPicks) { usedPicks.add(token) } && addRecipient(pickTarget, address)

    fun discard() {
        _draft.value = null
        started = null
    }

    /**
     * Queues the draft (MailRepository.send) and forgets it. Fails with
     * IllegalArgumentException for no recipients; the draft is kept then
     * and on errors from the bridge.
     */
    suspend fun send(): Result<String> {
        val d = _draft.value ?: return Result.failure(IllegalStateException("Nothing to send"))
        if (!d.hasRecipients) return Result.failure(IllegalArgumentException("Add a recipient first"))
        return mail.send(d.toOutgoing()).onSuccess {
            if (_draft.value == d) discard()
        }
    }

    private fun change(f: (MailDraft) -> MailDraft) = _draft.update { it?.let(f) }
}
