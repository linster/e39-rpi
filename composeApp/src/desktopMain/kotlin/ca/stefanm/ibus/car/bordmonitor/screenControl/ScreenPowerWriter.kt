package ca.stefanm.ibus.car.bordmonitor.screenControl

import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPublisherService
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named

class ScreenPowerWriter @Inject constructor(
    private val logger: Logger,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
) {

    suspend fun turnScreenOff() {
        logger.d("ScreenPowerWriter", "turnScreenOff()")
        messagesOut.send(
            IBusMessage(
                IBusDevice.NAV_VIDEOMODULE,
                IBusDevice.BOARDMONITOR_BUTTONS,
                byteArrayOf(0x4f, 0x00).toUByteArray()
            )
        )
    }
}