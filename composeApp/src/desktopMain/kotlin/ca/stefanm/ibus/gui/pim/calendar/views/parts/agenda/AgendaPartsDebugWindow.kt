package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.NumericTextViewWithSpinnerButtons
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventColors.green3
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventColors.plum1
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class AgendaPartsDebugWindow @Inject constructor(
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size: DpSize = DpSize(1800.dp, 1200.dp)

    override val title: String = "Agenda Parts Debug Window"

    override fun content(): @Composable WindowScope.() -> Unit = {

        val startDay = remember { mutableStateOf(Clock.System.now()) }
        val numberOfDays = remember { mutableStateOf(3) }
        Row {
            Column(modifier = Modifier.weight(0.5F)) {

                NestingCard {
                    NestingCardHeader("Config")
                    NestingCard {
                        NestingCardHeader("Number of Days")
                        NumericTextViewWithSpinnerButtons(
                            label = "Number of Days",
                            initialValue = numberOfDays.value,
                            stepOnButton = 1,
                            onValueChanged = { numberOfDays.value = it}
                        )
                    }
                }
                NestingCard {
                    NestingCardHeader("Start Day")
                    NestingCard {
                        Text("Current Start Day")
                        Text("${startDay.value.toLocalDateTime(TimeZone.currentSystemDefault()).date}")
                        with (startDay.value.toLocalDateTime(TimeZone.currentSystemDefault())) {
                            Text("${month.toJavaMonth().getDisplayName(TextStyle.SHORT, Locale.CANADA)} ${day}")
                        }
                    }
                }
                NestingCard {
                    NestingCardHeader("Event Config")
                    EventBuilderPane()
                }
            }
            Column(modifier = Modifier.weight(0.5F)) {
                NestingCard {
                    NestingCardHeader("Event Color Test")
                    EventColorTestWindow()
                }
                NestingCard {
                    NestingCardHeader("Layout Tester")
                    SlotLayoutTestWindow(
                        numberOfDays = numberOfDays.value
                    )
                }
            }
        }
    }

    @Composable
    fun EventColorTestWindow() {
        Column(
            Modifier
                .width(800.dp)
                .height(468.dp)
                .border(2.dp, Color.Red)
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            Row(Modifier.padding(10.dp)) {
                CalendarEventBox(
                    header = "Plast",
                    body = "7-9pm",
                    baseColor = plum1,
                    onClick = {}
                )
                CalendarEventBox(
                    header = "Curling",
                    body = "7-9pm",
                    baseColor = green3,
                    onClick = {}
                )
                CalendarEventBox(
                    header = "Trivia",
                    body = "7-9pm",
                    baseColor = Color.Magenta,
                    onClick = {}
                )
            }
        }
    }

    @Composable
    fun SlotLayoutTestWindow(numberOfDays : Int = 3) {
        Column(
            Modifier
                .width(800.dp)
                .height(468.dp)
                .border(2.dp, Color.Red)
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                knobState.subscribeEvents()
            }

            AgendaCalendarLayout(
                numberOfDays = numberOfDays,
                events = listOf(
                    AgendaCalendarEventData(
                        headerText = "Plast",
                        start = LocalDateTime(today.year, today.month, today.day, 3, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        end = LocalDateTime(today.year, today.month, today.day, 8, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        color = Color.Magenta
                    ),
                    AgendaCalendarEventData(
                        headerText = "Curling",
                        start = LocalDateTime(today.year, today.month, today.day + 1, 4, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        end = LocalDateTime(today.year, today.month, today.day + 1, 5, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        color = Color.Magenta
                    ),
                    AgendaCalendarEventData(
                        headerText = "Trivia",
                        start = LocalDateTime(today.year, today.month, today.day + 1, 7, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        end = LocalDateTime(today.year, today.month, today.day + 1, 9, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                        color = Color.Magenta
                    ),
                )
            )
        }
    }

    @Composable
    fun EventBuilderPane(
        onNewEventsList : () -> Unit = {}
    ) {

    }
}