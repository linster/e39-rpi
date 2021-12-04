package ca.stefanm.ibus.car.bordmonitor.input

import ca.stefanm.ibus.car.data.IBusDevice
import ca.stefanm.ibus.car.data.IBusMessage
import ca.stefanm.ibus.car.data.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.di.QualifierNames
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@ConfiguredCarScope
class IBusInputMessageParser @Inject constructor(

    @Named(QualifierNames.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    @Named(QualifierNames.INPUT_EVENTS_WRITER) private val eventSharedFlow: MutableSharedFlow<InputEvent>,

    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,
    indexSelectedMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser,
    mflKeyMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.MflKeyMessageParser,
    rtButtonKeyMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.RtButtonKeyMessageParser,
    bmBtSeekButtonMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.BmBtSeekButtonMessageParser,
    bmBtShowRadioStatusMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.BmBtShowRadioStatusMessageParser,
    bmBtMenuPressedMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.BmBtMenuPressedMessageParser,
    bmBtPhonePressedMessageParser: ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.BmBtPhonePressedMessageParser
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {

    private val messageMatchers = listOf(
        indexSelectedMessageParser,
        mflKeyMessageParser,
        rtButtonKeyMessageParser,
        bmBtSeekButtonMessageParser,
        bmBtShowRadioStatusMessageParser,
        bmBtMenuPressedMessageParser,
        bmBtPhonePressedMessageParser
    )

    @ExperimentalCoroutinesApi
    override suspend fun doWork() {
        incomingMessages.collect { message ->
            messageMatchers.forEach { matcher ->
                if (matcher.rawMessageMatches(message)) {
                    matcher.messageToInputEvent(message)?.let { event ->
                        eventSharedFlow.emit(event)
                    }
                }
            }
        }
    }

    suspend fun debugSend(inputEvent: InputEvent) {
        eventSharedFlow.emit(inputEvent)
    }

    interface InputMessageMatcher {
        fun rawMessageMatches(message: IBusMessage): Boolean
        fun messageToInputEvent(message: IBusMessage): InputEvent?
    }

    class IndexSelectedMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.NAV_VIDEOMODULE
                    && message.destinationDevice == IBusDevice.RADIO
                    && message.data.toList().map { it.toInt() }.take(3) == listOf(0x23, 0x62, 0x30)
        }

        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            with(Buffer()) {
                write(message.data.toByteArray())

                assert(readByte() == 0x23.toByte())
                assert(readByte() == 0x62.toByte())
                assert(readByte() == 0x30.toByte())

                val eventType = mapOf(
                    0x00 to Pair(0,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x40 to Pair(0,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x01 to Pair(1,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x41 to Pair(1,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x02 to Pair(2,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x42 to Pair(2,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x03 to Pair(3,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x43 to Pair(3,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x04 to Pair(4,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x44 to Pair(4,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x05 to Pair(5,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x45 to Pair(5,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x06 to Pair(6,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x46 to Pair(6,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x07 to Pair(7,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x47 to Pair(7,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x08 to Pair(8,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x48 to Pair(8,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    ),
                    0x09 to Pair(9,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.SELECTED
                    ), 0x49 to Pair(9,
                        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.IndexSelectedMessageParser.EventType.RELEASE
                    )
                )

                val rawIndex = readByte()
                val indexSelected = eventType[rawIndex.toInt()]?.first ?: return null
                return InputEvent.IndexSelectEvent(indexSelected)
            }
        }

        enum class EventType { SELECTED, RELEASE }
    }

    class MflKeyMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.MFL
                    && message.destinationDevice == IBusDevice.RADIO
        }

        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return when (message.data.toList().map { it.toInt() }) {
                listOf(0x3B, 0x01) -> InputEvent.NextTrack
                listOf(0x3B, 0x08) -> InputEvent.PrevTrack
                else -> null
            }
        }
    }

    class RtButtonKeyMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.MFL
                    && message.destinationDevice == IBusDevice.TELEPHONE
        }

        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return when (message.data.toList().map { it.toInt() }) {
                listOf(0x3B, 0x40) -> InputEvent.RTButton
                else -> null
            }
        }
    }

    class BmBtSeekButtonMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.RADIO
        }

        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return when (message.data.toList().map { it.toInt() }) {
                listOf(0x48, 0x00) -> InputEvent.NextTrack
                listOf(0x48, 0x10) -> InputEvent.PrevTrack
                else -> null
            }
        }
    }

    class BmBtShowRadioStatusMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return if (message.data.toList().map { it.toInt() } == listOf(0x48, 0x30)) {
                InputEvent.ShowRadioStatusScreen
            } else null
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.RADIO
        }
    }

    class BmBtMenuPressedMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return InputEvent.BMBTMenuPressed
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.BROADCAST
                    && message.data.toList().map { it.toInt() } == listOf(0x48, 0x34)
        }
    }

    class BmBtPhonePressedMessageParser @Inject constructor() :
        ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser.InputMessageMatcher {
        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return InputEvent.BMBTPhonePressed
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.BROADCAST
                    && message.data.toList().map { it.toInt() } == listOf(0x48, 0x08)
        }
    }
}