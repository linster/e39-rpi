package ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory

import ca.stefanm.e39.proto.ConfigProtoOuterClass.ConfigProto
import ca.stefanm.e39.proto.PiToPicoOuterClass.PiToPico
import ca.stefanm.e39.proto.piToPico
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

class PiToPicoMessageFactory @Inject constructor() {

    fun heartbeatRequest() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = PiToPico.MessageType.HeartbeatRequest
            }.toByteArray().toUByteArray()
        )
    }

    fun heartbeatResponse() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = PiToPico.MessageType.HeartbeatResponse
            }.toByteArray().toUByteArray()
        )
    }

    fun configStatusRequest() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = PiToPico.MessageType.ConfigStatusRequest
            }.toByteArray().toUByteArray()
        )
    }

    fun configPush(new : ConfigProto) : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = PiToPico.MessageType.ConfigPush
                newConfig = new
            }.toByteArray().toUByteArray()
        )
    }

    enum class PicoVideoRequestSource(val messageType : PiToPico.MessageType){
        Upstream(PiToPico.MessageType.PicoVideoRequestUpstream),
        Pico(PiToPico.MessageType.PicoVideoRequestPico),
        Rpi(PiToPico.MessageType.PicoVideoRequestRpi),
        RVC(PiToPico.MessageType.PicoVideoRequestRVC)
    }

    fun videoSourceRequest(source : PicoVideoRequestSource) : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = source.messageType
            }.toByteArray().toUByteArray()
        )
    }

    fun piHardPowerSwitch(isOn : Boolean) : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = if (isOn) {
                    PiToPico.MessageType.PicoPowerRequestOn
                } else {
                    PiToPico.MessageType.PicoPowerRequestOff
                }
            }.toByteArray().toUByteArray()
        )
    }

    enum class IgnitionPosition {
        POSITION_0,
        POSITION_1,
        POSITION_2,
        POSITION_3
    }
    fun simulateIgnition(position: IgnitionPosition) : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PI,
            destinationDevice = IBusDevice.PICO,
            data = piToPico {
                messageType = when(position) {
                    IgnitionPosition.POSITION_0 -> PiToPico.MessageType.SimulatedIgnitionPosition0
                    IgnitionPosition.POSITION_1 -> PiToPico.MessageType.SimulatedIgnitionPosition1
                    IgnitionPosition.POSITION_2 -> PiToPico.MessageType.SimulatedIgnitionPosition2
                    IgnitionPosition.POSITION_3 -> PiToPico.MessageType.SimulatedIgnitionPosition3
                }
            }.toByteArray().toUByteArray()
        )
    }

}