package ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.system.PiLifecycle
import kotlinx.coroutines.delay
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named


@PlatformServiceInfo(
    name = "RestartXParser",
    description = "Soft power request from Pico to restart the X server: restarts the HMI."
)
@PicoToPiParserGroup
class RestartXParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val logger: Logger,
    private val notificationHub: NotificationHub,
    private val piLifecycle: PiLifecycle,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    override suspend fun doWork() {
        incomingMessages.filter {
            it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
        }.collect {

            val message = try {
                PicoToPiOuterClass.PicoToPi.parser().parseFrom(it.data.toByteArray())
            } catch (e : Throwable) {
                logger.e("RestartXParser", "Could not parse protobuf", e)
                return@collect
            }

            if (message.messageType == PicoToPiOuterClass.PicoToPi.MessageType.PiSoftPowerRestartX) {
                commsDebugChannel.emit(IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
                    Instant.now(),
                    it,
                    message
                ))
                restartX()
            }
        }
    }

    /**
     * The Pico asks for the X server to be restarted (PiSoftPowerRestartX):
     * on the e39-rpi image that's restarting the HMI (its systemd user
     * service), which is what the user sees of X.
     */
    private suspend fun restartX() {
        logger.w("RestartXParser", "Pico asked to restart the HMI")
        notificationHub.postNotificationBackground(
            Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Restarting", "The Pico asked to restart the HMI")
        )
        delay(2_000)   // long enough to read
        piLifecycle.restartHmi().onFailure {
            notificationHub.postNotificationBackground(
                Notification(Notification.NotificationImage.ALERT_OCTAGON, "Can't restart the HMI", it.message ?: it.toString())
            )
        }
    }

}