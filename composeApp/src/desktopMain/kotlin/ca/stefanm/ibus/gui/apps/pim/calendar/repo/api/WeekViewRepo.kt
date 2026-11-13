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
     * default calendar). Only what changed is sent (the name; the times if
     * start or end moved). For a repeating event only this occurrence
     * changes. Returns the saved event; a failure's exception is a
     * [ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException]
     * (Conflict: it changed meanwhile) or an IllegalArgumentException.
     */
    suspend fun saveEditedEvent(
        original: AgendaCalendarEventData?,
        edited: AgendaCalendarEventData,
    ): Result<AgendaCalendarEventData> {
        val ref = original?.eventRef
        if (ref == null) {
            val times = if (edited.allDayEvent) {
                val zone = TimeZone.currentSystemDefault()
                EventTimes.AllDay(
                    startDate = edited.start.toLocalDateTime(zone).date,
                    endDateExclusive = edited.end.toLocalDateTime(zone).date.plusDays(1),
                )
            } else {
                EventTimes.Timed(edited.start, edited.end)
            }
            return remoteCalendarRepository.createEvent(summary = edited.headerText, times = times)
        }
        val summary = edited.headerText.takeIf { it != original.headerText }
        val times = if (edited.start != original.start || edited.end != original.end) {
            EventTimes.Timed(edited.start, edited.end)
        } else {
            null
        }
        if (summary == null && times == null) {
            logger.d(TAG, "Nothing changed in ${original.headerText}")
            return Result.success(original)
        }
        return remoteCalendarRepository.updateEvent(ref, EventScope.THIS_OCCURRENCE, summary = summary, times = times)
    }
}
