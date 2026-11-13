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
import ca.stefanm.ibus.gui.pim.calendar.repo.api.WeekViewRepo
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarLayout
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationComponent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.MenuWindow.Companion.MenuWindowKnobListener
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
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.plusDays
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import androidx.compose.ui.text.font.FontWeight
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import javax.inject.Inject
import javax.inject.Named
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
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
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

    /** Times shown (and used) for new events, and for all-day events given a time. */
    private val DEFAULT_START_TIME = LocalTime(13, 0)
    private val DEFAULT_END_TIME = LocalTime(15, 0)

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

        val saveScope = rememberCoroutineScope()

        val knobState = setupListener(
            knobListenerService,
            logger,
            "CalendarEventEditScreen"
        )


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

        // The event as opened (null for a new one). Fields that weren't
        // entered show (and keep, on Save) its values; an all-day event stays
        // all day until a time is entered.
        val original = (params?.requestParameters as? CreateCalendarEventEditScreenOpenMode.EditExisting)?.event
        val originalStart = original?.start?.toLocalDateTime(TimeZone.currentSystemDefault())
        val originalEnd = original?.end?.toLocalDateTime(TimeZone.currentSystemDefault())
        val staysAllDay = original?.allDayEvent == true && startLocalTime.value == null && endLocalTime.value == null

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
                eventRef = eventToEdit.value?.eventRef,
                onClick = {}
            )
        }

        val calendarDaysVisible = remember { mutableStateOf(3) }
        val calendarStartHour = remember { mutableStateOf(0) }

        val calendarStartDate = remember { mutableStateOf(today) }

        LaunchedEffect(isNewEventComplete.value, eventToEdit.value) {
            calendarStartDate.value = if (isNewEvent && !isNewEventComplete.value) {
                today
            } else {
                eventToEdit.value?.getStartLocalDate() ?: today
            }.minusDays(1)
        }

        // Remembered: each call queries the PIM bridge and watches it for changes.
        val todaysEvents = remember(today) { weekViewRepo.getAgendaViewEvents(today, 1) }
            .collectAsState(listOf()).value
        val events = remember(todaysEvents, eventToEdit.value) {
            val list = todaysEvents.map { event -> event.copy(onClick = {}) }
            mutableStateOf(eventToEdit.value?.let { list.plus(it.splitToMultipleEvents()) } ?: list)
        }

        ContentLayout(
            calendarEventList = events.value,
            knobState = knobState,
            isNewEvent = isNewEvent,
            isNewEventComplete = isNewEventComplete.value,
            today = today,
            calendarStartDate = calendarStartDate.value,
            eventToEdit = eventToEdit.value,
            eventName = eventName.value,

            onNewEventName = { eventName.value = it},

            startDate = startDateEntered.value ?: originalStart?.date ?: today,
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
            endDate = endDateEntered.value ?: originalEnd?.date ?: today,
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

            startLocalTime = startLocalTime.value ?: originalStart?.time?.takeIf { original?.allDayEvent == false } ?: DEFAULT_START_TIME,
            onStartLocalTimePicked = { startLocalTime.value = it},

            endLocalTime = endLocalTime.value ?: originalEnd?.time?.takeIf { original?.allDayEvent == false } ?: DEFAULT_END_TIME,
            onEndLocalTimePicked = { endLocalTime.value = it},

            onGoBackClicked = { navigationNodeTraverser.goBack()},
            allDayTimes = staysAllDay,
            onSaveClicked = {
                saveEvent(
                    scope = saveScope,
                    original = original,
                    name = eventName.value,
                    startDate = startDateEntered.value,
                    endDate = endDateEntered.value,
                    startTime = startLocalTime.value,
                    endTime = endLocalTime.value,
                )
            },
            canDelete = original?.eventRef?.let { !it.readOnly } == true,
            onDeleteClicked = { original?.let { confirmDelete(saveScope, it) } },


            onCalendarScrollLeft = { calendarStartDate.value = calendarStartDate.value.minusDays(1) },
            onCalendarScrollDaysResetToToday = { calendarStartDate.value = today.minusDays(1) },
            onCalendarScrollRight = { calendarStartDate.value = calendarStartDate.value.plusDays(1) },

            calendarStartHour = calendarStartHour.value,
            onCalendarScrollUp = {
                calendarStartHour.value = (calendarStartHour.value - 1).coerceIn(0 until 23)
            },
            onCalendarScrollDown = {
                calendarStartHour.value = (calendarStartHour.value + 1).coerceIn(0 until 23)
            },

            calendarDaysVisible = calendarDaysVisible.value,
            onCalendarDaysVisibleChanged = { calendarDaysVisible.value = it}
        )

    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content))
    }

    /**
     * The event to save from what was entered. New events need all four
     * date/time fields. Existing events keep their values for fields that
     * weren't entered; all-day events stay all day (with the entered dates)
     * unless a time was entered. Null (and a notification) if it can't be
     * saved.
     */
    private fun editedEvent(
        original: AgendaCalendarEventData?,
        name: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        startTime: LocalTime?,
        endTime: LocalTime?,
    ): AgendaCalendarEventData? {
        val zone = TimeZone.currentSystemDefault()
        val edited = if (original == null) {
            if (startDate == null || endDate == null || startTime == null || endTime == null) {
                notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not saved", "Enter the start and end first.")
                return null
            }
            AgendaCalendarEventData(
                headerText = name,
                start = LocalDateTime(startDate, startTime).toInstant(zone),
                end = LocalDateTime(endDate, endTime).toInstant(zone),
                color = Color.Magenta,
            )
        } else {
            val originalStart = original.start.toLocalDateTime(zone)
            val originalEnd = original.end.toLocalDateTime(zone)
            val firstDay = startDate ?: originalStart.date
            val lastDay = endDate ?: originalEnd.date
            if (original.allDayEvent && startTime == null && endTime == null) {
                if (lastDay < firstDay) {
                    notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not saved", "The end must be after the start.")
                    return null
                }
                // As CalendarMapper draws all-day events: midnight to just
                // before midnight after the last day.
                original.copy(
                    headerText = name,
                    start = firstDay.atStartOfDayIn(zone),
                    end = lastDay.plusDays(1).atStartOfDayIn(zone).minus(1.seconds),
                    allDayEvent = true,
                )
            } else {
                val from = startTime ?: if (original.allDayEvent) DEFAULT_START_TIME else originalStart.time
                val to = endTime ?: if (original.allDayEvent) DEFAULT_END_TIME else originalEnd.time
                original.copy(
                    headerText = name,
                    start = LocalDateTime(firstDay, from).toInstant(zone),
                    end = LocalDateTime(lastDay, to).toInstant(zone),
                    allDayEvent = false,
                )
            }
        }
        if (edited.end <= edited.start) {
            notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not saved", "The end must be after the start.")
            return null
        }
        return edited
    }

    /**
     * Saves through the PIM bridge. For a repeating event it asks first
     * whether the change is for this occurrence or the whole series (only
     * this occurrence if its times changed: they don't say where the series
     * should move).
     */
    private fun saveEvent(
        scope: CoroutineScope,
        original: AgendaCalendarEventData?,
        name: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        startTime: LocalTime?,
        endTime: LocalTime?,
    ) {
        val edited = editedEvent(original, name, startDate, endDate, startTime, endTime) ?: return
        val timesChanged = original != null &&
            (edited.start != original.start || edited.end != original.end || edited.allDayEvent != original.allDayEvent)
        val changed = original == null || timesChanged || edited.headerText != original.headerText
        // Nothing to ask about if it doesn't repeat or nothing changed.
        if (original?.eventRef?.recurring != true || !changed) {
            save(scope, original, edited, EventScope.THIS_OCCURRENCE)
            return
        }
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Save Changes To",
                text = @Composable {
                    InfoLabel(edited.headerText, FontWeight.Bold)
                    InfoLabel("This event repeats.")
                    if (timesChanged) {
                        InfoLabel("New times can only be saved for this occurrence.")
                    }
                },
                buttons = buildList {
                    add(TextMenuItem("This Occurrence", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        save(scope, original, edited, EventScope.THIS_OCCURRENCE)
                    }))
                    if (!timesChanged) {
                        add(TextMenuItem("Whole Series", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            save(scope, original, edited, EventScope.WHOLE_SERIES)
                        }))
                    }
                    add(TextMenuItem("Cancel", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    private fun save(scope: CoroutineScope, original: AgendaCalendarEventData?, edited: AgendaCalendarEventData, eventScope: EventScope) {
        scope.launch {
            weekViewRepo.saveEditedEvent(original, edited, eventScope).fold(
                onSuccess = { saved ->
                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Event saved", saved.headerText)
                    navigationNodeTraverser.goBack()
                },
                onFailure = { e -> notifyFailure("Not saved", edited.headerText, e) }
            )
        }
    }

    /** Asks first; for a repeating event, this occurrence or the whole series. */
    private fun confirmDelete(scope: CoroutineScope, event: AgendaCalendarEventData) {
        val recurring = event.eventRef?.recurring == true
        fun delete(eventScope: EventScope) {
            modalMenuService.closeSidePaneOverlay(true)
            scope.launch {
                weekViewRepo.deleteEvent(event, eventScope).fold(
                    onSuccess = {
                        notify(Notification.NotificationImage.MESSAGE_CIRCLE, "Event deleted", event.headerText)
                        navigationNodeTraverser.goBack()
                    },
                    onFailure = { e -> notifyFailure("Not deleted", event.headerText, e) }
                )
            }
        }
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Delete Event?",
                text = @Composable {
                    InfoLabel(event.headerText, FontWeight.Bold)
                    if (recurring) InfoLabel("This event repeats.")
                },
                buttons = buildList {
                    add(TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                    if (recurring) {
                        add(TextMenuItem("Delete This Occurrence", onClicked = { delete(EventScope.THIS_OCCURRENCE) }))
                        add(TextMenuItem("Delete Whole Series", onClicked = { delete(EventScope.WHOLE_SERIES) }))
                    } else {
                        add(TextMenuItem("Delete", onClicked = { delete(EventScope.WHOLE_SERIES) }))
                    }
                }
            )
        }
    }

    private fun notifyFailure(top: String, what: String, e: Throwable) {
        logger.w("CalendarEventEditScreen", "$top: $what: ${e.message}")
        notify(
            Notification.NotificationImage.ALERT_TRIANGLE,
            top,
            when (e) {
                is PimBackendException.Conflict -> "It was changed elsewhere meanwhile. Open it again and redo the change."
                is PimBackendException.Unavailable -> "Calendar not reachable: ${e.message}"
                else -> e.message ?: e.toString()
            }
        )
    }

    @Composable
    fun ContentLayout(
        calendarEventList : List<AgendaCalendarEventData>,
        knobState : KnobObserverBuilderState,
        isNewEvent : Boolean,
        isNewEventComplete : Boolean,
        today : LocalDate,
        calendarStartDate : LocalDate,
        calendarStartHour : Int,
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
        /** The time fields show "all day" instead of times. */
        allDayTimes : Boolean,
        onSaveClicked : () -> Unit,
        canDelete : Boolean,
        onDeleteClicked : () -> Unit,

        onCalendarScrollLeft : () -> Unit,
        onCalendarScrollDaysResetToToday : () -> Unit,
        onCalendarScrollRight : () -> Unit,
        onCalendarScrollUp : () -> Unit,
        onCalendarScrollDown : () -> Unit,
        calendarDaysVisible : Int,
        onCalendarDaysVisibleChanged : (Int) -> Unit,
    ) {

        val scope = rememberCoroutineScope()
        val newEventIncompleteMarker = if (isNewEventComplete) { "" } else {"\uD83D\uDDCB"}

        Column(Modifier
            .fillMaxSize()
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            //Make the header be a scrollable preview of the selected event, which can be rather small
            val singleLineHeaderIsPreview = remember { mutableStateOf(false) }
            val singleLineHeaderDefault = if (isNewEvent && !isNewEventComplete) {
                "Create new event"
            } else {
                "Edit event: ${eventToEdit?.headerText}"
            }
            val singleLineHeaderPreview = remember { mutableStateOf("") }
            if (singleLineHeaderIsPreview.value) {
                BmwSingleLineHeader(singleLineHeaderPreview.value)
            } else {
                BmwSingleLineHeader(singleLineHeaderDefault)
            }

            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.5F)) {
                    AgendaCalendarLayout(
                        knobState = knobState,
                        numberOfDays = calendarDaysVisible,
                        startDay = calendarStartDate,
                        events = calendarEventList,
                        startHour = calendarStartHour,
                        onCalendarItemSelectedChange = {
                            singleLineHeaderPreview.value = "${it.headerText} ${it.getBodyText()}"
                            scope.launch {
                                singleLineHeaderIsPreview.value = true
                                delay(2.seconds)
                                singleLineHeaderIsPreview.value = false
                            }
                        }
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
                                    label = "\uD83D\uDD70",
                                    chipOrientation = ItemChipOrientation.N,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        onCalendarScrollDaysResetToToday()
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

                    KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                        MenuItem(
                            boxModifier = Modifier.fillMaxWidth(),
                            label = "✐ ${newEventIncompleteMarker}$eventName",
                            chipOrientation = ItemChipOrientation.E,
                            isSelected = currentIndex == allocatedIndex,
                            isSmallSize = true,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                modalMenuService.showKeyboard(
                                    Keyboard.KeyboardType.FULL,
                                    prefilled = "",
                                    onTextEntered = { new -> onNewEventName(new) }
                                )
                            }
                        )
                    }

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
                                        if (allDayTimes) "all day" else startLocalTime.format(LocalTime.Format {
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
                                        if (allDayTimes) "all day" else endLocalTime.format(LocalTime.Format {
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

                    Row {
                        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                            MenuItem(
                                boxModifier = Modifier.weight(0.5F, true),
                                label = "Save",
                                chipOrientation = ItemChipOrientation.E,
                                isSelected = currentIndex == allocatedIndex,
                                isSmallSize = true,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    onSaveClicked()
                                }
                            )
                        }
                        if (canDelete) {
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.weight(0.5F, true),
                                    label = "Delete...",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        onDeleteClicked()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}