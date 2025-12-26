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
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
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

        val isNewEventComplete = remember { mutableStateOf(!isNewEvent) }

        val eventName = remember {
            mutableStateOf(
                if (isNewEvent) {
                    "Event Name"
                } else {
                    eventToEdit.value?.headerText ?: "null Event"
                }
            )
        }

        val startDateEntered = remember { mutableStateOf<LocalDate?>(null) }
        val endDateEntered = remember { mutableStateOf<LocalDate?>(null) }

        val startLocalTime = remember { mutableStateOf<LocalTime?>(null) }
        val endLocalTime = remember { mutableStateOf<LocalTime?>(null) }

        LaunchedEffect(
            eventName.value,
            startDateEntered.value,
            endDateEntered.value,
            startLocalTime.value,
            endLocalTime.value
        ) effect@ {
            val newEventName = eventName.value
            val newStartDate = startDateEntered.value ?: return@effect
            val newEndDate = endDateEntered.value ?: return@effect
            val newStartLocalTime = startLocalTime.value ?: return@effect
            val newEndLocalTime = endLocalTime.value ?: return@effect

            logger.d("CalendarEventEditScreen", "Updating eventToEdit")
            isNewEventComplete.value = true
            notificationHub.postNotificationBackground(
                Notification(
                Notification.NotificationImage.MESSAGE_CIRCLE,
                    "Calendar event filled in",
                    "New event now visible on preview"
            )
            )
            eventToEdit.value = AgendaCalendarEventData(
                headerText = newEventName,
                start = LocalDateTime(newStartDate, newStartLocalTime).toInstant(TimeZone.currentSystemDefault()),
                end = LocalDateTime(newEndDate, newEndLocalTime).toInstant(TimeZone.currentSystemDefault()),
                color = eventToEdit.value?.color ?: Color.Magenta,
                eventUuid = eventToEdit.value?.eventUuid,
                onClick = {}
            )
        }

        val calendarDaysVisible = remember { mutableStateOf(3) }

        val events = weekViewRepo
            .getAgendaViewEvents(today, 1)
            .map { list -> list.map { event -> event.copy(onClick = {}) } }
            .map { list -> if (eventToEdit.value == null) { list } else { list.plus(eventToEdit.value!!.splitToMultipleEvents())} }
            .collectAsState(listOf())

        ContentLayout(
            calendarEventList = events.value,
            knobState = knobState,
            isNewEvent = isNewEvent,
            isNewEventComplete = isNewEventComplete.value,
            today = today,
            eventToEdit = eventToEdit.value,
            eventName = eventName.value,

            onNewEventName = { eventName.value = it},

            startDate = if (startDateEntered.value == null) { today } else { startDateEntered.value!!},
            onStartDateEntered = startDateEntered@ { start ->
                endDateEntered.value.let { end ->
                    if (end == null) {
                        startDateEntered.value = start
                        return@startDateEntered
                    }
                    if (start > end) {
                        notificationHub.postNotificationBackground(
                            Notification(
                            Notification.NotificationImage.ALERT_TRIANGLE,
                                "Invalid Date Entered",
                                "Start date must be before end date."
                        ))
                        return@startDateEntered
                    }
                    startDateEntered.value = start
                } },
            endDate = if (endDateEntered.value == null) { today } else { endDateEntered.value!!},
            onEndDateEntered = endDateEntered@ { end ->
                startDateEntered.value.let { start ->
                    if (start == null) {
                        //Quickly enter
                        endDateEntered.value = end
                        return@endDateEntered
                    }
                    if (end < start) {
                        notificationHub.postNotificationBackground(
                            Notification(
                                Notification.NotificationImage.ALERT_TRIANGLE,
                                "Invalid Date Entered",
                                "End date must be after start date."
                            ))
                        return@endDateEntered
                    }
                    endDateEntered.value = end
                }
            },

            startLocalTime = if (startLocalTime.value == null) { LocalTime(13, 0)} else {startLocalTime.value!! },
            onStartLocalTimePicked = { startLocalTime.value = it},

            endLocalTime = if (endLocalTime.value == null ) { LocalTime(15, 0) } else { endLocalTime.value!! },
            onEndLocalTimePicked = { endLocalTime.value = it},

            onGoBackClicked = { navigationNodeTraverser.goBack()},
            onSaveClicked = {},


            onCalendarScrollLeft = {},
            onCalendarScrollRight = {},
            onCalendarScrollUp = {},
            onCalendarScrollDown = {},

            calendarDaysVisible = calendarDaysVisible.value,
            onCalendarDaysVisibleChanged = { calendarDaysVisible.value = it}
        )

    }

    @Composable
    fun ContentLayout(
        calendarEventList : List<AgendaCalendarEventData>,
        knobState : KnobObserverBuilderState,
        isNewEvent : Boolean,
        isNewEventComplete : Boolean,
        today : LocalDate,
        eventToEdit : AgendaCalendarEventData?,
        eventName : String,

        onNewEventName : (String) -> Unit,

        startDate : LocalDate,
        onStartDateEntered : (LocalDate) -> Unit,
        endDate : LocalDate,
        onEndDateEntered : (LocalDate) -> Unit,

        startLocalTime : LocalTime,
        onStartLocalTimePicked : (LocalTime) -> Unit,

        endLocalTime: LocalTime,
        onEndLocalTimePicked : (LocalTime) -> Unit,

        onGoBackClicked : () -> Unit,
        onSaveClicked : () -> Unit,

        onCalendarScrollLeft : () -> Unit,
        onCalendarScrollRight : () -> Unit,
        onCalendarScrollUp : () -> Unit,
        onCalendarScrollDown : () -> Unit,
        calendarDaysVisible : Int,
        onCalendarDaysVisibleChanged : (Int) -> Unit,
    ) {

        val newEventIncompleteMarker = if (isNewEventComplete) { "" } else {"\uD83D\uDDCB"}

        Column(Modifier
            .fillMaxSize()
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            if (isNewEvent) {
                BmwSingleLineHeader("Create new event")
            } else {
                BmwSingleLineHeader("Edit event: ${eventToEdit?.headerText}")
            }
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.5F)) {
                    AgendaCalendarLayout(
                        knobState = knobState,
                        numberOfDays = calendarDaysVisible,
                        startDay = if (isNewEvent) {
                            today
                        } else {
                            eventToEdit?.getStartLocalDate() ?: today
                        }.minusDays(1),
                        events = calendarEventList,
                        onCalendarItemSelectedChange = {}
                    )
                }
                Column(Modifier.weight(0.4F, true)) {
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
                                        onCalendarScrollLeft()
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
                                        onCalendarScrollRight()
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
                                        onCalendarScrollUp()
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
                                        onCalendarScrollDown()
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Days ⏿: $calendarDaysVisible",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = allocatedIndex == currentIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showKeyboard(Keyboard.KeyboardType.NUMERIC,
                                            ""
                                        ) {
                                            onCalendarDaysVisibleChanged(it.toIntOrNull()?.coerceIn(1..5) ?: calendarDaysVisible)
                                        }
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
                                onGoBackClicked()
                            }
                        )
                    }

                    MenuItem(
                        boxModifier = Modifier.fillMaxWidth(),
                        label = "✐ ${newEventIncompleteMarker}$eventName",
                        chipOrientation = ItemChipOrientation.E,
                        isSelected = false,
                        isSmallSize = true,
                        onClicked = {
                            modalMenuService.showKeyboard(
                                Keyboard.KeyboardType.FULL,
                                prefilled = "",
                                onTextEntered = { new -> onNewEventName(new)}
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
                                    label = "✐ ${newEventIncompleteMarker}${
                                        startDate.format(
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
                                            onStartDateEntered(it)
                                        }
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${newEventIncompleteMarker}${
                                        startLocalTime.format(LocalTime.Format {
                                            hour() ; char(':') ; minute() 
                                        })
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showTimePicker(startLocalTime) {
                                            onStartLocalTimePicked(it)
                                        }
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
                                    label = "✐ ${newEventIncompleteMarker}${
                                        endDate.format(
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
                                            onEndDateEntered(it)
                                        }
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "✐ ${newEventIncompleteMarker}${
                                        endLocalTime.format(LocalTime.Format {
                                            hour() ; char(':') ; minute()
                                        })
                                    }",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showTimePicker(endLocalTime) {
                                            onEndLocalTimePicked(it)
                                        }
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
                                onSaveClicked()
                            }
                        )
                    }
                }
            }
        }
    }

}