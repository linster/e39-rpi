package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
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

        val dayRefs = mutableMapOf<Int, ConstrainedLayoutReference>()
        (0 .. numberOfDays).forEach { dayNumber ->
            dayRefs[dayNumber] = createRef()

            VerticalDivider(
                Modifier.constrainAs(dayRefs[dayNumber]!!) {}
            )
        }

        val daysChain = createHorizontalChain(*dayRefs.values.toTypedArray(), chainStyle = ChainStyle.SpreadInside)
        constrain(daysChain) {
            start.linkTo(slotLabelBarrier)
            end.linkTo(parent.end)
        }

        // make a subdivision chain here.
        // every day is split up into subdivisions for overlapping events
        val subdivisionRefs = mutableMapOf<Int, MutableMap<Int, ConstrainedLayoutReference>>()

        val subdivisionsPerDay = 4
        (0 until numberOfDays).forEach { dayNumber ->
            subdivisionRefs[dayNumber] = mutableMapOf()
            (0 until subdivisionsPerDay).forEach { subDivisionNumber ->
                subdivisionRefs[dayNumber]!![subDivisionNumber] = createRef()
            }
            val subdivisionChain = createHorizontalChain(*subdivisionRefs[dayNumber]!!.values.toTypedArray(), chainStyle = ChainStyle.SpreadInside)
            constrain(subdivisionChain) {
                start.linkTo(dayRefs[dayNumber]!!.start)
                end.linkTo(dayRefs[dayNumber + 1]!!.end)
            }
        }
        val drawSubdivisions = false
        if (drawSubdivisions) {
            (0 until numberOfDays).forEach { dayNumber ->
                (0 until subdivisionsPerDay).forEach { subDivisionNumber ->
                    VerticalDivider(
                        Modifier.constrainAs(createRef()) {
                            val guide = subdivisionRefs[dayNumber]!![subDivisionNumber]!!
                            start.linkTo(guide.start)
                            end.linkTo(guide.end)
                        },
                        Color.Cyan
                    )
                }
            }

        }

        val labelDays = true
        if (labelDays) {
            (0 until  numberOfDays).forEach { dayNumber ->

                if (numberOfDays > 5) {
                    //Only label the first day and the 2nd last
                    if (dayNumber != 0) {
                        return@forEach
                    }
                }
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
                        start.linkTo(dayRefs[dayNumber]!!.start)
                        end.linkTo(dayRefs[dayNumber + 1]!!.start)
                    }
                )
            }
        }

        val subdivisionCalculator = SubdivisionCalculator()

        val constraintBag = object : AgendaCalendarLayoutConstrainableBag {
            override fun getSlotBarRefForHour(hour: Int): ConstrainedLayoutReference? = slotBarRefs[hour]
            override fun getDayRefForDay(day: Int): ConstrainedLayoutReference? = dayRefs[day]

            override fun getSubdivisionRefForDay(day: Int, subdivision: Int): ConstrainedLayoutReference? {
                return subdivisionRefs[day]!![subdivision]
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

        subdivisionCalculator.calculateAllSubdivisions(constraintBag)

        events.forEach { event ->
            if (event.isVisibleOnCalendar(startDay, numberOfDays)) {
                val ref = createRef()
                event.toView().invoke(
                    Modifier.constrainAs(ref) {
                        event.getVerticalConstraints(constraintBag)()

                        event.getHorizontalConstraints(constraintBag, startDay, numberOfDays)()

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