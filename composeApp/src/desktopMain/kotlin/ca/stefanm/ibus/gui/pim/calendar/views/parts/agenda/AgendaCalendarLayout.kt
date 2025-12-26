package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.*
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotDivider
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotLabel
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.VerticalDivider
import com.kizitonwose.calendar.core.plusDays
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import java.time.Instant
import java.time.format.TextStyle
import java.util.*
import kotlin.time.Clock

//Need a custom modifier that can be placed in a chain that allows slot bars to be set from not here.

interface AgendaCalendarBoxModifierDescription {

}

@Composable
fun AgendaCalendarLayout(
    modifier: Modifier = Modifier,
    knobState : KnobObserverBuilderState,
    events : List<AgendaCalendarEventData> = listOf(),

    startDay : LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
        .toLocalDate()
        .toKotlinLocalDate(),

    //Number of days to display past the startDay
    numberOfDays : Int = 3,

    startHour : Int,

    onDayScroll : (minHour : Int, maxHour : Int) -> Unit = { _, _ -> },
    onCalendarItemSelectedChange : (event : AgendaCalendarEventData) -> Unit = {}
) {

    val theme = ThemeWrapper.ThemeHandle.current
    val scope = rememberCoroutineScope()
    val startHourValue = remember { mutableStateOf(startHour) }

    val scrollState = rememberScrollState(0)

    val screenDensity = LocalDensity.current

    LaunchedEffect(startHour) {
        if (scrollState.viewportSize != 0 && !scrollState.isScrollInProgress) {
            //We can scroll, so jump to somewhere lol
            println("ZOMG WAT" + scrollState.value)
            println("ZOMG WAT start hour $startHour")
            println("ZOMG WAT viewportSize ${scrollState.viewportSize}")
            println("ZOMG WAT maxValue $${scrollState.maxValue}")

            val scrollFraction = ((startHour ) / 24f) * scrollState.maxValue
            println("ZOMG WAT scrollFraction ${scrollFraction}")
            val heightPerSlot = theme.smallItem.fontSize.value.toInt() + theme.bigItem.highlightWidth.let { it / 2F }.toInt()
            println("ZOMG WAT heightPerSlot ${heightPerSlot}")
            println("ZOMG WAT screendenity ${screenDensity.density}")
//            scope.launch {
                scrollState.scrollTo((startHour * heightPerSlot * 1.75).toInt())
//            }
            println("ZOMG WAT" + scrollState.value)
        }
    }




    ConstraintLayout(modifier = modifier.verticalScroll(scrollState)) outer@{

        val minHour = 0
        val maxHour = 23

        val slotBarRefs = mutableMapOf<Int, ConstrainedLayoutReference>()
        val slotLabelRefs = mutableMapOf<Int, ConstrainedLayoutReference>()

        ((minHour)..maxHour).forEach { hour ->
            slotBarRefs[hour] = createRef()
            slotLabelRefs[hour] = createRef()
            SlotDivider(Modifier.constrainAs(slotBarRefs[hour]!!) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                if (hour == minHour) {
                    //Link to parent because it's the first bar
                    top.linkTo(parent.top)
                } else {
                    top.linkTo(slotLabelRefs[hour - 1]!!.bottom, margin = 8.dp)
                }
            })

            val labelString = LocalTime(hour = hour, minute = 0, second = 0, nanosecond = 0).format(
                LocalTime.Format {
                    amPmHour()
                    char(':')
                    minute()
                    char(' ')
                    amPmMarker("AM", "PM")
                }
            )
            SlotLabel(label = labelString, modifier = Modifier.constrainAs(slotLabelRefs[hour]!!) {
                top.linkTo(slotBarRefs[hour]!!.bottom, margin = 2.dp)
                start.linkTo(parent.start, margin = 8.dp)
            })

        }

        //Create a barrier to the right of the time slot labels
        val slotLabelBarrier = createEndBarrier(*slotLabelRefs.values.toTypedArray(), margin = 8.dp)

        val slotLabelBarrierDivider = createRef()
        VerticalDivider(
            Modifier.constrainAs(slotLabelBarrierDivider) {
                start.linkTo(slotLabelBarrier)
            }
        )

        // 4 subdivisions per day means each day has subdivision indices 0, 1, 2, 3.
        // which means that given indices
        ///    0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        //     D  .  .  .  D  .  .  .  D  .
        val subdivisionsPerDay = 4
        val drawSubdivisions = true

        val dayRefs = mutableMapOf<Int, ConstrainedLayoutReference>()
        (0..(numberOfDays * subdivisionsPerDay)).forEach { dayNumber ->
            dayRefs[dayNumber] = createRef()
        }

        val daysChain = createHorizontalChain(*dayRefs.values.toTypedArray(), chainStyle = ChainStyle.SpreadInside)
        constrain(daysChain) {
            absoluteLeft.linkTo(slotLabelBarrier)
            absoluteRight.linkTo(parent.absoluteRight)

        }

        (0..(numberOfDays * (subdivisionsPerDay + 0))).forEach { dayNumber ->
            if (dayNumber % subdivisionsPerDay == 0) {
                VerticalDivider(
                    Modifier.constrainAs(dayRefs[dayNumber]!!) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
                )
            } else {
                //Invisible structural nonsense. Without this ConstraintLayout blows up and idk why.
                VerticalDivider(
                    Modifier.constrainAs(dayRefs[dayNumber]!!) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                    colorOverride = Color.Transparent
                )
            }
        }

        val labelDays = true
        if (labelDays) {
            (0 until numberOfDays).forEach { dayNumber ->
                val day = startDay.plusDays(dayNumber)
                val label = "${
                    day
                        .dayOfWeek
                        .toJavaDayOfWeek()
                        .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.CANADA)
                } ${day.day} ${day.month.toJavaMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.CANADA)}"

                DayHeaderChip(
                    label = label,
                    modifier = Modifier.constrainAs(createRef()) {
                        bottom.linkTo(parent.bottom, 8.dp)
                        start.linkTo(dayRefs[dayNumber * subdivisionsPerDay]!!.start)
                        end.linkTo(dayRefs[((dayNumber + 1) * subdivisionsPerDay)]!!.start)
                        width = Dimension.fillToConstraints
                    }
                )
            }
        }

        val logger = remember { DaggerApplicationComponent.create().logger() }
        val subdivisionCalculator = SubdivisionCalculator(
            logger,
            maxSubdivisionsPerDay = subdivisionsPerDay
        )


        events.forEach { event ->
            subdivisionCalculator.contributeEventToCalculation(event)
        }

        val result = subdivisionCalculator.calculateAllSubdivisions()

        events.forEach { event ->
            if (event.isVisibleOnCalendar(startDay, numberOfDays)) {
                val ref = createRef()
                KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->

                    if (allocatedIndex == currentIndex) {
                        onCalendarItemSelectedChange(event)
                    }

                    CalendarEventBox(
                        modifier = Modifier.constrainAs(ref) {

                            top.linkTo(slotBarRefs[event.getStartHour()]!!.bottom)
                            bottom.linkTo(slotBarRefs[event.getEndHour()]!!.top)
                            height = Dimension.fillToConstraints

                            val dayNumber =
                                (event.startTime.dayOfYear - startDay.dayOfYear).coerceIn(0..numberOfDays)

                            // start.linkTo(dayRefs[(dayNumber * subdivisionsPerDay)]!!.end)
                            // end.linkTo(dayRefs[((dayNumber + 1) * subdivisionsPerDay)]!!.start)
                            start.linkTo(dayRefs[(dayNumber * subdivisionsPerDay) + result[event]!!.first]!!.start)
                            end.linkTo(dayRefs[(dayNumber * subdivisionsPerDay) + result[event]!!.last]!!.end)
                            width = Dimension.fillToConstraints
                        },
                        header = event.headerText,
                        body = event.getBodyText(),
                        isSelected = allocatedIndex == currentIndex,
                        baseColor = event.color,
                        onClick = CallWhen(currentIndexIs = allocatedIndex) {
                            event.onClick(event)
                        }
                    )
                }
            }
        }
    }

}