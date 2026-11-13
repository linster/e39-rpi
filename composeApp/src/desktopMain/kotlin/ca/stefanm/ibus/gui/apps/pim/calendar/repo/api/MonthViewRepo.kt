package ca.stefanm.ibus.gui.pim.calendar.repo.api

import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/** Events for the month view, by day, from the PIM bridge. */
class MonthViewRepo @Inject constructor(
    private val remoteCalendarRepository: RemoteCalendarRepository,
) {
    /**
     * The events on each day from [firstDay] to [lastDay] (inclusive, local
     * time); an event spanning several days is on each of them. Days without
     * events are missing from the map. Emits again when they change.
     */
    fun eventsByDay(
        firstDay: LocalDate,
        lastDay: LocalDate,
    ): Flow<Map<LocalDate, List<AgendaCalendarEventData>>> {
        val zone = TimeZone.currentSystemDefault()
        return remoteCalendarRepository.events(
            start = firstDay.atStartOfDayIn(zone),
            end = lastDay.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone),
            zone = zone,
        ).map { events ->
            val byDay = mutableMapOf<LocalDate, MutableList<AgendaCalendarEventData>>()
            for (event in events) {
                var day = maxOf(event.start.toLocalDateTime(zone).date, firstDay)
                // end is exclusive: an event ending at midnight isn't on the next day.
                val last = minOf(event.end.minus(1.milliseconds).toLocalDateTime(zone).date, lastDay)
                while (day <= last) {
                    byDay.getOrPut(day) { mutableListOf() }.add(event)
                    day = day.plus(1, DateTimeUnit.DAY)
                }
            }
            byDay
        }
    }
}
