package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotDivider
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.SlotLabel
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.VerticalDivider
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char

@Composable
fun AgendaCalendarLayout(
    modifier: Modifier = Modifier,
    events : List<CalendarEventBox> = listOf(),

    //TODO startDay : SomeLocalDate,

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

        val (box1Ref, box2Ref) = createRefs()
        
        CalendarEventBox(
            modifier = Modifier.constrainAs(box1Ref) {

                top.linkTo(slotBarRefs[1]!!.bottom)
                bottom.linkTo(slotBarRefs[8]!!.top)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints

                start.linkTo(dayRefs[0]!!.end)
                end.linkTo(dayRefs[1]!!.start)
            },
            header = "Plast",
            body = "1-8am",
            isSelected = false,
            baseColor = Color.Magenta,
            onClick = {}
        )

        CalendarEventBox(
            modifier = Modifier.constrainAs(box2Ref) {

                top.linkTo(slotBarRefs[3]!!.bottom)
                bottom.linkTo(slotBarRefs[5]!!.top)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints

                start.linkTo(dayRefs[1]!!.end)
                end.linkTo(dayRefs[2]!!.start)
            },
            header = "Curling",
            body = "3-5am",
            isSelected = false,
            baseColor = Color.Magenta,
            onClick = {}
        )




        //Just for giggles lets see if we can get one box in there

    }
}