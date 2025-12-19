package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.*
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import kotlinx.datetime.*
import kotlin.math.min
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
    val multiDayTotalParts : Int = 1, //TODO actually calculate this by subtracting the LocaleDate day numbers.
) {

    private val TAG = "AgendaCalendarEventData"
    private val logger by lazy {
        DaggerApplicationComponent.create().logger()
    }

    private val startTime = start.toLocalDateTime(TimeZone.currentSystemDefault())

    private val endTime = end.toLocalDateTime(TimeZone.currentSystemDefault())

    /** Return the local date of the start of the event */
    fun getStartLocalDate() : LocalDate {
        return startTime.date
    }

    //If the event spans more than one day, set an "endTimeToday" for when today's part of the event ends
    private fun getEndTimeToday() : LocalDateTime {
        return LocalDateTime(startTime.date, LocalTime(hour = 23, minute = 59, second = 59))
    }

    fun getLengthInHours() : Int {
        return start.periodUntil(end, TimeZone.currentSystemDefault()).hours
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

        if (end < start) {
            logger.w(TAG, "splitToMultipleEvents, end before start")
            return listOf(this)
        }

        //Take the first event, split the rest.
        //We're going to assume the event starts at some point today, and runs over midnight to tomorrow. It might
        //also run for another day too.

        //Take the start, set the time to midnight, then make it to an instant.
        val endOfDayToday = LocalDateTime(startTime.date, LocalTime(hour = 23, minute = 59, second = 59)).toInstant(
            TimeZone.currentSystemDefault())

        if (end > endOfDayToday) {
            // Make the "today" part of the event
            val newEventToday = AgendaCalendarEventData(
                headerText = headerText,
                start = start,
                end = endOfDayToday,
                color = color,
                onClick = onClick,
                eventUuid = eventUuid,
                allDayEvent = allDayEvent,
                multiDayThisPart = multiDayThisPart,
                multiDayTotalParts = multiDayTotalParts
            )
            //Fabricate a new event that begins right after `endofDayToday` and runs until end.

            //Increment endOfDayToday to be the start of the following day.
            val newStart = LocalDateTime(startTime.year, startTime.month, startTime.day + 1, hour = 0, minute = 0, second = 0)
                .toInstant(TimeZone.currentSystemDefault())
            val newEventSplittable = AgendaCalendarEventData(
                headerText = headerText,
                start = newStart,
                end = end,
                color = color,
                onClick = onClick,
                eventUuid = eventUuid,
                allDayEvent = allDayEvent,
                multiDayThisPart = multiDayThisPart + 1,
                multiDayTotalParts = multiDayTotalParts
            )
            return listOf(
                newEventToday,
                *newEventSplittable.splitToMultipleEvents().toTypedArray()
            ).let {
                if (it.all { isAllOnOneDay() }) {
                    val totalEvents = it.size
                    it.map { event ->
                        event.copy(multiDayTotalParts = totalEvents)
                    }
                } else {
                    it
                }
            }
        }

        return listOf(this)
    }

    fun isVisibleOnCalendar(startDayVisible: LocalDate, numberOfDaysVisible: Int) : Boolean {
        //If the event is not visible based on the incoming start date and number of days visible, return false
        if (isAllOnOneDay()) {
            //Assume start and end are on the same day
            return startTime.dayOfYear in (startDayVisible.dayOfYear .. startDayVisible.dayOfYear + numberOfDaysVisible)
        } else {
            val startDays = splitToMultipleEvents().map { it.start.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear }
            return setOf(*startDays.toTypedArray())
                .union(
                    (startDayVisible.dayOfYear .. startDayVisible.dayOfYear + numberOfDaysVisible).toSet()
                )
                .isNotEmpty()
        }
    }

    /** Return a set of all the timeslots this event would modify */
    fun getConstituentSlots() : Set<Int> {
        if (!isAllOnOneDay()) {
            return emptySet()
        }

        return (startTime.hour .. endTime.hour).toSet()
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

//
class SubdivisionCalculator() {

    private val rawEvents = mutableSetOf<AgendaCalendarEventData>()
    fun contributeEventToCalculation(event: AgendaCalendarEventData) {
        rawEvents.add(event)
    }


    data class SubdivisionAllocation(
        val event : AgendaCalendarEventData,
        val slotsAllocated : IntRange
    )

    data class SubtractionForEvent(
        val event : AgendaCalendarEventData,
        val slotSubtractions : Int = 0
    )

    //Maybe keep track of number of subdivision subtractions while calculating and then allocate the subdivisions
    //at the very end.

    fun calculateAllSubdivisions(constrainableBag: AgendaCalendarLayoutConstrainableBag) {

        val allEventsSplitToSingleDayEvents = rawEvents.map { it.splitToMultipleEvents() }.flatten()

        val eventsByStartDate = allEventsSplitToSingleDayEvents.groupBy(
            keySelector = { it.getStartLocalDate() },
            valueTransform = { SubtractionForEvent(it, 0) }
        )


        eventsByStartDate.keys.forEach { date ->

            val slotToEvent : MutableMap<Int, MutableSet<SubtractionForEvent>> = mutableMapOf()
            (0 until 24).forEach {
                slotToEvent[it] = mutableSetOf()
            }

            eventsByStartDate[date]!!.forEach { event ->
                event.event.getConstituentSlots().forEach { slot ->
                    slotToEvent[slot]!!.add(event)
                }
            }
            //TODO now we sort by descending length

            //Then, loop over the events in order
            //Then, for each slot the event covers, check all the other events in that slot. Give them all an extra subtraction

            //Once we've figured out how narrow each thing should be (the number of subtractions), we then need to figure out
            //which actual subdivision we allocate to.

            //to do this, we again sort by descending length, then, see how many subtractions we accumulated. Give max - subtractions
            //number of subdivisions, and pick the left most subdivision range that fits. Should maybe be ok?? (because when we had the SubtractionForEvent
            //get built up, it calculated it for all overlaps...
            // if subtractions > subdivisions, just do a full-width event and hope for the best.

        }

        //Now we need to sort the events by start slot, then by length?
        //Maybe we need to sort by length of event first, place those, then fit everything else around it?
        allEventsSplitToSingleDayEvents.sortedByDescending { it.getLengthInHours() }

        val maxSubdivisions = constrainableBag.getMaxSubdivisionForDay()
    }
}