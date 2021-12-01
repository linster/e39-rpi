package ca.stefanm.ibus.car.hardware

import ca.stefanm.ibus.car.conduit.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.logger.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Named

@ConfiguredCarScope
class TelephoneButtonVideoSwitcherService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

    private val videoEnableRelayManager: VideoEnableRelayManager,
    private val logger: Logger,

    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        inputEvents.collect {
            if (it is InputEvent.BMBTPhonePressed) {
                val oldState = videoEnableRelayManager.videoEnabled
                logger.d("TEL", "Old Video Enable state is $oldState. Flipping to new state")
                videoEnableRelayManager.videoEnabled = !oldState
            }
        }
    }
}