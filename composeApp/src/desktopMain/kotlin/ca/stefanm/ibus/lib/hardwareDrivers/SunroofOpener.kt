package ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

class SunroofOpener @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>
) {
    suspend fun openSunroof() {
        messagesOut.send(
            IBusMessage(
                sourceDevice = IBusDevice.DIS,
                destinationDevice = IBusDevice.BODY_MODULE,
                data = with(Buffer()) {
                    writeByte(0x0C)
                    writeByte(0x00)
                    writeByte(0x66)
                }.readByteArray().toUByteArray()
            )
        )
    }
}