// Generated by the protocol buffer compiler. DO NOT EDIT!
// source: PiToPico.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")
package ca.stefanm.e39.proto;

@kotlin.jvm.JvmName("-initializepiToPico")
public inline fun piToPico(block: ca.stefanm.e39.proto.PiToPicoKt.Dsl.() -> kotlin.Unit): PiToPicoOuterClass.PiToPico =
  ca.stefanm.e39.proto.PiToPicoKt.Dsl._create(PiToPicoOuterClass.PiToPico.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `ca.stefanm.e39.proto.PiToPico`
 */
public object PiToPicoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: PiToPicoOuterClass.PiToPico.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: PiToPicoOuterClass.PiToPico.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): PiToPicoOuterClass.PiToPico = _builder.build()

    /**
     * `.ca.stefanm.e39.proto.PiToPico.MessageType messageType = 1;`
     */
    public var messageType: PiToPicoOuterClass.PiToPico.MessageType
      @JvmName("getMessageType")
      get() = _builder.getMessageType()
      @JvmName("setMessageType")
      set(value) {
        _builder.setMessageType(value)
      }
    public var messageTypeValue: kotlin.Int
      @JvmName("getMessageTypeValue")
      get() = _builder.getMessageTypeValue()
      @JvmName("setMessageTypeValue")
      set(value) {
        _builder.setMessageTypeValue(value)
      }
    /**
     * `.ca.stefanm.e39.proto.PiToPico.MessageType messageType = 1;`
     */
    public fun clearMessageType() {
      _builder.clearMessageType()
    }

    /**
     * `optional .ca.stefanm.e39.proto.ConfigProto newConfig = 2;`
     */
    public var newConfig: ConfigProtoOuterClass.ConfigProto
      @JvmName("getNewConfig")
      get() = _builder.getNewConfig()
      @JvmName("setNewConfig")
      set(value) {
        _builder.setNewConfig(value)
      }
    /**
     * `optional .ca.stefanm.e39.proto.ConfigProto newConfig = 2;`
     */
    public fun clearNewConfig() {
      _builder.clearNewConfig()
    }
    /**
     * `optional .ca.stefanm.e39.proto.ConfigProto newConfig = 2;`
     * @return Whether the newConfig field is set.
     */
    public fun hasNewConfig(): kotlin.Boolean {
      return _builder.hasNewConfig()
    }
    public val PiToPicoKt.Dsl.newConfigOrNull: ConfigProtoOuterClass.ConfigProto?
      get() = _builder.newConfigOrNull
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun PiToPicoOuterClass.PiToPico.copy(block: ca.stefanm.e39.proto.PiToPicoKt.Dsl.() -> kotlin.Unit): PiToPicoOuterClass.PiToPico =
  ca.stefanm.e39.proto.PiToPicoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val PiToPicoOuterClass.PiToPicoOrBuilder.newConfigOrNull: ConfigProtoOuterClass.ConfigProto?
  get() = if (hasNewConfig()) getNewConfig() else null
