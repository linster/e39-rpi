package ca.stefanm.ibus.gui.pim.calendar.views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.repo.api.WeekViewRepo
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarLayout
import ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.core.minusDays
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@ScreenDoc(
    screenName = "CalendarEventEditScreen",
    description = "Create or edit calendar events",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class CalendarEventEditScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val knobListenerService: KnobListenerService,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val weekViewRepo: WeekViewRepo
) : NavigationNode<Nothing>{

    companion object {

        fun createNewEvent(navigationNodeTraverser: NavigationNodeTraverser) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                CalendarEventEditScreen::class.java,
                CreateCalendarEventEditScreenOpenMode.NewEvent
            )
        }
        fun openEventForEditing(
            navigationNodeTraverser: NavigationNodeTraverser,
            agendaCalendarEventData: AgendaCalendarEventData) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                CalendarEventEditScreen::class.java,
                CreateCalendarEventEditScreenOpenMode.EditExisting(agendaCalendarEventData)
            )
        }
    }

    sealed class CreateCalendarEventEditScreenOpenMode {
        object NewEvent : CreateCalendarEventEditScreenOpenMode()
        data class EditExisting(val event : AgendaCalendarEventData) : CreateCalendarEventEditScreenOpenMode()
    }

    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = @Composable { params ->

        var isNewEvent = false
        val eventToEdit = remember { mutableStateOf<AgendaCalendarEventData?>(null) }
        if (params != null && params.requestParameters is CreateCalendarEventEditScreenOpenMode) {
            params.requestParameters
            if (params.requestParameters is CreateCalendarEventEditScreenOpenMode.NewEvent) {
                isNewEvent = true
            }
            if (params.requestParameters is CreateCalendarEventEditScreenOpenMode.EditExisting) {
                eventToEdit.value = params.requestParameters.event
            }
        }


        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        val knobState = remember(knobListenerService) { KnobObserverBuilderState(knobListenerService, logger) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            knobState.subscribeEvents()
        }

        val eventName = remember { mutableStateOf(if (isNewEvent) {"Event Name"} else { eventToEdit.value?.headerText ?: "null Event"}) }

        Column(Modifier
            .fillMaxSize()
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            if (isNewEvent) {
                BmwSingleLineHeader("Create new event")
            } else {
                BmwSingleLineHeader("Edit event: ${eventToEdit.value?.headerText}")
            }
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.5F)) {
                    AgendaCalendarLayout(
                        knobState = knobState,
                        numberOfDays = 3,
                        startDay = if (isNewEvent) {
                            today
                        } else {
                            eventToEdit.value?.getStartLocalDate() ?: today
                        }.minusDays(1),
                        events = weekViewRepo
                            .getAgendaViewEvents(today, 1)
                            .map { list -> list.map { event -> event.copy(onClick = {}) } }
                            .collectAsState(listOf()).value
                        ,
                        onCalendarItemSelectedChange = {}
                    )
                }
                Column(Modifier.weight(0.3F, true)) {
                    //TODO calendar scroll buttons.
                    Column {
                        Row {
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier,
                                    label = "⏴",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier,
                                    label = "⏵",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier,
                                    label = "⏶",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier,
                                    label = "⏷",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Days ⏿: 3",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = allocatedIndex == currentIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }
                        }
                    }
                    KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier.fillMaxWidth(),
                            label = "Go back",
                            chipOrientation = ItemChipOrientation.E,
                            isSelected = currentIndex == allocatedIndex,
                            isSmallSize = true,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.goBack()
                            }
                        )
                    }



                    MenuItem(
                        boxModifier = Modifier.fillMaxWidth(),
                        label = "✐ ${eventName.value}",
                        chipOrientation = ItemChipOrientation.E,
                        isSelected = false,
                        isSmallSize = true,
                        onClicked = {
                            modalMenuService.showKeyboard(
                                Keyboard.KeyboardType.FULL,
                                prefilled = "",
                                onTextEntered = { new -> eventName.value = new}
                            )
                        }
                    )

                    Row(
                        //Modifier.border(2.dp, Color.Red) //ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                    ) {
                        Column(Modifier
                            .weight(0.5F, true)
                            .border(3.dp, ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                        ) {
                            MenuItem(
                                boxModifier = Modifier.fillMaxWidth().background(ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder),
                                label = "Start",
                                chipOrientation = ItemChipOrientation.NONE,
                                isSelected = false,
                                isSmallSize = true,
                                onClicked = {}
                            )
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${
                                        (eventToEdit.value?.getStartLocalDate() ?: today).format(
                                            LocalDate.Format {
                                                day()
                                                char(' ')
                                                this.monthName(MonthNames.ENGLISH_ABBREVIATED)
                                            }
                                        )
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showDayPicker(today.yearMonth) {
                                            logger.d("WAT", "Start Date picked $it")
                                        }
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${
                                        (eventToEdit.value
                                            ?.start
                                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                            ?: LocalDateTime(today, LocalTime(hour = 8, minute = 0, second = 59))
                                                ).format(LocalDateTime.Format {
                                                hour() ; char(':') ; minute()
                                            })
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }

                        }
                        Column(Modifier
                            .weight(0.5F, true)
                            .border(3.dp, ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                        ) {
                            MenuItem(
                                boxModifier = Modifier.fillMaxWidth().background(ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder),
                                label = "End",
                                chipOrientation = ItemChipOrientation.NONE,
                                isSelected = false,
                                isSmallSize = true,
                                onClicked = {}
                            )

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${
                                        (eventToEdit.value?.getEndLocalDate() ?: today).format(
                                            LocalDate.Format {
                                                day()
                                                char(' ')
                                                this.monthName(MonthNames.ENGLISH_ABBREVIATED)
                                            }
                                        )
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showDayPicker(today.yearMonth) {
                                            logger.d("WAT", "End Date picked $it")
                                        }
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${
                                        (eventToEdit.value
                                            ?.end
                                            ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                            ?: LocalDateTime(today, LocalTime(hour = 13, minute = 0, second = 59))
                                                ).format(LocalDateTime.Format {
                                                hour() ; char(':') ; minute()
                                            })
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                                    }
                                )
                            }



                        }
                    }

                    KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier.fillMaxWidth(),
                            label = "Save",
                            chipOrientation = ItemChipOrientation.E,
                            isSelected = currentIndex == allocatedIndex,
                            isSmallSize = true,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.goBack()
                            }
                        )
                    }
                }
            }
        }
    }

}