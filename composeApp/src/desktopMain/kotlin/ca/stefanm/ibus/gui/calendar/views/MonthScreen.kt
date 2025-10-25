package ca.stefanm.ca.stefanm.ibus.gui.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject




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


        val currentMonth = remember { YearMonth.now() }
        val startMonth = remember { currentMonth }

        val daysOfTheWeek = remember { daysOfWeek() }

        val state = rememberCalendarState(
            startMonth = startMonth,
            firstDayOfWeek = daysOfTheWeek.first()
        )

        val scope = rememberCoroutineScope()
        scope.launch {
            knobListenerService.knobTurnEvents().collect {

            }
        }

        val holder = KnobListenerService.rememberArbitraryComposableHolder()



        val itemsList = remember { SnapshotStateList<ConjoinedListRecord<MenuItem, Placement>>() }
        //We're going to have to make use of the fact that listenForKnob returns a
        // Flow<SnapshotStateList>

        val selectedItems = remember(itemsList) {
            knobListenerService.listenForKnob(
                itemsList,
                onSelectAdapter = { item, isNowSelected -> item.item.copyAndSetIsSelected(isNowSelected) },
                isSelectableAdapter = { item -> item.item.isSelectable },
                onItemClickAdapter = { item -> item.item.onClicked() }
            )
        }

        //Try the MonthContainer

        Column {
            Row(
                modifier = Modifier
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .fillMaxWidth()) {

                LaunchedEffect(Unit) {
                    TextMenuItem(
                        "<-",
                        onClicked = {}
                    ).let { button ->
                        if (itemsList
                                .none {
                                    it.sourcePlacementEnum == Placement.TOP_BAR
                                            && (it.item as? TextMenuItem)?.title == button.title
                                }
                        ) {

                            itemsList.add(ConjoinedListRecord(button, Placement.TOP_BAR, 1))
                        }
                    }
                }
                selectedItems.value
                    .find {
                        it.sourcePlacementEnum == Placement.TOP_BAR &&
                                it.originalItemPosition == 1 }?.let {
                                    it.item.toView(
                                        boxModifier = Modifier.weight(1f),
                                        chipOrientation = ItemChipOrientation.N,
                                    )
                    }
                KnobListenerService.ArbtriraryScrollable(holder) { isSelected ->
                    MenuItem(
                        boxModifier = Modifier.weight(1f),
                        label = "<-",
                        chipOrientation = ItemChipOrientation.N,
                        isSelected = isSelected,
                        onClicked = {} //TODO STEFAN HMM....
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
                        onClicked = {
                            calendarOptionsMenu.showCalendarOptionsMenu()
                        }
                    )

                    MenuItem(
                        boxModifier = Modifier.weight(1f, fill = true),
                        label = "->",
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

                    CalendarDayView(
                        day = day,
                        isSelected = false,
                        onClicked = {}
                    )



                }
            )
        }

    }

    data class CalendarDayView(
        val day : CalendarDay,
        override val isSelectable: Boolean = true,
        override val isSelected: Boolean,
        override val onClicked: () -> Unit
    ) : MenuItem {



        override fun toView(
            boxModifier: Modifier,
            chipOrientation: ItemChipOrientation
        ): @Composable () -> Unit {
            return { CalendarDay(day, isSelected, onClicked) }
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
                        onClicked = { onClicked() }
                    )
                    Column {
                        //Event chips
                    }
                }
            }
        }


    }



}