package ca.stefanm.ibus.lib.hardwareDrivers

import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.logger.Logger
import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.i2c.I2CDevice
import com.pi4j.io.i2c.I2CFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


interface RelayReaderWriter {
    enum class Relay(val address : Int) {
        RELAY_1(address = 0x01),
        RELAY_2(address = 0x02),
        RELAY_3(address = 0x03),
        RELAY_4(address = 0x04)
    }

    fun writeRelayState(relay : Relay, enabled : Boolean)
    fun readRelayState(relay: Relay) : Boolean
}
@ConfiguredCarScope
class CliRelayReaderWriter @Inject constructor(
    private val logger: Logger
) : RelayReaderWriter {

    private companion object {
        const val TAG = "CliRelayReaderWriter"
    }
    private val relayState = mutableMapOf<RelayReaderWriter.Relay, Boolean>()

    override fun writeRelayState(relay: RelayReaderWriter.Relay, enabled: Boolean) {
        logger.d(TAG, "Relay ${relay.name} was ${relayState.getOrDefault(relay, false)} now set to value $enabled")
        relayState[relay] = enabled
    }

    override fun readRelayState(relay: RelayReaderWriter.Relay): Boolean {
        return relayState.getOrDefault(relay, false).also {
            logger.d(TAG, "Relay ${relay.name} has value $it")
        }
    }
}

@ConfiguredCarScope
class RpiRelayReaderWriter @Inject constructor() : RelayReaderWriter {

    //https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/I2CWiiMotionPlusExample.java
    /** https://wiki.52pi.com/index.php/DockerPi_4_Channel_Relay_SKU:_EP-0099 */
    private companion object {
        const val relayAddress : Int = 0x00 //Both dip switches off.
    }

    private val relayDevice : I2CDevice by lazy {
        val i2cbus : I2CBus = I2CFactory.getInstance(I2CBus.BUS_1)
        i2cbus.getDevice(relayAddress)
    }

    override fun writeRelayState(relay : RelayReaderWriter.Relay, enabled : Boolean) {
        val data = (if (enabled) { 0xFF } else { 0x00 }).toByte()
        relayDevice.write(relay.address, data)
    }

    override fun readRelayState(relay: RelayReaderWriter.Relay) : Boolean {
        val result = relayDevice.read(relay.address)

        if (result < 0) {
            //TODO warn on failure to read relay
        }

        return result == 0xFF
    }
}