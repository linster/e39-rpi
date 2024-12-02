package ca.stefanm.ibus.car.bordmonitor.input

import ca.stefanm.ibus.car.platform.SerialInterfaceServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.car.platform.LongRunningLoopingService
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@ConfiguredCarScope
@PlatformServiceInfo(
    name = "IbusInputMessageParser",
    description = "Parses IBus packets into IBusInputMessages",
)
@SerialInterfaceServiceGroup
class IBusInputMessageParser @Inject constructor(

    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,

    @Named(ApplicationModule.INPUT_EVENTS_WRITER) private val eventSharedFlow: MutableSharedFlow<InputEvent>,

    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,
    indexSelectedMessageParser: IndexSelectedMessageParser,
    mflKeyMessageParser: MflKeyMessageParser,
    rtButtonKeyMessageParser: RtButtonKeyMessageParser,
    bmBtSeekButtonMessageParser: BmBtSeekButtonMessageParser,
    bmBtShowRadioStatusMessageParser: BmBtShowRadioStatusMessageParser,
    bmBtMenuPressedMessageParser: BmBtMenuPressedMessageParser,
    bmBtPhonePressedMessageParser: BmBtPhonePressedMessageParser,
    navKnobMessageParser: NavKnobMessageParser
) : LongRunningLoopingService(coroutineScope, parsingDispatcher) {

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

    class IndexSelectedMessageParser @Inject constructor() : InputMessageMatcher {
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
                    0x00 to Pair(0, EventType.SELECTED), 0x40 to Pair(0, EventType.RELEASE),
                    0x01 to Pair(1, EventType.SELECTED), 0x41 to Pair(1, EventType.RELEASE),
                    0x02 to Pair(2, EventType.SELECTED), 0x42 to Pair(2, EventType.RELEASE),
                    0x03 to Pair(3, EventType.SELECTED), 0x43 to Pair(3, EventType.RELEASE),
                    0x04 to Pair(4, EventType.SELECTED), 0x44 to Pair(4, EventType.RELEASE),
                    0x05 to Pair(5, EventType.SELECTED), 0x45 to Pair(5, EventType.RELEASE),
                    0x06 to Pair(6, EventType.SELECTED), 0x46 to Pair(6, EventType.RELEASE),
                    0x07 to Pair(7, EventType.SELECTED), 0x47 to Pair(7, EventType.RELEASE),
                    0x08 to Pair(8, EventType.SELECTED), 0x48 to Pair(8, EventType.RELEASE),
                    0x09 to Pair(9, EventType.SELECTED), 0x49 to Pair(9, EventType.RELEASE)
                )

                val rawIndex = readByte()
                val indexSelected = eventType[rawIndex.toInt()]?.first ?: return null
                return InputEvent.IndexSelectEvent(indexSelected)
            }
        }

        enum class EventType { SELECTED, RELEASE }
    }

    class MflKeyMessageParser @Inject constructor() : InputMessageMatcher {
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

    class RtButtonKeyMessageParser @Inject constructor() : InputMessageMatcher {
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

    class BmBtSeekButtonMessageParser @Inject constructor() : InputMessageMatcher {
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

    class BmBtShowRadioStatusMessageParser @Inject constructor() : InputMessageMatcher {
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

    class BmBtMenuPressedMessageParser @Inject constructor() : InputMessageMatcher {
        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return InputEvent.BMBTMenuPressed
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.BROADCAST
                    && message.data.toList().map { it.toInt() } == listOf(0x48, 0x34)
        }
    }

    class BmBtPhonePressedMessageParser @Inject constructor() : InputMessageMatcher {
        override fun messageToInputEvent(message: IBusMessage): InputEvent? {
            return InputEvent.BMBTPhonePressed
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.BROADCAST
                    && message.data.toList().map { it.toInt() } == listOf(0x48, 0x08)
        }
    }

    class NavKnobMessageParser @Inject constructor() : InputMessageMatcher {

        private fun IBusMessage.pushDataMatches() = data.toList().map { it.toInt() } == listOf(0x48, 0x05)
        private fun IBusMessage.longPushDataMatches() = data.toList().map { it.toInt() } == listOf(0x48, 0x45)
        private fun IBusMessage.knobReleaseMatches() = data.toList().map { it.toInt() } == listOf(0x48, 0x85)
        private fun IBusMessage.knobTurnMatches() = data.toList().map { it.toInt() } in listOf(
            listOf(0x49, 0x01), listOf(0x49, 0x81),
            listOf(0x49, 0x02), listOf(0x49, 0x82),
            listOf(0x49, 0x03), listOf(0x49, 0x83),
            listOf(0x49, 0x04), listOf(0x49, 0x84),
            listOf(0x49, 0x05), listOf(0x49, 0x85),
            listOf(0x49, 0x06), listOf(0x49, 0x86),
            listOf(0x49, 0x07), listOf(0x49, 0x87),
            listOf(0x49, 0x08), listOf(0x49, 0x88),
            listOf(0x49, 0x09), listOf(0x49, 0x89),
        )

        override fun messageToInputEvent(message: IBusMessage): InputEvent? {

            if (message.pushDataMatches()) {
                return InputEvent.NavKnobPressed
            }

            if (message.knobTurnMatches()) {
                val rotation = message.data.toList().map { it.toInt() }[1]

                if (rotation - 0x80 in 1 .. 9) {
                    return InputEvent.NavKnobTurned(
                        direction = InputEvent.NavKnobTurned.Direction.RIGHT,
                        clicks = rotation - 0x80
                    )
                }

                if (rotation - 0x0 in 1..9) {
                    return InputEvent.NavKnobTurned(
                        direction = InputEvent.NavKnobTurned.Direction.LEFT,
                        clicks = rotation
                    )
                }
            }
            return null
        }

        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.BOARDMONITOR_BUTTONS
                    && message.destinationDevice == IBusDevice.NAV_VIDEOMODULE
                    && (message.pushDataMatches() ||
                        message.longPushDataMatches() ||
                        message.knobReleaseMatches() ||
                        message.knobTurnMatches()
                    )
        }
    }
}