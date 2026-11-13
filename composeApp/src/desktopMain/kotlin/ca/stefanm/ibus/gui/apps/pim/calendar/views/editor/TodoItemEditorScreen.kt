package ca.stefanm.ibus.gui.pim.calendar.views.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.repo.api.TodoItem
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteTodoRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Clock

/**
 * Creates or edits a to-do in the PIM bridge: summary, due date (a day, or a
 * day and time, or none), priority, notes, done. The current values are in
 * the top half; the knob menu below changes them, and nothing is written
 * until Save. Existing to-dos can also be deleted.
 */
@ScreenDoc(
    screenName = "TodoItemEditorScreen",
    description = "Create or edit a to-do",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class TodoItemEditorScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val todoRepository: RemoteTodoRepository,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "TodoItemEditorScreen"

        /** Opens the editor for [todo]. Without parameters it's for a new to-do. */
        fun editTodo(navigationNodeTraverser: NavigationNodeTraverser, todo: TodoItem) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                TodoItemEditorScreen::class.java,
                OpenMode.EditExisting(todo)
            )
        }

        /** Default time when a time is added to a due day. */
        val DEFAULT_DUE_TIME = LocalTime(17, 0)
    }

    sealed interface OpenMode {
        data object NewTodo : OpenMode
        data class EditExisting(val todo: TodoItem) : OpenMode
    }

    /** The to-do being edited, as it will be saved. */
    private data class Draft(
        val summary: String = "",
        val dueDay: LocalDate? = null,
        /** With [dueDay]: due at that time; without: due that day. */
        val dueTime: LocalTime? = null,
        val priority: Int = 0,
        val description: String = "",
        val completed: Boolean = false,
    ) {
        companion object {
            fun of(todo: TodoItem, zone: TimeZone): Draft {
                val dueAt = todo.due?.toLocalDateTime(zone)
                return Draft(
                    summary = todo.summary,
                    dueDay = todo.dueDate ?: dueAt?.date,
                    dueTime = dueAt?.time,
                    priority = todo.priority,
                    description = todo.description,
                    completed = todo.completed,
                )
            }
        }
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val mode = params?.requestParameters as? OpenMode ?: OpenMode.NewTodo
        val original = (mode as? OpenMode.EditExisting)?.todo
        val zone = remember { TimeZone.currentSystemDefault() }
        val draft = remember(mode) { mutableStateOf(original?.let { Draft.of(it, zone) } ?: Draft()) }
        val saving = remember(mode) { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader(if (original == null) "New To-do" else "Edit To-do")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                summaryLines(draft.value, original).forEach { line ->
                    TextMenuItem(
                        title = line,
                        isSelectable = false,
                        onClicked = {}
                    ).toView(Modifier.fillMaxWidth(), ItemChipOrientation.NONE)()
                }

                Column(Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    // TwoColumn keeps the items of its first composition, so
                    // rebuild it when a label changes.
                    key(draft.value.completed, saving.value) {
                        HalfScreenMenu.TwoColumn(
                            leftItems = listOf(
                                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                                TextMenuItem("Summary...", onClicked = { editSummary(draft) }),
                                TextMenuItem("Due...", onClicked = { showDuePane(draft) }),
                                TextMenuItem("Priority...", onClicked = { editPriority(draft) }),
                            ),
                            rightItems = buildList<MenuItem> {
                                add(TextMenuItem("Notes...", onClicked = { editNotes(draft) }))
                                add(TextMenuItem(
                                    if (draft.value.completed) "Mark Not Done" else "Mark Done",
                                    onClicked = { draft.value = draft.value.copy(completed = !draft.value.completed) }
                                ))
                                if (original != null) {
                                    add(TextMenuItem("Delete...", onClicked = { confirmDelete(scope, original, saving) }))
                                }
                                add(TextMenuItem(
                                    if (saving.value) "Saving..." else "Save",
                                    onClicked = { if (!saving.value) save(scope, original, draft.value, zone, saving) }
                                ))
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Display -----------------------------------------------------------

    private fun dayLabel(day: LocalDate): String {
        val weekday = day.dayOfWeek.toJavaDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val month = day.month.toJavaMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$weekday $month ${day.day} ${day.year}"
    }

    private fun dueLabel(draft: Draft): String = when {
        draft.dueDay == null -> "none"
        draft.dueTime == null -> dayLabel(draft.dueDay)
        else -> "${dayLabel(draft.dueDay)}, %02d:%02d".format(draft.dueTime.hour, draft.dueTime.minute)
    }

    private fun priorityLabel(priority: Int): String = when (priority) {
        0 -> "none"
        1 -> "1 (highest)"
        9 -> "9 (lowest)"
        else -> priority.toString()
    }

    private fun summaryLines(draft: Draft, original: TodoItem?): List<String> = buildList {
        add((if (draft.completed) "[X] " else "[ ] ") + draft.summary.ifBlank { "(no summary yet)" })
        add("Due: ${dueLabel(draft)}")
        add("Priority: ${priorityLabel(draft.priority)}" + if (original?.recurring == true) "   Repeats" else "")
        if (draft.description.isNotBlank()) {
            add("Notes: ${draft.description.lines().first()}")
        }
    }

    // --- Changing the draft --------------------------------------------------

    private fun editSummary(draft: MutableState<Draft>) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, draft.value.summary) { entered ->
            draft.value = draft.value.copy(summary = entered.trim())
        }
    }

    private fun editNotes(draft: MutableState<Draft>) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, draft.value.description) { entered ->
            draft.value = draft.value.copy(description = entered.trim())
        }
    }

    private fun editPriority(draft: MutableState<Draft>) {
        modalMenuService.showIntSlider(
            initialValue = draft.value.priority,
            validItems = 0..9,
            onCurrentValueChanged = { draft.value = draft.value.copy(priority = it) },
            hintText = "Priority: 0 = none, 1 = highest, 9 = lowest",
        )
    }

    /** Due day, time, all day, or no due date. */
    private fun showDuePane(draft: MutableState<Draft>) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            val current = draft.value
            SidePanelMenu.SidePanelMenu(
                title = "Due",
                text = @Composable {
                    InfoLabel(dueLabel(current), FontWeight.Bold)
                },
                buttons = buildList<MenuItem> {
                    add(TextMenuItem("Pick Day...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        modalMenuService.showDayPicker((current.dueDay ?: today).yearMonth) { day ->
                            draft.value = draft.value.copy(dueDay = day)
                        }
                    }))
                    add(TextMenuItem(if (current.dueTime == null) "Add Time..." else "Change Time...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        modalMenuService.showTimePicker(current.dueTime ?: DEFAULT_DUE_TIME) { time ->
                            // A time needs a day: today if none is set yet.
                            draft.value = draft.value.copy(dueDay = draft.value.dueDay ?: today, dueTime = time)
                        }
                    }))
                    if (current.dueTime != null) {
                        add(TextMenuItem("All Day (No Time)", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            draft.value = draft.value.copy(dueTime = null)
                        }))
                    }
                    if (current.dueDay != null) {
                        add(TextMenuItem("No Due Date", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            draft.value = draft.value.copy(dueDay = null, dueTime = null)
                        }))
                    }
                    add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    // --- Writing -------------------------------------------------------------

    private fun save(scope: CoroutineScope, original: TodoItem?, draft: Draft, zone: TimeZone, saving: MutableState<Boolean>) {
        if (draft.summary.isBlank()) {
            notify(Notification.NotificationImage.ALERT_TRIANGLE, "Not saved", "The to-do needs a summary.")
            return
        }
        val due = draft.dueDay?.let { day -> draft.dueTime?.let { LocalDateTime(day, it).toInstant(zone) } }
        val dueDate = draft.dueDay.takeIf { draft.dueTime == null }

        saving.value = true
        scope.launch {
            val result = if (original == null) {
                createTodo(draft, due, dueDate)
            } else {
                updateTodo(original, draft, zone, due, dueDate)
            }
            saving.value = false
            result.fold(
                onSuccess = { saved ->
                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, "To-do saved", saved.summary)
                    navigationNodeTraverser.goBack()
                },
                onFailure = { e -> notifyFailure("Not saved", e) }
            )
        }
    }

    private suspend fun createTodo(draft: Draft, due: kotlin.time.Instant?, dueDate: LocalDate?): Result<TodoItem> {
        val created = todoRepository.createTodo(
            summary = draft.summary,
            due = due,
            dueDate = dueDate,
            priority = draft.priority,
            description = draft.description,
        )
        // Created open; mark it done afterwards if asked to.
        return if (draft.completed) created.mapCatching { todoRepository.setCompleted(it, true).getOrThrow() } else created
    }

    /** Sends only what changed; completion last (it needs the newest revision). */
    private suspend fun updateTodo(
        original: TodoItem,
        draft: Draft,
        zone: TimeZone,
        due: kotlin.time.Instant?,
        dueDate: LocalDate?,
    ): Result<TodoItem> {
        val before = Draft.of(original, zone)
        val dueChanged = draft.dueDay != before.dueDay || draft.dueTime != before.dueTime
        val fieldsChanged = draft.summary != before.summary || draft.description != before.description ||
            draft.priority != before.priority || dueChanged

        var current = original
        if (fieldsChanged) {
            current = todoRepository.updateTodo(
                item = current,
                summary = draft.summary.takeIf { it != before.summary },
                description = draft.description.takeIf { it != before.description },
                priority = draft.priority.takeIf { it != before.priority },
                changeDue = dueChanged,
                due = due,
                dueDate = dueDate,
            ).getOrElse { return Result.failure(it) }
        }
        if (draft.completed != current.completed) {
            current = todoRepository.setCompleted(current, draft.completed).getOrElse { return Result.failure(it) }
        }
        return Result.success(current)
    }

    private fun confirmDelete(scope: CoroutineScope, todo: TodoItem, saving: MutableState<Boolean>) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Delete To-do?",
                text = @Composable {
                    InfoLabel(todo.summary, FontWeight.Bold)
                    if (todo.recurring) InfoLabel("All its repetitions are deleted too.")
                },
                buttons = listOf(
                    TextMenuItem("Keep It", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                    TextMenuItem("Delete", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        saving.value = true
                        scope.launch {
                            val result = todoRepository.deleteTodo(todo)
                            saving.value = false
                            result.fold(
                                onSuccess = {
                                    notify(Notification.NotificationImage.MESSAGE_CIRCLE, "To-do deleted", todo.summary)
                                    navigationNodeTraverser.goBack()
                                },
                                onFailure = { e -> notifyFailure("Not deleted", e) }
                            )
                        }
                    }),
                )
            )
        }
    }

    private fun notify(image: Notification.NotificationImage, top: String, content: String) {
        notificationHub.postNotificationBackground(Notification(image, top, content))
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
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
}
