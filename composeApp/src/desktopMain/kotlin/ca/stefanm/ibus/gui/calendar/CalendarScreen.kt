package ca.stefanm.ca.stefanm.ibus.gui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@ScreenDoc(
    screenName = "ChatSetupMenuRoot",
    description = "The main menu for the matrix chat client settings",
    navigatesTo = [
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class CalendarScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

//    https://github.com/jimmyale3102/compose-calendar
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CalendarScreen::class.java


    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {
            HalfScreenMenu.TopHalfTwoColumn(
                leftItems = listOf(TextMenuItem(title = "Go Back", onClicked = { navigationNodeTraverser.goBack() })),
                rightItems = listOf(TextMenuItem(title = "Calendar Menu", onClicked = {  })),
            )
            val currentDate = remember { LocalDate.now() }
            val currentMonth = remember { YearMonth.now() }
            val startDate = remember { currentMonth.minusMonths(100).atStartOfMonth() } // Adjust as needed
            val endDate = remember { currentMonth.plusMonths(100).atEndOfMonth() } // Adjust as needed
            val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

            val state = rememberWeekCalendarState(
                startDate = startDate,
                endDate = endDate,
                firstVisibleWeekDate = currentDate,
                firstDayOfWeek = firstDayOfWeek
            )
            val state2 = rememberCalendarState()

            //https://github.com/kizitonwose/Calendar/blob/main/docs/Compose.md
            MonthCalendar()
        }
    }

    enum class CalendarView {
        MonthCalendar,
        WeekCalendar,
        TodaysAgenda
    }

    @Composable
    fun MonthCalendar() {

    }
}