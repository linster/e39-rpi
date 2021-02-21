package ca.stefanm.ibus.lib.hardwareDrivers

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoEnableRelayManager @Inject constructor(
    private val relayReaderWriter: RelayReaderWriter
) {

    var videoEnabled : Boolean = true
        get() = !rpiVideoRelayOn
        set(value) {
            field = value
            rpiVideoRelayOn = !value
        }

    private var rpiVideoRelayOn : Boolean
        get() = relayReaderWriter.readRelayState(RelayReaderWriter.Relay.RELAY_1)
        set(value) = relayReaderWriter.writeRelayState(RelayReaderWriter.Relay.RELAY_1, value)
}