package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import androidx.compose.ui.graphics.Color
import ca.stefanm.e39pim.v1.Calendar
import ca.stefanm.e39pim.v1.ChangeNotice
import ca.stefanm.e39pim.v1.Date
import ca.stefanm.e39pim.v1.Event
import ca.stefanm.e39pim.v1.EventKt
import ca.stefanm.e39pim.v1.EventOccurrence
import ca.stefanm.e39pim.v1.Todo
import ca.stefanm.e39pim.v1.date
import ca.stefanm.e39pim.v1.eventOccurrence
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarEventRef
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarInfo
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventTimes
import ca.stefanm.ibus.gui.pim.calendar.repo.api.TodoItem
import kotlinx.datetime.number
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

/** An event (as GetEvent/CreateEvent/UpdateEvent return it) as the agenda views draw it. */
fun Event.toAgendaEvent(zone: TimeZone): AgendaCalendarEventData =
    eventOccurrence {
        eventId = this@toAgendaEvent.eventId
        if (this@toAgendaEvent.hasRecurrenceId()) recurrenceId = this@toAgendaEvent.recurrenceId
        calendarId = this@toAgendaEvent.calendarId
        summary = this@toAgendaEvent.summary
        location = this@toAgendaEvent.location
        allDay = this@toAgendaEvent.allDay
        if (this@toAgendaEvent.hasStart()) start = this@toAgendaEvent.start
        if (this@toAgendaEvent.hasEnd()) end = this@toAgendaEvent.end
        if (this@toAgendaEvent.hasStartDate()) startDate = this@toAgendaEvent.startDate
        if (this@toAgendaEvent.hasEndDate()) endDate = this@toAgendaEvent.endDate
        colorArgb = this@toAgendaEvent.colorArgb
        recurring = this@toAgendaEvent.hasRecurrenceId() || this@toAgendaEvent.rrule.isNotEmpty()
        readOnly = this@toAgendaEvent.readOnly
        revision = this@toAgendaEvent.revision
    }.toAgendaEvent(zone)

fun LocalDate.toProtoDate(): Date = date {
    year = this@toProtoDate.year
    month = this@toProtoDate.month.number
    day = this@toProtoDate.day
}

/** Sets the time fields (all_day, start/end or dates, time_zone) of an Event being built. */
fun EventKt.Dsl.setTimes(times: EventTimes) {
    when (times) {
        is EventTimes.Timed -> {
            allDay = false
            start = times.start.toTimestamp()
            end = times.end.toTimestamp()
            timeZone = times.zone.id
        }
        is EventTimes.AllDay -> {
            allDay = true
            startDate = times.startDate.toProtoDate()
            endDate = times.endDateExclusive.toProtoDate()
        }
    }
}

/** UpdateEvent mask paths for the time fields (they go together). */
val EVENT_TIME_PATHS = listOf("all_day", "start", "end", "start_date", "end_date", "time_zone")

fun Calendar.toCalendarInfo() = CalendarInfo(
    id = id,
    name = name,
    accountName = accountName,
    color = Color(colorArgb),
    readOnly = readOnly,
    hasEvents = hasEvents,
    hasTodos = hasTodos,
)

fun Todo.toTodoItem() = TodoItem(
    id = todoId,
    calendarId = calendarId,
    revision = revision,
    summary = summary,
    description = description,
    due = if (hasDue()) due.toInstant() else null,
    dueDate = if (hasDueDate()) dueDate.toLocalDate() else null,
    completed = completed,
    completedAt = if (hasCompletedAt()) completedAt.toInstant() else null,
    priority = priority,
    percentComplete = percentComplete,
    color = Color(colorArgb),
    readOnly = readOnly,
    recurring = recurring,
)

/** Whether to-do lists must query again after [notice] (see [affects]). */
fun ChangeNotice.affectsTodos(): Boolean = hasReady() || hasCalendarsChanged() || hasTodosChanged()

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
