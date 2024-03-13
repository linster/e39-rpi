package ca.stefanm.ibus.car.bluetooth

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bluetooth.blueZdbus.DbusReconnector
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
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

    private val dbusReconnector: DbusReconnector,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    var mediaPlayer1: MediaPlayer1? = null

    override suspend fun doWork() {
        inputEvents.collect {
            dispatchInputEvent(it)
        }
    }

    private fun dispatchInputEvent(event: InputEvent) {
        if (mediaPlayer1 == null) {
            logger.w("BT Dispatcher", "Attempting to dispatch to MediaPlayer where it doesn't exist.")
            mediaPlayer1 = dbusReconnector.reconnect().second
        }
        try {
            when (event) {
                InputEvent.PrevTrack -> mediaPlayer1?.Previous()
                InputEvent.NextTrack -> mediaPlayer1?.Next()
                else -> {}
            }
        } catch (e : UnknownObject) {
            logger.e("BT Dispatcher", "Unknown object?", e)
        }

    }
}