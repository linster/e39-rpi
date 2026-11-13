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

    fun saveEditedEvent(
        eventData: AgendaCalendarEventData
    ) {
        // The bridge gets CreateEvent/UpdateEvent in e39-rpi-pim's phase 4.
        logger.w(TAG, "Saving events isn't supported yet: ${eventData.headerText}")
    }
}
