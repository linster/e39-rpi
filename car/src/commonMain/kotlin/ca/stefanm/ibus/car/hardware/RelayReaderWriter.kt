package ca.stefanm.ibus.car.hardware

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