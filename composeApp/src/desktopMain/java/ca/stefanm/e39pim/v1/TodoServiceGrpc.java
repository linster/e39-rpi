package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class TodoServiceGrpc {

  private TodoServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.TodoService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListTodosRequest,
      ca.stefanm.e39pim.v1.ListTodosResponse> getListTodosMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTodos",
      requestType = ca.stefanm.e39pim.v1.ListTodosRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListTodosResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListTodosRequest,
      ca.stefanm.e39pim.v1.ListTodosResponse> getListTodosMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListTodosRequest, ca.stefanm.e39pim.v1.ListTodosResponse> getListTodosMethod;
    if ((getListTodosMethod = TodoServiceGrpc.getListTodosMethod) == null) {
      synchronized (TodoServiceGrpc.class) {
        if ((getListTodosMethod = TodoServiceGrpc.getListTodosMethod) == null) {
          TodoServiceGrpc.getListTodosMethod = getListTodosMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListTodosRequest, ca.stefanm.e39pim.v1.ListTodosResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTodos"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListTodosRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListTodosResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TodoServiceMethodDescriptorSupplier("ListTodos"))
              .build();
        }
      }
    }
    return getListTodosMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateTodoRequest,
      ca.stefanm.e39pim.v1.Todo> getCreateTodoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateTodo",
      requestType = ca.stefanm.e39pim.v1.CreateTodoRequest.class,
      responseType = ca.stefanm.e39pim.v1.Todo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateTodoRequest,
      ca.stefanm.e39pim.v1.Todo> getCreateTodoMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateTodoRequest, ca.stefanm.e39pim.v1.Todo> getCreateTodoMethod;
    if ((getCreateTodoMethod = TodoServiceGrpc.getCreateTodoMethod) == null) {
      synchronized (TodoServiceGrpc.class) {
        if ((getCreateTodoMethod = TodoServiceGrpc.getCreateTodoMethod) == null) {
          TodoServiceGrpc.getCreateTodoMethod = getCreateTodoMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.CreateTodoRequest, ca.stefanm.e39pim.v1.Todo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateTodo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.CreateTodoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Todo.getDefaultInstance()))
              .setSchemaDescriptor(new TodoServiceMethodDescriptorSupplier("CreateTodo"))
              .build();
        }
      }
    }
    return getCreateTodoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateTodoRequest,
      ca.stefanm.e39pim.v1.Todo> getUpdateTodoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateTodo",
      requestType = ca.stefanm.e39pim.v1.UpdateTodoRequest.class,
      responseType = ca.stefanm.e39pim.v1.Todo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateTodoRequest,
      ca.stefanm.e39pim.v1.Todo> getUpdateTodoMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateTodoRequest, ca.stefanm.e39pim.v1.Todo> getUpdateTodoMethod;
    if ((getUpdateTodoMethod = TodoServiceGrpc.getUpdateTodoMethod) == null) {
      synchronized (TodoServiceGrpc.class) {
        if ((getUpdateTodoMethod = TodoServiceGrpc.getUpdateTodoMethod) == null) {
          TodoServiceGrpc.getUpdateTodoMethod = getUpdateTodoMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.UpdateTodoRequest, ca.stefanm.e39pim.v1.Todo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateTodo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.UpdateTodoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Todo.getDefaultInstance()))
              .setSchemaDescriptor(new TodoServiceMethodDescriptorSupplier("UpdateTodo"))
              .build();
        }
      }
    }
    return getUpdateTodoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetTodoCompletedRequest,
      ca.stefanm.e39pim.v1.Todo> getSetTodoCompletedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetTodoCompleted",
      requestType = ca.stefanm.e39pim.v1.SetTodoCompletedRequest.class,
      responseType = ca.stefanm.e39pim.v1.Todo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetTodoCompletedRequest,
      ca.stefanm.e39pim.v1.Todo> getSetTodoCompletedMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetTodoCompletedRequest, ca.stefanm.e39pim.v1.Todo> getSetTodoCompletedMethod;
    if ((getSetTodoCompletedMethod = TodoServiceGrpc.getSetTodoCompletedMethod) == null) {
      synchronized (TodoServiceGrpc.class) {
        if ((getSetTodoCompletedMethod = TodoServiceGrpc.getSetTodoCompletedMethod) == null) {
          TodoServiceGrpc.getSetTodoCompletedMethod = getSetTodoCompletedMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SetTodoCompletedRequest, ca.stefanm.e39pim.v1.Todo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetTodoCompleted"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SetTodoCompletedRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Todo.getDefaultInstance()))
              .setSchemaDescriptor(new TodoServiceMethodDescriptorSupplier("SetTodoCompleted"))
              .build();
        }
      }
    }
    return getSetTodoCompletedMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteTodoRequest,
      com.google.protobuf.Empty> getDeleteTodoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTodo",
      requestType = ca.stefanm.e39pim.v1.DeleteTodoRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteTodoRequest,
      com.google.protobuf.Empty> getDeleteTodoMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteTodoRequest, com.google.protobuf.Empty> getDeleteTodoMethod;
    if ((getDeleteTodoMethod = TodoServiceGrpc.getDeleteTodoMethod) == null) {
      synchronized (TodoServiceGrpc.class) {
        if ((getDeleteTodoMethod = TodoServiceGrpc.getDeleteTodoMethod) == null) {
          TodoServiceGrpc.getDeleteTodoMethod = getDeleteTodoMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.DeleteTodoRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTodo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.DeleteTodoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new TodoServiceMethodDescriptorSupplier("DeleteTodo"))
              .build();
        }
      }
    }
    return getDeleteTodoMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TodoServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TodoServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TodoServiceStub>() {
        @java.lang.Override
        public TodoServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TodoServiceStub(channel, callOptions);
        }
      };
    return TodoServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static TodoServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TodoServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TodoServiceBlockingV2Stub>() {
        @java.lang.Override
        public TodoServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TodoServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return TodoServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TodoServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TodoServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TodoServiceBlockingStub>() {
        @java.lang.Override
        public TodoServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TodoServiceBlockingStub(channel, callOptions);
        }
      };
    return TodoServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TodoServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TodoServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TodoServiceFutureStub>() {
        @java.lang.Override
        public TodoServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TodoServiceFutureStub(channel, callOptions);
        }
      };
    return TodoServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Sorted: not completed first, then by due (none last), then priority,
     * then summary.
     * </pre>
     */
    default void listTodos(ca.stefanm.e39pim.v1.ListTodosRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListTodosResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTodosMethod(), responseObserver);
    }

    /**
     * <pre>
     * FAILED_PRECONDITION if the calendar is read-only or holds no to-dos.
     * </pre>
     */
    default void createTodo(ca.stefanm.e39pim.v1.CreateTodoRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateTodoMethod(), responseObserver);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    default void updateTodo(ca.stefanm.e39pim.v1.UpdateTodoRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateTodoMethod(), responseObserver);
    }

    /**
     * <pre>
     * Shortcut for the knob: tick/untick. ABORTED on a revision mismatch.
     * </pre>
     */
    default void setTodoCompleted(ca.stefanm.e39pim.v1.SetTodoCompletedRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetTodoCompletedMethod(), responseObserver);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    default void deleteTodo(ca.stefanm.e39pim.v1.DeleteTodoRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTodoMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TodoService.
   */
  public static abstract class TodoServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TodoServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TodoService.
   */
  public static final class TodoServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TodoServiceStub> {
    private TodoServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TodoServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TodoServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sorted: not completed first, then by due (none last), then priority,
     * then summary.
     * </pre>
     */
    public void listTodos(ca.stefanm.e39pim.v1.ListTodosRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListTodosResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTodosMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * FAILED_PRECONDITION if the calendar is read-only or holds no to-dos.
     * </pre>
     */
    public void createTodo(ca.stefanm.e39pim.v1.CreateTodoRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateTodoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public void updateTodo(ca.stefanm.e39pim.v1.UpdateTodoRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateTodoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Shortcut for the knob: tick/untick. ABORTED on a revision mismatch.
     * </pre>
     */
    public void setTodoCompleted(ca.stefanm.e39pim.v1.SetTodoCompletedRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetTodoCompletedMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public void deleteTodo(ca.stefanm.e39pim.v1.DeleteTodoRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTodoMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TodoService.
   */
  public static final class TodoServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<TodoServiceBlockingV2Stub> {
    private TodoServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TodoServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TodoServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Sorted: not completed first, then by due (none last), then priority,
     * then summary.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListTodosResponse listTodos(ca.stefanm.e39pim.v1.ListTodosRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListTodosMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * FAILED_PRECONDITION if the calendar is read-only or holds no to-dos.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo createTodo(ca.stefanm.e39pim.v1.CreateTodoRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCreateTodoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo updateTodo(ca.stefanm.e39pim.v1.UpdateTodoRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getUpdateTodoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Shortcut for the knob: tick/untick. ABORTED on a revision mismatch.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo setTodoCompleted(ca.stefanm.e39pim.v1.SetTodoCompletedRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSetTodoCompletedMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public com.google.protobuf.Empty deleteTodo(ca.stefanm.e39pim.v1.DeleteTodoRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getDeleteTodoMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service TodoService.
   */
  public static final class TodoServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TodoServiceBlockingStub> {
    private TodoServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TodoServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TodoServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sorted: not completed first, then by due (none last), then priority,
     * then summary.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListTodosResponse listTodos(ca.stefanm.e39pim.v1.ListTodosRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTodosMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * FAILED_PRECONDITION if the calendar is read-only or holds no to-dos.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo createTodo(ca.stefanm.e39pim.v1.CreateTodoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateTodoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo updateTodo(ca.stefanm.e39pim.v1.UpdateTodoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateTodoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Shortcut for the knob: tick/untick. ABORTED on a revision mismatch.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Todo setTodoCompleted(ca.stefanm.e39pim.v1.SetTodoCompletedRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetTodoCompletedMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public com.google.protobuf.Empty deleteTodo(ca.stefanm.e39pim.v1.DeleteTodoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTodoMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TodoService.
   */
  public static final class TodoServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TodoServiceFutureStub> {
    private TodoServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TodoServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TodoServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sorted: not completed first, then by due (none last), then priority,
     * then summary.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListTodosResponse> listTodos(
        ca.stefanm.e39pim.v1.ListTodosRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTodosMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * FAILED_PRECONDITION if the calendar is read-only or holds no to-dos.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Todo> createTodo(
        ca.stefanm.e39pim.v1.CreateTodoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateTodoMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Todo> updateTodo(
        ca.stefanm.e39pim.v1.UpdateTodoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateTodoMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Shortcut for the knob: tick/untick. ABORTED on a revision mismatch.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Todo> setTodoCompleted(
        ca.stefanm.e39pim.v1.SetTodoCompletedRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetTodoCompletedMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * ABORTED on a revision mismatch.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteTodo(
        ca.stefanm.e39pim.v1.DeleteTodoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTodoMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_TODOS = 0;
  private static final int METHODID_CREATE_TODO = 1;
  private static final int METHODID_UPDATE_TODO = 2;
  private static final int METHODID_SET_TODO_COMPLETED = 3;
  private static final int METHODID_DELETE_TODO = 4;

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
        case METHODID_LIST_TODOS:
          serviceImpl.listTodos((ca.stefanm.e39pim.v1.ListTodosRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListTodosResponse>) responseObserver);
          break;
        case METHODID_CREATE_TODO:
          serviceImpl.createTodo((ca.stefanm.e39pim.v1.CreateTodoRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo>) responseObserver);
          break;
        case METHODID_UPDATE_TODO:
          serviceImpl.updateTodo((ca.stefanm.e39pim.v1.UpdateTodoRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo>) responseObserver);
          break;
        case METHODID_SET_TODO_COMPLETED:
          serviceImpl.setTodoCompleted((ca.stefanm.e39pim.v1.SetTodoCompletedRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Todo>) responseObserver);
          break;
        case METHODID_DELETE_TODO:
          serviceImpl.deleteTodo((ca.stefanm.e39pim.v1.DeleteTodoRequest) request,
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
          getListTodosMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListTodosRequest,
              ca.stefanm.e39pim.v1.ListTodosResponse>(
                service, METHODID_LIST_TODOS)))
        .addMethod(
          getCreateTodoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.CreateTodoRequest,
              ca.stefanm.e39pim.v1.Todo>(
                service, METHODID_CREATE_TODO)))
        .addMethod(
          getUpdateTodoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.UpdateTodoRequest,
              ca.stefanm.e39pim.v1.Todo>(
                service, METHODID_UPDATE_TODO)))
        .addMethod(
          getSetTodoCompletedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SetTodoCompletedRequest,
              ca.stefanm.e39pim.v1.Todo>(
                service, METHODID_SET_TODO_COMPLETED)))
        .addMethod(
          getDeleteTodoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.DeleteTodoRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_TODO)))
        .build();
  }

  private static abstract class TodoServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TodoServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.TodoProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TodoService");
    }
  }

  private static final class TodoServiceFileDescriptorSupplier
      extends TodoServiceBaseDescriptorSupplier {
    TodoServiceFileDescriptorSupplier() {}
  }

  private static final class TodoServiceMethodDescriptorSupplier
      extends TodoServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TodoServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (TodoServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TodoServiceFileDescriptorSupplier())
              .addMethod(getListTodosMethod())
              .addMethod(getCreateTodoMethod())
              .addMethod(getUpdateTodoMethod())
              .addMethod(getSetTodoCompletedMethod())
              .addMethod(getDeleteTodoMethod())
              .build();
        }
      }
    }
    return result;
  }
}
