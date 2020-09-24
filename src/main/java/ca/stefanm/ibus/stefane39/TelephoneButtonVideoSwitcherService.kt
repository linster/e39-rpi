package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.lib.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.hardwareDrivers.VideoEnableRelayManager
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.IBusInputEventListenerService
import ca.stefanm.ibus.lib.platform.IBusMessageListenerService
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class TelephoneButtonVideoSwitcherService @Inject constructor(
    private val videoEnableRelayManager: VideoEnableRelayManager,
    private val logger: Logger,
    private val iBusInputMessageParser: IBusInputMessageParser,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), IBusInputEventListenerService {

    override val incomingIbusInputEvents: Channel<InputEvent> = Channel()

    override fun onCreate() {
        super.onCreate()
        iBusInputMessageParser.addMailbox(this)
    }

    override fun onShutdown() {
        iBusInputMessageParser.removeMailbox(this)
        super.onShutdown()
    }

    override suspend fun doWork() {
        incomingIbusInputEvents.receiveAsFlow().collect {
            if (it is InputEvent.BMBTPhonePressed) {
                val oldState = videoEnableRelayManager.videoEnabled
                logger.d("TEL", "Old Video Enable state is $oldState. Flipping to new state")
                videoEnableRelayManager.videoEnabled = !oldState
            }
        }
    }
}