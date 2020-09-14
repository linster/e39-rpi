package ca.stefanm.ibus.lib.bordmonitor.input

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class IBusInputMessageParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INPUT_CHANNEL) private val inputChannel : Channel<IBusMessage>,
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel : Channel<InputEvent>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,
    indexSelectedMessageParser: IndexSelectedMessageParser,
    mflKeyMessageParser: MflKeyMessageParser,
    rtButtonKeyMessageParser: RtButtonKeyMessageParser,
    bmBtSeekButtonMessageParser: BmBtSeekButtonMessageParser
) : LongRunningService(coroutineScope, parsingDispatcher) {

    private val messageMatchers = listOf(
        indexSelectedMessageParser,
        mflKeyMessageParser,
        rtButtonKeyMessageParser,
        bmBtSeekButtonMessageParser
    )

    @ExperimentalCoroutinesApi
    override suspend fun doWork() {
        inputChannel.consumeAsFlow().collect {
            messageMatchers.forEach { matcher ->
                if (matcher.rawMessageMatches(it)){
                    matcher.messageToInputEvent(it)?.let {event ->
                        inputEventChannel.send(event)
                    }
                }
            }
        }
    }

    interface InputMessageMatcher {
        fun rawMessageMatches(message: IBusMessage) : Boolean
        fun messageToInputEvent(message: IBusMessage) : InputEvent?

        fun UByteArray.startsWith(vararg bytes: Int) : Boolean {
            var startsWith = false

            bytes.forEachIndexed { i , byte ->
                if (this.getOrNull(i) == byte.toUByte()) {
                    startsWith = startsWith && true
                }
            }
            return startsWith
        }
    }

    class IndexSelectedMessageParser @Inject constructor() : InputMessageMatcher {
        override fun rawMessageMatches(message: IBusMessage): Boolean {
            return message.sourceDevice == IBusDevice.NAV_VIDEOMODULE
                    && message.destinationDevice == IBusDevice.RADIO
                    &&  message.data.startsWith(0x23, 0x62, 0x30)
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
}