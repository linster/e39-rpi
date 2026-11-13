package ca.stefanm.ibus.gui.pim.calendar.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.repo.api.TodoItem
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteTodoRepository
import ca.stefanm.ibus.gui.pim.calendar.views.editor.TodoItemEditorScreen
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock

@ScreenDoc(
    screenName = "TodoListScreen",
    description = "Todo list view",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class TodoListScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val calendarOptionsMenu: CalendarOptionsMenu,
    private val todoRepository: RemoteTodoRepository,
    private val notificationHub: NotificationHub,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "TodoListScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = TodoListScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val showCompleted = remember { mutableStateOf(false) }
        // Remembered: each call queries the PIM bridge and watches it for changes.
        val todos = remember(showCompleted.value) { todoRepository.todos(showCompleted.value) }
            .collectAsState(null).value
        val scope = rememberCoroutineScope()

        val items: List<MenuItem> = buildList {
            add(TextMenuItem("New To-do...", onClicked = { newTodo(scope) }))
            add(CheckBoxMenuItem(
                title = "Show completed",
                isChecked = showCompleted.value,
                onCheckChanged = { showCompleted.value = it }
            ))
            when {
                todos == null -> add(TextMenuItem("Loading...", isSelectable = false, onClicked = {}))
                todos.isEmpty() -> add(TextMenuItem("Nothing to do", isSelectable = false, onClicked = {}))
                else -> todos.forEach { todo ->
                    add(CheckBoxMenuItem(
                        title = label(todo),
                        isChecked = todo.completed,
                        labelColor = todo.color,
                        onCheckChanged = { checked -> setCompleted(scope, todo, checked) },
                        onClicked = { showTodoPane(scope, todo) },
                    ))
                }
            }
        }

        // The number of entries changes as to-dos are added and completed;
        // SmoothScroll allocates knob positions once, so rebuild it then.
        key(items.size) {
            SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreen(
                header = "To-do List",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    /** "Fri Nov 20", "Wed Nov 25 11:00", "... (late)" when overdue; null without a due date. */
    private fun dueLabel(todo: TodoItem): String? {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        val dueDay = todo.dueDate ?: todo.due?.toLocalDateTime(zone)?.date
        return dueDay?.let { day ->
            val weekday = day.dayOfWeek.toJavaDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val month = day.month.toJavaMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val time = todo.due?.toLocalDateTime(zone)?.time?.let { " %02d:%02d".format(it.hour, it.minute) } ?: ""
            "$weekday $month ${day.day}$time" + if (!todo.completed && day < today) " (late)" else ""
        }
    }

    /** "Buy wiper blades  !  Fri Nov 20": priority 1–3 gets a "!", overdue a "late". */
    private fun label(todo: TodoItem): String {
        val due = dueLabel(todo)
        return buildString {
            append(todo.summary)
            if (todo.priority in 1..3) append("  !")
            if (due != null) append("  $due")
            if (todo.recurring) append("  ↻")
        }
    }

    /**
     * The to-do as of when it was clicked, with what can be done with it. The
     * pane closes after an action (the list shows the result).
     */
    private fun showTodoPane(scope: CoroutineScope, todo: TodoItem) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "To-do",
                text = @Composable {
                    InfoLabel((if (todo.completed) "[X] " else "[ ] ") + todo.summary, FontWeight.Bold)
                    dueLabel(todo)?.let { InfoLabel("Due: $it") }
                    if (todo.priority > 0) InfoLabel("Priority: ${todo.priority} (1 = highest)")
                    if (todo.recurring) InfoLabel("Repeats")
                    if (todo.description.isNotBlank()) InfoLabel(todo.description)
                },
                buttons = listOf(
                    TextMenuItem("Toggle ticked", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        setCompleted(scope, todo, !todo.completed)
                    }),
                    TextMenuItem("Edit...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        TodoItemEditorScreen.editTodo(navigationNodeTraverser, todo)
                    }),
                    TextMenuItem("Go back", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    }),
                )
            )
        }
    }

    private fun setCompleted(scope: CoroutineScope, todo: TodoItem, completed: Boolean) {
        scope.launch {
            todoRepository.setCompleted(todo, completed).onFailure { e -> notifyFailure("Not changed", e) }
        }
    }

    private fun newTodo(scope: CoroutineScope) {
        modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, "") { entered ->
            val summary = entered.trim()
            if (summary.isEmpty()) return@showKeyboard
            scope.launch {
                todoRepository.createTodo(summary).fold(
                    onSuccess = {
                        notificationHub.postNotificationBackground(
                            Notification(Notification.NotificationImage.MESSAGE_CIRCLE, "To-do added", summary)
                        )
                    },
                    onFailure = { e -> notifyFailure("Not added", e) }
                )
            }
        }
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notificationHub.postNotificationBackground(
            Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                top,
                when (e) {
                    is PimBackendException.Conflict -> "It was changed elsewhere meanwhile; try again."
                    is PimBackendException.Unavailable -> "Calendar not reachable: ${e.message}"
                    else -> e.message ?: e.toString()
                }
            )
        )
    }
}
