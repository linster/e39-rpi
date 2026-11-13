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
    name = "RestartPiParser",
    description = "Soft power request from Pico to do an Init 6: reboots the computer."
)
@PicoToPiParserGroup
class RestartPiParser @Inject constructor(
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
                logger.e("RestartPiParser", "Could not parse protobuf", e)
                return@collect
            }

            if (message.messageType == PicoToPiOuterClass.PicoToPi.MessageType.PiSoftPowerRestartPi) {
                commsDebugChannel.emit(IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
                    Instant.now(),
                    it,
                    message
                ))
                restartPi()
            }
        }
    }

    /** The Pico asks for a reboot (PiSoftPowerRestartPi). */
    private suspend fun restartPi() {
        logger.w("RestartPiParser", "Pico asked to reboot")
        notificationHub.postNotificationBackground(
            Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Rebooting", "The Pico asked to reboot the computer")
        )
        delay(2_000)   // long enough to read
        piLifecycle.reboot().onFailure {
            notificationHub.postNotificationBackground(
                Notification(Notification.NotificationImage.ALERT_OCTAGON, "Can't reboot", it.message ?: it.toString())
            )
        }
    }

}