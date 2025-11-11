package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views

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

import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService.Companion
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService.Companion.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService.Companion.KnobObserverBuilderState
import com.kizitonwose.calendar.core.*


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


    enum class Placement { TOP_BAR, CALENDAR_DAY }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MonthScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        MonthCalendar()
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


        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger)}
        val scope = rememberCoroutineScope()
        scope.launch {
            knobState.subscribeEvents()
        }

        //Try the MonthContainer

        Column {
            Row(
                modifier = Modifier
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .fillMaxWidth()) {


                KnobObserverBuilder(knobState) { allocatedIndex : Int, currentIndex : Int ->
                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = "<-",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = currentIndex == allocatedIndex,
                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                            scope.launch {
                                val newMonth = currentMonth.value.minusMonths(1)
                                currentMonth.value = newMonth
                                state.scrollToMonth(currentMonth.value)
                            }
                        }
                    )
                }



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
                        label = currentMonth.value.let { yearMonth ->
                            " ${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}"
                        },
                        chipOrientation = ItemChipOrientation.NONE,
                        labelColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                        onClicked = {}
                    )
                }

                Row(Modifier.weight(2F)) {
                    KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                        MenuItem(
                            boxModifier = Modifier.weight(1f, fill = true),
                            label = "Menu",
                            chipOrientation = ItemChipOrientation.N,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                calendarOptionsMenu.showCalendarOptionsMenu()
                            }
                        )
                    }

                    KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                        MenuItem(
                            boxModifier = Modifier.weight(1f, fill = true),
                            label = "->",
                            chipOrientation = ItemChipOrientation.N,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                scope.launch {
                                    val newMonth = currentMonth.value.plusMonths(1)
                                    currentMonth.value = newMonth
                                    state.scrollToMonth(currentMonth.value)
                                }
                            }
                        )
                    }
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
                    labelColor = if (isSelected) {
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