package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.messages.IBusMessage.Companion.toIbusMessage
import ca.stefanm.ibus.lib.messages.toDeviceIdString
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.ForegroundPlatform
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okio.Buffer
import java.util.concurrent.Executors
import javax.inject.Inject

interface SerialPortReader {
    fun readMessages() : Flow<IBusMessage>
}

interface SerialPortWriter {
    suspend fun writeRawBytes(bytes : ByteArray)
}

@ExperimentalCoroutinesApi
@ConfiguredCarScope
class JSerialCommsAdapter @Inject constructor(
    private val deviceConfiguration: CarPlatformConfiguration,
    private val blockingWriter : BlockingJSerialCommsWriter,
    private val nonBlockingWriter : NonBlockingJSerialCommsWriter,
    private val blockingReader : BlockingJSerialCommsReader
) : SerialPortWriter, SerialPortReader {

    override suspend fun writeRawBytes(bytes: ByteArray) {
        when (deviceConfiguration.serialPortWriteMode) {
            CarPlatformConfiguration.SerialPortWriteMode.BLOCKING -> blockingWriter
            CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING -> nonBlockingWriter
        }.writeRawBytes(bytes)
    }

    override fun readMessages(): Flow<IBusMessage> {
        return when (deviceConfiguration.serialPortReadMode) {
            CarPlatformConfiguration.SerialPortReadMode.BLOCKING -> blockingReader.readMessages()
            else -> error("Not supported")
        }
    }
}

