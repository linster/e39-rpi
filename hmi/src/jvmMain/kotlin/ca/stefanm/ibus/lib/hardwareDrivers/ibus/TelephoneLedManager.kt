package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.car.conduit.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named
import kotlin.experimental.or

@ExperimentalStdlibApi
class TelephoneLedManager @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>
){

    //MID	Obc	Phone LED	c8 04 e7 2b xx chk	xx is a bitmask 7 6 5 4 3 2 1 0
    // Bit 0 = red, bit 1 = red+blink, bit 2 = orange, bit 3 = orange+blink, bit 4 = green, bit 5 = green+blink
    enum class LedState {
        OFF,
        ON,
        BLINK
    }

    enum class Led {
        RED,
        ORANGE,
        GREEN
    }

    data class LedStatus(val led : Led, val state : LedState)

    @ExperimentalStdlibApi
    class TelephoneLedEnableMessage(
        ledStatuses: Array<LedStatus>
    ) : IBusMessage(
        sourceDevice = IBusDevice.MID,
        destinationDevice = IBusDevice.RADIO,
        data = arrayOf(ledStatuses.toUByte()).toUByteArray()
    )

    suspend fun setTelephoneLeds(redStatus : LedState, orangeStatus : LedState, greenStatus : LedState) {
        messagesOut.send(
            TelephoneLedEnableMessage(
                arrayOf(
                    LedStatus(
                        Led.RED,
                        redStatus
                    ),
                    LedStatus(
                        Led.ORANGE,
                        orangeStatus
                    ),
                    LedStatus(
                        Led.GREEN,
                        greenStatus
                    )
                )
            )
        )
    }
}

@ExperimentalStdlibApi
fun Array<TelephoneLedManager.LedStatus>.toUByte() : UByte {
    var result = 0x00.toByte()
    this.forEach {
        val nibble = when(it.state) {
            TelephoneLedManager.LedState.OFF -> 0x00
            TelephoneLedManager.LedState.ON -> 0x01
            TelephoneLedManager.LedState.BLINK -> 0x02
        }
        result = result or nibble.toByte().rotateLeft(
            when (it.led) {
                TelephoneLedManager.Led.RED -> 0
                TelephoneLedManager.Led.ORANGE -> 2
                TelephoneLedManager.Led.GREEN -> 4
            }
        )
    }
    return result.toUByte()
}