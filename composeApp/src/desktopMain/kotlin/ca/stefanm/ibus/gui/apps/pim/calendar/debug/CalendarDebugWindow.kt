package ca.stefanm.ibus.gui.pim.calendar.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.e39pim.v1.Calendar
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.EnumColumn
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.NumericTextViewWithSpinnerButtons
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarInfo
import ca.stefanm.ibus.gui.pim.calendar.repo.api.CalendarViewConfigRepo
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventScope
import ca.stefanm.ibus.gui.pim.calendar.repo.api.EventTimes
import ca.stefanm.ibus.gui.pim.calendar.repo.api.TodoItem
import ca.stefanm.ibus.gui.pim.calendar.repo.api.MonthViewRepo
import ca.stefanm.ibus.gui.pim.calendar.repo.api.WeekViewRepo
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.CalendarGrpcClient
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository.BridgeSummary
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteTodoRepository
import ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda.AgendaCalendarEventData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Everything the calendar repositories (and the PIM bridge client under
 * them) can do, with the bridge's raw answers: settings and connection,
 * calendars, event queries through each repository (live: they re-emit on
 * change notices), GetEvent, creating/changing/deleting events, to-dos, the
 * raw change stream, the HMI's calendar view setting.
 */
class CalendarDebugWindow @Inject constructor(
    private val settingsRepository: PimBackendSettingsRepository,
    private val remoteCalendarRepository: RemoteCalendarRepository,
    private val calendarGrpcClient: CalendarGrpcClient,
    private val weekViewRepo: WeekViewRepo,
    private val monthViewRepo: MonthViewRepo,
    private val calendarViewConfigRepo: CalendarViewConfigRepo,
    private val todoRepository: RemoteTodoRepository,
) : WindowManager.E39Window {

    companion object {
        const val MAX_LOG_LINES = 100
    }

    override val tag: Any
        get() = this

    override val title: String = "Calendar (PIM Bridge) Debug"

    override val size: DpSize = DpSize(1500.dp, 1000.dp)

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    /** Which repository answers the event query. */
    enum class EventSource {
        /** RemoteCalendarRepository.events(start, end, zone) */
        REMOTE_REPOSITORY,
        /** WeekViewRepo.getAgendaViewEvents(startDay, numDays) */
        WEEK_VIEW_REPO,
        /** MonthViewRepo.eventsByDay(firstDay, lastDay) */
        MONTH_VIEW_REPO,
    }

    override fun content(): @Composable WindowScope.() -> Unit = {
        // Shared between panes: clicking an event fills in GetEvent.
        val selectedEvent = remember { mutableStateOf<AgendaCalendarEventData?>(null) }

        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(0.45f).verticalScroll(rememberScrollState())) {
                BackendPane()
                CalendarsPane()
                ViewConfigPane()
                TodosPane()
                ChangeStreamPane()
            }
            Column(Modifier.weight(0.55f).verticalScroll(rememberScrollState())) {
                EventQueryPane(onEventSelected = { selectedEvent.value = it })
                GetEventPane(selectedEvent.value)
                WriteEventPane(selectedEvent.value, onSaved = { selectedEvent.value = it })
            }
        }
    }

    // --- Settings, connection --------------------------------------------

    @Composable
    private fun BackendPane() {
        val settings = settingsRepository.settings.collectAsState().value
        val connectionState = remoteCalendarRepository.connectionState.collectAsState().value
        val summary = remember { mutableStateOf<Result<BridgeSummary>?>(null) }
        val scope = rememberCoroutineScope()

        NestingCard {
            NestingCardHeader("PIM bridge (PimBackendSettingsRepository)")
            Text("Current: ${settings.target}")
            Text("Connection state: $connectionState")

            NestingCard {
                NestingCardHeader("Change settings")
                val host = remember(settings) { mutableStateOf(settings.host) }
                val port = remember(settings) { mutableStateOf(settings.port.toString()) }
                val error = remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(host.value, { host.value = it }, label = { Text("Host") }, singleLine = true,
                        modifier = Modifier.width(220.dp))
                    OutlinedTextField(port.value, { port.value = it }, label = { Text("Port") }, singleLine = true,
                        modifier = Modifier.width(100.dp))
                }
                Row {
                    Button(onClick = {
                        error.value = try {
                            settingsRepository.update(
                                PimBackendSettings(host.value, port.value.trim().toIntOrNull() ?: -1)
                            )
                            ""
                        } catch (e: IllegalArgumentException) {
                            e.message ?: e.toString()
                        }
                    }) { Text("update()") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { settingsRepository.resetToDefaults() }) {
                        Text("resetToDefaults() (${PimBackendSettings.DEFAULT.target})")
                    }
                }
                if (error.value.isNotEmpty()) Text("Rejected: ${error.value}", color = Color.Red)
            }

            NestingCard {
                NestingCardHeader("checkConnection() (GetStatus + ListCalendars)")
                Button(onClick = {
                    scope.launch { summary.value = remoteCalendarRepository.checkConnection() }
                }) { Text("checkConnection()") }
                summary.value?.let { result ->
                    result.fold(
                        onSuccess = { s ->
                            Text("Akonadi: ${s.akonadiState} ${s.brokenReason}")
                            Text("KDE ${s.akonadiVersion}, bridge ${s.bridgeVersion}")
                            Text("Calendars: ${s.calendars.joinToString(", ").ifEmpty { "(none)" }}")
                        },
                        onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
                    )
                }
            }
        }
    }

    // --- Calendars -------------------------------------------------------

    @Composable
    private fun CalendarsPane() {
        val calendars = remember { mutableStateOf<Result<List<Calendar>>?>(null) }
        val repoCalendars = remember { mutableStateOf<Result<List<CalendarInfo>>?>(null) }
        val defaultCalendar = remember { mutableStateOf<Result<CalendarInfo>?>(null) }
        val scope = rememberCoroutineScope()

        NestingCard {
            NestingCardHeader("Calendars (RemoteCalendarRepository)")
            Row {
                Button(onClick = { scope.launch { repoCalendars.value = remoteCalendarRepository.calendars() } }) { Text("calendars()") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { scope.launch { defaultCalendar.value = remoteCalendarRepository.defaultEventCalendar() } }) {
                    Text("defaultEventCalendar()")
                }
            }
            repoCalendars.value?.fold(
                onSuccess = { list -> list.forEach { Text(it.toString(), fontSize = 12.sp) } },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
            defaultCalendar.value?.fold(
                onSuccess = { Text("Default for new events: ${it.id} ${it.name}") },
                onFailure = { Text("Default: ${it.describe()}", color = Color.Red) }
            )
        }
        NestingCard {
            NestingCardHeader("Calendars (CalendarGrpcClient.listCalendars)")
            Button(onClick = {
                scope.launch { calendars.value = runCatching { calendarGrpcClient.listCalendars() } }
            }) { Text("listCalendars()") }
            calendars.value?.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) Text("(no calendars)")
                    list.forEach { c ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorSwatch(Color(c.colorArgb))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${c.id}: ${c.name} — ${c.accountName} (${c.accountId})" +
                                    (if (c.readOnly) " [read-only]" else "") +
                                    (if (c.hasEvents) " events" else "") +
                                    (if (c.hasTodos) " to-dos" else "")
                            )
                        }
                    }
                },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    // --- HMI view setting ------------------------------------------------

    @Composable
    private fun ViewConfigPane() {
        NestingCard {
            NestingCardHeader("Calendar view (CalendarViewConfigRepo.screenView)")
            EnumColumn(
                currentEntry = calendarViewConfigRepo.screenView.collectAsState().value,
                onEntryChanged = { calendarViewConfigRepo.screenView.value = it }
            )
            Text("Takes effect the next time the calendar screen opens.")
        }
    }

    // --- Raw change stream -----------------------------------------------

    @Composable
    private fun ChangeStreamPane() {
        val watching = remember { mutableStateOf(false) }
        val log = remember { mutableStateListOf<String>() }

        fun append(line: String) {
            log.add(0, "${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time} $line")
            while (log.size > MAX_LOG_LINES) log.removeAt(log.lastIndex)
        }

        LaunchedEffect(watching.value) {
            if (!watching.value) return@LaunchedEffect
            append("watching...")
            try {
                calendarGrpcClient.watchChanges().collect { notice ->
                    append(notice.toString().trim().replace(Regex("\\s+"), " "))
                }
                append("stream ended")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                append("stream failed: ${e.describe()}")
            }
        }

        NestingCard {
            NestingCardHeader("Change notices (CalendarGrpcClient.watchChanges, raw)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckBoxWithLabel(watching.value, { watching.value = it }, "Watch")
                Spacer(Modifier.width(8.dp))
                Button(onClick = { log.clear() }) { Text("Clear") }
            }
            Text("The repositories share their own stream; this one is separate.")
            SelectionContainer {
                Column {
                    log.forEach { Text(it, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                }
            }
        }
    }

    // --- Event queries ---------------------------------------------------

    @Composable
    private fun EventQueryPane(onEventSelected: (AgendaCalendarEventData) -> Unit) {
        val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        val source = remember { mutableStateOf(EventSource.REMOTE_REPOSITORY) }
        val startDayText = remember { mutableStateOf(today.toString()) }
        val numberOfDays = remember { mutableStateOf(7) }
        val zoneText = remember { mutableStateOf(TimeZone.currentSystemDefault().id) }
        val live = remember { mutableStateOf(false) }

        val startDay = runCatching { LocalDate.parse(startDayText.value.trim()) }.getOrNull()
        val zone = runCatching { TimeZone.of(zoneText.value.trim()) }.getOrNull()

        NestingCard {
            NestingCardHeader("Events")
            Row {
                EnumColumn(source.value, { source.value = it })
                Spacer(Modifier.width(16.dp))
                Column {
                    OutlinedTextField(startDayText.value, { startDayText.value = it }, singleLine = true,
                        label = { Text("Start day (YYYY-MM-DD)") }, isError = startDay == null,
                        modifier = Modifier.width(240.dp))
                    OutlinedTextField(zoneText.value, { zoneText.value = it }, singleLine = true,
                        label = { Text("Time zone (remote repository only)") }, isError = zone == null,
                        modifier = Modifier.width(320.dp))
                }
            }
            NumericTextViewWithSpinnerButtons(
                label = "Days",
                initialValue = numberOfDays.value,
                onValueChanged = { numberOfDays.value = it.coerceIn(1, 366) }
            )
            Text(
                when (source.value) {
                    EventSource.REMOTE_REPOSITORY -> "events(start of day 1, start of day 1 + days, zone)"
                    EventSource.WEEK_VIEW_REPO -> "getAgendaViewEvents(startDay, days) (system zone)"
                    EventSource.MONTH_VIEW_REPO -> "eventsByDay(startDay, startDay + days - 1) (system zone), by day"
                }
            )
            CheckBoxWithLabel(live.value, { live.value = it }, "Collect (live: re-emits when the bridge reports changes)")

            if (live.value && startDay != null && zone != null) {
                EventResults(source.value, startDay, numberOfDays.value, zone, onEventSelected)
            }
        }
    }

    /** One line per emission: what came back, grouped by day for the month repo. */
    private data class Emission(val number: Int, val at: Instant, val groups: List<Pair<String, List<AgendaCalendarEventData>>>)

    @Composable
    private fun EventResults(
        source: EventSource,
        startDay: LocalDate,
        numberOfDays: Int,
        zone: TimeZone,
        onEventSelected: (AgendaCalendarEventData) -> Unit,
    ) {
        val flow: Flow<List<Pair<String, List<AgendaCalendarEventData>>>> = remember(source, startDay, numberOfDays, zone) {
            when (source) {
                EventSource.REMOTE_REPOSITORY -> remoteCalendarRepository.events(
                    start = startDay.atStartOfDayIn(zone),
                    end = startDay.plus(numberOfDays, DateTimeUnit.DAY).atStartOfDayIn(zone),
                    zone = zone,
                ).map { listOf("All" to it) }
                EventSource.WEEK_VIEW_REPO -> weekViewRepo.getAgendaViewEvents(startDay, numberOfDays)
                    .map { listOf("All" to it) }
                EventSource.MONTH_VIEW_REPO -> monthViewRepo.eventsByDay(
                    firstDay = startDay,
                    lastDay = startDay.plus(numberOfDays - 1, DateTimeUnit.DAY),
                ).map { byDay -> byDay.entries.sortedBy { it.key }.map { (day, events) -> day.toString() to events } }
            }
        }
        val emission = remember(flow) { mutableStateOf<Emission?>(null) }
        LaunchedEffect(flow) {
            var count = 0
            flow.collect { emission.value = Emission(++count, Clock.System.now(), it) }
        }

        val current = emission.value
        if (current == null) {
            Text("Waiting for the first answer...")
            return
        }
        Text(
            "Emission #${current.number} at ${current.at.toLocalDateTime(TimeZone.currentSystemDefault()).time}: " +
                "${current.groups.sumOf { it.second.size }} events (click one for GetEvent)"
        )
        current.groups.forEach { (label, events) ->
            if (current.groups.size > 1 || label != "All") Text(label, fontSize = 13.sp)
            events.forEach { event ->
                EventRow(event, zone, onClick = { onEventSelected(event) })
            }
        }
    }

    @Composable
    private fun EventRow(event: AgendaCalendarEventData, zone: TimeZone, onClick: () -> Unit) {
        val start = event.start.toLocalDateTime(zone)
        val end = event.end.toLocalDateTime(zone)
        val ref = event.eventRef
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorSwatch(event.color)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    "${event.headerText}  ${start.date} ${start.time} → ${end.date} ${end.time}" +
                        if (event.allDayEvent) "  [all day]" else ""
                )
                if (ref != null) {
                    Text(
                        "event ${ref.eventId}, calendar ${ref.calendarId}, rev ${ref.revision}" +
                            (ref.recurrenceId?.let { ", recurrence $it" } ?: "") +
                            (if (ref.recurring) ", repeats" else "") +
                            (if (ref.readOnly) ", read-only" else ""),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }

    // --- GetEvent ----------------------------------------------------------

    @Composable
    private fun GetEventPane(selected: AgendaCalendarEventData?) {
        val eventId = remember { mutableStateOf("") }
        val recurrenceId = remember { mutableStateOf("") }
        val zoneText = remember { mutableStateOf(TimeZone.currentSystemDefault().id) }
        val result = remember { mutableStateOf<Result<String>?>(null) }
        val scope = rememberCoroutineScope()

        // Fill in from the clicked event.
        LaunchedEffect(selected) {
            selected?.eventRef?.let { ref ->
                eventId.value = ref.eventId
                recurrenceId.value = ref.recurrenceId?.toString() ?: ""
            }
        }

        NestingCard {
            NestingCardHeader("Event details (CalendarGrpcClient.getEvent)")
            Row {
                OutlinedTextField(eventId.value, { eventId.value = it }, label = { Text("event_id") }, singleLine = true,
                    modifier = Modifier.width(140.dp))
                OutlinedTextField(recurrenceId.value, { recurrenceId.value = it }, singleLine = true,
                    label = { Text("recurrence_id (ISO instant, optional)") }, modifier = Modifier.width(300.dp))
                OutlinedTextField(zoneText.value, { zoneText.value = it }, label = { Text("Time zone") }, singleLine = true,
                    modifier = Modifier.width(220.dp))
            }
            Button(onClick = {
                scope.launch {
                    result.value = runCatching {
                        val rid = recurrenceId.value.trim().takeIf { it.isNotEmpty() }?.let { Instant.parse(it) }
                        calendarGrpcClient.getEvent(eventId.value.trim(), rid, TimeZone.of(zoneText.value.trim())).toString()
                    }
                }
            }) { Text("getEvent()") }
            result.value?.fold(
                onSuccess = { text ->
                    SelectionContainer { Text(text, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    // --- Writing events ----------------------------------------------------

    /** Parses the time fields of the write pane; throws IllegalArgumentException with a message. */
    private fun parseTimes(allDay: Boolean, startText: String, endText: String, zoneText: String): EventTimes {
        return try {
            if (allDay) {
                EventTimes.AllDay(LocalDate.parse(startText.trim().take(10)), LocalDate.parse(endText.trim().take(10)))
            } else {
                val zone = TimeZone.of(zoneText.trim())
                EventTimes.Timed(
                    LocalDateTime.parse(startText.trim()).toInstant(zone),
                    LocalDateTime.parse(endText.trim()).toInstant(zone),
                    zone,
                )
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't read the times: ${e.message}", e)
        }
    }

    @Composable
    private fun WriteEventPane(selected: AgendaCalendarEventData?, onSaved: (AgendaCalendarEventData) -> Unit) {
        val scope = rememberCoroutineScope()
        val summary = remember { mutableStateOf("Debug event") }
        val location = remember { mutableStateOf("") }
        val allDay = remember { mutableStateOf(false) }
        val startText = remember { mutableStateOf("2026-11-16T10:00") }
        val endText = remember { mutableStateOf("2026-11-16T11:00") }
        val zoneText = remember { mutableStateOf(TimeZone.currentSystemDefault().id) }
        val calendarId = remember { mutableStateOf("") }
        val eventScope = remember { mutableStateOf(EventScope.THIS_OCCURRENCE) }
        val changeName = remember { mutableStateOf(true) }
        val changeLocation = remember { mutableStateOf(false) }
        val changeTimes = remember { mutableStateOf(false) }
        val outcome = remember { mutableStateOf("") }

        fun report(what: String, result: Result<*>) {
            outcome.value = result.fold(
                onSuccess = { "$what: OK" + if (it == null || it == Unit) "" else " $it" },
                onFailure = { "$what failed: ${it.describe()}" }
            )
        }

        NestingCard {
            NestingCardHeader("Write events (RemoteCalendarRepository, WeekViewRepo)")
            Row {
                OutlinedTextField(summary.value, { summary.value = it }, label = { Text("Summary") }, singleLine = true,
                    modifier = Modifier.width(260.dp))
                OutlinedTextField(location.value, { location.value = it }, label = { Text("Location") }, singleLine = true,
                    modifier = Modifier.width(200.dp))
                OutlinedTextField(calendarId.value, { calendarId.value = it }, singleLine = true,
                    label = { Text("Calendar ID (empty: default)") }, modifier = Modifier.width(220.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckBoxWithLabel(allDay.value, { allDay.value = it }, "All day")
                OutlinedTextField(startText.value, { startText.value = it }, singleLine = true,
                    label = { Text(if (allDay.value) "First day" else "Start (local)") }, modifier = Modifier.width(200.dp))
                OutlinedTextField(endText.value, { endText.value = it }, singleLine = true,
                    label = { Text(if (allDay.value) "End day (exclusive)" else "End (local)") }, modifier = Modifier.width(200.dp))
                OutlinedTextField(zoneText.value, { zoneText.value = it }, singleLine = true,
                    label = { Text("Zone") }, modifier = Modifier.width(200.dp), enabled = !allDay.value)
            }

            NestingCard {
                NestingCardHeader("New event")
                Button(onClick = {
                    scope.launch {
                        val result = runCatching { parseTimes(allDay.value, startText.value, endText.value, zoneText.value) }
                            .fold(
                                onSuccess = { times ->
                                    remoteCalendarRepository.createEvent(
                                        summary = summary.value,
                                        times = times,
                                        location = location.value,
                                        calendarId = calendarId.value.trim().ifEmpty { null },
                                    )
                                },
                                onFailure = { Result.failure(it) }
                            )
                        result.onSuccess(onSaved)
                        report("createEvent", result.map { it.headerText })
                    }
                }) { Text("createEvent()") }
            }

            NestingCard {
                NestingCardHeader("Selected event")
                val ref = selected?.eventRef
                Text("Selected: ${selected?.headerText ?: "(click an event in Events)"}" +
                    (ref?.let { " — event ${it.eventId}, rev ${it.revision}" + if (it.recurring) ", repeats" else "" } ?: ""))
                EnumColumn(eventScope.value, { eventScope.value = it })
                Row {
                    CheckBoxWithLabel(changeName.value, { changeName.value = it }, "summary")
                    CheckBoxWithLabel(changeLocation.value, { changeLocation.value = it }, "location")
                    CheckBoxWithLabel(changeTimes.value, { changeTimes.value = it }, "times (from the fields above)")
                }
                Row {
                    Button(enabled = ref != null, onClick = {
                        if (ref == null) return@Button
                        scope.launch {
                            val result = runCatching {
                                if (changeTimes.value) parseTimes(allDay.value, startText.value, endText.value, zoneText.value) else null
                            }.fold(
                                onSuccess = { times ->
                                    remoteCalendarRepository.updateEvent(
                                        ref = ref,
                                        scope = eventScope.value,
                                        summary = summary.value.takeIf { changeName.value },
                                        location = location.value.takeIf { changeLocation.value },
                                        times = times,
                                    )
                                },
                                onFailure = { Result.failure(it) }
                            )
                            result.onSuccess(onSaved)
                            report("updateEvent", result.map { it.headerText })
                        }
                    }) { Text("updateEvent()") }
                    Spacer(Modifier.width(8.dp))
                    Button(enabled = ref != null, onClick = {
                        if (ref == null) return@Button
                        scope.launch { report("deleteEvent", remoteCalendarRepository.deleteEvent(ref, eventScope.value)) }
                    }) { Text("deleteEvent()") }
                    Spacer(Modifier.width(8.dp))
                    Button(enabled = selected != null, onClick = {
                        if (selected == null) return@Button
                        scope.launch {
                            val result = weekViewRepo.saveEditedEvent(selected, selected.copy(headerText = summary.value))
                            result.onSuccess(onSaved)
                            report("saveEditedEvent (rename)", result.map { it.headerText })
                        }
                    }) { Text("WeekViewRepo.saveEditedEvent(rename)") }
                }
                Text("Revisions come from the selection: after a change, click the event again (or use the event returned) before the next one.", fontSize = 11.sp)
            }
            if (outcome.value.isNotEmpty()) {
                Text(outcome.value, color = if (outcome.value.contains("failed")) Color.Red else Color.Unspecified)
            }
        }
    }

    // --- To-dos ------------------------------------------------------------------

    @Composable
    private fun TodosPane() {
        val scope = rememberCoroutineScope()
        val live = remember { mutableStateOf(false) }
        val includeCompleted = remember { mutableStateOf(true) }
        val selected = remember { mutableStateOf<TodoItem?>(null) }
        val summary = remember { mutableStateOf("Debug to-do") }
        val dueDateText = remember { mutableStateOf("") }
        val priority = remember { mutableStateOf(0) }
        val outcome = remember { mutableStateOf("") }

        fun report(what: String, result: Result<*>) {
            outcome.value = result.fold(onSuccess = { "$what: OK" }, onFailure = { "$what failed: ${it.describe()}" })
        }
        fun dueDate(): LocalDate? = dueDateText.value.trim().takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }

        NestingCard {
            NestingCardHeader("To-dos (RemoteTodoRepository)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckBoxWithLabel(live.value, { live.value = it }, "Collect todos() (live)")
                CheckBoxWithLabel(includeCompleted.value, { includeCompleted.value = it }, "include completed")
            }
            if (live.value) {
                val todos = remember(includeCompleted.value) { todoRepository.todos(includeCompleted.value) }
                    .collectAsState(null).value
                if (todos == null) Text("Waiting for the first answer...")
                todos?.forEach { todo ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selected.value = todo }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorSwatch(todo.color)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            (if (todo.completed) "[X] " else "[ ] ") + todo.summary +
                                (todo.dueDate?.let { "  due $it" } ?: todo.due?.let { "  due $it" } ?: "") +
                                (if (todo.priority > 0) "  p${todo.priority}" else "") +
                                "  (id ${todo.id}, rev ${todo.revision})",
                            fontSize = 13.sp,
                        )
                    }
                }
            }
            Row {
                OutlinedTextField(summary.value, { summary.value = it }, label = { Text("Summary") }, singleLine = true,
                    modifier = Modifier.width(220.dp))
                OutlinedTextField(dueDateText.value, { dueDateText.value = it }, label = { Text("Due day (YYYY-MM-DD)") },
                    singleLine = true, modifier = Modifier.width(180.dp))
            }
            NumericTextViewWithSpinnerButtons(label = "Priority", initialValue = priority.value,
                onValueChanged = { priority.value = it.coerceIn(0, 9) })
            Button(onClick = {
                scope.launch {
                    report("createTodo", runCatching { dueDate() }.fold(
                        onSuccess = { todoRepository.createTodo(summary.value, dueDate = it, priority = priority.value) },
                        onFailure = { Result.failure(it) }
                    ))
                }
            }) { Text("createTodo()") }

            val todo = selected.value
            Text("Selected: ${todo?.let { "${it.summary} (rev ${it.revision})" } ?: "(click a to-do)"}")
            Row {
                Button(enabled = todo != null, onClick = {
                    if (todo == null) return@Button
                    scope.launch {
                        val result = todoRepository.setCompleted(todo, !todo.completed)
                        result.onSuccess { selected.value = it }
                        report("setCompleted(${!todo.completed})", result)
                    }
                }) { Text("setCompleted(toggle)") }
                Spacer(Modifier.width(6.dp))
                Button(enabled = todo != null, onClick = {
                    if (todo == null) return@Button
                    scope.launch {
                        val result = todoRepository.updateTodo(todo, summary = summary.value, priority = priority.value)
                        result.onSuccess { selected.value = it }
                        report("updateTodo(summary, priority)", result)
                    }
                }) { Text("updateTodo()") }
                Spacer(Modifier.width(6.dp))
                Button(enabled = todo != null, onClick = {
                    if (todo == null) return@Button
                    scope.launch {
                        val result = runCatching { dueDate() }.fold(
                            onSuccess = { todoRepository.updateTodo(todo, changeDue = true, dueDate = it) },
                            onFailure = { Result.failure(it) }
                        )
                        result.onSuccess { selected.value = it }
                        report("updateTodo(due)", result)
                    }
                }) { Text("set due day") }
                Spacer(Modifier.width(6.dp))
                Button(enabled = todo != null, onClick = {
                    if (todo == null) return@Button
                    scope.launch {
                        val result = todoRepository.deleteTodo(todo)
                        result.onSuccess { selected.value = null }
                        report("deleteTodo", result)
                    }
                }) { Text("deleteTodo()") }
            }
            if (outcome.value.isNotEmpty()) {
                Text(outcome.value, color = if (outcome.value.contains("failed")) Color.Red else Color.Unspecified)
            }
        }
    }

    // --- Helpers -------------------------------------------------------------

    @Composable
    private fun ColorSwatch(color: Color) {
        Box(Modifier.size(14.dp).background(color))
    }

    private fun Throwable.describe(): String = when (this) {
        is PimBackendException -> "${this::class.simpleName}: $message"
        else -> PimBackendException.from(this).let { "${it::class.simpleName}: ${it.message}" }
    }
}
