package ca.stefanm.ibus.car.bluetooth

import ca.stefanm.ca.stefanm.ibus.car.bluetooth.blueZdbus.FlowDbusConnector
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.bluez.MediaPlayer1
import org.freedesktop.dbus.errors.UnknownObject
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "BluetoothEventDispatcherService",
    description = "Takes input events from IBUS and yeets them at DBus to change the track"
)
@BluetoothServiceGroup
@ConfiguredCarScope
class BluetoothEventDispatcherService @Inject constructor(
    @Named(ApplicationModule.INPUT_EVENTS) private val inputEvents : SharedFlow<InputEvent>,

    private val flowDbusConnector: FlowDbusConnector,

    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        /// TODO something is very wrong with this service.
        /// TODO it steals input events. There might be an unexpected rendezvous

        inputEvents.filter { it is InputEvent.PrevTrack || it is InputEvent.NextTrack }.collect { event ->
            flowDbusConnector.getPlayer().collect { player ->
                dispatchInputEvent(player, event)
            }
        }
    }

    private fun dispatchInputEvent(mediaPlayer1: MediaPlayer1?, event: InputEvent) {
        if (mediaPlayer1 == null) {
            logger.w("BT Dispatcher", "Attempting to dispatch to MediaPlayer where it doesn't exist.")
            return
        }
        try {
            when (event) {
                InputEvent.PrevTrack -> mediaPlayer1.Previous()
                InputEvent.NextTrack -> mediaPlayer1.Next()
                else -> {}
            }
        } catch (e : UnknownObject) {
            logger.e("BT Dispatcher", "Unknown object?", e)
        }

    }
}