@ExperimentalCoroutinesApi
@ConfiguredCarScope
class BlockingJSerialCommsReader @Inject constructor(
    private val logger: Logger,
    serialPortProvider: JSerialCommsSerialPortProvider,
    private val coroutineScope: CoroutineScope,
    private val flowDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val readerDispatcher : CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
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
            .onCompletion { readerJob?.cancel(ForegroundPlatform.PlatformShutdownCancellationException()) }
    }

    private val rawSerialPackets = Channel<UByteArray>()

    private val rawBuffer = Buffer()

    private var readerJob : Job? = null

    private suspend fun setupJSerialComm() {
        logger.v(TAG, "Setting up jSerialComm read coroutine.")

        readerJob = coroutineScope.launch(readerDispatcher) {
            while (true) {
                val bytesAvailable = port.bytesAvailable()
                if (bytesAvailable == 0) { yield() }
                if (bytesAvailable == -1) { logger.w(TAG, "Port not open") ; break }

                val readBytes = ByteArray(bytesAvailable)
//                port.readBytes(readBytes, bytesAvailable.toLong())
                port.readBytes(readBytes, bytesAvailable)

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
                try {
                    // subtract 2 because length includes checksum and dest address
                    buffer.readByteArray(packetLength.toLong() - 2).toUByteArray()
                } catch (e : Exception) {
                    logger.e("BYTE READER", "Malformed packet", e)
                    logger.e(
                        "BYTE READER",
                        "Data from malformed packet read so far: " +
                                "[src = ${sourceDevice.toDeviceIdString()}] " +
                                "[src_16 = ${sourceDevice.toString(16)}] " +
                                "[len_10 = ${packetLength.toString(10)}] " +
                                "[len_16 = ${packetLength.toString(16)}] " +
                                "[dest = ${destDevice.toDeviceIdString()}] "
                    )
                    ubyteArrayOf()
                    return@flow
                }
            }
            val givenCrc = buffer.readByte().toUByte()

            var actualCrc = 0x00
            ubyteArrayOf(sourceDevice, packetLength.toUByte(), destDevice, *data).forEach { byte -> actualCrc = actualCrc xor byte.toInt() }

            val reAssembledPacket = ubyteArrayOf(sourceDevice, packetLength.toUByte(), destDevice, *data, givenCrc)

            if (false) {
                logger.v(
                    "BYTE READER",
                    "Read raw packet : " +
                            "[src = ${sourceDevice.toDeviceIdString()}] " +
                            "[src_16 = ${sourceDevice.toString(16)}] " +
                            "[len_10 = ${packetLength.toString(10)}] " +
                            "[len_16 = ${packetLength.toString(16)}] " +
                            "[dest = ${destDevice.toDeviceIdString()}] " +
                            "<${data.size} bytes data> " +
                            "[data_10 = ${data}] " +
                            "[CRC_10 g/a : $givenCrc / $actualCrc ]" +
                            "[CRC_16 g/a : ${givenCrc.toString(16)} / ${actualCrc.toString(radix = 16)} ]"
                )
                logger.v(
                    "BYTE READER",
                    "Total Packet: " +
                    "reassembled_packet : ${reAssembledPacket}" +
                    "reassembled_packet_size : ${reAssembledPacket.size}"
                )
            }

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

@ConfiguredCarScope
class JSerialCommsSerialPortProvider @Inject constructor(
    private val logger: Logger,
    private val deviceConfiguration: CarPlatformConfiguration
) {

    companion object {
        const val TAG = "SerialPortProvider"
        const val SEND_TIMEOUT_MS = 100
        const val READ_TIMEOUT_NO_DATA_MS = 100 //maybe 75?
    }

    val serialPort : SerialPort = setupSerialPort()

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

        when (deviceConfiguration.serialPortBaudRate) {
            CarPlatformConfiguration.PicoSerialBaudRate.SERIAL_115200_8N1 -> {
                port.setComPortParameters(
                    115200, 8, 1, SerialPort.NO_PARITY
                )
            }
            CarPlatformConfiguration.PicoSerialBaudRate.SERIAL_9600_8E1 -> {
                port.setComPortParameters(
                    9600, 8, 1, SerialPort.EVEN_PARITY
                )
            }
        }

        if (deviceConfiguration.serialPortReadMode == CarPlatformConfiguration.SerialPortReadMode.BLOCKING
            && deviceConfiguration.serialPortWriteMode == CarPlatformConfiguration.SerialPortWriteMode.BLOCKING) {
            //We know this works experimentally.
            port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING or SerialPort.TIMEOUT_WRITE_BLOCKING,
                READ_TIMEOUT_NO_DATA_MS,
                SEND_TIMEOUT_MS
            )
        } else {
            var timeoutMode = 0

            timeoutMode = timeoutMode or when (deviceConfiguration.serialPortReadMode) {
                CarPlatformConfiguration.SerialPortReadMode.NON_BLOCKING -> SerialPort.TIMEOUT_NONBLOCKING
                CarPlatformConfiguration.SerialPortReadMode.BLOCKING -> SerialPort.TIMEOUT_READ_BLOCKING
            }

            timeoutMode = timeoutMode or when (deviceConfiguration.serialPortWriteMode) {
                CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING -> SerialPort.TIMEOUT_NONBLOCKING
                CarPlatformConfiguration.SerialPortWriteMode.BLOCKING -> SerialPort.TIMEOUT_WRITE_BLOCKING
            }

            port.setComPortTimeouts(timeoutMode, READ_TIMEOUT_NO_DATA_MS, SEND_TIMEOUT_MS)
        }

        if (!port.isOpen) {
            port.openPort()
        }
        return port
    }
}

@ConfiguredCarScope
class NonBlockingJSerialCommsWriter @Inject constructor(
    private val logger: Logger,
    serialPortProvider: JSerialCommsSerialPortProvider
) : SerialPortWriter {

    private val port = serialPortProvider.serialPort

    override suspend fun writeRawBytes(bytes: ByteArray) {
        var bytesWritten = 0
        while (bytesWritten < bytes.size) {
            bytesWritten += port.writeBytes(bytes, 1, bytesWritten)
            yield()
        }
    }
}

@ConfiguredCarScope
class BlockingJSerialCommsWriter @Inject constructor(
    private val logger: Logger,
    serialPortProvider: JSerialCommsSerialPortProvider,
    private val coroutineScope: CoroutineScope
) : SerialPortWriter {

    private val serialOutCoroutineDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    private val port by lazy { serialPortProvider.serialPort }

    override suspend fun writeRawBytes(bytes: ByteArray) {
        withContext(coroutineScope.coroutineContext + serialOutCoroutineDispatcher) {
            //Wait 2ms to ensure previous message send went out. Realistically we only need to wait 1.2ms.
//            delay(200)
            delay((bytes.size * 8).toLong()) //Experimentally found to give the BMBT enough time to respond to long packets.

            port.writeBytes(bytes, bytes.size)

            logger.d("TAG", "Awaiting write: ${port.bytesAwaitingWrite()}")
        }
    }
}