package ca.stefanm.ibus.lib.bordmonitor.input

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.cli.debugPrinters.IbusInputEventCliPrinter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.IBusInputEventListenerService
import ca.stefanm.ibus.lib.platform.IBusMessageListenerService
import ca.stefanm.ibus.lib.platform.LongRunningLoopingService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class IBusInputMessageParser @Inject constructor(
    private val serialListenerService: SerialListenerService,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher,
    indexSelectedMessageParser: IndexSelectedMessageParser,
    mflKeyMessageParser: MflKeyMessageParser,
    rtButtonKeyMessageParser: RtButtonKeyMessageParser,
    bmBtSeekButtonMessageParser: BmBtSeekButtonMessageParser,
    bmBtShowRadioStatusMessageParser: BmBtShowRadioStatusMessageParser,
    bmBtMenuPressedMessageParser: BmBtMenuPressedMessageParser,
    bmBtPhonePressedMessageParser: BmBtPhonePressedMessageParser
) : LongRunningLoopingService(coroutineScope, parsingDispatcher), IBusMessageListenerService {

    private val messageMatchers = listOf(
        indexSelectedMessageParser,
        mflKeyMessageParser,
        rtButtonKeyMessageParser,
        bmBtSeekButtonMessageParser,
        bmBtShowRadioStatusMessageParser,
        bmBtMenuPressedMessageParser,
        bmBtPhonePressedMessageParser
    )

    private val mailboxes = mutableListOf<IBusInputEventListenerService>()

    fun addMailbox(serviceWithMailbox: IBusInputEventListenerService) {
        mailboxes.add(serviceWithMailbox)
    }

    fun removeMailbox(serviceWithMailbox: IBusInputEventListenerService) {
        mailboxes.remove(serviceWithMailbox)
    }

    override fun onCreate() {
        super.onCreate()
        serialListenerService.addMailbox(this)
    }

    override fun onShutdown() {
        serialListenerService.removeMailbox(this)
        super.onShutdown()
    }

    override val incomingIBusMessageMailbox: Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)

    @ExperimentalCoroutinesApi
    override suspend fun doWork() {
        incomingIBusMessageMailbox.poll()?.let { message ->
            messageMatchers.forEach { matcher ->
                if (matcher.rawMessageMatches(message)) {
                    matcher.messageToInputEvent(message)?.let { event ->
                        mailboxes.forEach { mailbox -> mailbox.incomingIbusInputEvents.send(event) }
                    }
                }
            }
        }
    }

    suspend fun debugSend(inputEvent: InputEvent) {
        mailboxes.forEach { mailbox -> mailbox.incomingIbusInputEvents.send(inputEvent) }
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
}