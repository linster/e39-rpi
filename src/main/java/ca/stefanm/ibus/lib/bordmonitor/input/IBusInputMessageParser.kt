package ca.stefanm.ibus.lib.bordmonitor.input

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class IBusInputMessageParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INPUT_CHANNEL) private val inputChannel : Channel<IBusMessage>,
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel : Channel<InputEvent>,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,
    private val indexSelectedMessageParser: IndexSelectedMessageParser
) : LongRunningService(coroutineScope, parsingDispatcher) {

    @ExperimentalCoroutinesApi
    override suspend fun doWork() {
        inputChannel.poll()?.let {
            if (indexSelectedMessageParser.rawMessageMatches(it)){
                indexSelectedMessageParser.messageToInputEvent(it)?.let {
                    inputEventChannel.send(it)
                }
            }
        }
    }

    interface InputMessageMatcher {
        fun rawMessageMatches(message: IBusMessage) : Boolean
        fun messageToInputEvent(message: IBusMessage) : InputEvent?

        fun ByteArray.startsWith(vararg bytes: Byte) : Boolean {
            var startsWith = false

            bytes.forEachIndexed { i , byte ->
                if (this.getOrNull(i) == byte) {
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
                write(message.data)

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
}