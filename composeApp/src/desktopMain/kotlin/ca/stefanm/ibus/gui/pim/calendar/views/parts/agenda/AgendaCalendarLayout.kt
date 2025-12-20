package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotDivider
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotLabel
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.VerticalDivider
import com.kizitonwose.calendar.core.plusDays
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

    //TODO startDay : SomeLocalDate,

    startDay : LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
        .toLocalDate()
        .toKotlinLocalDate(),

    //Number of days to display past the startDay
    numberOfDays : Int = 3,

//    onDayScroll : (minHourVisible : Int, maxHourVisible: Int, minHour : Int, maxHour : Int) -> Unit,
) {

    ConstraintLayout(modifier = modifier) outer@ {

        val minHour = 0
        val maxHour = 23

        val slotBarRefs = mutableMapOf<Int, ConstrainedLayoutReference>()
        val slotLabelRefs = mutableMapOf<Int, ConstrainedLayoutReference>()

        (minHour .. maxHour).forEach { hour ->
            slotBarRefs[hour] = createRef()
            slotLabelRefs[hour] = createRef()
            SlotDivider(Modifier.constrainAs(slotBarRefs[hour]!!) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                if (hour == 0) {
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
        (0 .. (numberOfDays * subdivisionsPerDay)).forEach { dayNumber ->
            dayRefs[dayNumber] = createRef()
        }

        val daysChain = createHorizontalChain(*dayRefs.values.toTypedArray(), chainStyle = ChainStyle.SpreadInside)
        constrain(daysChain) {
            absoluteLeft.linkTo(slotLabelBarrier)
            absoluteRight.linkTo(parent.absoluteRight)

        }

        (0 .. (numberOfDays * (subdivisionsPerDay + 0))).forEach { dayNumber ->
            if (dayNumber % subdivisionsPerDay == 0) {
                VerticalDivider(
                    Modifier.constrainAs(dayRefs[dayNumber]!!) {}
                )
            } else {
                //Invisible structural nonsense. Without this ConstraintLayout blows up and idk why.
                VerticalDivider(
                    Modifier.constrainAs(dayRefs[dayNumber]!!) {},
                    colorOverride = Color.Transparent
                )
            }
        }

        val labelDays = true
        if (labelDays) {
            (0 until  numberOfDays).forEach { dayNumber ->

//                if (numberOfDays > 5) {
//                    //Only label the first day and the 2nd last
//                    if (dayNumber != 0) {
//                        return@forEach
//                    }
//                }
                val day = startDay.plusDays(dayNumber)
                val label = "${day
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
        val subdivisionCalculator = SubdivisionCalculator(logger)

        val constraintBag = object : AgendaCalendarLayoutConstrainableBag {
            override fun getSlotBarRefForHour(hour: Int): ConstrainedLayoutReference? = slotBarRefs[hour]
            override fun getDayRefForDay(day: Int): ConstrainedLayoutReference? = dayRefs[(day * subdivisionsPerDay)]

            override fun getSubdivisionRefForDay(day: Int, subdivision: Int): ConstrainedLayoutReference? {
                return dayRefs[ (day * subdivisionsPerDay) + subdivision]!!
            }

            override fun getMaxSubdivisionForDay(): Int {
                return subdivisionsPerDay
            }

            override fun getSubdivisionCalculator(): SubdivisionCalculator {
                return subdivisionCalculator
            }

        }


        events.forEach { event ->
            subdivisionCalculator.contributeEventToCalculation(event)
        }

        val result = subdivisionCalculator.calculateAllSubdivisions(constraintBag)

        events.forEach { event ->
            if (event.isVisibleOnCalendar(startDay, numberOfDays)) {
                val ref = createRef()
                event.toView().invoke(
                    Modifier.constrainAs(ref) {

                        top.linkTo(slotBarRefs[event.getStartHour()]!!.bottom)
                        bottom.linkTo(slotBarRefs[event.getEndHour()]!!.top)
                        height = Dimension.fillToConstraints

                        val dayNumber = (event.startTime.dayOfYear - startDay.dayOfYear).coerceIn(0..numberOfDays)
            start.linkTo(constraintBag.getDayRefForDay(dayNumber)!!.end)
            end.linkTo(constraintBag.getDayRefForDay(dayNumber + 1)!!.start)
//                        start.linkTo(constraintBag.getSubdivisionRefForDay(dayNumber, result[event]!!.first)!!.start)
//                        end.linkTo(constraintBag.getSubdivisionRefForDay(dayNumber, result[event]!!.last)!!.end)
                        width = Dimension.fillToConstraints
                    }, knobState
                )
            }
        }

//        val (box1Ref, box2Ref) = createRefs()

//        CalendarEventBox(
//            modifier = Modifier.constrainAs(box1Ref) {
//
//                top.linkTo(slotBarRefs[1]!!.bottom)
//                bottom.linkTo(slotBarRefs[8]!!.top)
//                height = Dimension.fillToConstraints
//                width = Dimension.fillToConstraints
//
//                start.linkTo(dayRefs[0]!!.end)
//                end.linkTo(dayRefs[1]!!.start)
//            },
//            header = "Plast",
//            body = "1-8am",
//            isSelected = false,
//            baseColor = Color.Magenta,
//            onClick = {}
//        )

//        CalendarEventBox(
//            modifier = Modifier.constrainAs(box2Ref) {
//
//                top.linkTo(slotBarRefs[3]!!.bottom)
//                bottom.linkTo(slotBarRefs[5]!!.top)
//                height = Dimension.fillToConstraints
//
//
//                width = Dimension.fillToConstraints
//                start.linkTo(dayRefs[1]!!.end)
//                end.linkTo(dayRefs[2]!!.start)
//            },
//            header = "Curling",
//            body = "3-5am",
//            isSelected = false,
//            baseColor = Color.Magenta,
//            onClick = {}
//        )




        //Just for giggles lets see if we can get one box in there

    }
}


interface AgendaCalendarLayoutConstrainableBag {

    /** Vertical references */
    fun getSlotBarRefForHour(hour : Int) : ConstrainedLayoutReference?


    /** Horizontal references */
    // 0 is the start of the first day, 1 is the vertical line between day 0 and day 1.
    // That way, between 0 and 1 is day 0.
    fun getDayRefForDay(day : Int) : ConstrainedLayoutReference?

    fun getSubdivisionCalculator() : SubdivisionCalculator

    //TODO subdivisions for day
    fun getSubdivisionRefForDay(day : Int, subdivision : Int) : ConstrainedLayoutReference?
    fun getMaxSubdivisionForDay() : Int
}