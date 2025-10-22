package ca.stefanm.ca.stefanm.ibus.gui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.*
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
//            HalfScreenMenu.TwoColumn(
//                leftItems = listOf(TextMenuItem(title = "Go Back", onClicked = { navigationNodeTraverser.goBack() })),
//                rightItems = listOf(TextMenuItem(title = "Calendar Menu", onClicked = { showCalendarOptionsMenu() })),
//            )
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



    private fun showCalendarOptionsMenu() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Calendar Options",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Change View", onClicked = {
                        showCalendarViewMenu()
                    }),
                    TextMenuItem("New Event", onClicked = {  }),
                    TextMenuItem("New Todo", onClicked = {  }),
                    TextMenuItem("Go Back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    })
                )
            )
        }
    }

    private fun showCalendarViewMenu() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Select View",
                @Composable {
                    """
                         
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem("Month", onClicked = { calendarViewMode.value = CalendarView.MonthCalendar }),
                    TextMenuItem("Week", onClicked = { calendarViewMode.value = CalendarView.WeekCalendar }),
                    TextMenuItem("Agenda", onClicked = { calendarViewMode.value = CalendarView.TodaysAgenda }),
                    TextMenuItem("TodoList", onClicked = { calendarViewMode.value = CalendarView.TodoList }),
                    TextMenuItem("Go Back", onClicked = {})
                ).map { it.copy(onClicked = { it.onClicked; modalMenuService.closeSidePaneOverlay(true)}) }
            )
        }
    }

    @Composable
    fun MonthCalendar() {


        val currentMonth = remember { YearMonth.now() }
        val startMonth = remember { currentMonth }

        val daysOfTheWeek = remember { daysOfWeek() }

        val state = rememberCalendarState(
            startMonth = startMonth,
            firstDayOfWeek = daysOfTheWeek.first()
        )

        //Try the MonthContainer

        Column {
            Row(
                modifier = Modifier
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .fillMaxWidth()) {

                MenuItem(
                    boxModifier = Modifier.weight(1f),
                    label = "<",
                    chipOrientation = ItemChipOrientation.N,
                    onClicked = {}
                )


                val measurements = ThemeWrapper.ThemeHandle.current.bigItem


                val chipWidth = measurements.chipWidth
                val chipColor = ThemeWrapper.ThemeHandle.current.colors.chipColor
                val chipHighlights = ThemeWrapper.ThemeHandle.current.colors.chipHighlights
                val highlightWidth = measurements.highlightWidth
                Column(
                    Modifier.weight(3f, fill = true).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuItem(
                        boxModifier = Modifier
                            .padding(
                                top = (chipWidth).dp.halveIfNotPixelDoubled(),
                                bottom = highlightWidth.dp.halveIfNotPixelDoubled()
                            ),
                        label = startMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        chipOrientation = ItemChipOrientation.NONE,
                        labelColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                        onClicked = {}
                    )
                }

                Row(Modifier.weight(2F)) {
                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = "Menu",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = true,
                        onClicked = {}
                    )

                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = ">",
                        chipOrientation = ItemChipOrientation.N,
                        onClicked = {}
                    )
                }
            }
            Row(modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()) {
                for (day in daysOfTheWeek) {
                    MenuItem(
                        boxModifier = Modifier.weight(1f),
                        label = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        onClicked = {}
                    )
                }
            }
            HorizontalCalendar(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                userScrollEnabled = false,
                dayContent = @Composable { day ->
                    Box(
                        modifier = Modifier
                            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                            .fillMaxWidth()
                            .border(3.dp, Color.White)
//                        .fillMaxWidth()
                            .height(50.dp),
                        //.aspectRatio(1f), // This is important for square sizing!
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            MenuItem(
                                boxModifier = Modifier.weight(1f),
                                label = day.date.dayOfMonth.toString(),
                                onClicked = {}
                            )
                            Column {
                                //Event chips
                            }
                        }
                    }
                }
            )
        }

    }

    @Composable
    fun WeekCalendar() {

    }
}