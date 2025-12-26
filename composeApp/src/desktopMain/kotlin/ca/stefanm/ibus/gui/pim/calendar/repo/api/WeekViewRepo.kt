package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.repo.api

import androidx.compose.ui.graphics.Color
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import com.kizitonwose.calendar.core.plusDays
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.time.Clock

class WeekViewRepo @Inject constructor() {

    fun getAgendaViewEvents(
        startDay : LocalDate,
        endDate: LocalDate
    ) : Flow<List<AgendaCalendarEventData>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return flowOf(
            listOf(
                AgendaCalendarEventData(
                    headerText = "Plast",
                    start = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        3,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    end = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        14,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    color = Color.Magenta
                ),
                AgendaCalendarEventData(
                    headerText = "Curling",
                    start = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        4,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    end = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        5,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    color = Color.Magenta
                ),
                AgendaCalendarEventData(
                    headerText = "Trivia",
                    start = LocalDateTime(
                        today.year,
                        today.month,
                        today.day + 1,
                        7,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    end = LocalDateTime(
                        today.year,
                        today.month,
                        today.day + 1,
                        9,
                        0,
                        0
                    ).toInstant(TimeZone.currentSystemDefault()),
                    color = Color.Magenta
                )
            )

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

    }
}