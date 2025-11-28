package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import com.macaosoftware.ui.dailyagenda.*
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ScreenDoc(
    screenName = "OneWeekScreen",
    description = "One-week calendar view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class OneWeekScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val knobListenerService: KnobListenerService,
    private val calendarOptionsMenu: CalendarOptionsMenu
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = OneWeekScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        OneWeekCalendar()
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    fun OneWeekCalendar() {

        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            knobState.subscribeEvents()
        }

        val timeSlotsStateController = remember {
            TimeSlotsStateController(
                timeSlotConfig = TimeSlotConfig(slotScale = 2, slotHeight = 48),
                eventsArrangement =
                    EventsArrangement.MixedDirections(EventWidthType.FixedSizeFillLastEvent)
            ).apply {
                timeSlotsDataUpdater.postUpdate {
                    addEvent(
                        uuid = Uuid.random(),
                        startTime = LocalTime(hour = 8, minute = 0),
                        endTime = LocalTime(hour = 8, minute = 30),
                        title = "Event 0",
                        description = "Description 0"
                    )
                    addEventList(
                        startTime = LocalTime(hour = 8, minute = 0), // This is the slot start time
                        events =
                            listOf(
                                LocalTimeEvent(
                                    uuid = Uuid.random(),
                                    startTime = LocalTime(hour = 8, minute = 0),
                                    endTime = LocalTime(hour = 8, minute = 45),
                                    title = "Event 1",
                                    description = "Description 1"
                                ),
                                LocalTimeEvent(
                                    uuid = Uuid.random(),
                                    startTime = LocalTime(hour = 8, minute = 0),
                                    endTime = LocalTime(hour = 9, minute = 0),
                                    title = "Event 2",
                                    description = "Description 2"
                                )
                            )
                    )
                }
            }
        }

        TimeSlotsView(timeSlotsStateController = timeSlotsStateController) { localTimeEvent ->
            Box(modifier = Modifier.fillMaxSize().padding(all = 2.dp).background(color = Color.Gray)) {
                Text(
                    text =
                        "${localTimeEvent.title}: ${localTimeEvent.startTime}-${localTimeEvent.endTime}",
                    fontSize = 12.sp
                )
            }
        }
    }
}