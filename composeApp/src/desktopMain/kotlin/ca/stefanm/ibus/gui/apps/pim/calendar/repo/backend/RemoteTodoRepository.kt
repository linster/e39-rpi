package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.todo
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.api.TodoItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * To-dos from the PIM bridge, reached through [TodoGrpcClient]. [todos]
 * re-queries when the bridge reports to-do changes ([PimChangeStream]) and
 * retries every [RETRY_INTERVAL] while it's unreachable, keeping the last
 * list. Writes return [Result]s; [PimBackendException.Conflict] means the
 * to-do changed meanwhile (the list will update; try again from it).
 */
@ApplicationScope
class RemoteTodoRepository @Inject constructor(
    private val client: TodoGrpcClient,
    private val calendarRepository: RemoteCalendarRepository,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    private val changeStream: PimChangeStream,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "RemoteTodoRepository"
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    /** Not completed first, then by due date, priority, summary. Remember the flow in composables. */
    fun todos(includeCompleted: Boolean = false): Flow<List<TodoItem>> = channelFlow {
        val reloads = Channel<Unit>(Channel.CONFLATED)
        launch { changeStream.changes.collect { if (it.affectsTodos()) reloads.send(Unit) } }
        launch { settingsRepository.settings.drop(1).collect { reloads.send(Unit) } }
        reloads.send(Unit)

        var retry: Job? = null
        for (reload in reloads) {
            retry?.cancel()
            try {
                send(client.listTodos(includeCompleted).map { it.toTodoItem() })
            } catch (e: PimBackendException) {
                logger.w(TAG, "To-dos: ${e.message}")
                retry = launch {
                    delay(RETRY_INTERVAL)
                    backendChannel.resetConnectBackoff()
                    reloads.send(Unit)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Creates a to-do in [calendarId] (null: the first writable calendar
     * with to-dos). Due at [due] or on [dueDate] (not both), or neither.
     */
    suspend fun createTodo(
        summary: String,
        due: Instant? = null,
        dueDate: LocalDate? = null,
        priority: Int = 0,
        description: String = "",
        calendarId: String? = null,
    ): Result<TodoItem> = result {
        require(due == null || dueDate == null) { "due or dueDate, not both" }
        val calendar = calendarId ?: calendarRepository.calendars().getOrThrow()
            .firstOrNull { it.hasTodos && !it.readOnly }?.id
            ?: throw PimBackendException.FailedPrecondition("No writable calendar for to-dos (add an account first)", null)
        client.createTodo(calendar, todo {
            this.summary = summary
            this.description = description
            if (due != null) this.due = due.toTimestamp()
            if (dueDate != null) this.dueDate = dueDate.toProtoDate()
            this.priority = priority
        }).toTodoItem()
    }

    /**
     * Changes the given fields (null: unchanged) of [item], based on its
     * revision. [changeDue] with [due]/[dueDate] (both null: no due date).
     */
    suspend fun updateTodo(
        item: TodoItem,
        summary: String? = null,
        description: String? = null,
        priority: Int? = null,
        changeDue: Boolean = false,
        due: Instant? = null,
        dueDate: LocalDate? = null,
    ): Result<TodoItem> = result {
        require(due == null || dueDate == null) { "due or dueDate, not both" }
        val paths = buildList {
            if (summary != null) add("summary")
            if (description != null) add("description")
            if (priority != null) add("priority")
            if (changeDue) add("due")
        }
        require(paths.isNotEmpty()) { "Nothing to change" }
        client.updateTodo(todo {
            todoId = item.id
            revision = item.revision
            if (summary != null) this.summary = summary
            if (description != null) this.description = description
            if (priority != null) this.priority = priority
            if (due != null) this.due = due.toTimestamp()
            if (dueDate != null) this.dueDate = dueDate.toProtoDate()
        }, paths).toTodoItem()
    }

    /** Ticks/unticks [item]. Completing a repeating to-do moves it to its next occurrence. */
    suspend fun setCompleted(item: TodoItem, completed: Boolean): Result<TodoItem> = result {
        client.setTodoCompleted(item.id, item.revision, completed).toTodoItem()
    }

    suspend fun deleteTodo(item: TodoItem): Result<Unit> = result {
        client.deleteTodo(item.id, item.revision)
    }

    private suspend fun <T> result(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        logger.w(TAG, "PIM bridge call failed: ${e.message}")
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }
}
