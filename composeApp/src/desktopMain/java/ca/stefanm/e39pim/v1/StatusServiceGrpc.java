package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class StatusServiceGrpc {

  private StatusServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.StatusService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetStatusRequest,
      ca.stefanm.e39pim.v1.BridgeStatus> getGetStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStatus",
      requestType = ca.stefanm.e39pim.v1.GetStatusRequest.class,
      responseType = ca.stefanm.e39pim.v1.BridgeStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetStatusRequest,
      ca.stefanm.e39pim.v1.BridgeStatus> getGetStatusMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetStatusRequest, ca.stefanm.e39pim.v1.BridgeStatus> getGetStatusMethod;
    if ((getGetStatusMethod = StatusServiceGrpc.getGetStatusMethod) == null) {
      synchronized (StatusServiceGrpc.class) {
        if ((getGetStatusMethod = StatusServiceGrpc.getGetStatusMethod) == null) {
          StatusServiceGrpc.getGetStatusMethod = getGetStatusMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.GetStatusRequest, ca.stefanm.e39pim.v1.BridgeStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.GetStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.BridgeStatus.getDefaultInstance()))
              .setSchemaDescriptor(new StatusServiceMethodDescriptorSupplier("GetStatus"))
              .build();
        }
      }
    }
    return getGetStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchStatusRequest,
      ca.stefanm.e39pim.v1.BridgeStatus> getWatchStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchStatus",
      requestType = ca.stefanm.e39pim.v1.WatchStatusRequest.class,
      responseType = ca.stefanm.e39pim.v1.BridgeStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchStatusRequest,
      ca.stefanm.e39pim.v1.BridgeStatus> getWatchStatusMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchStatusRequest, ca.stefanm.e39pim.v1.BridgeStatus> getWatchStatusMethod;
    if ((getWatchStatusMethod = StatusServiceGrpc.getWatchStatusMethod) == null) {
      synchronized (StatusServiceGrpc.class) {
        if ((getWatchStatusMethod = StatusServiceGrpc.getWatchStatusMethod) == null) {
          StatusServiceGrpc.getWatchStatusMethod = getWatchStatusMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.WatchStatusRequest, ca.stefanm.e39pim.v1.BridgeStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.WatchStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.BridgeStatus.getDefaultInstance()))
              .setSchemaDescriptor(new StatusServiceMethodDescriptorSupplier("WatchStatus"))
              .build();
        }
      }
    }
    return getWatchStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StatusServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StatusServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StatusServiceStub>() {
        @java.lang.Override
        public StatusServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StatusServiceStub(channel, callOptions);
        }
      };
    return StatusServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static StatusServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StatusServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StatusServiceBlockingV2Stub>() {
        @java.lang.Override
        public StatusServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StatusServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return StatusServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StatusServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StatusServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StatusServiceBlockingStub>() {
        @java.lang.Override
        public StatusServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StatusServiceBlockingStub(channel, callOptions);
        }
      };
    return StatusServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StatusServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StatusServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StatusServiceFutureStub>() {
        @java.lang.Override
        public StatusServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StatusServiceFutureStub(channel, callOptions);
        }
      };
    return StatusServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Never fails while the bridge is up (it works without Akonadi).
     * </pre>
     */
    default void getStatus(ca.stefanm.e39pim.v1.GetStatusRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStatusMethod(), responseObserver);
    }

    /**
     * <pre>
     * The current status first, then again whenever it changes.
     * </pre>
     */
    default void watchStatus(ca.stefanm.e39pim.v1.WatchStatusRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchStatusMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service StatusService.
   */
  public static abstract class StatusServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return StatusServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service StatusService.
   */
  public static final class StatusServiceStub
      extends io.grpc.stub.AbstractAsyncStub<StatusServiceStub> {
    private StatusServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatusServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StatusServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Never fails while the bridge is up (it works without Akonadi).
     * </pre>
     */
    public void getStatus(ca.stefanm.e39pim.v1.GetStatusRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * The current status first, then again whenever it changes.
     * </pre>
     */
    public void watchStatus(ca.stefanm.e39pim.v1.WatchStatusRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchStatusMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service StatusService.
   */
  public static final class StatusServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<StatusServiceBlockingV2Stub> {
    private StatusServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatusServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StatusServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Never fails while the bridge is up (it works without Akonadi).
     * </pre>
     */
    public ca.stefanm.e39pim.v1.BridgeStatus getStatus(ca.stefanm.e39pim.v1.GetStatusRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetStatusMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * The current status first, then again whenever it changes.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, ca.stefanm.e39pim.v1.BridgeStatus>
        watchStatus(ca.stefanm.e39pim.v1.WatchStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getWatchStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service StatusService.
   */
  public static final class StatusServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<StatusServiceBlockingStub> {
    private StatusServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatusServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StatusServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Never fails while the bridge is up (it works without Akonadi).
     * </pre>
     */
    public ca.stefanm.e39pim.v1.BridgeStatus getStatus(ca.stefanm.e39pim.v1.GetStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStatusMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * The current status first, then again whenever it changes.
     * </pre>
     */
    public java.util.Iterator<ca.stefanm.e39pim.v1.BridgeStatus> watchStatus(
        ca.stefanm.e39pim.v1.WatchStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service StatusService.
   */
  public static final class StatusServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<StatusServiceFutureStub> {
    private StatusServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatusServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StatusServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Never fails while the bridge is up (it works without Akonadi).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.BridgeStatus> getStatus(
        ca.stefanm.e39pim.v1.GetStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_STATUS = 0;
  private static final int METHODID_WATCH_STATUS = 1;

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
        case METHODID_GET_STATUS:
          serviceImpl.getStatus((ca.stefanm.e39pim.v1.GetStatusRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus>) responseObserver);
          break;
        case METHODID_WATCH_STATUS:
          serviceImpl.watchStatus((ca.stefanm.e39pim.v1.WatchStatusRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.BridgeStatus>) responseObserver);
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
          getGetStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.GetStatusRequest,
              ca.stefanm.e39pim.v1.BridgeStatus>(
                service, METHODID_GET_STATUS)))
        .addMethod(
          getWatchStatusMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.WatchStatusRequest,
              ca.stefanm.e39pim.v1.BridgeStatus>(
                service, METHODID_WATCH_STATUS)))
        .build();
  }

  private static abstract class StatusServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StatusServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.StatusProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StatusService");
    }
  }

  private static final class StatusServiceFileDescriptorSupplier
      extends StatusServiceBaseDescriptorSupplier {
    StatusServiceFileDescriptorSupplier() {}
  }

  private static final class StatusServiceMethodDescriptorSupplier
      extends StatusServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    StatusServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (StatusServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StatusServiceFileDescriptorSupplier())
              .addMethod(getGetStatusMethod())
              .addMethod(getWatchStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
