package ca.stefanm.e39pim.v1

import ca.stefanm.e39pim.v1.TodoServiceGrpc.getServiceDescriptor
import com.google.protobuf.Empty
import io.grpc.CallOptions
import io.grpc.CallOptions.DEFAULT
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerServiceDefinition.builder
import io.grpc.ServiceDescriptor
import io.grpc.Status.UNIMPLEMENTED
import io.grpc.StatusException
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.AbstractCoroutineStub
import io.grpc.kotlin.ClientCalls.unaryRpc
import io.grpc.kotlin.ServerCalls.unaryServerMethodDefinition
import io.grpc.kotlin.StubFor
import kotlin.String
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Holder for Kotlin coroutine-based client and server APIs for e39pim.v1.TodoService.
 */
public object TodoServiceGrpcKt {
  public const val SERVICE_NAME: String = TodoServiceGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val listTodosMethod: MethodDescriptor<ListTodosRequest, ListTodosResponse>
    @JvmStatic
    get() = TodoServiceGrpc.getListTodosMethod()

  public val createTodoMethod: MethodDescriptor<CreateTodoRequest, Todo>
    @JvmStatic
    get() = TodoServiceGrpc.getCreateTodoMethod()

  public val updateTodoMethod: MethodDescriptor<UpdateTodoRequest, Todo>
    @JvmStatic
    get() = TodoServiceGrpc.getUpdateTodoMethod()

  public val setTodoCompletedMethod: MethodDescriptor<SetTodoCompletedRequest, Todo>
    @JvmStatic
    get() = TodoServiceGrpc.getSetTodoCompletedMethod()

  public val deleteTodoMethod: MethodDescriptor<DeleteTodoRequest, Empty>
    @JvmStatic
    get() = TodoServiceGrpc.getDeleteTodoMethod()

  /**
   * A stub for issuing RPCs to a(n) e39pim.v1.TodoService service as suspending coroutines.
   */
  @StubFor(TodoServiceGrpc::class)
  public class TodoServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<TodoServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): TodoServiceCoroutineStub = TodoServiceCoroutineStub(channel, callOptions)

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun listTodos(request: ListTodosRequest, headers: Metadata = Metadata()): ListTodosResponse = unaryRpc(
      channel,
      TodoServiceGrpc.getListTodosMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun createTodo(request: CreateTodoRequest, headers: Metadata = Metadata()): Todo = unaryRpc(
      channel,
      TodoServiceGrpc.getCreateTodoMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun updateTodo(request: UpdateTodoRequest, headers: Metadata = Metadata()): Todo = unaryRpc(
      channel,
      TodoServiceGrpc.getUpdateTodoMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun setTodoCompleted(request: SetTodoCompletedRequest, headers: Metadata = Metadata()): Todo = unaryRpc(
      channel,
      TodoServiceGrpc.getSetTodoCompletedMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][io.grpc.Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return The single response from the server.
     */
    public suspend fun deleteTodo(request: DeleteTodoRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      TodoServiceGrpc.getDeleteTodoMethod(),
      request,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the e39pim.v1.TodoService service based on Kotlin coroutines.
   */
  public abstract class TodoServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for e39pim.v1.TodoService.ListTodos.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listTodos(request: ListTodosRequest): ListTodosResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.TodoService.ListTodos is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.TodoService.CreateTodo.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun createTodo(request: CreateTodoRequest): Todo = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.TodoService.CreateTodo is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.TodoService.UpdateTodo.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun updateTodo(request: UpdateTodoRequest): Todo = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.TodoService.UpdateTodo is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.TodoService.SetTodoCompleted.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun setTodoCompleted(request: SetTodoCompletedRequest): Todo = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.TodoService.SetTodoCompleted is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.TodoService.DeleteTodo.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun deleteTodo(request: DeleteTodoRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.TodoService.DeleteTodo is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = TodoServiceGrpc.getListTodosMethod(),
      implementation = ::listTodos
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = TodoServiceGrpc.getCreateTodoMethod(),
      implementation = ::createTodo
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = TodoServiceGrpc.getUpdateTodoMethod(),
      implementation = ::updateTodo
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = TodoServiceGrpc.getSetTodoCompletedMethod(),
      implementation = ::setTodoCompleted
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = TodoServiceGrpc.getDeleteTodoMethod(),
      implementation = ::deleteTodo
    )).build()
  }
}
