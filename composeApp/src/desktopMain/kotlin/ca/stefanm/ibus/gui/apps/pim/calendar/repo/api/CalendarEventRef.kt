package ca.stefanm.ibus.gui.pim.calendar.repo.api

import kotlin.time.Instant

/**
 * Which event (occurrence) in the PIM bridge an [ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData]
 * shows, for opening its details and (later) editing it. IDs are the
 * bridge's (e39pim.v1): opaque, not to be parsed or kept.
 */
data class CalendarEventRef(
    /** The event; for repeating events, the series. */
    val eventId: String,
    /** The occurrence's original start, for repeating events. */
    val recurrenceId: Instant?,
    val calendarId: String,
    /** Sent back with changes; the bridge refuses them if the event changed meanwhile. */
    val revision: Long,
    val readOnly: Boolean,
    val recurring: Boolean,
)
