package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class AccountServiceGrpc {

  private AccountServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.AccountService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListAccountsRequest,
      ca.stefanm.e39pim.v1.ListAccountsResponse> getListAccountsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListAccounts",
      requestType = ca.stefanm.e39pim.v1.ListAccountsRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListAccountsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListAccountsRequest,
      ca.stefanm.e39pim.v1.ListAccountsResponse> getListAccountsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListAccountsRequest, ca.stefanm.e39pim.v1.ListAccountsResponse> getListAccountsMethod;
    if ((getListAccountsMethod = AccountServiceGrpc.getListAccountsMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getListAccountsMethod = AccountServiceGrpc.getListAccountsMethod) == null) {
          AccountServiceGrpc.getListAccountsMethod = getListAccountsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListAccountsRequest, ca.stefanm.e39pim.v1.ListAccountsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListAccounts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListAccountsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListAccountsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("ListAccounts"))
              .build();
        }
      }
    }
    return getListAccountsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAccountsRequest,
      ca.stefanm.e39pim.v1.ListAccountsResponse> getWatchAccountsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchAccounts",
      requestType = ca.stefanm.e39pim.v1.WatchAccountsRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListAccountsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAccountsRequest,
      ca.stefanm.e39pim.v1.ListAccountsResponse> getWatchAccountsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAccountsRequest, ca.stefanm.e39pim.v1.ListAccountsResponse> getWatchAccountsMethod;
    if ((getWatchAccountsMethod = AccountServiceGrpc.getWatchAccountsMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getWatchAccountsMethod = AccountServiceGrpc.getWatchAccountsMethod) == null) {
          AccountServiceGrpc.getWatchAccountsMethod = getWatchAccountsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.WatchAccountsRequest, ca.stefanm.e39pim.v1.ListAccountsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchAccounts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.WatchAccountsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListAccountsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("WatchAccounts"))
              .build();
        }
      }
    }
    return getWatchAccountsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddCalDavAccountRequest,
      ca.stefanm.e39pim.v1.Account> getAddCalDavAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddCalDavAccount",
      requestType = ca.stefanm.e39pim.v1.AddCalDavAccountRequest.class,
      responseType = ca.stefanm.e39pim.v1.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddCalDavAccountRequest,
      ca.stefanm.e39pim.v1.Account> getAddCalDavAccountMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddCalDavAccountRequest, ca.stefanm.e39pim.v1.Account> getAddCalDavAccountMethod;
    if ((getAddCalDavAccountMethod = AccountServiceGrpc.getAddCalDavAccountMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getAddCalDavAccountMethod = AccountServiceGrpc.getAddCalDavAccountMethod) == null) {
          AccountServiceGrpc.getAddCalDavAccountMethod = getAddCalDavAccountMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.AddCalDavAccountRequest, ca.stefanm.e39pim.v1.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddCalDavAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.AddCalDavAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Account.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("AddCalDavAccount"))
              .build();
        }
      }
    }
    return getAddCalDavAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddGoogleAccountRequest,
      ca.stefanm.e39pim.v1.StartLoginResponse> getAddGoogleAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddGoogleAccount",
      requestType = ca.stefanm.e39pim.v1.AddGoogleAccountRequest.class,
      responseType = ca.stefanm.e39pim.v1.StartLoginResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddGoogleAccountRequest,
      ca.stefanm.e39pim.v1.StartLoginResponse> getAddGoogleAccountMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.AddGoogleAccountRequest, ca.stefanm.e39pim.v1.StartLoginResponse> getAddGoogleAccountMethod;
    if ((getAddGoogleAccountMethod = AccountServiceGrpc.getAddGoogleAccountMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getAddGoogleAccountMethod = AccountServiceGrpc.getAddGoogleAccountMethod) == null) {
          AccountServiceGrpc.getAddGoogleAccountMethod = getAddGoogleAccountMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.AddGoogleAccountRequest, ca.stefanm.e39pim.v1.StartLoginResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddGoogleAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.AddGoogleAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.StartLoginResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("AddGoogleAccount"))
              .build();
        }
      }
    }
    return getAddGoogleAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.StartLoginRequest,
      ca.stefanm.e39pim.v1.StartLoginResponse> getStartLoginMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StartLogin",
      requestType = ca.stefanm.e39pim.v1.StartLoginRequest.class,
      responseType = ca.stefanm.e39pim.v1.StartLoginResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.StartLoginRequest,
      ca.stefanm.e39pim.v1.StartLoginResponse> getStartLoginMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.StartLoginRequest, ca.stefanm.e39pim.v1.StartLoginResponse> getStartLoginMethod;
    if ((getStartLoginMethod = AccountServiceGrpc.getStartLoginMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getStartLoginMethod = AccountServiceGrpc.getStartLoginMethod) == null) {
          AccountServiceGrpc.getStartLoginMethod = getStartLoginMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.StartLoginRequest, ca.stefanm.e39pim.v1.StartLoginResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StartLogin"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.StartLoginRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.StartLoginResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("StartLogin"))
              .build();
        }
      }
    }
    return getStartLoginMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.RemoveAccountRequest,
      com.google.protobuf.Empty> getRemoveAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveAccount",
      requestType = ca.stefanm.e39pim.v1.RemoveAccountRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.RemoveAccountRequest,
      com.google.protobuf.Empty> getRemoveAccountMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.RemoveAccountRequest, com.google.protobuf.Empty> getRemoveAccountMethod;
    if ((getRemoveAccountMethod = AccountServiceGrpc.getRemoveAccountMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getRemoveAccountMethod = AccountServiceGrpc.getRemoveAccountMethod) == null) {
          AccountServiceGrpc.getRemoveAccountMethod = getRemoveAccountMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.RemoveAccountRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.RemoveAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("RemoveAccount"))
              .build();
        }
      }
    }
    return getRemoveAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetAccountOnlineRequest,
      ca.stefanm.e39pim.v1.Account> getSetAccountOnlineMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetAccountOnline",
      requestType = ca.stefanm.e39pim.v1.SetAccountOnlineRequest.class,
      responseType = ca.stefanm.e39pim.v1.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetAccountOnlineRequest,
      ca.stefanm.e39pim.v1.Account> getSetAccountOnlineMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetAccountOnlineRequest, ca.stefanm.e39pim.v1.Account> getSetAccountOnlineMethod;
    if ((getSetAccountOnlineMethod = AccountServiceGrpc.getSetAccountOnlineMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getSetAccountOnlineMethod = AccountServiceGrpc.getSetAccountOnlineMethod) == null) {
          AccountServiceGrpc.getSetAccountOnlineMethod = getSetAccountOnlineMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SetAccountOnlineRequest, ca.stefanm.e39pim.v1.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetAccountOnline"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SetAccountOnlineRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Account.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("SetAccountOnline"))
              .build();
        }
      }
    }
    return getSetAccountOnlineMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SyncNowRequest,
      com.google.protobuf.Empty> getSyncNowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SyncNow",
      requestType = ca.stefanm.e39pim.v1.SyncNowRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SyncNowRequest,
      com.google.protobuf.Empty> getSyncNowMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SyncNowRequest, com.google.protobuf.Empty> getSyncNowMethod;
    if ((getSyncNowMethod = AccountServiceGrpc.getSyncNowMethod) == null) {
      synchronized (AccountServiceGrpc.class) {
        if ((getSyncNowMethod = AccountServiceGrpc.getSyncNowMethod) == null) {
          AccountServiceGrpc.getSyncNowMethod = getSyncNowMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SyncNowRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SyncNow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SyncNowRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new AccountServiceMethodDescriptorSupplier("SyncNow"))
              .build();
        }
      }
    }
    return getSyncNowMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AccountServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceStub>() {
        @java.lang.Override
        public AccountServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceStub(channel, callOptions);
        }
      };
    return AccountServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static AccountServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingV2Stub>() {
        @java.lang.Override
        public AccountServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return AccountServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AccountServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceBlockingStub>() {
        @java.lang.Override
        public AccountServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceBlockingStub(channel, callOptions);
        }
      };
    return AccountServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AccountServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AccountServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AccountServiceFutureStub>() {
        @java.lang.Override
        public AccountServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AccountServiceFutureStub(channel, callOptions);
        }
      };
    return AccountServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * By name.
     * </pre>
     */
    default void listAccounts(ca.stefanm.e39pim.v1.ListAccountsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListAccountsMethod(), responseObserver);
    }

    /**
     * <pre>
     * The whole list first, then again whenever any account changes (added,
     * removed, renamed, online/offline, sync progress, errors).
     * </pre>
     */
    default void watchAccounts(ca.stefanm.e39pim.v1.WatchAccountsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchAccountsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a CalDAV account, stores its password and starts a first sync.
     * Use an app password where the server offers them. The login is tried
     * first: UNAUTHENTICATED if the server refuses it, UNAVAILABLE if the
     * server can't be reached (message: why); nothing is created then.
     * INVALID_ARGUMENT for a malformed URL or a missing user name/password.
     * </pre>
     */
    default void addCalDavAccount(ca.stefanm.e39pim.v1.AddCalDavAccountRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddCalDavAccountMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a Google account and starts its login: an AuthRequest appears
     * on AuthService.WatchAuthRequests. The account says needs_login until
     * the login finishes.
     * </pre>
     */
    default void addGoogleAccount(ca.stefanm.e39pim.v1.AddGoogleAccountRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddGoogleAccountMethod(), responseObserver);
    }

    /**
     * <pre>
     * Starts a new login for an account that says needs_login.
     * FAILED_PRECONDITION for account types without OAuth.
     * </pre>
     */
    default void startLogin(ca.stefanm.e39pim.v1.StartLoginRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStartLoginMethod(), responseObserver);
    }

    /**
     * <pre>
     * Removes the account, its stored password/tokens and its local copy of
     * the data (nothing on the server is deleted).
     * </pre>
     */
    default void removeAccount(ca.stefanm.e39pim.v1.RemoveAccountRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveAccountMethod(), responseObserver);
    }

    /**
     * <pre>
     * Offline accounts don't sync (data stays readable). Akonadi also takes
     * accounts offline by itself when the network is down.
     * </pre>
     */
    default void setAccountOnline(ca.stefanm.e39pim.v1.SetAccountOnlineRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetAccountOnlineMethod(), responseObserver);
    }

    /**
     * <pre>
     * Sync now instead of at the next interval.
     * </pre>
     */
    default void syncNow(ca.stefanm.e39pim.v1.SyncNowRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSyncNowMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AccountService.
   */
  public static abstract class AccountServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AccountServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AccountService.
   */
  public static final class AccountServiceStub
      extends io.grpc.stub.AbstractAsyncStub<AccountServiceStub> {
    private AccountServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * By name.
     * </pre>
     */
    public void listAccounts(ca.stefanm.e39pim.v1.ListAccountsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListAccountsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * The whole list first, then again whenever any account changes (added,
     * removed, renamed, online/offline, sync progress, errors).
     * </pre>
     */
    public void watchAccounts(ca.stefanm.e39pim.v1.WatchAccountsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchAccountsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a CalDAV account, stores its password and starts a first sync.
     * Use an app password where the server offers them. The login is tried
     * first: UNAUTHENTICATED if the server refuses it, UNAVAILABLE if the
     * server can't be reached (message: why); nothing is created then.
     * INVALID_ARGUMENT for a malformed URL or a missing user name/password.
     * </pre>
     */
    public void addCalDavAccount(ca.stefanm.e39pim.v1.AddCalDavAccountRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddCalDavAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a Google account and starts its login: an AuthRequest appears
     * on AuthService.WatchAuthRequests. The account says needs_login until
     * the login finishes.
     * </pre>
     */
    public void addGoogleAccount(ca.stefanm.e39pim.v1.AddGoogleAccountRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddGoogleAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Starts a new login for an account that says needs_login.
     * FAILED_PRECONDITION for account types without OAuth.
     * </pre>
     */
    public void startLogin(ca.stefanm.e39pim.v1.StartLoginRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStartLoginMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Removes the account, its stored password/tokens and its local copy of
     * the data (nothing on the server is deleted).
     * </pre>
     */
    public void removeAccount(ca.stefanm.e39pim.v1.RemoveAccountRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Offline accounts don't sync (data stays readable). Akonadi also takes
     * accounts offline by itself when the network is down.
     * </pre>
     */
    public void setAccountOnline(ca.stefanm.e39pim.v1.SetAccountOnlineRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetAccountOnlineMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Sync now instead of at the next interval.
     * </pre>
     */
    public void syncNow(ca.stefanm.e39pim.v1.SyncNowRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSyncNowMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AccountService.
   */
  public static final class AccountServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<AccountServiceBlockingV2Stub> {
    private AccountServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * By name.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListAccountsResponse listAccounts(ca.stefanm.e39pim.v1.ListAccountsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListAccountsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * The whole list first, then again whenever any account changes (added,
     * removed, renamed, online/offline, sync progress, errors).
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, ca.stefanm.e39pim.v1.ListAccountsResponse>
        watchAccounts(ca.stefanm.e39pim.v1.WatchAccountsRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getWatchAccountsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a CalDAV account, stores its password and starts a first sync.
     * Use an app password where the server offers them. The login is tried
     * first: UNAUTHENTICATED if the server refuses it, UNAVAILABLE if the
     * server can't be reached (message: why); nothing is created then.
     * INVALID_ARGUMENT for a malformed URL or a missing user name/password.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Account addCalDavAccount(ca.stefanm.e39pim.v1.AddCalDavAccountRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getAddCalDavAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a Google account and starts its login: an AuthRequest appears
     * on AuthService.WatchAuthRequests. The account says needs_login until
     * the login finishes.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.StartLoginResponse addGoogleAccount(ca.stefanm.e39pim.v1.AddGoogleAccountRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getAddGoogleAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Starts a new login for an account that says needs_login.
     * FAILED_PRECONDITION for account types without OAuth.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.StartLoginResponse startLogin(ca.stefanm.e39pim.v1.StartLoginRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getStartLoginMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Removes the account, its stored password/tokens and its local copy of
     * the data (nothing on the server is deleted).
     * </pre>
     */
    public com.google.protobuf.Empty removeAccount(ca.stefanm.e39pim.v1.RemoveAccountRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getRemoveAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Offline accounts don't sync (data stays readable). Akonadi also takes
     * accounts offline by itself when the network is down.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Account setAccountOnline(ca.stefanm.e39pim.v1.SetAccountOnlineRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSetAccountOnlineMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sync now instead of at the next interval.
     * </pre>
     */
    public com.google.protobuf.Empty syncNow(ca.stefanm.e39pim.v1.SyncNowRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSyncNowMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service AccountService.
   */
  public static final class AccountServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AccountServiceBlockingStub> {
    private AccountServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * By name.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListAccountsResponse listAccounts(ca.stefanm.e39pim.v1.ListAccountsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListAccountsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * The whole list first, then again whenever any account changes (added,
     * removed, renamed, online/offline, sync progress, errors).
     * </pre>
     */
    public java.util.Iterator<ca.stefanm.e39pim.v1.ListAccountsResponse> watchAccounts(
        ca.stefanm.e39pim.v1.WatchAccountsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchAccountsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a CalDAV account, stores its password and starts a first sync.
     * Use an app password where the server offers them. The login is tried
     * first: UNAUTHENTICATED if the server refuses it, UNAVAILABLE if the
     * server can't be reached (message: why); nothing is created then.
     * INVALID_ARGUMENT for a malformed URL or a missing user name/password.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Account addCalDavAccount(ca.stefanm.e39pim.v1.AddCalDavAccountRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddCalDavAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a Google account and starts its login: an AuthRequest appears
     * on AuthService.WatchAuthRequests. The account says needs_login until
     * the login finishes.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.StartLoginResponse addGoogleAccount(ca.stefanm.e39pim.v1.AddGoogleAccountRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddGoogleAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Starts a new login for an account that says needs_login.
     * FAILED_PRECONDITION for account types without OAuth.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.StartLoginResponse startLogin(ca.stefanm.e39pim.v1.StartLoginRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStartLoginMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Removes the account, its stored password/tokens and its local copy of
     * the data (nothing on the server is deleted).
     * </pre>
     */
    public com.google.protobuf.Empty removeAccount(ca.stefanm.e39pim.v1.RemoveAccountRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Offline accounts don't sync (data stays readable). Akonadi also takes
     * accounts offline by itself when the network is down.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Account setAccountOnline(ca.stefanm.e39pim.v1.SetAccountOnlineRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetAccountOnlineMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sync now instead of at the next interval.
     * </pre>
     */
    public com.google.protobuf.Empty syncNow(ca.stefanm.e39pim.v1.SyncNowRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSyncNowMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AccountService.
   */
  public static final class AccountServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<AccountServiceFutureStub> {
    private AccountServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AccountServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AccountServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * By name.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListAccountsResponse> listAccounts(
        ca.stefanm.e39pim.v1.ListAccountsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListAccountsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a CalDAV account, stores its password and starts a first sync.
     * Use an app password where the server offers them. The login is tried
     * first: UNAUTHENTICATED if the server refuses it, UNAVAILABLE if the
     * server can't be reached (message: why); nothing is created then.
     * INVALID_ARGUMENT for a malformed URL or a missing user name/password.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Account> addCalDavAccount(
        ca.stefanm.e39pim.v1.AddCalDavAccountRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddCalDavAccountMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a Google account and starts its login: an AuthRequest appears
     * on AuthService.WatchAuthRequests. The account says needs_login until
     * the login finishes.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.StartLoginResponse> addGoogleAccount(
        ca.stefanm.e39pim.v1.AddGoogleAccountRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddGoogleAccountMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Starts a new login for an account that says needs_login.
     * FAILED_PRECONDITION for account types without OAuth.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.StartLoginResponse> startLogin(
        ca.stefanm.e39pim.v1.StartLoginRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStartLoginMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Removes the account, its stored password/tokens and its local copy of
     * the data (nothing on the server is deleted).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> removeAccount(
        ca.stefanm.e39pim.v1.RemoveAccountRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveAccountMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Offline accounts don't sync (data stays readable). Akonadi also takes
     * accounts offline by itself when the network is down.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Account> setAccountOnline(
        ca.stefanm.e39pim.v1.SetAccountOnlineRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetAccountOnlineMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Sync now instead of at the next interval.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> syncNow(
        ca.stefanm.e39pim.v1.SyncNowRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSyncNowMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_ACCOUNTS = 0;
  private static final int METHODID_WATCH_ACCOUNTS = 1;
  private static final int METHODID_ADD_CAL_DAV_ACCOUNT = 2;
  private static final int METHODID_ADD_GOOGLE_ACCOUNT = 3;
  private static final int METHODID_START_LOGIN = 4;
  private static final int METHODID_REMOVE_ACCOUNT = 5;
  private static final int METHODID_SET_ACCOUNT_ONLINE = 6;
  private static final int METHODID_SYNC_NOW = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_ACCOUNTS:
          serviceImpl.listAccounts((ca.stefanm.e39pim.v1.ListAccountsRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse>) responseObserver);
          break;
        case METHODID_WATCH_ACCOUNTS:
          serviceImpl.watchAccounts((ca.stefanm.e39pim.v1.WatchAccountsRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListAccountsResponse>) responseObserver);
          break;
        case METHODID_ADD_CAL_DAV_ACCOUNT:
          serviceImpl.addCalDavAccount((ca.stefanm.e39pim.v1.AddCalDavAccountRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account>) responseObserver);
          break;
        case METHODID_ADD_GOOGLE_ACCOUNT:
          serviceImpl.addGoogleAccount((ca.stefanm.e39pim.v1.AddGoogleAccountRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse>) responseObserver);
          break;
        case METHODID_START_LOGIN:
          serviceImpl.startLogin((ca.stefanm.e39pim.v1.StartLoginRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.StartLoginResponse>) responseObserver);
          break;
        case METHODID_REMOVE_ACCOUNT:
          serviceImpl.removeAccount((ca.stefanm.e39pim.v1.RemoveAccountRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SET_ACCOUNT_ONLINE:
          serviceImpl.setAccountOnline((ca.stefanm.e39pim.v1.SetAccountOnlineRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Account>) responseObserver);
          break;
        case METHODID_SYNC_NOW:
          serviceImpl.syncNow((ca.stefanm.e39pim.v1.SyncNowRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListAccountsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListAccountsRequest,
              ca.stefanm.e39pim.v1.ListAccountsResponse>(
                service, METHODID_LIST_ACCOUNTS)))
        .addMethod(
          getWatchAccountsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.WatchAccountsRequest,
              ca.stefanm.e39pim.v1.ListAccountsResponse>(
                service, METHODID_WATCH_ACCOUNTS)))
        .addMethod(
          getAddCalDavAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.AddCalDavAccountRequest,
              ca.stefanm.e39pim.v1.Account>(
                service, METHODID_ADD_CAL_DAV_ACCOUNT)))
        .addMethod(
          getAddGoogleAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.AddGoogleAccountRequest,
              ca.stefanm.e39pim.v1.StartLoginResponse>(
                service, METHODID_ADD_GOOGLE_ACCOUNT)))
        .addMethod(
          getStartLoginMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.StartLoginRequest,
              ca.stefanm.e39pim.v1.StartLoginResponse>(
                service, METHODID_START_LOGIN)))
        .addMethod(
          getRemoveAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.RemoveAccountRequest,
              com.google.protobuf.Empty>(
                service, METHODID_REMOVE_ACCOUNT)))
        .addMethod(
          getSetAccountOnlineMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SetAccountOnlineRequest,
              ca.stefanm.e39pim.v1.Account>(
                service, METHODID_SET_ACCOUNT_ONLINE)))
        .addMethod(
          getSyncNowMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SyncNowRequest,
              com.google.protobuf.Empty>(
                service, METHODID_SYNC_NOW)))
        .build();
  }

  private static abstract class AccountServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AccountServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.AccountProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AccountService");
    }
  }

  private static final class AccountServiceFileDescriptorSupplier
      extends AccountServiceBaseDescriptorSupplier {
    AccountServiceFileDescriptorSupplier() {}
  }

  private static final class AccountServiceMethodDescriptorSupplier
      extends AccountServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AccountServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AccountServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AccountServiceFileDescriptorSupplier())
              .addMethod(getListAccountsMethod())
              .addMethod(getWatchAccountsMethod())
              .addMethod(getAddCalDavAccountMethod())
              .addMethod(getAddGoogleAccountMethod())
              .addMethod(getStartLoginMethod())
              .addMethod(getRemoveAccountMethod())
              .addMethod(getSetAccountOnlineMethod())
              .addMethod(getSyncNowMethod())
              .build();
        }
      }
    }
    return result;
  }
}
