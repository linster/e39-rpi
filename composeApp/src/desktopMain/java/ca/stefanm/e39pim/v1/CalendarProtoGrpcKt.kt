package ca.stefanm.e39pim.v1

import ca.stefanm.e39pim.v1.CalendarServiceGrpc.getServiceDescriptor
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
import io.grpc.kotlin.ClientCalls.serverStreamingRpc
import io.grpc.kotlin.ClientCalls.unaryRpc
import io.grpc.kotlin.ServerCalls.serverStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.unaryServerMethodDefinition
import io.grpc.kotlin.StubFor
import kotlin.String
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

/**
 * Holder for Kotlin coroutine-based client and server APIs for e39pim.v1.CalendarService.
 */
public object CalendarServiceGrpcKt {
  public const val SERVICE_NAME: String = CalendarServiceGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val listCalendarsMethod: MethodDescriptor<ListCalendarsRequest, ListCalendarsResponse>
    @JvmStatic
    get() = CalendarServiceGrpc.getListCalendarsMethod()

  public val listEventsMethod: MethodDescriptor<ListEventsRequest, ListEventsResponse>
    @JvmStatic
    get() = CalendarServiceGrpc.getListEventsMethod()

  public val getEventMethod: MethodDescriptor<GetEventRequest, Event>
    @JvmStatic
    get() = CalendarServiceGrpc.getGetEventMethod()

  public val createEventMethod: MethodDescriptor<CreateEventRequest, Event>
    @JvmStatic
    get() = CalendarServiceGrpc.getCreateEventMethod()

  public val updateEventMethod: MethodDescriptor<UpdateEventRequest, Event>
    @JvmStatic
    get() = CalendarServiceGrpc.getUpdateEventMethod()

  public val deleteEventMethod: MethodDescriptor<DeleteEventRequest, Empty>
    @JvmStatic
    get() = CalendarServiceGrpc.getDeleteEventMethod()

  public val watchChangesMethod: MethodDescriptor<WatchChangesRequest, ChangeNotice>
    @JvmStatic
    get() = CalendarServiceGrpc.getWatchChangesMethod()

  /**
   * A stub for issuing RPCs to a(n) e39pim.v1.CalendarService service as suspending coroutines.
   */
  @StubFor(CalendarServiceGrpc::class)
  public class CalendarServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<CalendarServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): CalendarServiceCoroutineStub = CalendarServiceCoroutineStub(channel, callOptions)

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
    public suspend fun listCalendars(request: ListCalendarsRequest, headers: Metadata = Metadata()): ListCalendarsResponse = unaryRpc(
      channel,
      CalendarServiceGrpc.getListCalendarsMethod(),
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
    public suspend fun listEvents(request: ListEventsRequest, headers: Metadata = Metadata()): ListEventsResponse = unaryRpc(
      channel,
      CalendarServiceGrpc.getListEventsMethod(),
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
    public suspend fun getEvent(request: GetEventRequest, headers: Metadata = Metadata()): Event = unaryRpc(
      channel,
      CalendarServiceGrpc.getGetEventMethod(),
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
    public suspend fun createEvent(request: CreateEventRequest, headers: Metadata = Metadata()): Event = unaryRpc(
      channel,
      CalendarServiceGrpc.getCreateEventMethod(),
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
    public suspend fun updateEvent(request: UpdateEventRequest, headers: Metadata = Metadata()): Event = unaryRpc(
      channel,
      CalendarServiceGrpc.getUpdateEventMethod(),
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
    public suspend fun deleteEvent(request: DeleteEventRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      CalendarServiceGrpc.getDeleteEventMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][io.grpc.Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun watchChanges(request: WatchChangesRequest, headers: Metadata = Metadata()): Flow<ChangeNotice> = serverStreamingRpc(
      channel,
      CalendarServiceGrpc.getWatchChangesMethod(),
      request,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the e39pim.v1.CalendarService service based on Kotlin coroutines.
   */
  public abstract class CalendarServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.ListCalendars.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listCalendars(request: ListCalendarsRequest): ListCalendarsResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.ListCalendars is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.ListEvents.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listEvents(request: ListEventsRequest): ListEventsResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.ListEvents is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.GetEvent.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun getEvent(request: GetEventRequest): Event = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.GetEvent is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.CreateEvent.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun createEvent(request: CreateEventRequest): Event = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.CreateEvent is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.UpdateEvent.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun updateEvent(request: UpdateEventRequest): Event = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.UpdateEvent is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.CalendarService.DeleteEvent.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun deleteEvent(request: DeleteEventRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.DeleteEvent is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for e39pim.v1.CalendarService.WatchChanges.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [io.grpc.Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open fun watchChanges(request: WatchChangesRequest): Flow<ChangeNotice> = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.CalendarService.WatchChanges is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getListCalendarsMethod(),
      implementation = ::listCalendars
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getListEventsMethod(),
      implementation = ::listEvents
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getGetEventMethod(),
      implementation = ::getEvent
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getCreateEventMethod(),
      implementation = ::createEvent
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getUpdateEventMethod(),
      implementation = ::updateEvent
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getDeleteEventMethod(),
      implementation = ::deleteEvent
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CalendarServiceGrpc.getWatchChangesMethod(),
      implementation = ::watchChanges
    )).build()
  }
}
