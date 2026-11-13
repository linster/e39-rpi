package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.Todo
import ca.stefanm.e39pim.v1.TodoServiceGrpcKt
import ca.stefanm.e39pim.v1.createTodoRequest
import ca.stefanm.e39pim.v1.deleteTodoRequest
import ca.stefanm.e39pim.v1.listTodosRequest
import ca.stefanm.e39pim.v1.setTodoCompletedRequest
import ca.stefanm.e39pim.v1.updateTodoRequest
import com.google.protobuf.fieldMask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for to-dos: e39pim.v1 TodoService calls, with
 * protobuf messages in and out. [RemoteTodoRepository] builds on it. Every
 * call throws [PimBackendException] on failure.
 */
class TodoGrpcClient @Inject constructor(
    private val backendChannel: PimBackendChannel,
) {
    private fun stub() = TodoServiceGrpcKt.TodoServiceCoroutineStub(backendChannel.channel())
        .withDeadlineAfter(PimBackendChannel.CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    /** Not completed first, then by due date, priority, summary. */
    suspend fun listTodos(includeCompleted: Boolean, calendarIds: List<String> = emptyList()): List<Todo> = pimCall {
        stub().listTodos(listTodosRequest {
            this.includeCompleted = includeCompleted
            this.calendarIds.addAll(calendarIds)
        }).todosList
    }

    suspend fun createTodo(calendarId: String, todo: Todo): Todo = pimCall {
        stub().createTodo(createTodoRequest {
            this.calendarId = calendarId
            this.todo = todo
        })
    }

    /** Changes the [paths] of todo.todoId based on todo.revision; empty: all editable fields. */
    suspend fun updateTodo(todo: Todo, paths: Collection<String>): Todo = pimCall {
        stub().updateTodo(updateTodoRequest {
            this.todo = todo
            if (paths.isNotEmpty()) updateMask = fieldMask { this.paths.addAll(paths) }
        })
    }

    suspend fun setTodoCompleted(todoId: String, revision: Long, completed: Boolean): Todo = pimCall {
        stub().setTodoCompleted(setTodoCompletedRequest {
            this.todoId = todoId
            this.revision = revision
            this.completed = completed
        })
    }

    suspend fun deleteTodo(todoId: String, revision: Long) {
        pimCall {
            stub().deleteTodo(deleteTodoRequest {
                this.todoId = todoId
                this.revision = revision
            })
        }
    }
}
