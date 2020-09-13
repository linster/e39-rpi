package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.lib.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.messages.IBusMessage.Companion.toIbusMessage
import ca.stefanm.ibus.lib.messages.toDeviceIdString
import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import ca.stefanm.ibus.lib.platform.Platform
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okio.Buffer
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
    private val coroutineScope: CoroutineScope,
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
        return rawSerialPackets.consumeAsFlow()
            .flowOn(flowDispatcher)
            .mapNotNull { it.toIbusMessage() }
            .onStart { setupJSerialComm() }
            .onCompletion { readerJob?.cancel(Platform.PlatformShutdownCancellationException()) }
    }

    private val rawSerialPackets = Channel<UByteArray>()

    private val rawBuffer = Buffer()

    private var readerJob : Job? = null

    private suspend fun setupJSerialComm() {
        //Add a listener here
        //https://fazecast.github.io/jSerialComm/javadoc/com/fazecast/jSerialComm/SerialPortDataListener.html
        //for a data available listener. Then,
        logger.v(TAG, "Setting up jSerialComm read coroutine.")

        readerJob = coroutineScope.launch(flowDispatcher) {
            while (true) {
                val bytesAvailable = port.bytesAvailable()
                if (bytesAvailable == 0) { yield() }
                if (bytesAvailable == -1) { logger.w(TAG, "Port not open") ; break }

                val readBytes = ByteArray(bytesAvailable)
                port.readBytes(readBytes, bytesAvailable.toLong())

                if (bytesAvailable > 0) {
//                    logger.v("BYTE READER", "Read $bytesAvailable bytes from serial port.")
                }
                rawBuffer.write(readBytes)

                breakBufferIntoPackets(rawBuffer).collect { packet -> rawSerialPackets.send(packet)}
            }
        }
    }




    @ExperimentalUnsignedTypes
    private fun breakBufferIntoPackets(buffer: Buffer) : Flow<UByteArray> = flow {
        val debugBuffer = buffer.copy()
        while (!buffer.exhausted()) {
            if (buffer.size < 4) {
                //No source, len, dest, xor checksum
                return@flow
            }

            if (buffer.size < buffer.get(1) + 2) { //Need the +2 to get source + length bytes.
                //We haven't collected the amount of data this packet says it should have.
                //logger.v(TAG, "Buffer underrun -- waiting for more bytes. Expected ${buffer.get(1)} got ${buffer.size}")
                return@flow
            }

            val sourceDevice = buffer.readByte().toUByte()
            val packetLength = buffer.readByte().toUByte().toInt()
            val destDevice = buffer.readByte().toUByte()

            val data = if (packetLength <= 2) {
                ubyteArrayOf()
            } else {
                // subtract 2 because length includes checksum and dest address
                buffer.readByteArray(packetLength.toLong() - 2).toUByteArray()
            }
            val givenCrc = buffer.readByte().toUByte()

            var actualCrc = 0x00
            ubyteArrayOf(sourceDevice, packetLength.toUByte(), destDevice, *data).forEach { byte -> actualCrc = actualCrc xor byte.toInt() }

            val reAssembledPacket = ubyteArrayOf(sourceDevice, packetLength.toUByte(), destDevice, *data, givenCrc)

            logger.v("BYTE READER",
                "Read raw packet : " +
                        "[${sourceDevice.toDeviceIdString()}] " +
                        "[${packetLength.toString(10)}] " +
                        "[${destDevice.toDeviceIdString()}] " +
                        "<${data.size} bytes data> " +
                        "[CRC g/a : $givenCrc / $actualCrc ]")


            if (packetLength != data.size + 2) {
                logger.w("BYTE READER", "Data size mismatch. [e/a] ${packetLength - 2}/${data.size}")
            }
            if (givenCrc == actualCrc.toUByte()) {
                emit(reAssembledPacket)
            }
        }
    }

    //https://github.com/tedsalmon/DroidIBus/blob/master/app/src/main/java/com/ibus/droidibus/ibus/IBusMessageService.java#L156
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

    val serialPort : SerialPort by lazy { setupSerialPort() }

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