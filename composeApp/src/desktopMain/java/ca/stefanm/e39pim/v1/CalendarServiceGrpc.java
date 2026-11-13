package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class CalendarServiceGrpc {

  private CalendarServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.CalendarService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListCalendarsRequest,
      ca.stefanm.e39pim.v1.ListCalendarsResponse> getListCalendarsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListCalendars",
      requestType = ca.stefanm.e39pim.v1.ListCalendarsRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListCalendarsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListCalendarsRequest,
      ca.stefanm.e39pim.v1.ListCalendarsResponse> getListCalendarsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListCalendarsRequest, ca.stefanm.e39pim.v1.ListCalendarsResponse> getListCalendarsMethod;
    if ((getListCalendarsMethod = CalendarServiceGrpc.getListCalendarsMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getListCalendarsMethod = CalendarServiceGrpc.getListCalendarsMethod) == null) {
          CalendarServiceGrpc.getListCalendarsMethod = getListCalendarsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListCalendarsRequest, ca.stefanm.e39pim.v1.ListCalendarsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListCalendars"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListCalendarsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListCalendarsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("ListCalendars"))
              .build();
        }
      }
    }
    return getListCalendarsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListEventsRequest,
      ca.stefanm.e39pim.v1.ListEventsResponse> getListEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListEvents",
      requestType = ca.stefanm.e39pim.v1.ListEventsRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListEventsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListEventsRequest,
      ca.stefanm.e39pim.v1.ListEventsResponse> getListEventsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListEventsRequest, ca.stefanm.e39pim.v1.ListEventsResponse> getListEventsMethod;
    if ((getListEventsMethod = CalendarServiceGrpc.getListEventsMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getListEventsMethod = CalendarServiceGrpc.getListEventsMethod) == null) {
          CalendarServiceGrpc.getListEventsMethod = getListEventsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListEventsRequest, ca.stefanm.e39pim.v1.ListEventsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListEventsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("ListEvents"))
              .build();
        }
      }
    }
    return getListEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetEventRequest,
      ca.stefanm.e39pim.v1.Event> getGetEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEvent",
      requestType = ca.stefanm.e39pim.v1.GetEventRequest.class,
      responseType = ca.stefanm.e39pim.v1.Event.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetEventRequest,
      ca.stefanm.e39pim.v1.Event> getGetEventMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetEventRequest, ca.stefanm.e39pim.v1.Event> getGetEventMethod;
    if ((getGetEventMethod = CalendarServiceGrpc.getGetEventMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getGetEventMethod = CalendarServiceGrpc.getGetEventMethod) == null) {
          CalendarServiceGrpc.getGetEventMethod = getGetEventMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.GetEventRequest, ca.stefanm.e39pim.v1.Event>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.GetEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Event.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("GetEvent"))
              .build();
        }
      }
    }
    return getGetEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateEventRequest,
      ca.stefanm.e39pim.v1.Event> getCreateEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateEvent",
      requestType = ca.stefanm.e39pim.v1.CreateEventRequest.class,
      responseType = ca.stefanm.e39pim.v1.Event.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateEventRequest,
      ca.stefanm.e39pim.v1.Event> getCreateEventMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.CreateEventRequest, ca.stefanm.e39pim.v1.Event> getCreateEventMethod;
    if ((getCreateEventMethod = CalendarServiceGrpc.getCreateEventMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getCreateEventMethod = CalendarServiceGrpc.getCreateEventMethod) == null) {
          CalendarServiceGrpc.getCreateEventMethod = getCreateEventMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.CreateEventRequest, ca.stefanm.e39pim.v1.Event>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.CreateEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Event.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("CreateEvent"))
              .build();
        }
      }
    }
    return getCreateEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateEventRequest,
      ca.stefanm.e39pim.v1.Event> getUpdateEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateEvent",
      requestType = ca.stefanm.e39pim.v1.UpdateEventRequest.class,
      responseType = ca.stefanm.e39pim.v1.Event.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateEventRequest,
      ca.stefanm.e39pim.v1.Event> getUpdateEventMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.UpdateEventRequest, ca.stefanm.e39pim.v1.Event> getUpdateEventMethod;
    if ((getUpdateEventMethod = CalendarServiceGrpc.getUpdateEventMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getUpdateEventMethod = CalendarServiceGrpc.getUpdateEventMethod) == null) {
          CalendarServiceGrpc.getUpdateEventMethod = getUpdateEventMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.UpdateEventRequest, ca.stefanm.e39pim.v1.Event>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.UpdateEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.Event.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("UpdateEvent"))
              .build();
        }
      }
    }
    return getUpdateEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteEventRequest,
      com.google.protobuf.Empty> getDeleteEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteEvent",
      requestType = ca.stefanm.e39pim.v1.DeleteEventRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteEventRequest,
      com.google.protobuf.Empty> getDeleteEventMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.DeleteEventRequest, com.google.protobuf.Empty> getDeleteEventMethod;
    if ((getDeleteEventMethod = CalendarServiceGrpc.getDeleteEventMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getDeleteEventMethod = CalendarServiceGrpc.getDeleteEventMethod) == null) {
          CalendarServiceGrpc.getDeleteEventMethod = getDeleteEventMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.DeleteEventRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.DeleteEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("DeleteEvent"))
              .build();
        }
      }
    }
    return getDeleteEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchChangesRequest,
      ca.stefanm.e39pim.v1.ChangeNotice> getWatchChangesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchChanges",
      requestType = ca.stefanm.e39pim.v1.WatchChangesRequest.class,
      responseType = ca.stefanm.e39pim.v1.ChangeNotice.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchChangesRequest,
      ca.stefanm.e39pim.v1.ChangeNotice> getWatchChangesMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchChangesRequest, ca.stefanm.e39pim.v1.ChangeNotice> getWatchChangesMethod;
    if ((getWatchChangesMethod = CalendarServiceGrpc.getWatchChangesMethod) == null) {
      synchronized (CalendarServiceGrpc.class) {
        if ((getWatchChangesMethod = CalendarServiceGrpc.getWatchChangesMethod) == null) {
          CalendarServiceGrpc.getWatchChangesMethod = getWatchChangesMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.WatchChangesRequest, ca.stefanm.e39pim.v1.ChangeNotice>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchChanges"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.WatchChangesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ChangeNotice.getDefaultInstance()))
              .setSchemaDescriptor(new CalendarServiceMethodDescriptorSupplier("WatchChanges"))
              .build();
        }
      }
    }
    return getWatchChangesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CalendarServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CalendarServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CalendarServiceStub>() {
        @java.lang.Override
        public CalendarServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CalendarServiceStub(channel, callOptions);
        }
      };
    return CalendarServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static CalendarServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CalendarServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CalendarServiceBlockingV2Stub>() {
        @java.lang.Override
        public CalendarServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CalendarServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return CalendarServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CalendarServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CalendarServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CalendarServiceBlockingStub>() {
        @java.lang.Override
        public CalendarServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CalendarServiceBlockingStub(channel, callOptions);
        }
      };
    return CalendarServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CalendarServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CalendarServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CalendarServiceFutureStub>() {
        @java.lang.Override
        public CalendarServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CalendarServiceFutureStub(channel, callOptions);
        }
      };
    return CalendarServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * All calendars (with events, to-dos or both), by account then name.
     * </pre>
     */
    default void listCalendars(ca.stefanm.e39pim.v1.ListCalendarsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListCalendarsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListCalendarsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Occurrences overlapping [start, end), sorted by start then end.
     * INVALID_ARGUMENT if start isn't before end or the range is longer than
     * 366 days.
     * </pre>
     */
    default void listEvents(ca.stefanm.e39pim.v1.ListEventsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListEventsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListEventsMethod(), responseObserver);
    }

    /**
     * <pre>
     * One event with all its details; with recurrence_id, that occurrence's
     * times. NOT_FOUND for an unknown event or an occurrence that doesn't
     * exist.
     * </pre>
     */
    default void getEvent(ca.stefanm.e39pim.v1.GetEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns the new event. INVALID_ARGUMENT for a missing summary or
     * inconsistent times; FAILED_PRECONDITION if the calendar is read-only or
     * holds no events.
     * </pre>
     */
    default void createEvent(ca.stefanm.e39pim.v1.CreateEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * A whole event/series, or one occurrence of a series (recurrence_id
     * set: the first change creates an exception, later ones change it).
     * Returns the changed event (occurrence). ABORTED on a revision mismatch;
     * NOT_FOUND for an occurrence that isn't on the series' schedule;
     * FAILED_PRECONDITION for read-only calendars.
     * </pre>
     */
    default void updateEvent(ca.stefanm.e39pim.v1.UpdateEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * A whole event (a series with all its exceptions), or one occurrence of
     * a series (recurrence_id set: excluded from the series, its exception
     * removed, so it doesn't come back at its original time). ABORTED on a
     * revision mismatch.
     * </pre>
     */
    default void deleteEvent(ca.stefanm.e39pim.v1.DeleteEventRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Tells clients what to re-query. The first message is `ready` (the
     * stream is live: query now and rely on the notices after it). Notices
     * carry no event data; bursts (a sync changing hundreds of events) are
     * coalesced.
     * </pre>
     */
    default void watchChanges(ca.stefanm.e39pim.v1.WatchChangesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ChangeNotice> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchChangesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service CalendarService.
   */
  public static abstract class CalendarServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return CalendarServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service CalendarService.
   */
  public static final class CalendarServiceStub
      extends io.grpc.stub.AbstractAsyncStub<CalendarServiceStub> {
    private CalendarServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CalendarServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CalendarServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * All calendars (with events, to-dos or both), by account then name.
     * </pre>
     */
    public void listCalendars(ca.stefanm.e39pim.v1.ListCalendarsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListCalendarsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListCalendarsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Occurrences overlapping [start, end), sorted by start then end.
     * INVALID_ARGUMENT if start isn't before end or the range is longer than
     * 366 days.
     * </pre>
     */
    public void listEvents(ca.stefanm.e39pim.v1.ListEventsRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListEventsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * One event with all its details; with recurrence_id, that occurrence's
     * times. NOT_FOUND for an unknown event or an occurrence that doesn't
     * exist.
     * </pre>
     */
    public void getEvent(ca.stefanm.e39pim.v1.GetEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns the new event. INVALID_ARGUMENT for a missing summary or
     * inconsistent times; FAILED_PRECONDITION if the calendar is read-only or
     * holds no events.
     * </pre>
     */
    public void createEvent(ca.stefanm.e39pim.v1.CreateEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * A whole event/series, or one occurrence of a series (recurrence_id
     * set: the first change creates an exception, later ones change it).
     * Returns the changed event (occurrence). ABORTED on a revision mismatch;
     * NOT_FOUND for an occurrence that isn't on the series' schedule;
     * FAILED_PRECONDITION for read-only calendars.
     * </pre>
     */
    public void updateEvent(ca.stefanm.e39pim.v1.UpdateEventRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * A whole event (a series with all its exceptions), or one occurrence of
     * a series (recurrence_id set: excluded from the series, its exception
     * removed, so it doesn't come back at its original time). ABORTED on a
     * revision mismatch.
     * </pre>
     */
    public void deleteEvent(ca.stefanm.e39pim.v1.DeleteEventRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Tells clients what to re-query. The first message is `ready` (the
     * stream is live: query now and rely on the notices after it). Notices
     * carry no event data; bursts (a sync changing hundreds of events) are
     * coalesced.
     * </pre>
     */
    public void watchChanges(ca.stefanm.e39pim.v1.WatchChangesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ChangeNotice> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchChangesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service CalendarService.
   */
  public static final class CalendarServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<CalendarServiceBlockingV2Stub> {
    private CalendarServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CalendarServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CalendarServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * All calendars (with events, to-dos or both), by account then name.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListCalendarsResponse listCalendars(ca.stefanm.e39pim.v1.ListCalendarsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListCalendarsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Occurrences overlapping [start, end), sorted by start then end.
     * INVALID_ARGUMENT if start isn't before end or the range is longer than
     * 366 days.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListEventsResponse listEvents(ca.stefanm.e39pim.v1.ListEventsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListEventsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * One event with all its details; with recurrence_id, that occurrence's
     * times. NOT_FOUND for an unknown event or an occurrence that doesn't
     * exist.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event getEvent(ca.stefanm.e39pim.v1.GetEventRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the new event. INVALID_ARGUMENT for a missing summary or
     * inconsistent times; FAILED_PRECONDITION if the calendar is read-only or
     * holds no events.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event createEvent(ca.stefanm.e39pim.v1.CreateEventRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCreateEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A whole event/series, or one occurrence of a series (recurrence_id
     * set: the first change creates an exception, later ones change it).
     * Returns the changed event (occurrence). ABORTED on a revision mismatch;
     * NOT_FOUND for an occurrence that isn't on the series' schedule;
     * FAILED_PRECONDITION for read-only calendars.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event updateEvent(ca.stefanm.e39pim.v1.UpdateEventRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getUpdateEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A whole event (a series with all its exceptions), or one occurrence of
     * a series (recurrence_id set: excluded from the series, its exception
     * removed, so it doesn't come back at its original time). ABORTED on a
     * revision mismatch.
     * </pre>
     */
    public com.google.protobuf.Empty deleteEvent(ca.stefanm.e39pim.v1.DeleteEventRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getDeleteEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tells clients what to re-query. The first message is `ready` (the
     * stream is live: query now and rely on the notices after it). Notices
     * carry no event data; bursts (a sync changing hundreds of events) are
     * coalesced.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, ca.stefanm.e39pim.v1.ChangeNotice>
        watchChanges(ca.stefanm.e39pim.v1.WatchChangesRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getWatchChangesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service CalendarService.
   */
  public static final class CalendarServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<CalendarServiceBlockingStub> {
    private CalendarServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CalendarServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CalendarServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * All calendars (with events, to-dos or both), by account then name.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListCalendarsResponse listCalendars(ca.stefanm.e39pim.v1.ListCalendarsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListCalendarsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Occurrences overlapping [start, end), sorted by start then end.
     * INVALID_ARGUMENT if start isn't before end or the range is longer than
     * 366 days.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListEventsResponse listEvents(ca.stefanm.e39pim.v1.ListEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListEventsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * One event with all its details; with recurrence_id, that occurrence's
     * times. NOT_FOUND for an unknown event or an occurrence that doesn't
     * exist.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event getEvent(ca.stefanm.e39pim.v1.GetEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the new event. INVALID_ARGUMENT for a missing summary or
     * inconsistent times; FAILED_PRECONDITION if the calendar is read-only or
     * holds no events.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event createEvent(ca.stefanm.e39pim.v1.CreateEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A whole event/series, or one occurrence of a series (recurrence_id
     * set: the first change creates an exception, later ones change it).
     * Returns the changed event (occurrence). ABORTED on a revision mismatch;
     * NOT_FOUND for an occurrence that isn't on the series' schedule;
     * FAILED_PRECONDITION for read-only calendars.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.Event updateEvent(ca.stefanm.e39pim.v1.UpdateEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A whole event (a series with all its exceptions), or one occurrence of
     * a series (recurrence_id set: excluded from the series, its exception
     * removed, so it doesn't come back at its original time). ABORTED on a
     * revision mismatch.
     * </pre>
     */
    public com.google.protobuf.Empty deleteEvent(ca.stefanm.e39pim.v1.DeleteEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tells clients what to re-query. The first message is `ready` (the
     * stream is live: query now and rely on the notices after it). Notices
     * carry no event data; bursts (a sync changing hundreds of events) are
     * coalesced.
     * </pre>
     */
    public java.util.Iterator<ca.stefanm.e39pim.v1.ChangeNotice> watchChanges(
        ca.stefanm.e39pim.v1.WatchChangesRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchChangesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service CalendarService.
   */
  public static final class CalendarServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<CalendarServiceFutureStub> {
    private CalendarServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CalendarServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CalendarServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * All calendars (with events, to-dos or both), by account then name.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListCalendarsResponse> listCalendars(
        ca.stefanm.e39pim.v1.ListCalendarsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListCalendarsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Occurrences overlapping [start, end), sorted by start then end.
     * INVALID_ARGUMENT if start isn't before end or the range is longer than
     * 366 days.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListEventsResponse> listEvents(
        ca.stefanm.e39pim.v1.ListEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListEventsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * One event with all its details; with recurrence_id, that occurrence's
     * times. NOT_FOUND for an unknown event or an occurrence that doesn't
     * exist.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Event> getEvent(
        ca.stefanm.e39pim.v1.GetEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns the new event. INVALID_ARGUMENT for a missing summary or
     * inconsistent times; FAILED_PRECONDITION if the calendar is read-only or
     * holds no events.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Event> createEvent(
        ca.stefanm.e39pim.v1.CreateEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * A whole event/series, or one occurrence of a series (recurrence_id
     * set: the first change creates an exception, later ones change it).
     * Returns the changed event (occurrence). ABORTED on a revision mismatch;
     * NOT_FOUND for an occurrence that isn't on the series' schedule;
     * FAILED_PRECONDITION for read-only calendars.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.Event> updateEvent(
        ca.stefanm.e39pim.v1.UpdateEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * A whole event (a series with all its exceptions), or one occurrence of
     * a series (recurrence_id set: excluded from the series, its exception
     * removed, so it doesn't come back at its original time). ABORTED on a
     * revision mismatch.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteEvent(
        ca.stefanm.e39pim.v1.DeleteEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteEventMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_CALENDARS = 0;
  private static final int METHODID_LIST_EVENTS = 1;
  private static final int METHODID_GET_EVENT = 2;
  private static final int METHODID_CREATE_EVENT = 3;
  private static final int METHODID_UPDATE_EVENT = 4;
  private static final int METHODID_DELETE_EVENT = 5;
  private static final int METHODID_WATCH_CHANGES = 6;

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
        case METHODID_LIST_CALENDARS:
          serviceImpl.listCalendars((ca.stefanm.e39pim.v1.ListCalendarsRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListCalendarsResponse>) responseObserver);
          break;
        case METHODID_LIST_EVENTS:
          serviceImpl.listEvents((ca.stefanm.e39pim.v1.ListEventsRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListEventsResponse>) responseObserver);
          break;
        case METHODID_GET_EVENT:
          serviceImpl.getEvent((ca.stefanm.e39pim.v1.GetEventRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event>) responseObserver);
          break;
        case METHODID_CREATE_EVENT:
          serviceImpl.createEvent((ca.stefanm.e39pim.v1.CreateEventRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event>) responseObserver);
          break;
        case METHODID_UPDATE_EVENT:
          serviceImpl.updateEvent((ca.stefanm.e39pim.v1.UpdateEventRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.Event>) responseObserver);
          break;
        case METHODID_DELETE_EVENT:
          serviceImpl.deleteEvent((ca.stefanm.e39pim.v1.DeleteEventRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_WATCH_CHANGES:
          serviceImpl.watchChanges((ca.stefanm.e39pim.v1.WatchChangesRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ChangeNotice>) responseObserver);
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
          getListCalendarsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListCalendarsRequest,
              ca.stefanm.e39pim.v1.ListCalendarsResponse>(
                service, METHODID_LIST_CALENDARS)))
        .addMethod(
          getListEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListEventsRequest,
              ca.stefanm.e39pim.v1.ListEventsResponse>(
                service, METHODID_LIST_EVENTS)))
        .addMethod(
          getGetEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.GetEventRequest,
              ca.stefanm.e39pim.v1.Event>(
                service, METHODID_GET_EVENT)))
        .addMethod(
          getCreateEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.CreateEventRequest,
              ca.stefanm.e39pim.v1.Event>(
                service, METHODID_CREATE_EVENT)))
        .addMethod(
          getUpdateEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.UpdateEventRequest,
              ca.stefanm.e39pim.v1.Event>(
                service, METHODID_UPDATE_EVENT)))
        .addMethod(
          getDeleteEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.DeleteEventRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_EVENT)))
        .addMethod(
          getWatchChangesMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.WatchChangesRequest,
              ca.stefanm.e39pim.v1.ChangeNotice>(
                service, METHODID_WATCH_CHANGES)))
        .build();
  }

  private static abstract class CalendarServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CalendarServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.CalendarProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CalendarService");
    }
  }

  private static final class CalendarServiceFileDescriptorSupplier
      extends CalendarServiceBaseDescriptorSupplier {
    CalendarServiceFileDescriptorSupplier() {}
  }

  private static final class CalendarServiceMethodDescriptorSupplier
      extends CalendarServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    CalendarServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (CalendarServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CalendarServiceFileDescriptorSupplier())
              .addMethod(getListCalendarsMethod())
              .addMethod(getListEventsMethod())
              .addMethod(getGetEventMethod())
              .addMethod(getCreateEventMethod())
              .addMethod(getUpdateEventMethod())
              .addMethod(getDeleteEventMethod())
              .addMethod(getWatchChangesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
