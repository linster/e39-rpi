package ca.stefanm.e39maps.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class PoiServiceGrpc {

  private PoiServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39maps.v1.PoiService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ListPoisRequest,
      ca.stefanm.e39maps.v1.ListPoisResponse> getListPoisMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListPois",
      requestType = ca.stefanm.e39maps.v1.ListPoisRequest.class,
      responseType = ca.stefanm.e39maps.v1.ListPoisResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ListPoisRequest,
      ca.stefanm.e39maps.v1.ListPoisResponse> getListPoisMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.ListPoisRequest, ca.stefanm.e39maps.v1.ListPoisResponse> getListPoisMethod;
    if ((getListPoisMethod = PoiServiceGrpc.getListPoisMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getListPoisMethod = PoiServiceGrpc.getListPoisMethod) == null) {
          PoiServiceGrpc.getListPoisMethod = getListPoisMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.ListPoisRequest, ca.stefanm.e39maps.v1.ListPoisResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListPois"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.ListPoisRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.ListPoisResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("ListPois"))
              .build();
        }
      }
    }
    return getListPoisMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.GetPoiRequest,
      ca.stefanm.e39maps.v1.Poi> getGetPoiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPoi",
      requestType = ca.stefanm.e39maps.v1.GetPoiRequest.class,
      responseType = ca.stefanm.e39maps.v1.Poi.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.GetPoiRequest,
      ca.stefanm.e39maps.v1.Poi> getGetPoiMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.GetPoiRequest, ca.stefanm.e39maps.v1.Poi> getGetPoiMethod;
    if ((getGetPoiMethod = PoiServiceGrpc.getGetPoiMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getGetPoiMethod = PoiServiceGrpc.getGetPoiMethod) == null) {
          PoiServiceGrpc.getGetPoiMethod = getGetPoiMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.GetPoiRequest, ca.stefanm.e39maps.v1.Poi>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPoi"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.GetPoiRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.Poi.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("GetPoi"))
              .build();
        }
      }
    }
    return getGetPoiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.CreatePoiRequest,
      ca.stefanm.e39maps.v1.Poi> getCreatePoiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreatePoi",
      requestType = ca.stefanm.e39maps.v1.CreatePoiRequest.class,
      responseType = ca.stefanm.e39maps.v1.Poi.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.CreatePoiRequest,
      ca.stefanm.e39maps.v1.Poi> getCreatePoiMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.CreatePoiRequest, ca.stefanm.e39maps.v1.Poi> getCreatePoiMethod;
    if ((getCreatePoiMethod = PoiServiceGrpc.getCreatePoiMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getCreatePoiMethod = PoiServiceGrpc.getCreatePoiMethod) == null) {
          PoiServiceGrpc.getCreatePoiMethod = getCreatePoiMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.CreatePoiRequest, ca.stefanm.e39maps.v1.Poi>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreatePoi"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.CreatePoiRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.Poi.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("CreatePoi"))
              .build();
        }
      }
    }
    return getCreatePoiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.UpdatePoiRequest,
      ca.stefanm.e39maps.v1.Poi> getUpdatePoiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdatePoi",
      requestType = ca.stefanm.e39maps.v1.UpdatePoiRequest.class,
      responseType = ca.stefanm.e39maps.v1.Poi.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.UpdatePoiRequest,
      ca.stefanm.e39maps.v1.Poi> getUpdatePoiMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.UpdatePoiRequest, ca.stefanm.e39maps.v1.Poi> getUpdatePoiMethod;
    if ((getUpdatePoiMethod = PoiServiceGrpc.getUpdatePoiMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getUpdatePoiMethod = PoiServiceGrpc.getUpdatePoiMethod) == null) {
          PoiServiceGrpc.getUpdatePoiMethod = getUpdatePoiMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.UpdatePoiRequest, ca.stefanm.e39maps.v1.Poi>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdatePoi"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.UpdatePoiRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.Poi.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("UpdatePoi"))
              .build();
        }
      }
    }
    return getUpdatePoiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.DeletePoiRequest,
      com.google.protobuf.Empty> getDeletePoiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeletePoi",
      requestType = ca.stefanm.e39maps.v1.DeletePoiRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.DeletePoiRequest,
      com.google.protobuf.Empty> getDeletePoiMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.DeletePoiRequest, com.google.protobuf.Empty> getDeletePoiMethod;
    if ((getDeletePoiMethod = PoiServiceGrpc.getDeletePoiMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getDeletePoiMethod = PoiServiceGrpc.getDeletePoiMethod) == null) {
          PoiServiceGrpc.getDeletePoiMethod = getDeletePoiMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.DeletePoiRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeletePoi"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.DeletePoiRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("DeletePoi"))
              .build();
        }
      }
    }
    return getDeletePoiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.SearchPoisRequest,
      ca.stefanm.e39maps.v1.SearchPoisResponse> getSearchPoisMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchPois",
      requestType = ca.stefanm.e39maps.v1.SearchPoisRequest.class,
      responseType = ca.stefanm.e39maps.v1.SearchPoisResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.SearchPoisRequest,
      ca.stefanm.e39maps.v1.SearchPoisResponse> getSearchPoisMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39maps.v1.SearchPoisRequest, ca.stefanm.e39maps.v1.SearchPoisResponse> getSearchPoisMethod;
    if ((getSearchPoisMethod = PoiServiceGrpc.getSearchPoisMethod) == null) {
      synchronized (PoiServiceGrpc.class) {
        if ((getSearchPoisMethod = PoiServiceGrpc.getSearchPoisMethod) == null) {
          PoiServiceGrpc.getSearchPoisMethod = getSearchPoisMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39maps.v1.SearchPoisRequest, ca.stefanm.e39maps.v1.SearchPoisResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchPois"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.SearchPoisRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39maps.v1.SearchPoisResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PoiServiceMethodDescriptorSupplier("SearchPois"))
              .build();
        }
      }
    }
    return getSearchPoisMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PoiServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PoiServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PoiServiceStub>() {
        @java.lang.Override
        public PoiServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PoiServiceStub(channel, callOptions);
        }
      };
    return PoiServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static PoiServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PoiServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PoiServiceBlockingV2Stub>() {
        @java.lang.Override
        public PoiServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PoiServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return PoiServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PoiServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PoiServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PoiServiceBlockingStub>() {
        @java.lang.Override
        public PoiServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PoiServiceBlockingStub(channel, callOptions);
        }
      };
    return PoiServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PoiServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PoiServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PoiServiceFutureStub>() {
        @java.lang.Override
        public PoiServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PoiServiceFutureStub(channel, callOptions);
        }
      };
    return PoiServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * The user's POIs, by name.
     * </pre>
     */
    default void listPois(ca.stefanm.e39maps.v1.ListPoisRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.ListPoisResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListPoisMethod(), responseObserver);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    default void getPoi(ca.stefanm.e39maps.v1.GetPoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPoiMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a user POI (id, source and timestamps are set by the server).
     * INVALID_ARGUMENT for a missing name or location, or coordinates out of
     * range.
     * </pre>
     */
    default void createPoi(ca.stefanm.e39maps.v1.CreatePoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreatePoiMethod(), responseObserver);
    }

    /**
     * <pre>
     * Changes the fields named in update_mask (all editable fields if it's
     * empty). NOT_FOUND / INVALID_ARGUMENT as above.
     * </pre>
     */
    default void updatePoi(ca.stefanm.e39maps.v1.UpdatePoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdatePoiMethod(), responseObserver);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    default void deletePoi(ca.stefanm.e39maps.v1.DeletePoiRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeletePoiMethod(), responseObserver);
    }

    /**
     * <pre>
     * POIs near a point or inside an area, optionally matching a name, nearest
     * first (near) or by name (bounding box / no area).
     * </pre>
     */
    default void searchPois(ca.stefanm.e39maps.v1.SearchPoisRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.SearchPoisResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchPoisMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PoiService.
   */
  public static abstract class PoiServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PoiServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PoiService.
   */
  public static final class PoiServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PoiServiceStub> {
    private PoiServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PoiServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PoiServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * The user's POIs, by name.
     * </pre>
     */
    public void listPois(ca.stefanm.e39maps.v1.ListPoisRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.ListPoisResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListPoisMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public void getPoi(ca.stefanm.e39maps.v1.GetPoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPoiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a user POI (id, source and timestamps are set by the server).
     * INVALID_ARGUMENT for a missing name or location, or coordinates out of
     * range.
     * </pre>
     */
    public void createPoi(ca.stefanm.e39maps.v1.CreatePoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreatePoiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Changes the fields named in update_mask (all editable fields if it's
     * empty). NOT_FOUND / INVALID_ARGUMENT as above.
     * </pre>
     */
    public void updatePoi(ca.stefanm.e39maps.v1.UpdatePoiRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdatePoiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public void deletePoi(ca.stefanm.e39maps.v1.DeletePoiRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeletePoiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * POIs near a point or inside an area, optionally matching a name, nearest
     * first (near) or by name (bounding box / no area).
     * </pre>
     */
    public void searchPois(ca.stefanm.e39maps.v1.SearchPoisRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.SearchPoisResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchPoisMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PoiService.
   */
  public static final class PoiServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<PoiServiceBlockingV2Stub> {
    private PoiServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PoiServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PoiServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * The user's POIs, by name.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.ListPoisResponse listPois(ca.stefanm.e39maps.v1.ListPoisRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListPoisMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi getPoi(ca.stefanm.e39maps.v1.GetPoiRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetPoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a user POI (id, source and timestamps are set by the server).
     * INVALID_ARGUMENT for a missing name or location, or coordinates out of
     * range.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi createPoi(ca.stefanm.e39maps.v1.CreatePoiRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCreatePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Changes the fields named in update_mask (all editable fields if it's
     * empty). NOT_FOUND / INVALID_ARGUMENT as above.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi updatePoi(ca.stefanm.e39maps.v1.UpdatePoiRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getUpdatePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public com.google.protobuf.Empty deletePoi(ca.stefanm.e39maps.v1.DeletePoiRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getDeletePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * POIs near a point or inside an area, optionally matching a name, nearest
     * first (near) or by name (bounding box / no area).
     * </pre>
     */
    public ca.stefanm.e39maps.v1.SearchPoisResponse searchPois(ca.stefanm.e39maps.v1.SearchPoisRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSearchPoisMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service PoiService.
   */
  public static final class PoiServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PoiServiceBlockingStub> {
    private PoiServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PoiServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PoiServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * The user's POIs, by name.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.ListPoisResponse listPois(ca.stefanm.e39maps.v1.ListPoisRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListPoisMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi getPoi(ca.stefanm.e39maps.v1.GetPoiRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a user POI (id, source and timestamps are set by the server).
     * INVALID_ARGUMENT for a missing name or location, or coordinates out of
     * range.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi createPoi(ca.stefanm.e39maps.v1.CreatePoiRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreatePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Changes the fields named in update_mask (all editable fields if it's
     * empty). NOT_FOUND / INVALID_ARGUMENT as above.
     * </pre>
     */
    public ca.stefanm.e39maps.v1.Poi updatePoi(ca.stefanm.e39maps.v1.UpdatePoiRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdatePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public com.google.protobuf.Empty deletePoi(ca.stefanm.e39maps.v1.DeletePoiRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeletePoiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * POIs near a point or inside an area, optionally matching a name, nearest
     * first (near) or by name (bounding box / no area).
     * </pre>
     */
    public ca.stefanm.e39maps.v1.SearchPoisResponse searchPois(ca.stefanm.e39maps.v1.SearchPoisRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchPoisMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PoiService.
   */
  public static final class PoiServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PoiServiceFutureStub> {
    private PoiServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PoiServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PoiServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * The user's POIs, by name.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.ListPoisResponse> listPois(
        ca.stefanm.e39maps.v1.ListPoisRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListPoisMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.Poi> getPoi(
        ca.stefanm.e39maps.v1.GetPoiRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPoiMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a user POI (id, source and timestamps are set by the server).
     * INVALID_ARGUMENT for a missing name or location, or coordinates out of
     * range.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.Poi> createPoi(
        ca.stefanm.e39maps.v1.CreatePoiRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreatePoiMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Changes the fields named in update_mask (all editable fields if it's
     * empty). NOT_FOUND / INVALID_ARGUMENT as above.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.Poi> updatePoi(
        ca.stefanm.e39maps.v1.UpdatePoiRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdatePoiMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * NOT_FOUND if there's no POI with that id.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deletePoi(
        ca.stefanm.e39maps.v1.DeletePoiRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeletePoiMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * POIs near a point or inside an area, optionally matching a name, nearest
     * first (near) or by name (bounding box / no area).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39maps.v1.SearchPoisResponse> searchPois(
        ca.stefanm.e39maps.v1.SearchPoisRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchPoisMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_POIS = 0;
  private static final int METHODID_GET_POI = 1;
  private static final int METHODID_CREATE_POI = 2;
  private static final int METHODID_UPDATE_POI = 3;
  private static final int METHODID_DELETE_POI = 4;
  private static final int METHODID_SEARCH_POIS = 5;

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
        case METHODID_LIST_POIS:
          serviceImpl.listPois((ca.stefanm.e39maps.v1.ListPoisRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.ListPoisResponse>) responseObserver);
          break;
        case METHODID_GET_POI:
          serviceImpl.getPoi((ca.stefanm.e39maps.v1.GetPoiRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi>) responseObserver);
          break;
        case METHODID_CREATE_POI:
          serviceImpl.createPoi((ca.stefanm.e39maps.v1.CreatePoiRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi>) responseObserver);
          break;
        case METHODID_UPDATE_POI:
          serviceImpl.updatePoi((ca.stefanm.e39maps.v1.UpdatePoiRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.Poi>) responseObserver);
          break;
        case METHODID_DELETE_POI:
          serviceImpl.deletePoi((ca.stefanm.e39maps.v1.DeletePoiRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SEARCH_POIS:
          serviceImpl.searchPois((ca.stefanm.e39maps.v1.SearchPoisRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39maps.v1.SearchPoisResponse>) responseObserver);
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
          getListPoisMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.ListPoisRequest,
              ca.stefanm.e39maps.v1.ListPoisResponse>(
                service, METHODID_LIST_POIS)))
        .addMethod(
          getGetPoiMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.GetPoiRequest,
              ca.stefanm.e39maps.v1.Poi>(
                service, METHODID_GET_POI)))
        .addMethod(
          getCreatePoiMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.CreatePoiRequest,
              ca.stefanm.e39maps.v1.Poi>(
                service, METHODID_CREATE_POI)))
        .addMethod(
          getUpdatePoiMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.UpdatePoiRequest,
              ca.stefanm.e39maps.v1.Poi>(
                service, METHODID_UPDATE_POI)))
        .addMethod(
          getDeletePoiMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.DeletePoiRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_POI)))
        .addMethod(
          getSearchPoisMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39maps.v1.SearchPoisRequest,
              ca.stefanm.e39maps.v1.SearchPoisResponse>(
                service, METHODID_SEARCH_POIS)))
        .build();
  }

  private static abstract class PoiServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PoiServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39maps.v1.Pois.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PoiService");
    }
  }

  private static final class PoiServiceFileDescriptorSupplier
      extends PoiServiceBaseDescriptorSupplier {
    PoiServiceFileDescriptorSupplier() {}
  }

  private static final class PoiServiceMethodDescriptorSupplier
      extends PoiServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PoiServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (PoiServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PoiServiceFileDescriptorSupplier())
              .addMethod(getListPoisMethod())
              .addMethod(getGetPoiMethod())
              .addMethod(getCreatePoiMethod())
              .addMethod(getUpdatePoiMethod())
              .addMethod(getDeletePoiMethod())
              .addMethod(getSearchPoisMethod())
              .build();
        }
      }
    }
    return result;
  }
}
