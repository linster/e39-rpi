package ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory

import ca.stefanm.e39.proto.ConfigProtoOuterClass.ConfigProto
import ca.stefanm.e39.proto.PicoToPiOuterClass.PicoToPi.MessageType
import ca.stefanm.e39.proto.configProto
import ca.stefanm.e39.proto.picoToPi
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import javax.inject.Inject

//This class is mostly used for making raw ibus messages
// PicoToPi so that we can test the PI's parsing logic.

//We can call the protobuf code on the Pi using this fake
//test data, then compare it with the protobuf code on the
//pico and see if it makes the same raw IbusMessage.
class PicoToPiMessageFactory @Inject constructor() {

    fun heartbeatRequest() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PICO,
            destinationDevice = IBusDevice.PI,
            data = picoToPi {
                messageTypeValue = MessageType.HeartbeatRequest_VALUE
            }.toByteArray().toUByteArray()
        )
    }

    fun heartbeatResponse() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PICO,
            destinationDevice = IBusDevice.PI,
            data = picoToPi {
                messageTypeValue = MessageType.HeartbeatResponse_VALUE
            }.toByteArray().toUByteArray()
        )
    }

    fun logStatement(log : String) : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PICO,
            destinationDevice = IBusDevice.PI,
            data = picoToPi {
                messageTypeValue = MessageType.LogStatement_VALUE
                loggerStatement = log
            }.toByteArray().toUByteArray()
        )
    }

    fun softRestartX() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PICO,
            destinationDevice = IBusDevice.PI,
            data = picoToPi {
                messageTypeValue = MessageType.PiSoftPowerRestartX_VALUE

                //Pico always puts in an empty string for this field, even though
                //it's optional.
//                loggerStatement = ""

//                existingConfig = configProto {  }
            }.toByteArray().toUByteArray()
        )
    }

    fun softRestartPi() : IBusMessage {
        return IBusMessage(
            sourceDevice = IBusDevice.PICO,
            destinationDevice = IBusDevice.PI,
            data = picoToPi {
                messageTypeValue = MessageType.PiSoftPowerRestartPi_VALUE

                //Pico always puts in an empty string for this field, even though
                //it's optional.
//                loggerStatement = ""

//                existingConfig = configProto {  }
            }.toByteArray().toUByteArray()
        )
    }

    data class ConfigStatusResponse(
        val rawMessage : IBusMessage,
        val configProto: ConfigProto
    )
//
//    fun syntheticConfigStatusResponse(
//        givenConfigProto: ConfigProto
//    ) : ConfigStatusResponse {
//        return ConfigStatusResponse(
//            rawMessage = IBusMessage(
//                sourceDevice = IBusDevice.PICO,
//                destinationDevice = IBusDevice.PI,
//                data = picoToPi {
//                    messageTypeValue = MessageType.ConfigStatusResponse_VALUE
//                    existingConfig = givenConfigProto
//                }.toByteArray().toUByteArray()
//            ),
//            configProto = givenConfigProto
//        )
//    }
}