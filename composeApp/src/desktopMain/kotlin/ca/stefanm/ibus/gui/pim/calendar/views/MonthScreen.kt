package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.NorthButtonRow
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ConjoinedListRecord
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

import com.kizitonwose.calendar.core.*
import kotlinx.datetime.*

import kotlin.time.Clock


@ScreenDoc(
    screenName = "MonthScreen",
    description = "Calendar month view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MonthScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val calendarOptionsMenu: CalendarOptionsMenu
) : NavigationNode<Nothing> {


    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MonthScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(Modifier.fillMaxSize()) {
            MonthCalendar()
        }
    }

    @Composable
    fun MonthCalendar() {


        val currentMonth = remember { mutableStateOf(YearMonth.now()) }
        val startMonth = remember(currentMonth) { currentMonth.value.minusMonths(50) }
        val endMonth = remember(currentMonth) { currentMonth.value.plusMonths(50) }

        val daysOfTheWeek = remember { daysOfWeek() }

        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth.value,
            firstDayOfWeek = daysOfTheWeek.first(),

        )

        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            knobState.subscribeEvents()
        }

        //Try the MonthContainer

        Column {

            NorthButtonRow(
                knobState = knobState,
                timePeriodLabel = currentMonth.value.let { yearMonth ->
                    " ${yearMonth.month.toJavaMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}"
                },
                onMenuClicked = {
                    calendarOptionsMenu.showCalendarOptionsMenu()
                },
                onPreviousClicked = {
                    scope.launch {
                        val newMonth = currentMonth.value.minusMonths(1)
                        currentMonth.value = newMonth
                        state.scrollToMonth(currentMonth.value)
                    }
                },
                onNextClicked = {
                    scope.launch {
                        val newMonth = currentMonth.value.plusMonths(1)
                        currentMonth.value = newMonth
                        state.scrollToMonth(currentMonth.value)
                    }
                }
            )

            Row(modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()) {
                for (day in daysOfTheWeek) {
                    MenuItem(
                        boxModifier = Modifier.weight(1f),
                        label = day.toJavaDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        onClicked = {}
                    )
                }
            }
            HorizontalCalendar(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                userScrollEnabled = true,
                calendarScrollPaged = false,
                dayContent = @Composable { day ->

                    KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                        CalendarDay(
                            day = day,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {}
                        )
                    }
                }
            )
        }

    }

    @Composable
    fun CalendarDay(
        day : CalendarDay,
        isSelected : Boolean,
        onClicked : () -> Unit
    ) {

        val isToday : Boolean = day.date.toJavaLocalDate().dayOfYear ==
                Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    .dayOfYear

        Box(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth()
                .border(3.dp, if (!isSelected) Color.White else ThemeWrapper.ThemeHandle.current.colors.selectedColor)
//                        .fillMaxWidth()
                .height(50.dp),
            //.aspectRatio(1f), // This is important for square sizing!
            contentAlignment = Alignment.Center
        ) {
            Row {
                MenuItem(
                    boxModifier = Modifier.weight(1f),
                    label = day.date.dayOfMonth.toString(),
                    labelColor = if (isSelected || isToday) {
                        ThemeWrapper.ThemeHandle.current.colors.selectedColor
                    } else {
                        ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE
                    },
                    onClicked = { onClicked() }
                )
                Column {
                    //Event chips
                }
            }
        }
    }



}