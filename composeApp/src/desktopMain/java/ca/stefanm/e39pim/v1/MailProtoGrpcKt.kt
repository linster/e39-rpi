package ca.stefanm.e39pim.v1

import ca.stefanm.e39pim.v1.MailServiceGrpc.getServiceDescriptor
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
 * Holder for Kotlin coroutine-based client and server APIs for e39pim.v1.MailService.
 */
public object MailServiceGrpcKt {
  public const val SERVICE_NAME: String = MailServiceGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val listMailFoldersMethod:
      MethodDescriptor<ListMailFoldersRequest, ListMailFoldersResponse>
    @JvmStatic
    get() = MailServiceGrpc.getListMailFoldersMethod()

  public val listMessagesMethod: MethodDescriptor<ListMessagesRequest, ListMessagesResponse>
    @JvmStatic
    get() = MailServiceGrpc.getListMessagesMethod()

  public val getMessageMethod: MethodDescriptor<GetMessageRequest, MailMessage>
    @JvmStatic
    get() = MailServiceGrpc.getGetMessageMethod()

  public val getMessageHtmlMethod: MethodDescriptor<GetMessageRequest, MailMessageHtml>
    @JvmStatic
    get() = MailServiceGrpc.getGetMessageHtmlMethod()

  public val setMessageFlagsMethod: MethodDescriptor<SetMessageFlagsRequest, Empty>
    @JvmStatic
    get() = MailServiceGrpc.getSetMessageFlagsMethod()

  public val moveMessagesMethod: MethodDescriptor<MoveMessagesRequest, Empty>
    @JvmStatic
    get() = MailServiceGrpc.getMoveMessagesMethod()

  public val trashMessagesMethod: MethodDescriptor<TrashMessagesRequest, Empty>
    @JvmStatic
    get() = MailServiceGrpc.getTrashMessagesMethod()

  public val sendMessageMethod: MethodDescriptor<SendMessageRequest, SendMessageResponse>
    @JvmStatic
    get() = MailServiceGrpc.getSendMessageMethod()

  public val searchMessagesMethod: MethodDescriptor<SearchMessagesRequest, ListMessagesResponse>
    @JvmStatic
    get() = MailServiceGrpc.getSearchMessagesMethod()

  public val watchMailMethod: MethodDescriptor<WatchMailRequest, MailNotice>
    @JvmStatic
    get() = MailServiceGrpc.getWatchMailMethod()

  /**
   * A stub for issuing RPCs to a(n) e39pim.v1.MailService service as suspending coroutines.
   */
  @StubFor(MailServiceGrpc::class)
  public class MailServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<MailServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): MailServiceCoroutineStub = MailServiceCoroutineStub(channel, callOptions)

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
    public suspend fun listMailFolders(request: ListMailFoldersRequest, headers: Metadata = Metadata()): ListMailFoldersResponse = unaryRpc(
      channel,
      MailServiceGrpc.getListMailFoldersMethod(),
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
    public suspend fun listMessages(request: ListMessagesRequest, headers: Metadata = Metadata()): ListMessagesResponse = unaryRpc(
      channel,
      MailServiceGrpc.getListMessagesMethod(),
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
    public suspend fun getMessage(request: GetMessageRequest, headers: Metadata = Metadata()): MailMessage = unaryRpc(
      channel,
      MailServiceGrpc.getGetMessageMethod(),
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
    public suspend fun getMessageHtml(request: GetMessageRequest, headers: Metadata = Metadata()): MailMessageHtml = unaryRpc(
      channel,
      MailServiceGrpc.getGetMessageHtmlMethod(),
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
    public suspend fun setMessageFlags(request: SetMessageFlagsRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      MailServiceGrpc.getSetMessageFlagsMethod(),
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
    public suspend fun moveMessages(request: MoveMessagesRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      MailServiceGrpc.getMoveMessagesMethod(),
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
    public suspend fun trashMessages(request: TrashMessagesRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      MailServiceGrpc.getTrashMessagesMethod(),
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
    public suspend fun sendMessage(request: SendMessageRequest, headers: Metadata = Metadata()): SendMessageResponse = unaryRpc(
      channel,
      MailServiceGrpc.getSendMessageMethod(),
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
    public suspend fun searchMessages(request: SearchMessagesRequest, headers: Metadata = Metadata()): ListMessagesResponse = unaryRpc(
      channel,
      MailServiceGrpc.getSearchMessagesMethod(),
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
    public fun watchMail(request: WatchMailRequest, headers: Metadata = Metadata()): Flow<MailNotice> = serverStreamingRpc(
      channel,
      MailServiceGrpc.getWatchMailMethod(),
      request,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the e39pim.v1.MailService service based on Kotlin coroutines.
   */
  public abstract class MailServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for e39pim.v1.MailService.ListMailFolders.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listMailFolders(request: ListMailFoldersRequest): ListMailFoldersResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.ListMailFolders is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.ListMessages.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listMessages(request: ListMessagesRequest): ListMessagesResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.ListMessages is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.GetMessage.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun getMessage(request: GetMessageRequest): MailMessage = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.GetMessage is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.GetMessageHtml.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun getMessageHtml(request: GetMessageRequest): MailMessageHtml = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.GetMessageHtml is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.SetMessageFlags.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun setMessageFlags(request: SetMessageFlagsRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.SetMessageFlags is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.MoveMessages.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun moveMessages(request: MoveMessagesRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.MoveMessages is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.TrashMessages.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun trashMessages(request: TrashMessagesRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.TrashMessages is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.SendMessage.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun sendMessage(request: SendMessageRequest): SendMessageResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.SendMessage is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.MailService.SearchMessages.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun searchMessages(request: SearchMessagesRequest): ListMessagesResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.SearchMessages is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for e39pim.v1.MailService.WatchMail.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [io.grpc.Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open fun watchMail(request: WatchMailRequest): Flow<MailNotice> = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.MailService.WatchMail is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getListMailFoldersMethod(),
      implementation = ::listMailFolders
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getListMessagesMethod(),
      implementation = ::listMessages
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getGetMessageMethod(),
      implementation = ::getMessage
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getGetMessageHtmlMethod(),
      implementation = ::getMessageHtml
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getSetMessageFlagsMethod(),
      implementation = ::setMessageFlags
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getMoveMessagesMethod(),
      implementation = ::moveMessages
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getTrashMessagesMethod(),
      implementation = ::trashMessages
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getSendMessageMethod(),
      implementation = ::sendMessage
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getSearchMessagesMethod(),
      implementation = ::searchMessages
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = MailServiceGrpc.getWatchMailMethod(),
      implementation = ::watchMail
    )).build()
  }
}
