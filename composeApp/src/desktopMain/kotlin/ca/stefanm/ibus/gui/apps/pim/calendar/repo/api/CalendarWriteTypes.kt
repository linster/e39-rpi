package ca.stefanm.ibus.gui.pim.calendar.repo.api

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** When an event happens, for creating or changing it. */
sealed interface EventTimes {
    /** [start] before [end]; stored in [zone] (repeats keep their local time across DST there). */
    data class Timed(
        val start: Instant,
        val end: Instant,
        val zone: TimeZone = TimeZone.currentSystemDefault(),
    ) : EventTimes

    /** Whole days, [startDate] up to (not including) [endDateExclusive]. */
    data class AllDay(val startDate: LocalDate, val endDateExclusive: LocalDate) : EventTimes
}

/** Which part of a repeating event a change or deletion is for. */
enum class EventScope {
    /** Only the occurrence shown (an exception in the iCalendar sense). */
    THIS_OCCURRENCE,
    /** The whole series (or the event, if it doesn't repeat). */
    WHOLE_SERIES,
}

/** A calendar the HMI can show and (unless [readOnly]) write to. */
data class CalendarInfo(
    val id: String,
    val name: String,
    val accountName: String,
    val color: Color,
    val readOnly: Boolean,
    val hasEvents: Boolean,
    val hasTodos: Boolean,
)

/** A to-do from the PIM bridge. IDs and [revision] are the bridge's (opaque). */
data class TodoItem(
    val id: String,
    val calendarId: String,
    /** Sent back with changes; the bridge refuses them if the to-do changed meanwhile. */
    val revision: Long,
    val summary: String,
    val description: String,
    /** Due at a time, or on a day ([dueDate]), or neither. */
    val due: Instant?,
    val dueDate: LocalDate?,
    val completed: Boolean,
    val completedAt: Instant?,
    /** 0 = none, 1 = highest .. 9 = lowest. */
    val priority: Int,
    val percentComplete: Int,
    val color: Color,
    val readOnly: Boolean,
    val recurring: Boolean,
)
