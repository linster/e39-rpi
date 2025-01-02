package ca.stefanm.ibus.lib.hardwareDrivers.pico

import ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named

class PicoScreenStatusManager @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val piToPicoMessageFactory: PiToPicoMessageFactory
) {
    suspend fun goBackToBmw() {
        messagesOut.send(
            piToPicoMessageFactory
                .videoSourceRequest(
                    PiToPicoMessageFactory.PicoVideoRequestSource.Upstream
                )
        )
    }
}