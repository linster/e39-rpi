package ca.stefanm.e39pim.v1

import ca.stefanm.e39pim.v1.AccountServiceGrpc.getServiceDescriptor
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
 * Holder for Kotlin coroutine-based client and server APIs for e39pim.v1.AccountService.
 */
public object AccountServiceGrpcKt {
  public const val SERVICE_NAME: String = AccountServiceGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = getServiceDescriptor()

  public val listAccountsMethod: MethodDescriptor<ListAccountsRequest, ListAccountsResponse>
    @JvmStatic
    get() = AccountServiceGrpc.getListAccountsMethod()

  public val watchAccountsMethod: MethodDescriptor<WatchAccountsRequest, ListAccountsResponse>
    @JvmStatic
    get() = AccountServiceGrpc.getWatchAccountsMethod()

  public val addCalDavAccountMethod: MethodDescriptor<AddCalDavAccountRequest, Account>
    @JvmStatic
    get() = AccountServiceGrpc.getAddCalDavAccountMethod()

  public val addGoogleAccountMethod: MethodDescriptor<AddGoogleAccountRequest, StartLoginResponse>
    @JvmStatic
    get() = AccountServiceGrpc.getAddGoogleAccountMethod()

  public val discoverMailSettingsMethod:
      MethodDescriptor<DiscoverMailSettingsRequest, DiscoverMailSettingsResponse>
    @JvmStatic
    get() = AccountServiceGrpc.getDiscoverMailSettingsMethod()

  public val addMailAccountMethod: MethodDescriptor<AddMailAccountRequest, Account>
    @JvmStatic
    get() = AccountServiceGrpc.getAddMailAccountMethod()

  public val startLoginMethod: MethodDescriptor<StartLoginRequest, StartLoginResponse>
    @JvmStatic
    get() = AccountServiceGrpc.getStartLoginMethod()

  public val removeAccountMethod: MethodDescriptor<RemoveAccountRequest, Empty>
    @JvmStatic
    get() = AccountServiceGrpc.getRemoveAccountMethod()

  public val setAccountOnlineMethod: MethodDescriptor<SetAccountOnlineRequest, Account>
    @JvmStatic
    get() = AccountServiceGrpc.getSetAccountOnlineMethod()

  public val syncNowMethod: MethodDescriptor<SyncNowRequest, Empty>
    @JvmStatic
    get() = AccountServiceGrpc.getSyncNowMethod()

  /**
   * A stub for issuing RPCs to a(n) e39pim.v1.AccountService service as suspending coroutines.
   */
  @StubFor(AccountServiceGrpc::class)
  public class AccountServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<AccountServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): AccountServiceCoroutineStub = AccountServiceCoroutineStub(channel, callOptions)

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
    public suspend fun listAccounts(request: ListAccountsRequest, headers: Metadata = Metadata()): ListAccountsResponse = unaryRpc(
      channel,
      AccountServiceGrpc.getListAccountsMethod(),
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
    public fun watchAccounts(request: WatchAccountsRequest, headers: Metadata = Metadata()): Flow<ListAccountsResponse> = serverStreamingRpc(
      channel,
      AccountServiceGrpc.getWatchAccountsMethod(),
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
    public suspend fun addCalDavAccount(request: AddCalDavAccountRequest, headers: Metadata = Metadata()): Account = unaryRpc(
      channel,
      AccountServiceGrpc.getAddCalDavAccountMethod(),
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
    public suspend fun addGoogleAccount(request: AddGoogleAccountRequest, headers: Metadata = Metadata()): StartLoginResponse = unaryRpc(
      channel,
      AccountServiceGrpc.getAddGoogleAccountMethod(),
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
    public suspend fun discoverMailSettings(request: DiscoverMailSettingsRequest, headers: Metadata = Metadata()): DiscoverMailSettingsResponse = unaryRpc(
      channel,
      AccountServiceGrpc.getDiscoverMailSettingsMethod(),
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
    public suspend fun addMailAccount(request: AddMailAccountRequest, headers: Metadata = Metadata()): Account = unaryRpc(
      channel,
      AccountServiceGrpc.getAddMailAccountMethod(),
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
    public suspend fun startLogin(request: StartLoginRequest, headers: Metadata = Metadata()): StartLoginResponse = unaryRpc(
      channel,
      AccountServiceGrpc.getStartLoginMethod(),
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
    public suspend fun removeAccount(request: RemoveAccountRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      AccountServiceGrpc.getRemoveAccountMethod(),
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
    public suspend fun setAccountOnline(request: SetAccountOnlineRequest, headers: Metadata = Metadata()): Account = unaryRpc(
      channel,
      AccountServiceGrpc.getSetAccountOnlineMethod(),
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
    public suspend fun syncNow(request: SyncNowRequest, headers: Metadata = Metadata()): Empty = unaryRpc(
      channel,
      AccountServiceGrpc.getSyncNowMethod(),
      request,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the e39pim.v1.AccountService service based on Kotlin coroutines.
   */
  public abstract class AccountServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.ListAccounts.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun listAccounts(request: ListAccountsRequest): ListAccountsResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.ListAccounts is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for e39pim.v1.AccountService.WatchAccounts.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [io.grpc.Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open fun watchAccounts(request: WatchAccountsRequest): Flow<ListAccountsResponse> = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.WatchAccounts is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.AddCalDavAccount.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun addCalDavAccount(request: AddCalDavAccountRequest): Account = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.AddCalDavAccount is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.AddGoogleAccount.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun addGoogleAccount(request: AddGoogleAccountRequest): StartLoginResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.AddGoogleAccount is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.DiscoverMailSettings.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun discoverMailSettings(request: DiscoverMailSettingsRequest): DiscoverMailSettingsResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.DiscoverMailSettings is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.AddMailAccount.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun addMailAccount(request: AddMailAccountRequest): Account = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.AddMailAccount is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.StartLogin.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun startLogin(request: StartLoginRequest): StartLoginResponse = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.StartLogin is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.RemoveAccount.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun removeAccount(request: RemoveAccountRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.RemoveAccount is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.SetAccountOnline.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun setAccountOnline(request: SetAccountOnlineRequest): Account = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.SetAccountOnline is unimplemented"))

    /**
     * Returns the response to an RPC for e39pim.v1.AccountService.SyncNow.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [io.grpc.Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open suspend fun syncNow(request: SyncNowRequest): Empty = throw StatusException(UNIMPLEMENTED.withDescription("Method e39pim.v1.AccountService.SyncNow is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getListAccountsMethod(),
      implementation = ::listAccounts
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getWatchAccountsMethod(),
      implementation = ::watchAccounts
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getAddCalDavAccountMethod(),
      implementation = ::addCalDavAccount
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getAddGoogleAccountMethod(),
      implementation = ::addGoogleAccount
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getDiscoverMailSettingsMethod(),
      implementation = ::discoverMailSettings
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getAddMailAccountMethod(),
      implementation = ::addMailAccount
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getStartLoginMethod(),
      implementation = ::startLogin
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getRemoveAccountMethod(),
      implementation = ::removeAccount
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getSetAccountOnlineMethod(),
      implementation = ::setAccountOnline
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = AccountServiceGrpc.getSyncNowMethod(),
      implementation = ::syncNow
    )).build()
  }
}
