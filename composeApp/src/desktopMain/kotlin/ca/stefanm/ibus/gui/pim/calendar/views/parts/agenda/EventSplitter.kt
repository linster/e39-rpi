package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.*
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import kotlinx.datetime.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

//Need to split some model into many things that can be placed into the layout

data class AgendaCalendarEventData(
    val headerText : String,

    //The start instance of the event. Must be before the end.
    //Multi-day events have this updated for when the box should be drawn
    val start : Instant,

    //The end instance of the event. Must be after the start.
    //Multi-day events have this updated for when the box should be drawn
    val end : Instant,

    val color: Color,

    val eventUuid: Uuid? = null,

    val onClick : (event : AgendaCalendarEventData) -> Unit = {},

    //True if we only care about the days and not the time.
    val allDayEvent : Boolean = false,

    val multiDayThisPart : Int = 1,
    val multiDayTotalParts : Int = 1,
) {

    private val startTime = start.toLocalDateTime(TimeZone.currentSystemDefault())

    private val endTime = end.toLocalDateTime(TimeZone.currentSystemDefault())

    //If the event spans more than one day, set an "endTimeToday" for when today's part of the event ends
    private fun getEndTimeToday() : LocalDateTime {
        return LocalDateTime(startTime.date, LocalTime(hour = 23, minute = 59, second = 59))
    }

    fun isAllOnOneDay() : Boolean {
        //You could have an event < 24 hours, but span two days, so don't just use the Period.
        return startTime.dayOfYear == endTime.dayOfYear
    }

    private fun getBodyText() : String {
        return if (isAllOnOneDay()) {
            "${startTime.emitHourText()} - ${endTime.emitHourText()}"
        } else {
            "(${multiDayThisPart} / ${multiDayTotalParts}) ${startTime.emitHourText()} - ${getEndTimeToday().emitHourText()}"
        }
    }

    private fun LocalDateTime.emitHourText() : String {
        return if (minute == 0) {
            hour.toString(10)
        } else {
            "${hour}:$minute"
        }
    }

    private fun getStartHour() : Int {
        return startTime.hour
    }

    private fun getEndHour() : Int {
        return if (isAllOnOneDay()) {
            endTime.hour
        } else {
            getEndTimeToday().hour
        }
    }

    fun splitToMultipleEvents() : List<AgendaCalendarEventData> {
        //start from the start, and see when we cross a dateline. truncate the event and add it to an accumulator. Repeat
        //for what's left until done
        //TODO

        if (isAllOnOneDay()) {
            return listOf(this)
        }

        return listOf(this)
    }

    fun isVisibleOnCalendar(startDayVisible: LocalDate, numberOfDaysVisible: Int) : Boolean {
        //If the event is not visible based on the incoming start date and number of days visible, return false
        //TODO
        return true
    }

    fun getVerticalConstraints(constrainableBag: AgendaCalendarLayoutConstrainableBag) : ConstrainScope.() -> Unit {
        return {
            top.linkTo(constrainableBag.getSlotBarRefForHour(getStartHour())!!.bottom)
            bottom.linkTo(constrainableBag.getSlotBarRefForHour(getEndHour())!!.top)
            height = Dimension.fillToConstraints
        }
    }

    fun getHorizontalConstraints(constrainableBag: AgendaCalendarLayoutConstrainableBag, startDayVisible : LocalDate, numberOfDaysVisible : Int) : ConstrainScope.() -> Unit {
        //If the event is not visible based on the incoming start date and number of days visible, return null.
        if (!isVisibleOnCalendar(startDayVisible, numberOfDaysVisible)) {
            return {}
        }
        return {
            //First we have to figure out what day number we are on.

            val dayNumber = 0
            //TODO subdivisions

            width = Dimension.fillToConstraints
//
            start.linkTo(constrainableBag.getDayRefForDay(dayNumber)!!.end)
            end.linkTo(constrainableBag.getDayRefForDay(dayNumber + 1)!!.start)
        }
    }

    fun toView() : @Composable (
        modifier : Modifier, //The modifier should provide extra info about what to bind to.
        knobState : KnobObserverBuilderState,
    ) -> Unit = @Composable { modifier, knobState ->
        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
            CalendarEventBox(
                modifier = modifier,
                header = headerText,
                body = getBodyText(),
                isSelected = allocatedIndex == currentIndex,
                baseColor = color,
                onClick = CallWhen(currentIndexIs = allocatedIndex) {
                    onClick(this@AgendaCalendarEventData)
                }
            )
        }
    }
}
