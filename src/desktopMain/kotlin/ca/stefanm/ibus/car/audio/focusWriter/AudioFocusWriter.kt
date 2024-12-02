package ca.stefanm.ca.stefanm.ibus.car.audio.focusWriter

import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named

class AudioFocusWriter @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val logger: Logger
) {

    suspend fun navTv() {
        // To switch radio to NAV/TV
        // Src Device: GT
        // Dest Device: RAD
        // Datagram: 3B 05 68 4E 01 00 19

        //Mode button on BMBT won't work till aux sent.
        logger.d("AudioFocusWriter", "Request Audio Source NAV/TV.");

        messagesOut.send(
            IBusMessage(
                IBusDevice.NAV_VIDEOMODULE,
                IBusDevice.RADIO,
                byteArrayOf(0x4E, 0x01, 0x00).toUByteArray()
            )
        )
    }

    suspend fun aux() {
        // To switch radio to AUX:
        // Src Device: GT
        // Dest Device: RAD
        // Datagram: 3B 05 68 4E 00 00 18
        logger.d("AudioFocusWriter", "Request Audio Source Aux");

        messagesOut.send(
            IBusMessage(
                IBusDevice.NAV_VIDEOMODULE,
                IBusDevice.RADIO,
                byteArrayOf(0x4E, 0x00, 0x00).toUByteArray()
            )
        )
    }
}