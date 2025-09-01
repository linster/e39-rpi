package ca.stefanm.ca.stefanm.ibus.gui.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.flow.MutableStateFlow
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

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CalendarScreen::class.java


    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {
            HalfScreenMenu.TwoColumn(
                leftItems = listOf(TextMenuItem(title = "Go Back", onClicked = { navigationNodeTraverser.goBack() })),
                rightItems = listOf(TextMenuItem(title = "Calendar Menu", onClicked = { showCalendarViewMenu() })),
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


            val calendarViewModeState = calendarViewMode.collectAsState(CalendarView.MonthCalendar)

            when (calendarViewModeState.value) {
                CalendarView.MonthCalendar -> BoxWithConstraints(modifier = Modifier.fillMaxHeight().border(2.dp, Color.Red)) { MonthCalendar() }
                CalendarView.WeekCalendar -> WeekCalendar()
                else -> {}
            }
        }
    }

    enum class CalendarView {
        MonthCalendar,
        WeekCalendar,
        TodaysAgenda,
        TodoList
    }

    private val calendarViewMode = MutableStateFlow(CalendarView.MonthCalendar)

    private fun showCalendarViewMenu() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Calendar Options",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Month View", onClicked = { calendarViewMode.value = CalendarView.MonthCalendar }),
                    TextMenuItem("Week View", onClicked = { calendarViewMode.value = CalendarView.WeekCalendar }),
                    TextMenuItem("Agenda View", onClicked = { calendarViewMode.value = CalendarView.TodaysAgenda }),
                    TextMenuItem("TodoList View", onClicked = { calendarViewMode.value = CalendarView.TodoList }),
                    TextMenuItem("New Event", onClicked = {  }),
                    TextMenuItem("New Todo", onClicked = {  }),
                    TextMenuItem("Go Back", onClicked = {})
                ).map { it.copy(onClicked = { it.onClicked; modalMenuService.closeSidePaneOverlay(true)}) }
            )
        }

    }

    @Composable
    fun MonthCalendar() {


        val currentMonth = remember { YearMonth.now() }
        val startMonth = remember { currentMonth }

        val state = rememberCalendarState(
            startMonth = startMonth
        )

        //Try the MonthContainer
        HorizontalCalendar(
            modifier = Modifier.fillMaxHeight(),
            state = state,
            userScrollEnabled = false,
            dayContent = @Composable { day ->
                Box(
                    modifier = Modifier
                        .border(2.dp, Color.Black)
                        .fillMaxWidth()
                        .height( 50.dp ),
                        //.aspectRatio(1f), // This is important for square sizing!
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.date.dayOfMonth.toString())
                }
            }
        )

    }

    @Composable
    fun WeekCalendar() {

    }
}