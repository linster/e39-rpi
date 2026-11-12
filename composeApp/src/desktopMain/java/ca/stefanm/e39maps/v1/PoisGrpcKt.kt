package ca.stefanm.e39maps.v1

import ca.stefanm.e39maps.v1.PoiServiceGrpc.getServiceDescriptor
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
 * Holder for Kotlin coroutine-based client and server APIs for e39maps.v1.PoiService.
 */
public object PoiServiceGrpcKt {
  public const val SERVICE_NAME: String = PoiServiceGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val listPoisMethod: MethodDescriptor<ListPoisRequest, ListPoisResponse>
    @JvmStatic
    get() = PoiServiceGrpc.getListPoisMethod()

  public val getPoiMethod: MethodDescriptor<GetPoiRequest, Poi>
    @JvmStatic
    get() = PoiServiceGrpc.getGetPoiMethod()

  public val createPoiMethod: MethodDescriptor<CreatePoiRequest, Poi>
    @JvmStatic
    get() = PoiServiceGrpc.getCreatePoiMethod()

  public val updatePoiMethod: MethodDescriptor<UpdatePoiRequest, Poi>
    @JvmStatic
    get() = PoiServiceGrpc.getUpdatePoiMethod()

  public val deletePoiMethod: MethodDescriptor<DeletePoiRequest, Empty>
    @JvmStatic
    get() = PoiServiceGrpc.getDeletePoiMethod()

  public val searchPoisMethod: MethodDescriptor<SearchPoisRequest, SearchPoisResponse>
    @JvmStatic
    get() = PoiServiceGrpc.getSearchPoisMethod()

  /**
   * A stub for issuing RPCs to a(n) e39maps.v1.PoiService service as suspending coroutines.
   */
  @StubFor(PoiServiceGrpc::class)
  public class PoiServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<PoiServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): PoiServiceCoroutineStub = PoiServiceCoroutineStub(channel, callOptions)

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
    public suspend fun listPois(request: ListPoisRequest, headers: Metadata = Metadata()): ListPoisResponse = unaryRpc(
      channel,
      PoiServiceGrpc.getListPoisMethod(),
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
    public suspend fun getPoi(request: GetPoiRequest, headers: Metadata = Metadata()): Poi = unaryRpc(
      channel,
      PoiServiceGrpc.getGetPoiMethod(),
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
    public suspend fun createPoi(request: CreatePoiRequest, headers: Metadata = Metadata()): Poi = unaryRpc(
      channel,
      PoiServiceGrpc.getCreatePoiMethod(),
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
    public suspend fun updatePoi(request: UpdatePoiRequest, headers: Metadata = Metadata()): Poi = unaryRpc(
      channel,
      PoiServiceGrpc.getUpdatePoiMethod(),
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
    public suspend fun deletePoi(request: DeletePoiRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      PoiServiceGrpc.getDeletePoiMethod(),
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
    public suspend fun searchPois(request: SearchPoisRequest, headers: Metadata = Metadata()): SearchPoisResponse = unaryRpc(
      channel,
      PoiServiceGrpc.getSearchPoisMethod(),
      request,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the e39maps.v1.PoiService service based on Kotlin coroutines.
   */
  public abstract class PoiServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.ListPois.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listPois(request: ListPoisRequest): ListPoisResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.ListPois is unimplemented"))

    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.GetPoi.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun getPoi(request: GetPoiRequest): Poi = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.GetPoi is unimplemented"))

    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.CreatePoi.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun createPoi(request: CreatePoiRequest): Poi = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.CreatePoi is unimplemented"))

    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.UpdatePoi.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun updatePoi(request: UpdatePoiRequest): Poi = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.UpdatePoi is unimplemented"))

    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.DeletePoi.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun deletePoi(request: DeletePoiRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.DeletePoi is unimplemented"))

    /**
     * Returns the response to an RPC for e39maps.v1.PoiService.SearchPois.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun searchPois(request: SearchPoisRequest): SearchPoisResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39maps.v1.PoiService.SearchPois is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getListPoisMethod(),
      implementation = ::listPois
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getGetPoiMethod(),
      implementation = ::getPoi
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getCreatePoiMethod(),
      implementation = ::createPoi
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getUpdatePoiMethod(),
      implementation = ::updatePoi
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getDeletePoiMethod(),
      implementation = ::deletePoi
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = PoiServiceGrpc.getSearchPoisMethod(),
      implementation = ::searchPois
    )).build()
  }
}
