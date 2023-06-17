package ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.SerialInterfaceServiceDebugGroup
import ca.stefanm.ibus.car.platform.SerialInterfaceServiceGroup
import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named


sealed class IbusCommsDebugMessage(val createdAt : Instant) {

    sealed class IncomingMessage(open val recievedAt : Instant, val message : IBusMessage) : IbusCommsDebugMessage(createdAt = recievedAt) {
        class RawMessage(
            recievedAt : Instant,
            message : IBusMessage
        ) : IncomingMessage(recievedAt, message)

        data class InputEventMessage(
            override val recievedAt : Instant,
            val rawMessage : IBusMessage,
            val inputEvent : InputEvent
        ) : IncomingMessage(recievedAt, rawMessage)

        data class SyntheticInputEventMessage(
            override val recievedAt : Instant,
            val inputEvent : InputEvent
        ) : IncomingMessage(recievedAt, IBusMessage(IBusDevice.BROADCAST, IBusDevice.BROADCAST, UByteArray(0)))


        data class PicoToPiMessage(
            override val recievedAt: Instant,
            val rawMessage: IBusMessage
        ) : IncomingMessage(recievedAt, rawMessage)

        data class SyntheticPicoToPiMessage(
            override val recievedAt: Instant,
            val rawMessage: IBusMessage,
        ) : IncomingMessage(recievedAt, rawMessage)
    }

    sealed class OutgoingMessage(
        open val sentAt: Instant,
        val message: IBusMessage
    ) : IbusCommsDebugMessage(createdAt = sentAt){
        data class RawMessage(
            val outgoingMessage: IBusMessage,
            override val sentAt : Instant
        ) : OutgoingMessage(sentAt = sentAt, message = outgoingMessage)

        data class SyntheticPiToPicoMessage(
            val outgoingMessage: IBusMessage,
            override val sentAt: Instant
        ) : OutgoingMessage(sentAt, outgoingMessage)
    }
}

//These two services exist so that we can start and stop them independently
//Of the other SerialServices.

@PlatformServiceInfo(
    name = "SerialListenerDebugService",
    description = "A service to provide debug information about incoming IBusMessages"
)
@SerialInterfaceServiceDebugGroup
@ConfiguredCarScope
class SerialListenerDebugService @Inject constructor(
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,

    indexSelectedMessageParser: IBusInputMessageParser.IndexSelectedMessageParser,
    mflKeyMessageParser: IBusInputMessageParser.MflKeyMessageParser,
    rtButtonKeyMessageParser: IBusInputMessageParser.RtButtonKeyMessageParser,
    bmBtSeekButtonMessageParser: IBusInputMessageParser.BmBtSeekButtonMessageParser,
    bmBtShowRadioStatusMessageParser: IBusInputMessageParser.BmBtShowRadioStatusMessageParser,
    bmBtMenuPressedMessageParser: IBusInputMessageParser.BmBtMenuPressedMessageParser,
    bmBtPhonePressedMessageParser: IBusInputMessageParser.BmBtPhonePressedMessageParser,
    navKnobMessageParser: IBusInputMessageParser.NavKnobMessageParser

) : LongRunningService(coroutineScope, parsingDispatcher) {
    companion object {
        private val messagePipe = Channel<IBusMessage>(capacity = 256, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    suspend fun logMessage(iBusMessage: IBusMessage) {
        messagePipe.send(iBusMessage)
    }

    private val messageMatchers = listOf(
        indexSelectedMessageParser,
        mflKeyMessageParser,
        rtButtonKeyMessageParser,
        bmBtSeekButtonMessageParser,
        bmBtShowRadioStatusMessageParser,
        bmBtMenuPressedMessageParser,
        bmBtPhonePressedMessageParser,
        navKnobMessageParser
    )

    override fun onCreate() {
        super.onCreate()
        //We don't always log the complete set of matchers.
        logger.d("SerialListenerDebugService",
            "Starting SerialListenerDebugService with matcherList: ${messageMatchers.map{ it::class.simpleName}}")
    }

    override fun onShutdown() {
        super.onShutdown()
        logger.d("SerialListenerDebugService", "Stopped emitting items to logback channel.")
    }

    override suspend fun doWork() {
        messagePipe.consumeAsFlow().collect {
            val receivedAt = Instant.now()
            val rawMessage = it

            val matchedEvents = messageMatchers.map {matcher ->
                if (matcher.rawMessageMatches(rawMessage)) {
                    matcher.messageToInputEvent(rawMessage)
                } else { null }
            }.filterNotNull()

            if (matchedEvents.isEmpty()) {
                commsDebugChannel.emit(
                    IbusCommsDebugMessage.IncomingMessage.RawMessage(
                        recievedAt = receivedAt,
                        message = rawMessage
                    )
                )
            } else {
                matchedEvents.forEach { matchedEvent ->
                    commsDebugChannel.emit(
                        IbusCommsDebugMessage.IncomingMessage.InputEventMessage(
                            recievedAt = receivedAt,
                            rawMessage = rawMessage,
                            inputEvent = matchedEvent
                        )
                    )
                }
            }
        }
    }
}

@PlatformServiceInfo(
    name = "SyntheticIBusInputEventDebugLoggerService",
    description = "A service to log InputEvents. When running while SerialListenerDebugService is also running," +
            "input events will be duplicated. This is only useful in development when no serial device is attached" +
            "and the KeyEventSimulator is used."
)
@SerialInterfaceServiceDebugGroup
@ConfiguredCarScope
class SyntheticIBusInputEventDebugLoggerService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS_WRITER) private val eventSharedFlow: MutableSharedFlow<InputEvent>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {
    override suspend fun doWork() {
        eventSharedFlow.collect {
            logger.d("SyntheticIBusInputEventDebugLoggerService", "Writing message to IBUS_COMMS_DEBUG_CHANNEL (${commsDebugChannel.hashCode()}")
            commsDebugChannel.emit(
                IbusCommsDebugMessage.IncomingMessage.SyntheticInputEventMessage(
                    recievedAt = Instant.now(),
                    it
                )
            )
        }
    }
}

@PlatformServiceInfo(
    name = "SerialWriterDebugService",
    description = "A service to provide debug information about IbusMessages departing the Rpi"
)
@SerialInterfaceServiceDebugGroup
@ConfiguredCarScope
class SerialWriterDebugService @Inject constructor(
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    private companion object {
        val messagePipe = MutableSharedFlow<IbusCommsDebugMessage>(extraBufferCapacity = 256, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    override fun onCreate() {
        logger.d("SerialWriterDebugService", "onCreate()")
    }

    override fun onShutdown() {
        logger.d("SerialWriterDebugService", "onShutdown()")
    }

    override suspend fun doWork() {
        messagePipe.collect {
            commsDebugChannel.emit(it)
        }
    }

    suspend fun logMessage(iBusMessage: IBusMessage) {
        logger.d("SerialWriterDebugService", "Writing message to IBUS_COMMS_DEBUG_CHANNEL")
        messagePipe.emit(
            IbusCommsDebugMessage.OutgoingMessage.RawMessage(
                outgoingMessage = iBusMessage,
                sentAt = Instant.now()
            )
        )
        logger.d("SerialWriterDebugService", "Wrote message to IBUS_COMMS_DEBUG_CHANNEL")
    }

    suspend fun logPiToPicoMessage() {

    }
}