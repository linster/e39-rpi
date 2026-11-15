package ca.stefanm.ibus.gui.pim.email.repo.api

import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** How the mail screens write dates, sizes, addresses and counts. No UI code. */
object MailFormat {

    private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    /**
     * Short, for lists: "14:05" today, "Tue" within the last week, "Nov 3"
     * this year, else "2025-11-03".
     */
    fun listDate(date: Instant, now: Instant = Clock.System.now(), zone: TimeZone = TimeZone.currentSystemDefault()): String {
        val d = date.toLocalDateTime(zone)
        val today = now.toLocalDateTime(zone).date
        return when {
            d.date == today -> "%02d:%02d".format(d.hour, d.minute)
            d.date > today.minus(DatePeriod(days = 7)) && d.date < today -> DAYS[d.dayOfWeek.ordinal]
            d.year == today.year -> "${MONTHS[d.month.ordinal]} ${d.day}"
            else -> "%04d-%02d-%02d".format(d.year, d.month.ordinal + 1, d.day)
        }
    }

    /** For the reader: "Sat Nov 14 2026, 14:05". */
    fun fullDate(date: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String {
        val d = date.toLocalDateTime(zone)
        return "${DAYS[d.dayOfWeek.ordinal]} ${MONTHS[d.month.ordinal]} ${d.day} ${d.year}, %02d:%02d".format(d.hour, d.minute)
    }

    /** "820 B", "12 KB", "1.4 MB". */
    fun size(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes + 512) / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }

    /** Names (else addresses), comma separated; "and n more" after [max]. */
    fun addresses(list: List<MailAddress>, max: Int = 3): String = when {
        list.isEmpty() -> ""
        list.size <= max -> list.joinToString(", ") { it.display }
        else -> list.take(max).joinToString(", ") { it.display } + " and ${list.size - max} more"
    }

    /** For a badge: "" for none, "999+" past 999. */
    fun unreadBadge(count: Long): String = when {
        count <= 0 -> ""
        count > 999 -> "999+"
        else -> count.toString()
    }

    /** "1–100 of 250", "No messages". */
    fun range(first: Long, shown: Int, total: Long): String =
        if (total == 0L || shown == 0) "No messages" else "$first–${first + shown - 1} of $total"
}
