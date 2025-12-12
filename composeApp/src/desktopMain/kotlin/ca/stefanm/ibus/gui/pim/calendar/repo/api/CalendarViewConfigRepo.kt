package ca.stefanm.ibus.gui.pim.calendar.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class CalendarView {
    MonthCalendar,
    OneWeekCalendar,
    TwoWeekCalendar,
    TodaysAgenda,
    TodoList
}

@ApplicationScope
class CalendarViewConfigRepo @Inject constructor() {

    val screenView = MutableStateFlow<CalendarView>(CalendarView.OneWeekCalendar)
}

