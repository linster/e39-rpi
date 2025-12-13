package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.NumericTextViewWithSpinnerButtons
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventBox
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventColors.green3
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.CalendarEventColors.plum1
import javax.inject.Inject

class AgendaPartsDebugWindow @Inject constructor(

) : WindowManager.E39Window {
    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size: DpSize = DpSize(1800.dp, 1200.dp)

    override val title: String = "Agenda Parts Debug Window"

    override fun content(): @Composable WindowScope.() -> Unit = {

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
            AgendaCalendarLayout(
                numberOfDays = numberOfDays
            )
        }
    }

    @Composable
    fun EventBuilderPane(
        onNewEventsList : () -> Unit = {}
    ) {

    }
}