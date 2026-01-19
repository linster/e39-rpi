package ca.stefanm.ca.stefanm.ibus.car.desktop.input

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.PeripheralsDesktopGroup
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.irgaly.kfswatch.KfsEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okio.Buffer
import okio.buffer
import okio.source
import org.freedesktop.dbus.types.UInt16
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "GriffinPowermateListener",
    description = "Uses a udev-rule to bind /dev/input/powermate to ibus knob turn events."
)
@PeripheralsDesktopGroup
@ConfiguredCarScope
class GriffinPowermateReader @Inject constructor(

    @Named(ApplicationModule.INPUT_EVENTS_WRITER) private val eventSharedFlow: MutableSharedFlow<InputEvent>,

    private val logger: Logger,

    private val notificationHub: NotificationHub,

    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE)
    private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER)
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    companion object {
        const val TAG = "GriffinPowermateReader"
        const val devFilePath = "/dev/input/powermate"
    }

    override suspend fun doWork() {

        getDeviceConnectedStatus()
            .onStart {
                //Hot-start if we're already plugged in
                if (File(devFilePath).exists()) {
                    emit(DeviceConnectedStatus.DeviceConnected(File(devFilePath)))
                }
            }
            .onEach {
                //Notify connection events
                val notification = when (it) {
                    is DeviceConnectedStatus.DeviceConnected ->
                        Notification(
                            topText = "Powermate",
                            contentText = "Connected: ${it.deviceFile.path}"
                        )
                    DeviceConnectedStatus.DeviceDisconnected ->
                        Notification(
                            topText = "Powermate",
                            contentText = "Disconnected"
                        )
                }
                notificationHub.postNotification(notification)
            }
            .filter { it is DeviceConnectedStatus.DeviceConnected }
            .transformLatest { status ->
                status as DeviceConnectedStatus.DeviceConnected
                getEvEvents(status.deviceFile).collect { emit(it) }
            }
            .filterNot { it.type == 0 && it.code == 0 && it.value == 0 }
            .transform {
                if (it.type == 2 && it.code == 7) {
                    //Knob turn
                    if (it.value == 1) {
                        emit(PowerMateEvent.TurnRight(it.timestamp))
                    }
                    if (it.value == -1) {
                        emit(PowerMateEvent.Turnleft(it.timestamp))
                    }
                }

                if (it.type == 1 && it.code == 256) {
                    //Knob press and release
                    if (it.value == 1) {
                        emit(PowerMateEvent.KnobPress(it.timestamp))
                    }
                    if (it.value == 0) {
                        emit(PowerMateEvent.KnobRelease(it.timestamp))
                    }
                }
            }
            .transform {
                when (it) {
                    is PowerMateEvent.Turnleft -> InputEvent.NavKnobTurned(1, InputEvent.NavKnobTurned.Direction.LEFT)
                    is PowerMateEvent.TurnRight -> InputEvent.NavKnobTurned(1, InputEvent.NavKnobTurned.Direction.RIGHT)
                    is PowerMateEvent.KnobPress -> InputEvent.NavKnobPressed
                    else -> null
                }?.let { event -> emit(event) }

            }
            .collect {
                logger.d(TAG, it.toString())
                eventSharedFlow.emit(it)
            }


    }

    sealed class DeviceConnectedStatus {
        data class DeviceConnected(val deviceFile : File) : DeviceConnectedStatus()
        data object DeviceDisconnected : DeviceConnectedStatus()
    }

    private fun getDeviceConnectedStatus() : Flow<DeviceConnectedStatus> {
        return callbackFlow producerScope@ {
            val watcher = KfsDirectoryWatcher(this@producerScope)
            watcher.add("/dev/input")
            watcher.onEventFlow
                .filter { it.path == "powermate" }
                .transform {
                    if (it.event == KfsEvent.Create) {
                        emit(DeviceConnectedStatus.DeviceConnected(File(devFilePath)))
                    }
                    if (it.event == KfsEvent.Delete) {
                        emit(DeviceConnectedStatus.DeviceDisconnected)
                    }
                }
                .collect { send(it) }
            // No need to awaitClose for the watcher, it'll self close
            // when producerScope is closed.
        }.onEach {
            logger.d(TAG, "Event: $it")
        }
    }

    //https://github.com/Callisto95/Java-EventReader/blob/main/src/main/java/net/unknownuser/eventreader/InputEvent.java
    //Looks like the input_event struct is a format that never changes.
    //Do the same think with ibus packets, but use Okio Buffer to serialize
    //to things.
    // The Java-EventReader toy library just takes a bunch of C header files
    // from the linux kernel and makes them into java enums.

    //https://stackoverflow.com/a/25423818
    //https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/tree/include/uapi/linux/input.h


    data class KtEvEvent(
        val timestamp : Long,
        val type : Int, //UInt16
        val code : Int, //UInt16
        val value : Int //Short32
    )

//
//    data class EvEventRaw(
//        __u16 type;
//    UInt code;
//    __s32 value;
//    )


    fun breakBufferIntoEvent(buffer : Buffer) : KtEvEvent {
        /**
         * The struct input_event as a Java record.
         * <pre>
         * [====64====][====64====][=16=][=16=][==32==] => 192 bits
         * [---- 8----][---- 8----][- 2-][- 2-][-- 4--] -> 24 bytes
         * time                    type  code  value
         * sec         microsec
         * </pre>
         */
        //https://github.com/Callisto95/Java-EventReader/blob/main/src/main/java/net/unknownuser/eventreader/InputEvent.java
        val sec = buffer.readLongLe()
        val msec = buffer.readLongLe()
        val type = buffer.readShortLe().toInt()
        val code = buffer.readShortLe().toInt()
        val value = buffer.readIntLe()
        return KtEvEvent(
            timestamp = sec,
            type = type,
            code = code,
            value = value
        )
    }


    private fun getEvEvents(deviceFile: File) : Flow<KtEvEvent>{
        return callbackFlow {
            val inputStream = deviceFile.inputStream()
            val buffer = inputStream.source().buffer()

            launch {
                while (isActive) {
                    //logger.d(TAG, "Bytes available: ${buffer.buffer.size}")

                    buffer.request(24)
                    //logger.d(TAG, "Snapshot: ${buffer.buffer.snapshot(24)}")

                    send(breakBufferIntoEvent(buffer.buffer))
                }
            }
            awaitClose {
                buffer.close()
                inputStream.close()
            }
        }
    }

    sealed class PowerMateEvent(open val timestamp: Long) {
        data class KnobPress(override val timestamp: Long) : PowerMateEvent(timestamp)
        data class KnobRelease(override val timestamp: Long) : PowerMateEvent(timestamp)
        data class Turnleft(override val timestamp: Long) : PowerMateEvent(timestamp)
        data class TurnRight(override val timestamp: Long) : PowerMateEvent(timestamp)
    }
}


