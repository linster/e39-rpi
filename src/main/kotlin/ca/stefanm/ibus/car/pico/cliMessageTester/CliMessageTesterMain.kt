package ca.stefanm.ca.stefanm.ibus.car.pico.cliMessageTester

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ca.stefanm.ibus.car.pico.messageFactory.PicoToPiMessageFactory
import ca.stefanm.ibus.lib.messages.IBusMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.ktor.http.*
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintStream
import javax.inject.Inject


fun main(args : Array<String>) {
    CliMessageTesterMain().main(args)
}

class CliMessageTesterMain {

    val picoToPiMessageFactory = PicoToPiMessageFactory()
    val piToPicoMessageFactory = PiToPicoMessageFactory()

    fun main(args: Array<String>) {

    }

    fun printUsage() {

    }

    //Set up CLIKT here

    interface PiToPicoCommand {}

    class HeartBeatRequestPiToPicoCommand : CliktCommand() {
        override fun run() = Unit
    }
    


    class ListValidMessagesCommand() : CliktCommand(
        name = "list-messages"
    ) {
        companion object {
            val picoToPiMessages : Map<String, PicoToPiMessageFactory.()->IBusMessage> = mapOf(
                "heartbeatRequest" to { heartbeatRequest() },
                "heartbeatResponse" to { heartbeatResponse()},
                "softRestartX" to { softRestartX()},
                "softRestartPi" to { softRestartPi()},
            )

            val piToPicoMessages : Map<String, PiToPicoMessageFactory.()->IBusMessage> = mapOf(
                "heartbeatRequest" to { heartbeatRequest() },
                "heartbeatResponse" to { heartbeatResponse()},
                "configStatusRequest" to { configStatusRequest() },
                "configPush" to {},
                "videoSourceRequest" to {},
                "piHardPowerSwitch" to {}
            )
        }

        override fun run() {
            echo()
        }
    }

    class PrintToCliCommand() : CliktCommand() {

        val messageName by argument()
        override fun run() {

        }
    }

    class PrintToCppCommand() : CliktCommand() {

        override fun run() {

        }
    }

    class PrintToRawBytesCommand() : CliktCommand() {

        override fun run() {

        }
    }



    fun IBusMessage.printToCli(name : String) : String {
        //Return a friendly string that can be printed to the CLI
        //or also included in the CPP file as a comment showing what's in the message
        val retMsg = "Name: ${name}, Message: ${this}"
        println(retMsg)
        return retMsg
    }

    fun IBusMessage.printCppFile(messageName : String) {
        //Print the message as part of a CPP file that can be included in e39-rpi-fw-unittest

        val retString = """
            #ifndef E39_RPI_FW_UNITTEST_MESSAGE_${messageName}_H
            #define E39_RPI_FW_UNITTEST_MESSAGE_${messageName}_H

            class ${messageName}Generator {
            public:
                static std::vector<uint8_t> generate_${messageName}() {
                    std::vector<uint8_t> ret = std::vector<uint8_t>();
                    ${
                        this.toWireBytes().map {
                            "ret.push_back(${it.toString(radix = 16)}); \n\n"    
                        }.fold("") { acc, new -> acc + new }
                    }   
                    return ret;
                }
            }
            #endif //E39_RPI_FW_UNITTEST_MESSAGE_${messageName}_H
        """.trimIndent()


        println(retString)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun IBusMessage.printRawBytes() {
        //Print the raw bytes to stdout, so we could redirect to a serial port if we had to.
        val printStreamWriter = PrintStream(System.out)
        this.toWireBytes().forEach { printStreamWriter.print(it) }
    }

}