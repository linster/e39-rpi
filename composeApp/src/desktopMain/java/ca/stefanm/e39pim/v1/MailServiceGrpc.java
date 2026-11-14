package ca.stefanm.e39pim.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class MailServiceGrpc {

  private MailServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "e39pim.v1.MailService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMailFoldersRequest,
      ca.stefanm.e39pim.v1.ListMailFoldersResponse> getListMailFoldersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListMailFolders",
      requestType = ca.stefanm.e39pim.v1.ListMailFoldersRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListMailFoldersResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMailFoldersRequest,
      ca.stefanm.e39pim.v1.ListMailFoldersResponse> getListMailFoldersMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMailFoldersRequest, ca.stefanm.e39pim.v1.ListMailFoldersResponse> getListMailFoldersMethod;
    if ((getListMailFoldersMethod = MailServiceGrpc.getListMailFoldersMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getListMailFoldersMethod = MailServiceGrpc.getListMailFoldersMethod) == null) {
          MailServiceGrpc.getListMailFoldersMethod = getListMailFoldersMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListMailFoldersRequest, ca.stefanm.e39pim.v1.ListMailFoldersResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListMailFolders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListMailFoldersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListMailFoldersResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("ListMailFolders"))
              .build();
        }
      }
    }
    return getListMailFoldersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMessagesRequest,
      ca.stefanm.e39pim.v1.ListMessagesResponse> getListMessagesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListMessages",
      requestType = ca.stefanm.e39pim.v1.ListMessagesRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListMessagesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMessagesRequest,
      ca.stefanm.e39pim.v1.ListMessagesResponse> getListMessagesMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.ListMessagesRequest, ca.stefanm.e39pim.v1.ListMessagesResponse> getListMessagesMethod;
    if ((getListMessagesMethod = MailServiceGrpc.getListMessagesMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getListMessagesMethod = MailServiceGrpc.getListMessagesMethod) == null) {
          MailServiceGrpc.getListMessagesMethod = getListMessagesMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.ListMessagesRequest, ca.stefanm.e39pim.v1.ListMessagesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListMessages"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListMessagesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListMessagesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("ListMessages"))
              .build();
        }
      }
    }
    return getListMessagesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest,
      ca.stefanm.e39pim.v1.MailMessage> getGetMessageMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMessage",
      requestType = ca.stefanm.e39pim.v1.GetMessageRequest.class,
      responseType = ca.stefanm.e39pim.v1.MailMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest,
      ca.stefanm.e39pim.v1.MailMessage> getGetMessageMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest, ca.stefanm.e39pim.v1.MailMessage> getGetMessageMethod;
    if ((getGetMessageMethod = MailServiceGrpc.getGetMessageMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getGetMessageMethod = MailServiceGrpc.getGetMessageMethod) == null) {
          MailServiceGrpc.getGetMessageMethod = getGetMessageMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.GetMessageRequest, ca.stefanm.e39pim.v1.MailMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.GetMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.MailMessage.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("GetMessage"))
              .build();
        }
      }
    }
    return getGetMessageMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest,
      ca.stefanm.e39pim.v1.MailMessageHtml> getGetMessageHtmlMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMessageHtml",
      requestType = ca.stefanm.e39pim.v1.GetMessageRequest.class,
      responseType = ca.stefanm.e39pim.v1.MailMessageHtml.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest,
      ca.stefanm.e39pim.v1.MailMessageHtml> getGetMessageHtmlMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.GetMessageRequest, ca.stefanm.e39pim.v1.MailMessageHtml> getGetMessageHtmlMethod;
    if ((getGetMessageHtmlMethod = MailServiceGrpc.getGetMessageHtmlMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getGetMessageHtmlMethod = MailServiceGrpc.getGetMessageHtmlMethod) == null) {
          MailServiceGrpc.getGetMessageHtmlMethod = getGetMessageHtmlMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.GetMessageRequest, ca.stefanm.e39pim.v1.MailMessageHtml>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMessageHtml"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.GetMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.MailMessageHtml.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("GetMessageHtml"))
              .build();
        }
      }
    }
    return getGetMessageHtmlMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetMessageFlagsRequest,
      com.google.protobuf.Empty> getSetMessageFlagsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetMessageFlags",
      requestType = ca.stefanm.e39pim.v1.SetMessageFlagsRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetMessageFlagsRequest,
      com.google.protobuf.Empty> getSetMessageFlagsMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SetMessageFlagsRequest, com.google.protobuf.Empty> getSetMessageFlagsMethod;
    if ((getSetMessageFlagsMethod = MailServiceGrpc.getSetMessageFlagsMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getSetMessageFlagsMethod = MailServiceGrpc.getSetMessageFlagsMethod) == null) {
          MailServiceGrpc.getSetMessageFlagsMethod = getSetMessageFlagsMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SetMessageFlagsRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetMessageFlags"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SetMessageFlagsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("SetMessageFlags"))
              .build();
        }
      }
    }
    return getSetMessageFlagsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.MoveMessagesRequest,
      com.google.protobuf.Empty> getMoveMessagesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MoveMessages",
      requestType = ca.stefanm.e39pim.v1.MoveMessagesRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.MoveMessagesRequest,
      com.google.protobuf.Empty> getMoveMessagesMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.MoveMessagesRequest, com.google.protobuf.Empty> getMoveMessagesMethod;
    if ((getMoveMessagesMethod = MailServiceGrpc.getMoveMessagesMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getMoveMessagesMethod = MailServiceGrpc.getMoveMessagesMethod) == null) {
          MailServiceGrpc.getMoveMessagesMethod = getMoveMessagesMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.MoveMessagesRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MoveMessages"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.MoveMessagesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("MoveMessages"))
              .build();
        }
      }
    }
    return getMoveMessagesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.TrashMessagesRequest,
      com.google.protobuf.Empty> getTrashMessagesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TrashMessages",
      requestType = ca.stefanm.e39pim.v1.TrashMessagesRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.TrashMessagesRequest,
      com.google.protobuf.Empty> getTrashMessagesMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.TrashMessagesRequest, com.google.protobuf.Empty> getTrashMessagesMethod;
    if ((getTrashMessagesMethod = MailServiceGrpc.getTrashMessagesMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getTrashMessagesMethod = MailServiceGrpc.getTrashMessagesMethod) == null) {
          MailServiceGrpc.getTrashMessagesMethod = getTrashMessagesMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.TrashMessagesRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TrashMessages"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.TrashMessagesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("TrashMessages"))
              .build();
        }
      }
    }
    return getTrashMessagesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SendMessageRequest,
      ca.stefanm.e39pim.v1.SendMessageResponse> getSendMessageMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendMessage",
      requestType = ca.stefanm.e39pim.v1.SendMessageRequest.class,
      responseType = ca.stefanm.e39pim.v1.SendMessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SendMessageRequest,
      ca.stefanm.e39pim.v1.SendMessageResponse> getSendMessageMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SendMessageRequest, ca.stefanm.e39pim.v1.SendMessageResponse> getSendMessageMethod;
    if ((getSendMessageMethod = MailServiceGrpc.getSendMessageMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getSendMessageMethod = MailServiceGrpc.getSendMessageMethod) == null) {
          MailServiceGrpc.getSendMessageMethod = getSendMessageMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SendMessageRequest, ca.stefanm.e39pim.v1.SendMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SendMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SendMessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("SendMessage"))
              .build();
        }
      }
    }
    return getSendMessageMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SearchMessagesRequest,
      ca.stefanm.e39pim.v1.ListMessagesResponse> getSearchMessagesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchMessages",
      requestType = ca.stefanm.e39pim.v1.SearchMessagesRequest.class,
      responseType = ca.stefanm.e39pim.v1.ListMessagesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SearchMessagesRequest,
      ca.stefanm.e39pim.v1.ListMessagesResponse> getSearchMessagesMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.SearchMessagesRequest, ca.stefanm.e39pim.v1.ListMessagesResponse> getSearchMessagesMethod;
    if ((getSearchMessagesMethod = MailServiceGrpc.getSearchMessagesMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getSearchMessagesMethod = MailServiceGrpc.getSearchMessagesMethod) == null) {
          MailServiceGrpc.getSearchMessagesMethod = getSearchMessagesMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.SearchMessagesRequest, ca.stefanm.e39pim.v1.ListMessagesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchMessages"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.SearchMessagesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.ListMessagesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("SearchMessages"))
              .build();
        }
      }
    }
    return getSearchMessagesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchMailRequest,
      ca.stefanm.e39pim.v1.MailNotice> getWatchMailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WatchMail",
      requestType = ca.stefanm.e39pim.v1.WatchMailRequest.class,
      responseType = ca.stefanm.e39pim.v1.MailNotice.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchMailRequest,
      ca.stefanm.e39pim.v1.MailNotice> getWatchMailMethod() {
    io.grpc.MethodDescriptor<ca.stefanm.e39pim.v1.WatchMailRequest, ca.stefanm.e39pim.v1.MailNotice> getWatchMailMethod;
    if ((getWatchMailMethod = MailServiceGrpc.getWatchMailMethod) == null) {
      synchronized (MailServiceGrpc.class) {
        if ((getWatchMailMethod = MailServiceGrpc.getWatchMailMethod) == null) {
          MailServiceGrpc.getWatchMailMethod = getWatchMailMethod =
              io.grpc.MethodDescriptor.<ca.stefanm.e39pim.v1.WatchMailRequest, ca.stefanm.e39pim.v1.MailNotice>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WatchMail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.WatchMailRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ca.stefanm.e39pim.v1.MailNotice.getDefaultInstance()))
              .setSchemaDescriptor(new MailServiceMethodDescriptorSupplier("WatchMail"))
              .build();
        }
      }
    }
    return getWatchMailMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MailServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MailServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MailServiceStub>() {
        @java.lang.Override
        public MailServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MailServiceStub(channel, callOptions);
        }
      };
    return MailServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static MailServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MailServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MailServiceBlockingV2Stub>() {
        @java.lang.Override
        public MailServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MailServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return MailServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MailServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MailServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MailServiceBlockingStub>() {
        @java.lang.Override
        public MailServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MailServiceBlockingStub(channel, callOptions);
        }
      };
    return MailServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MailServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MailServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MailServiceFutureStub>() {
        @java.lang.Override
        public MailServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MailServiceFutureStub(channel, callOptions);
        }
      };
    return MailServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Folders, per account: the inbox first, then the other folders in tree
     * order (depth first, by name). Empty account_id = all accounts.
     * </pre>
     */
    default void listMailFolders(ca.stefanm.e39pim.v1.ListMailFoldersRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMailFoldersResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListMailFoldersMethod(), responseObserver);
    }

    /**
     * <pre>
     * A folder's messages, newest first, in pages. NOT_FOUND for an unknown
     * folder.
     * </pre>
     */
    default void listMessages(ca.stefanm.e39pim.v1.ListMessagesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListMessagesMethod(), responseObserver);
    }

    /**
     * <pre>
     * One message with its text. Downloads it from the server if it isn't
     * stored yet (UNAVAILABLE if the account is offline or unreachable).
     * </pre>
     */
    default void getMessage(ca.stefanm.e39pim.v1.GetMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMessageMethod(), responseObserver);
    }

    /**
     * <pre>
     * Like GetMessage (the same message, text and all), plus the message's
     * HTML body for showing it as a web page. See MailMessageHtml.
     * </pre>
     */
    default void getMessageHtml(ca.stefanm.e39pim.v1.GetMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessageHtml> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMessageHtmlMethod(), responseObserver);
    }

    /**
     * <pre>
     * Read/unread, flagged/unflagged; only the fields that are set change.
     * </pre>
     */
    default void setMessageFlags(ca.stefanm.e39pim.v1.SetMessageFlagsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetMessageFlagsMethod(), responseObserver);
    }

    /**
     * <pre>
     * To another folder of the same account. FAILED_PRECONDITION across
     * accounts or into a folder that can't hold messages.
     * </pre>
     */
    default void moveMessages(ca.stefanm.e39pim.v1.MoveMessagesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMoveMessagesMethod(), responseObserver);
    }

    /**
     * <pre>
     * To the account's trash folder; messages already in the trash are
     * deleted. FAILED_PRECONDITION if the account has no trash folder.
     * </pre>
     */
    default void trashMessages(ca.stefanm.e39pim.v1.TrashMessagesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTrashMessagesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Puts a message in the outbox; the account's server sends it, then it
     * moves to the account's sent folder. WatchMail reports the result
     * (sent / send_failed). FAILED_PRECONDITION if no sending account is set
     * up; INVALID_ARGUMENT for no recipients or a malformed address.
     * </pre>
     */
    default void sendMessage(ca.stefanm.e39pim.v1.SendMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.SendMessageResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendMessageMethod(), responseObserver);
    }

    /**
     * <pre>
     * Messages whose subject, sender or recipients contain the text
     * (case-insensitive), newest first. Searches what the bridge has stored:
     * headers of all messages, not bodies.
     * </pre>
     */
    default void searchMessages(ca.stefanm.e39pim.v1.SearchMessagesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchMessagesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Tells clients what to re-query, and about new mail. The first message
     * is `ready`. Bursts (a sync bringing hundreds of messages) are
     * coalesced.
     * </pre>
     */
    default void watchMail(ca.stefanm.e39pim.v1.WatchMailRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailNotice> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWatchMailMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MailService.
   */
  public static abstract class MailServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MailServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MailService.
   */
  public static final class MailServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MailServiceStub> {
    private MailServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MailServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MailServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Folders, per account: the inbox first, then the other folders in tree
     * order (depth first, by name). Empty account_id = all accounts.
     * </pre>
     */
    public void listMailFolders(ca.stefanm.e39pim.v1.ListMailFoldersRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMailFoldersResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListMailFoldersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * A folder's messages, newest first, in pages. NOT_FOUND for an unknown
     * folder.
     * </pre>
     */
    public void listMessages(ca.stefanm.e39pim.v1.ListMessagesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListMessagesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * One message with its text. Downloads it from the server if it isn't
     * stored yet (UNAVAILABLE if the account is offline or unreachable).
     * </pre>
     */
    public void getMessage(ca.stefanm.e39pim.v1.GetMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMessageMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Like GetMessage (the same message, text and all), plus the message's
     * HTML body for showing it as a web page. See MailMessageHtml.
     * </pre>
     */
    public void getMessageHtml(ca.stefanm.e39pim.v1.GetMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessageHtml> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMessageHtmlMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Read/unread, flagged/unflagged; only the fields that are set change.
     * </pre>
     */
    public void setMessageFlags(ca.stefanm.e39pim.v1.SetMessageFlagsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetMessageFlagsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * To another folder of the same account. FAILED_PRECONDITION across
     * accounts or into a folder that can't hold messages.
     * </pre>
     */
    public void moveMessages(ca.stefanm.e39pim.v1.MoveMessagesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMoveMessagesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * To the account's trash folder; messages already in the trash are
     * deleted. FAILED_PRECONDITION if the account has no trash folder.
     * </pre>
     */
    public void trashMessages(ca.stefanm.e39pim.v1.TrashMessagesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTrashMessagesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Puts a message in the outbox; the account's server sends it, then it
     * moves to the account's sent folder. WatchMail reports the result
     * (sent / send_failed). FAILED_PRECONDITION if no sending account is set
     * up; INVALID_ARGUMENT for no recipients or a malformed address.
     * </pre>
     */
    public void sendMessage(ca.stefanm.e39pim.v1.SendMessageRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.SendMessageResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendMessageMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Messages whose subject, sender or recipients contain the text
     * (case-insensitive), newest first. Searches what the bridge has stored:
     * headers of all messages, not bodies.
     * </pre>
     */
    public void searchMessages(ca.stefanm.e39pim.v1.SearchMessagesRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchMessagesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Tells clients what to re-query, and about new mail. The first message
     * is `ready`. Bursts (a sync bringing hundreds of messages) are
     * coalesced.
     * </pre>
     */
    public void watchMail(ca.stefanm.e39pim.v1.WatchMailRequest request,
        io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailNotice> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getWatchMailMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MailService.
   */
  public static final class MailServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<MailServiceBlockingV2Stub> {
    private MailServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MailServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MailServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Folders, per account: the inbox first, then the other folders in tree
     * order (depth first, by name). Empty account_id = all accounts.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMailFoldersResponse listMailFolders(ca.stefanm.e39pim.v1.ListMailFoldersRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListMailFoldersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A folder's messages, newest first, in pages. NOT_FOUND for an unknown
     * folder.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMessagesResponse listMessages(ca.stefanm.e39pim.v1.ListMessagesRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getListMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * One message with its text. Downloads it from the server if it isn't
     * stored yet (UNAVAILABLE if the account is offline or unreachable).
     * </pre>
     */
    public ca.stefanm.e39pim.v1.MailMessage getMessage(ca.stefanm.e39pim.v1.GetMessageRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Like GetMessage (the same message, text and all), plus the message's
     * HTML body for showing it as a web page. See MailMessageHtml.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.MailMessageHtml getMessageHtml(ca.stefanm.e39pim.v1.GetMessageRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetMessageHtmlMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Read/unread, flagged/unflagged; only the fields that are set change.
     * </pre>
     */
    public com.google.protobuf.Empty setMessageFlags(ca.stefanm.e39pim.v1.SetMessageFlagsRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSetMessageFlagsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * To another folder of the same account. FAILED_PRECONDITION across
     * accounts or into a folder that can't hold messages.
     * </pre>
     */
    public com.google.protobuf.Empty moveMessages(ca.stefanm.e39pim.v1.MoveMessagesRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getMoveMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * To the account's trash folder; messages already in the trash are
     * deleted. FAILED_PRECONDITION if the account has no trash folder.
     * </pre>
     */
    public com.google.protobuf.Empty trashMessages(ca.stefanm.e39pim.v1.TrashMessagesRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getTrashMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Puts a message in the outbox; the account's server sends it, then it
     * moves to the account's sent folder. WatchMail reports the result
     * (sent / send_failed). FAILED_PRECONDITION if no sending account is set
     * up; INVALID_ARGUMENT for no recipients or a malformed address.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.SendMessageResponse sendMessage(ca.stefanm.e39pim.v1.SendMessageRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSendMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Messages whose subject, sender or recipients contain the text
     * (case-insensitive), newest first. Searches what the bridge has stored:
     * headers of all messages, not bodies.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMessagesResponse searchMessages(ca.stefanm.e39pim.v1.SearchMessagesRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getSearchMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tells clients what to re-query, and about new mail. The first message
     * is `ready`. Bursts (a sync bringing hundreds of messages) are
     * coalesced.
     * </pre>
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, ca.stefanm.e39pim.v1.MailNotice>
        watchMail(ca.stefanm.e39pim.v1.WatchMailRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getWatchMailMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service MailService.
   */
  public static final class MailServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MailServiceBlockingStub> {
    private MailServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MailServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MailServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Folders, per account: the inbox first, then the other folders in tree
     * order (depth first, by name). Empty account_id = all accounts.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMailFoldersResponse listMailFolders(ca.stefanm.e39pim.v1.ListMailFoldersRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListMailFoldersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * A folder's messages, newest first, in pages. NOT_FOUND for an unknown
     * folder.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMessagesResponse listMessages(ca.stefanm.e39pim.v1.ListMessagesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * One message with its text. Downloads it from the server if it isn't
     * stored yet (UNAVAILABLE if the account is offline or unreachable).
     * </pre>
     */
    public ca.stefanm.e39pim.v1.MailMessage getMessage(ca.stefanm.e39pim.v1.GetMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Like GetMessage (the same message, text and all), plus the message's
     * HTML body for showing it as a web page. See MailMessageHtml.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.MailMessageHtml getMessageHtml(ca.stefanm.e39pim.v1.GetMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMessageHtmlMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Read/unread, flagged/unflagged; only the fields that are set change.
     * </pre>
     */
    public com.google.protobuf.Empty setMessageFlags(ca.stefanm.e39pim.v1.SetMessageFlagsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetMessageFlagsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * To another folder of the same account. FAILED_PRECONDITION across
     * accounts or into a folder that can't hold messages.
     * </pre>
     */
    public com.google.protobuf.Empty moveMessages(ca.stefanm.e39pim.v1.MoveMessagesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMoveMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * To the account's trash folder; messages already in the trash are
     * deleted. FAILED_PRECONDITION if the account has no trash folder.
     * </pre>
     */
    public com.google.protobuf.Empty trashMessages(ca.stefanm.e39pim.v1.TrashMessagesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTrashMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Puts a message in the outbox; the account's server sends it, then it
     * moves to the account's sent folder. WatchMail reports the result
     * (sent / send_failed). FAILED_PRECONDITION if no sending account is set
     * up; INVALID_ARGUMENT for no recipients or a malformed address.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.SendMessageResponse sendMessage(ca.stefanm.e39pim.v1.SendMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Messages whose subject, sender or recipients contain the text
     * (case-insensitive), newest first. Searches what the bridge has stored:
     * headers of all messages, not bodies.
     * </pre>
     */
    public ca.stefanm.e39pim.v1.ListMessagesResponse searchMessages(ca.stefanm.e39pim.v1.SearchMessagesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchMessagesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tells clients what to re-query, and about new mail. The first message
     * is `ready`. Bursts (a sync bringing hundreds of messages) are
     * coalesced.
     * </pre>
     */
    public java.util.Iterator<ca.stefanm.e39pim.v1.MailNotice> watchMail(
        ca.stefanm.e39pim.v1.WatchMailRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getWatchMailMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MailService.
   */
  public static final class MailServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MailServiceFutureStub> {
    private MailServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MailServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MailServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Folders, per account: the inbox first, then the other folders in tree
     * order (depth first, by name). Empty account_id = all accounts.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListMailFoldersResponse> listMailFolders(
        ca.stefanm.e39pim.v1.ListMailFoldersRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListMailFoldersMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * A folder's messages, newest first, in pages. NOT_FOUND for an unknown
     * folder.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListMessagesResponse> listMessages(
        ca.stefanm.e39pim.v1.ListMessagesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListMessagesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * One message with its text. Downloads it from the server if it isn't
     * stored yet (UNAVAILABLE if the account is offline or unreachable).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.MailMessage> getMessage(
        ca.stefanm.e39pim.v1.GetMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMessageMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Like GetMessage (the same message, text and all), plus the message's
     * HTML body for showing it as a web page. See MailMessageHtml.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.MailMessageHtml> getMessageHtml(
        ca.stefanm.e39pim.v1.GetMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMessageHtmlMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Read/unread, flagged/unflagged; only the fields that are set change.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> setMessageFlags(
        ca.stefanm.e39pim.v1.SetMessageFlagsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetMessageFlagsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * To another folder of the same account. FAILED_PRECONDITION across
     * accounts or into a folder that can't hold messages.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> moveMessages(
        ca.stefanm.e39pim.v1.MoveMessagesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMoveMessagesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * To the account's trash folder; messages already in the trash are
     * deleted. FAILED_PRECONDITION if the account has no trash folder.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> trashMessages(
        ca.stefanm.e39pim.v1.TrashMessagesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTrashMessagesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Puts a message in the outbox; the account's server sends it, then it
     * moves to the account's sent folder. WatchMail reports the result
     * (sent / send_failed). FAILED_PRECONDITION if no sending account is set
     * up; INVALID_ARGUMENT for no recipients or a malformed address.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.SendMessageResponse> sendMessage(
        ca.stefanm.e39pim.v1.SendMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendMessageMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Messages whose subject, sender or recipients contain the text
     * (case-insensitive), newest first. Searches what the bridge has stored:
     * headers of all messages, not bodies.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ca.stefanm.e39pim.v1.ListMessagesResponse> searchMessages(
        ca.stefanm.e39pim.v1.SearchMessagesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchMessagesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_MAIL_FOLDERS = 0;
  private static final int METHODID_LIST_MESSAGES = 1;
  private static final int METHODID_GET_MESSAGE = 2;
  private static final int METHODID_GET_MESSAGE_HTML = 3;
  private static final int METHODID_SET_MESSAGE_FLAGS = 4;
  private static final int METHODID_MOVE_MESSAGES = 5;
  private static final int METHODID_TRASH_MESSAGES = 6;
  private static final int METHODID_SEND_MESSAGE = 7;
  private static final int METHODID_SEARCH_MESSAGES = 8;
  private static final int METHODID_WATCH_MAIL = 9;

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
        case METHODID_LIST_MAIL_FOLDERS:
          serviceImpl.listMailFolders((ca.stefanm.e39pim.v1.ListMailFoldersRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMailFoldersResponse>) responseObserver);
          break;
        case METHODID_LIST_MESSAGES:
          serviceImpl.listMessages((ca.stefanm.e39pim.v1.ListMessagesRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse>) responseObserver);
          break;
        case METHODID_GET_MESSAGE:
          serviceImpl.getMessage((ca.stefanm.e39pim.v1.GetMessageRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessage>) responseObserver);
          break;
        case METHODID_GET_MESSAGE_HTML:
          serviceImpl.getMessageHtml((ca.stefanm.e39pim.v1.GetMessageRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailMessageHtml>) responseObserver);
          break;
        case METHODID_SET_MESSAGE_FLAGS:
          serviceImpl.setMessageFlags((ca.stefanm.e39pim.v1.SetMessageFlagsRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_MOVE_MESSAGES:
          serviceImpl.moveMessages((ca.stefanm.e39pim.v1.MoveMessagesRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_TRASH_MESSAGES:
          serviceImpl.trashMessages((ca.stefanm.e39pim.v1.TrashMessagesRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SEND_MESSAGE:
          serviceImpl.sendMessage((ca.stefanm.e39pim.v1.SendMessageRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.SendMessageResponse>) responseObserver);
          break;
        case METHODID_SEARCH_MESSAGES:
          serviceImpl.searchMessages((ca.stefanm.e39pim.v1.SearchMessagesRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.ListMessagesResponse>) responseObserver);
          break;
        case METHODID_WATCH_MAIL:
          serviceImpl.watchMail((ca.stefanm.e39pim.v1.WatchMailRequest) request,
              (io.grpc.stub.StreamObserver<ca.stefanm.e39pim.v1.MailNotice>) responseObserver);
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
          getListMailFoldersMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListMailFoldersRequest,
              ca.stefanm.e39pim.v1.ListMailFoldersResponse>(
                service, METHODID_LIST_MAIL_FOLDERS)))
        .addMethod(
          getListMessagesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.ListMessagesRequest,
              ca.stefanm.e39pim.v1.ListMessagesResponse>(
                service, METHODID_LIST_MESSAGES)))
        .addMethod(
          getGetMessageMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.GetMessageRequest,
              ca.stefanm.e39pim.v1.MailMessage>(
                service, METHODID_GET_MESSAGE)))
        .addMethod(
          getGetMessageHtmlMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.GetMessageRequest,
              ca.stefanm.e39pim.v1.MailMessageHtml>(
                service, METHODID_GET_MESSAGE_HTML)))
        .addMethod(
          getSetMessageFlagsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SetMessageFlagsRequest,
              com.google.protobuf.Empty>(
                service, METHODID_SET_MESSAGE_FLAGS)))
        .addMethod(
          getMoveMessagesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.MoveMessagesRequest,
              com.google.protobuf.Empty>(
                service, METHODID_MOVE_MESSAGES)))
        .addMethod(
          getTrashMessagesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.TrashMessagesRequest,
              com.google.protobuf.Empty>(
                service, METHODID_TRASH_MESSAGES)))
        .addMethod(
          getSendMessageMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SendMessageRequest,
              ca.stefanm.e39pim.v1.SendMessageResponse>(
                service, METHODID_SEND_MESSAGE)))
        .addMethod(
          getSearchMessagesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.SearchMessagesRequest,
              ca.stefanm.e39pim.v1.ListMessagesResponse>(
                service, METHODID_SEARCH_MESSAGES)))
        .addMethod(
          getWatchMailMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              ca.stefanm.e39pim.v1.WatchMailRequest,
              ca.stefanm.e39pim.v1.MailNotice>(
                service, METHODID_WATCH_MAIL)))
        .build();
  }

  private static abstract class MailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MailServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ca.stefanm.e39pim.v1.MailProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MailService");
    }
  }

  private static final class MailServiceFileDescriptorSupplier
      extends MailServiceBaseDescriptorSupplier {
    MailServiceFileDescriptorSupplier() {}
  }

  private static final class MailServiceMethodDescriptorSupplier
      extends MailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MailServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (MailServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MailServiceFileDescriptorSupplier())
              .addMethod(getListMailFoldersMethod())
              .addMethod(getListMessagesMethod())
              .addMethod(getGetMessageMethod())
              .addMethod(getGetMessageHtmlMethod())
              .addMethod(getSetMessageFlagsMethod())
              .addMethod(getMoveMessagesMethod())
              .addMethod(getTrashMessagesMethod())
              .addMethod(getSendMessageMethod())
              .addMethod(getSearchMessagesMethod())
              .addMethod(getWatchMailMethod())
              .build();
        }
      }
    }
    return result;
  }
}
