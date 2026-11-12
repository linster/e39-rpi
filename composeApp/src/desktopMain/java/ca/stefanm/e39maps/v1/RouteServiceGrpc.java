package ca.stefanm.e39maps.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class RouteServiceGrpc {

  private RouteServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39maps.v1.RouteService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ComputeRouteRequest,
      ca.stefanm.e39maps.v1.Route> getComputeRouteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ComputeRoute",
      requestType = ca.stefanm.e39maps.v1.ComputeRouteRequest.class,
      responseType = ca.stefanm.e39maps.v1.Route.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ComputeRouteRequest,
      ca.stefanm.e39maps.v1.Route> getComputeRouteMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ComputeRouteRequest, ca.stefanm.e39maps.v1.Route> getComputeRouteMethod;
    if ((getComputeRouteMethod = RouteServiceGrpc.getComputeRouteMethod) == null) {
      synchronized (RouteServiceGrpc.class) {
        if ((getComputeRouteMethod = RouteServiceGrpc.getComputeRouteMethod) == null) {
          RouteServiceGrpc.getComputeRouteMethod = getComputeRouteMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.ComputeRouteRequest, ca.stefanm.e39maps.v1.Route>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ComputeRoute"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.ComputeRouteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.Route.getDefaultInstance()))
              .setSchemaDescriptor(new RouteServiceMethodDescriptorSupplier("ComputeRoute"))
              .build();
        }
      }
    }
    return getComputeRouteMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RouteServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RouteServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RouteServiceStub>() {
        @java.lang.Override
        public RouteServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RouteServiceStub(channel, callOptions);
        }
      };
    return RouteServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static RouteServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RouteServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RouteServiceBlockingV2Stub>() {
        @java.lang.Override
        public RouteServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RouteServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return RouteServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RouteServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RouteServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RouteServiceBlockingStub>() {
        @java.lang.Override
        public RouteServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RouteServiceBlockingStub(channel, callOptions);
        }
      };
    return RouteServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RouteServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RouteServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RouteServiceFutureStub>() {
        @java.lang.Override
        public RouteServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RouteServiceFutureStub(channel, callOptions);
        }
      };
    return RouteServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * INVALID_ARGUMENT for missing/out-of-range points; NOT_FOUND if no route
     * connects them (e.g. a point far from any road, or outside the map
     * data); UNAVAILABLE if the routing tiles (or Valhalla) aren't installed.
     * Via points are stops: each ends a leg; the legs are joined into one
     * shape, with the maneuvers' shape indexes into the joined shape.
     * </pre>
     */
    default void computeRoute(ca.stefanm.e39maps.v1.ComputeRouteRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Route> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getComputeRouteMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service RouteService.
   */
  public static abstract class RouteServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RouteServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service RouteService.
   */
  public static final class RouteServiceStub
      extends io.grpc.stub.AbstractAsyncStub<RouteServiceStub> {
    private RouteServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RouteServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RouteServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * INVALID_ARGUMENT for missing/out-of-range points; NOT_FOUND if no route
     * connects them (e.g. a point far from any road, or outside the map
     * data); UNAVAILABLE if the routing tiles (or Valhalla) aren't installed.
     * Via points are stops: each ends a leg; the legs are joined into one
     * shape, with the maneuvers' shape indexes into the joined shape.
     * </pre>
     */
    public void computeRoute(ca.stefanm.e39maps.v1.ComputeRouteRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Route> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getComputeRouteMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service RouteService.
   */
  public static final class RouteServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<RouteServiceBlockingV2Stub> {
    private RouteServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RouteServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RouteServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * INVALID_ARGUMENT for missing/out-of-range points; NOT_FOUND if no route
     * connects them (e.g. a point far from any road, or outside the map
     * data); UNAVAILABLE if the routing tiles (or Valhalla) aren't installed.
     * Via points are stops: each ends a leg; the legs are joined into one
     * shape, with the maneuvers' shape indexes into the joined shape.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Route computeRoute(ca.stefanm.e39maps.v1.ComputeRouteRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getComputeRouteMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service RouteService.
   */
  public static final class RouteServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RouteServiceBlockingStub> {
    private RouteServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RouteServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RouteServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * INVALID_ARGUMENT for missing/out-of-range points; NOT_FOUND if no route
     * connects them (e.g. a point far from any road, or outside the map
     * data); UNAVAILABLE if the routing tiles (or Valhalla) aren't installed.
     * Via points are stops: each ends a leg; the legs are joined into one
     * shape, with the maneuvers' shape indexes into the joined shape.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Route computeRoute(ca.stefanm.e39maps.v1.ComputeRouteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getComputeRouteMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service RouteService.
   */
  public static final class RouteServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<RouteServiceFutureStub> {
    private RouteServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RouteServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RouteServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * INVALID_ARGUMENT for missing/out-of-range points; NOT_FOUND if no route
     * connects them (e.g. a point far from any road, or outside the map
     * data); UNAVAILABLE if the routing tiles (or Valhalla) aren't installed.
     * Via points are stops: each ends a leg; the legs are joined into one
     * shape, with the maneuvers' shape indexes into the joined shape.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.Route> computeRoute(
        ca.stefanm.e39maps.v1.ComputeRouteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getComputeRouteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_COMPUTE_ROUTE = 0;

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
        case METHODID_COMPUTE_ROUTE:
          serviceImpl.computeRoute((ca.stefanm.e39maps.v1.ComputeRouteRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Route>) responseObserver);
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
          getComputeRouteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.ComputeRouteRequest,
              ca.stefanm.e39maps.v1.Route>(
                service, METHODID_COMPUTE_ROUTE)))
        .build();
  }

  private static abstract class RouteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RouteServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39maps.v1.Routes.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RouteService");
    }
  }

  private static final class RouteServiceFileDescriptorSupplier
      extends RouteServiceBaseDescriptorSupplier {
    RouteServiceFileDescriptorSupplier() {}
  }

  private static final class RouteServiceMethodDescriptorSupplier
      extends RouteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    RouteServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (RouteServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RouteServiceFileDescriptorSupplier())
              .addMethod(getComputeRouteMethod())
              .build();
        }
      }
    }
    return result;
  }
}
