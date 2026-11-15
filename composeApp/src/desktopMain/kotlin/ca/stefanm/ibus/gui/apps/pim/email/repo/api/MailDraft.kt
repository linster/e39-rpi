package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailMessage
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import ca.stefanm.ibus.gui.pim.email.repo.OutgoingMail

/** To or Cc. */
enum class RecipientField { TO, CC }

/** A message being written. [text] is what the user typed; [quote] is added below it when [includeQuote]. */
data class MailDraft(
    val mode: Mode,
    /** Whose address and server to send with; null = the bridge's default. */
    val accountId: String?,
    val to: List<MailAddress> = emptyList(),
    val cc: List<MailAddress> = emptyList(),
    val subject: String = "",
    val text: String = "",
    /** Replies: the original, quoted ("On ..., Anna wrote:" and "> " lines). */
    val quote: String = "",
    val includeQuote: Boolean = true,
    /** Replies and forwards: the message answered or forwarded. */
    val original: MailSummary? = null,
) {
    enum class Mode { NEW, REPLY, REPLY_ALL, FORWARD }

    fun recipients(field: RecipientField) = if (field == RecipientField.TO) to else cc

    fun withRecipients(field: RecipientField, list: List<MailAddress>) =
        if (field == RecipientField.TO) copy(to = list) else copy(cc = list)

    val hasRecipients: Boolean get() = to.isNotEmpty() || cc.isNotEmpty()

    /** What's sent as the text. */
    val body: String get() = if (includeQuote && quote.isNotEmpty()) "${text.trimEnd()}\n\n$quote" else text

    fun toOutgoing() = OutgoingMail(
        to = to,
        cc = cc,
        subject = subject.trim(),
        text = body,
        accountId = accountId,
        inReplyTo = original.takeIf { mode == Mode.REPLY || mode == Mode.REPLY_ALL },
        forwardOf = original.takeIf { mode == Mode.FORWARD },
    )

    companion object {
        fun new(accountId: String? = null, to: List<MailAddress> = emptyList()) = MailDraft(Mode.NEW, accountId, to = to)

        /**
         * A reply to [message] (in [accountId]'s folder): to its Reply-To (else
         * its sender; for one of [own] messages, its recipients). [all]: the
         * other recipients in Cc, without [own] addresses.
         */
        fun reply(message: MailMessage, accountId: String?, all: Boolean, own: Set<String>): MailDraft {
            val s = message.summary
            val fromMe = s.from.email.lowercase() in own
            val to = when {
                fromMe -> s.to
                message.replyTo.isNotEmpty() -> message.replyTo
                else -> listOf(s.from)
            }.distinctByEmail()
            val cc = if (!all) emptyList() else (s.to + message.cc)
                .filter { it.email.lowercase() !in own && to.none { t -> t.email.equals(it.email, ignoreCase = true) } }
                .distinctByEmail()
            return MailDraft(
                mode = if (all) Mode.REPLY_ALL else Mode.REPLY,
                accountId = accountId,
                to = to,
                cc = cc,
                subject = prefixed("Re:", s.subject),
                quote = quoteOf(message),
                original = s,
            )
        }

        /** [message] forwarded whole (the bridge attaches it). */
        fun forward(message: MailMessage, accountId: String?) = MailDraft(
            mode = Mode.FORWARD,
            accountId = accountId,
            subject = prefixed("Fwd:", message.summary.subject),
            original = message.summary,
        )

        /** "Re: x" for "x"; "Re: x" stays as it is (also "RE:", "Aw:" for Re). */
        fun prefixed(prefix: String, subject: String): String {
            val trimmed = subject.trim()
            val known = if (prefix == "Re:") listOf("re:", "aw:", "sv:") else listOf("fwd:", "fw:", "wg:")
            return if (known.any { trimmed.lowercase().startsWith(it) }) trimmed else "$prefix $trimmed".trim()
        }

        fun quoteOf(message: MailMessage): String {
            val header = "On ${MailFormat.fullDate(message.summary.date)}, ${message.summary.from.display} wrote:"
            val lines = message.text.trimEnd().lines().joinToString("\n") { if (it.isEmpty()) ">" else "> $it" }
            return "$header\n$lines"
        }

        private fun List<MailAddress>.distinctByEmail() = distinctBy { it.email.lowercase() }
    }
}

/** Reading addresses typed on the keyboard. */
object MailAddresses {
    private val ADDRESS = Regex("^[^\\s@<>]+@[^\\s@<>]+\\.[^\\s@<>]+$")

    fun isValid(email: String) = ADDRESS.matches(email.trim())

    /** "anna@example.com" or "Anna Kowalski <anna@example.com>"; null if it isn't an address. */
    fun parse(input: String): MailAddress? {
        val text = input.trim()
        val angle = Regex("^(.*)<([^>]+)>$").find(text)
        val (name, email) = if (angle != null) {
            angle.groupValues[1].trim().trim('"') to angle.groupValues[2].trim()
        } else {
            "" to text
        }
        return if (isValid(email)) MailAddress(name, email) else null
    }
}
