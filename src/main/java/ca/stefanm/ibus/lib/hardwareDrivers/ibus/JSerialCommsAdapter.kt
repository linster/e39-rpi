package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.messages.IBusMessage.Companion.toIbusMessage
import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okio.Source
import okio.buffer
import javax.inject.Inject
import javax.inject.Singleton


interface SerialPortReader {
    fun readMessages() : Flow<IBusMessage>
}

interface SerialPortWriter {
    suspend fun writeRawBytes(bytes : ByteArray)
}

class JSerialCommsAdapter @Inject constructor(
    private val writer : JSerialCommsWriter,
    private val reader : JSerialCommsReader
) : SerialPortWriter, SerialPortReader {

    override suspend fun writeRawBytes(bytes: ByteArray) {
        writer.writeRawBytes(bytes)
    }

    override fun readMessages(): Flow<IBusMessage> {
        return reader.readMessages()
    }
}

@ExperimentalCoroutinesApi
class JSerialCommsReader @Inject constructor(
    private val logger: Logger,
    serialPortProvider: JSerialCommsSerialPortProvider,
    private val flowDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SerialPortReader{

    private companion object {
        const val MAX_MESSAGE_LEGNTH = 128 //Max length of an IBUS Message Packet
        const val MAX_MESSAGES_TO_BUFFER = 24 //Max number of messages to buffer from the serial port before processing.
    }

    private val port = serialPortProvider.serialPort

    //readtimeout should be set to 75ms
    private val TAG = "OKIO"

    override fun readMessages(): Flow<IBusMessage> {
        return rawIBusPackets.consumeAsFlow()
            .flowOn(flowDispatcher)
            .mapNotNull { it.toIbusMessage() }
            .onStart { setupJSerialComm() }
    }

    private val rawIBusPackets = Channel<ByteArray>()

    fun setupJSerialComm() {
        //Add a listener here
        //https://fazecast.github.io/jSerialComm/javadoc/com/fazecast/jSerialComm/SerialPortDataListener.html
        //for a data available listener. Then,
        logger.v(TAG, "Setting up jSerialComm")

        port.addDataListener(object : SerialPortDataListener {
            private val listenerTAG = "SerialPortDataListener"
            override fun getListeningEvents() = SerialPort.LISTENING_EVENT_DATA_AVAILABLE

            override fun serialEvent(event: SerialPortEvent?) {

                //TODO We need to dump the data into a buffer real fast here, no processing on the main thread.
                //TODO The OS can kill us at any minute.

                if (event == null) {
                    logger.w(listenerTAG, "SerialPortEvent was null?!")
                    return
                }

                //Kick-off the reading of the bus.

                val rawPacket = byteArrayOf()
                val bytesAvailable = port.bytesAvailable()
                port.readBytes(rawPacket, bytesAvailable.toLong())

                logger.v("BYTE READER",
                    "Read IBUS byte array: ${rawPacket.map { it.toString(16) }.joinToString { " " }}")
                //TODO check here if we even read the amount we're supposed to.
                rawIBusPackets.sendBlocking(rawPacket)
            }
        })
    }

    //https://github.com/tedsalmon/DroidIBus/blob/master/app/src/main/java/com/ibus/droidibus/ibus/IBusMessageService.java#L156
    fun bufferRawByteStream(input : Source) {

        val bufferedInput = input.buffer()

        // [Source] [Len] [Dest] [Data...] [CRC]
        val lengthByte = bufferedInput.buffer.get(1)

        val messageBytes = byteArrayOf()

        //CRC is included in the length of the message.
        val bytesRead = bufferedInput.buffer.read(messageBytes, 0, 2 + lengthByte)

        if (bytesRead < lengthByte + 2) {
            logger.w(TAG, "Read too few bytes: expected length byte $lengthByte got $bytesRead")
        }

        //TODO get that messageBytes outta here and flush the buffer.

    }

}

@Singleton
class JSerialCommsSerialPortProvider @Inject constructor(
    private val logger: Logger,
    private val deviceConfiguration: DeviceConfiguration
) {

    companion object {
        const val TAG = "SerialPortProvider"
        const val SEND_TIMEOUT_MS = 100
        const val READ_TIMEOUT_NO_DATA_MS = 100 //maybe 75?
    }

    val serialPort : SerialPort
        get() = setupSerialPort()

    private fun setupSerialPort() : SerialPort {
        val port = try {
            SerialPort.getCommPort(deviceConfiguration.iBusInterfaceUri)
        } catch (e : SerialPortInvalidPortException) {
            logger.e(TAG,
                "Invalid serial port chosen. Valid ports are: ${SerialPort.getCommPorts().map { it.systemPortName }}")
            throw e
        }

        if (port.systemPortName !in SerialPort.getCommPorts().toList().map { it.systemPortName }) {
            logger.w(TAG,
                "Unrecognized serial port ${port.systemPortName} chosen. Valid ports are: ${SerialPort.getCommPorts().map { it.systemPortName }}")
        } else {
            logger.d(TAG, "Opened serial port ${port.systemPortName}")
        }

        port.setComPortParameters(
            9600, 8, 1, SerialPort.EVEN_PARITY
        )

        port.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_BLOCKING,
            READ_TIMEOUT_NO_DATA_MS,
            SEND_TIMEOUT_MS
        )

        if (!port.isOpen) {
            port.openPort()
        }
        return port
    }
}

class JSerialCommsWriter @Inject constructor(
    private val logger: Logger,
    serialPortProvider: JSerialCommsSerialPortProvider
) : SerialPortWriter {

    private val port = serialPortProvider.serialPort

    override suspend fun writeRawBytes(bytes: ByteArray) {
        //Wait 2ms to ensure previous message send went out. Realistically we only need to wait 1.2ms.
        delay(2)

        //We're doing non-blocking IO for the serial port because we don't want to block the coroutine..
        var offset = 0L
        while (offset < bytes.size -1) {
            val bytesToAttemptWrite = bytes.size - offset
            val bytesWritten = port.writeBytes(bytes, bytesToAttemptWrite, offset)

            if (bytesWritten == -1) {
                logger.e("SERIAL WRITER", "Error writing to port")
                return
            }

            offset += bytesWritten + 1
        }
    }
}