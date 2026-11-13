package ca.stefanm.ibus.gui.pim.calendar.repo.api

import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.core.plusDays
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import javax.inject.Inject

/** Events for the agenda views (day columns), from the PIM bridge. */
class WeekViewRepo @Inject constructor(
    private val remoteCalendarRepository: RemoteCalendarRepository,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "WeekViewRepo"
    }

    /**
     * Events from the start of [startDay] to the start of [endDate] (local
     * time). Emits again when they change in the calendar (sync, edits on
     * other devices). Remember the flow in composables: each call starts
     * its own queries.
     */
    fun getAgendaViewEvents(
        startDay : LocalDate,
        endDate: LocalDate
    ) : Flow<List<AgendaCalendarEventData>> {
        val zone = TimeZone.currentSystemDefault()
        return remoteCalendarRepository.events(
            start = startDay.atStartOfDayIn(zone),
            end = endDate.atStartOfDayIn(zone),
            zone = zone,
        )
    }

    fun getAgendaViewEvents(
        startDay: LocalDate,
        numDays : Int
    ) : Flow<List<AgendaCalendarEventData>> {
        return getAgendaViewEvents(
            startDay,
            startDay.plusDays(numDays)
        )
    }

    /**
     * Saves an event from the editor. [original]: the event as it was
     * opened (null, or without an eventRef: a new event, created in the
     * default calendar). Only what changed is sent: the name, and the times
     * if start or end moved ([edited]'s allDayEvent decides whether they're
     * sent as days or times). For a repeating event, [scope] says whether
     * the change is for this occurrence or the whole series (then the times
     * can't change). Returns the saved event; a failure's exception is a
     * [ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException]
     * (Conflict: it changed meanwhile) or an IllegalArgumentException.
     */
    suspend fun saveEditedEvent(
        original: AgendaCalendarEventData?,
        edited: AgendaCalendarEventData,
        scope: EventScope = EventScope.THIS_OCCURRENCE,
    ): Result<AgendaCalendarEventData> {
        val ref = original?.eventRef
        if (ref == null) {
            return remoteCalendarRepository.createEvent(summary = edited.headerText, times = timesOf(edited))
        }
        val summary = edited.headerText.takeIf { it != original.headerText }
        val times = if (edited.start != original.start || edited.end != original.end || edited.allDayEvent != original.allDayEvent) {
            timesOf(edited)
        } else {
            null
        }
        if (summary == null && times == null) {
            logger.d(TAG, "Nothing changed in ${original.headerText}")
            return Result.success(original)
        }
        return remoteCalendarRepository.updateEvent(ref, scope, summary = summary, times = times)
    }

    /**
     * Deletes the event [event] shows: for a repeating event, this
     * occurrence or the whole series ([scope]).
     */
    suspend fun deleteEvent(event: AgendaCalendarEventData, scope: EventScope = EventScope.THIS_OCCURRENCE): Result<Unit> {
        val ref = event.eventRef ?: return Result.failure(IllegalArgumentException("${event.headerText} isn't from a calendar"))
        return remoteCalendarRepository.deleteEvent(ref, scope)
    }

    /**
     * An agenda event's times for the bridge. All-day events run from
     * midnight of the first day to just before midnight after the last (see
     * CalendarMapper), so the last day is the end's date.
     */
    private fun timesOf(event: AgendaCalendarEventData): EventTimes {
        if (!event.allDayEvent) {
            return EventTimes.Timed(event.start, event.end)
        }
        val zone = TimeZone.currentSystemDefault()
        return EventTimes.AllDay(
            startDate = event.start.toLocalDateTime(zone).date,
            endDateExclusive = event.end.toLocalDateTime(zone).date.plusDays(1),
        )
    }
}
