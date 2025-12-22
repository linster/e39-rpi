package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import ca.stefanm.ibus.gui.pim.calendar.views.parts.NorthButtonRowWithScroll
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.pim.calendar.views.parts.NorthButtonRowWithScroll
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.plusDays
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.format.TextStyle
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun AgendaScreen(
    knobListenerService : KnobListenerService,
    logger : Logger,
    numberOfDays : Int,
    eventData: List<AgendaCalendarEventData>,
    onMenuClicked : () -> Unit = {}
) {
    val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        knobState.subscribeEvents()
    }

    val startDay = remember {
        mutableStateOf(
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toJavaLocalDateTime()
                .toLocalDate()
                .toKotlinLocalDate()
        )
    }

    fun getLabelForDateRange(start : LocalDate, numberOfDays: Int) : String {
        fun labelForDay(day : LocalDate) =
            "${day.day} ${day.month.toJavaMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.CANADA)}"

        return "${labelForDay(start)} - ${labelForDay(start.plusDays(numberOfDays))}"
    }

    val timePeriodLabel = remember { mutableStateOf(getLabelForDateRange(startDay.value, numberOfDays)) }

    val currentlySelectedEventLabel = remember { mutableStateOf("Event") }

    val agendaCalendarLayoutHeader = remember(timePeriodLabel.value) { mutableStateOf(timePeriodLabel.value) }

    key(Unit) {
        Column {
            NorthButtonRowWithScroll(
                knobState = knobState,
                timePeriodLabel = agendaCalendarLayoutHeader.value,
                onPreviousClicked = {
                    startDay.value = startDay.value.minusDays(1 * numberOfDays)
                    timePeriodLabel.value = getLabelForDateRange(startDay.value, numberOfDays)
                },
                onNextClicked = {
                    startDay.value = startDay.value.plusDays(1 * numberOfDays)
                    timePeriodLabel.value = getLabelForDateRange(startDay.value, numberOfDays)
                },
                onMenuClicked = { onMenuClicked() },
                onUpClicked = {},
                onDownClicked = {}
            )
            AgendaCalendarLayout(
                knobState = knobState,
                numberOfDays = numberOfDays,
                startDay = startDay.value,
                events = eventData,
                onCalendarItemSelectedChange = { event ->
                    currentlySelectedEventLabel.value = "Event: ${event.headerText}, ${event.getBodyText()}"
                    scope.launch {
                        agendaCalendarLayoutHeader.value = currentlySelectedEventLabel.value
                        delay(2.seconds)
                        agendaCalendarLayoutHeader.value = timePeriodLabel.value
                    }
                }
            )
        }
    }

}