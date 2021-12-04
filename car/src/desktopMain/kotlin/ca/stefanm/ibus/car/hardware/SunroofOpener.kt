package ca.stefanm.ibus.car.hardware

import ca.stefanm.ibus.car.conduit.CarConduitModule
import ca.stefanm.ibus.car.conduit.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named

class SunroofOpener @Inject constructor(
    @Named(CarConduitModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>
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