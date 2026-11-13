package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class AuthServiceGrpc {

  private AuthServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.AuthService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAuthRequestsRequest,
      ca.stefanm.e39pim.v1.AuthEvent> getWatchAuthRequestsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchAuthRequests",
      requestType = ca.stefanm.e39pim.v1.WatchAuthRequestsRequest.class,
      responseType = ca.stefanm.e39pim.v1.AuthEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAuthRequestsRequest,
      ca.stefanm.e39pim.v1.AuthEvent> getWatchAuthRequestsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchAuthRequestsRequest, ca.stefanm.e39pim.v1.AuthEvent> getWatchAuthRequestsMethod;
    if ((getWatchAuthRequestsMethod = AuthServiceGrpc.getWatchAuthRequestsMethod) == null) {
      synchronized (AuthServiceGrpc.class) {
        if ((getWatchAuthRequestsMethod = AuthServiceGrpc.getWatchAuthRequestsMethod) == null) {
          AuthServiceGrpc.getWatchAuthRequestsMethod = getWatchAuthRequestsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.WatchAuthRequestsRequest, ca.stefanm.e39pim.v1.AuthEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchAuthRequests"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.WatchAuthRequestsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.AuthEvent.getDefaultInstance()))
              .setSchemaDescriptor(new AuthServiceMethodDescriptorSupplier("WatchAuthRequests"))
              .build();
        }
      }
    }
    return getWatchAuthRequestsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CancelAuthRequest,
      com.google.protobuf.Empty> getCancelAuthMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelAuth",
      requestType = ca.stefanm.e39pim.v1.CancelAuthRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CancelAuthRequest,
      com.google.protobuf.Empty> getCancelAuthMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CancelAuthRequest, com.google.protobuf.Empty> getCancelAuthMethod;
    if ((getCancelAuthMethod = AuthServiceGrpc.getCancelAuthMethod) == null) {
      synchronized (AuthServiceGrpc.class) {
        if ((getCancelAuthMethod = AuthServiceGrpc.getCancelAuthMethod) == null) {
          AuthServiceGrpc.getCancelAuthMethod = getCancelAuthMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.CancelAuthRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelAuth"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.CancelAuthRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new AuthServiceMethodDescriptorSupplier("CancelAuth"))
              .build();
        }
      }
    }
    return getCancelAuthMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AuthServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthServiceStub>() {
        @java.lang.Override
        public AuthServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthServiceStub(channel, callOptions);
        }
      };
    return AuthServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static AuthServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthServiceBlockingV2Stub>() {
        @java.lang.Override
        public AuthServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return AuthServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AuthServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthServiceBlockingStub>() {
        @java.lang.Override
        public AuthServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthServiceBlockingStub(channel, callOptions);
        }
      };
    return AuthServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AuthServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthServiceFutureStub>() {
        @java.lang.Override
        public AuthServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthServiceFutureStub(channel, callOptions);
        }
      };
    return AuthServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Logins waiting for the user first (as opened), then new ones and their
     * results as they happen.
     * </pre>
     */
    default void watchAuthRequests(ca.stefanm.e39pim.v1.WatchAuthRequestsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.AuthEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchAuthRequestsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gives up on a login (the user closed the browser). It finishes with
     * success = false. NOT_FOUND if there's no such login running.
     * </pre>
     */
    default void cancelAuth(ca.stefanm.e39pim.v1.CancelAuthRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelAuthMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AuthService.
   */
  public static abstract class AuthServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AuthServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AuthService.
   */
  public static final class AuthServiceStub
      extends io.grpc.stub.AbstractAsyncStub<AuthServiceStub> {
    private AuthServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Logins waiting for the user first (as opened), then new ones and their
     * results as they happen.
     * </pre>
     */
    public void watchAuthRequests(ca.stefanm.e39pim.v1.WatchAuthRequestsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.AuthEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchAuthRequestsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gives up on a login (the user closed the browser). It finishes with
     * success = false. NOT_FOUND if there's no such login running.
     * </pre>
     */
    public void cancelAuth(ca.stefanm.e39pim.v1.CancelAuthRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelAuthMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AuthService.
   */
  public static final class AuthServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<AuthServiceBlockingV2Stub> {
    private AuthServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Logins waiting for the user first (as opened), then new ones and their
     * results as they happen.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, ca.stefanm.e39pim.v1.AuthEvent>
        watchAuthRequests(ca.stefanm.e39pim.v1.WatchAuthRequestsRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getWatchAuthRequestsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gives up on a login (the user closed the browser). It finishes with
     * success = false. NOT_FOUND if there's no such login running.
     * </pre>
     */
    public com.google.protobuf.Empty cancelAuth(ca.stefanm.e39pim.v1.CancelAuthRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCancelAuthMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service AuthService.
   */
  public static final class AuthServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AuthServiceBlockingStub> {
    private AuthServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Logins waiting for the user first (as opened), then new ones and their
     * results as they happen.
     * </pre>
     */
    public java.util.Iterator<ca.stefanm.e39pim.v1.AuthEvent> watchAuthRequests(
        ca.stefanm.e39pim.v1.WatchAuthRequestsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchAuthRequestsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gives up on a login (the user closed the browser). It finishes with
     * success = false. NOT_FOUND if there's no such login running.
     * </pre>
     */
    public com.google.protobuf.Empty cancelAuth(ca.stefanm.e39pim.v1.CancelAuthRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelAuthMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AuthService.
   */
  public static final class AuthServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<AuthServiceFutureStub> {
    private AuthServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Gives up on a login (the user closed the browser). It finishes with
     * success = false. NOT_FOUND if there's no such login running.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> cancelAuth(
        ca.stefanm.e39pim.v1.CancelAuthRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelAuthMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_WATCH_AUTH_REQUESTS = 0;
  private static final int METHODID_CANCEL_AUTH = 1;

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
        case METHODID_WATCH_AUTH_REQUESTS:
          serviceImpl.watchAuthRequests((ca.stefanm.e39pim.v1.WatchAuthRequestsRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.AuthEvent>) responseObserver);
          break;
        case METHODID_CANCEL_AUTH:
          serviceImpl.cancelAuth((ca.stefanm.e39pim.v1.CancelAuthRequest) request,
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
          getWatchAuthRequestsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.WatchAuthRequestsRequest,
              ca.stefanm.e39pim.v1.AuthEvent>(
                service, METHODID_WATCH_AUTH_REQUESTS)))
        .addMethod(
          getCancelAuthMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.CancelAuthRequest,
              com.google.protobuf.Empty>(
                service, METHODID_CANCEL_AUTH)))
        .build();
  }

  private static abstract class AuthServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AuthServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.AuthProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AuthService");
    }
  }

  private static final class AuthServiceFileDescriptorSupplier
      extends AuthServiceBaseDescriptorSupplier {
    AuthServiceFileDescriptorSupplier() {}
  }

  private static final class AuthServiceMethodDescriptorSupplier
      extends AuthServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AuthServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (AuthServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AuthServiceFileDescriptorSupplier())
              .addMethod(getWatchAuthRequestsMethod())
              .addMethod(getCancelAuthMethod())
              .build();
        }
      }
    }
    return result;
  }
}
