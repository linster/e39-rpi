package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import androidx.compose.ui.graphics.Color
import ca.stefanm.e39pim.v1.ChangeNotice
import ca.stefanm.e39pim.v1.Date
import ca.stefanm.e39pim.v1.EventOccurrence
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarEventRef
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** e39pim.v1 messages ⇄ the HMI's calendar types. */

fun Instant.toTimestamp(): Timestamp = timestamp {
    seconds = epochSeconds
    nanos = nanosecondsOfSecond
}

fun Timestamp.toInstant(): Instant = Instant.fromEpochSeconds(seconds, nanos)

fun Date.toLocalDate(): LocalDate = LocalDate(year, month, day)

/**
 * An occurrence as the agenda views draw it. All-day events run from
 * midnight of their first day to one second before midnight after their
 * last: ending exactly at midnight, the agenda's day splitter would add an
 * empty piece on the next day.
 */
fun EventOccurrence.toAgendaEvent(zone: TimeZone): AgendaCalendarEventData {
    val (start, end) = if (allDay) {
        startDate.toLocalDate().atStartOfDayIn(zone) to
            endDate.toLocalDate().atStartOfDayIn(zone).minus(1.seconds)
    } else {
        start.toInstant() to end.toInstant()
    }
    return AgendaCalendarEventData(
        headerText = summary,
        start = start,
        // The agenda needs end after start; zero-length events get a minute.
        end = if (end > start) end else start.plus(60.seconds),
        color = Color(colorArgb),
        allDayEvent = allDay,
        eventRef = CalendarEventRef(
            eventId = eventId,
            recurrenceId = if (hasRecurrenceId()) recurrenceId.toInstant() else null,
            calendarId = calendarId,
            revision = revision,
            readOnly = readOnly,
            recurring = recurring,
        ),
    )
}

/**
 * Whether views showing [[start], [end]) must query again after [notice].
 * `ready` means the change stream (re)connected: changes may have been
 * missed.
 */
fun ChangeNotice.affects(start: Instant, end: Instant): Boolean = when {
    hasReady() || hasCalendarsChanged() -> true
    hasEventsChanged() -> with(eventsChanged) {
        !hasStart() || !hasEnd() || (this.start.toInstant() < end && this.end.toInstant() > start)
    }
    else -> false // to-dos: not shown in the agenda
}
