package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.messages.IBusMessage.Companion.toIbusMessage
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.ForegroundPlatform
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
    private val logger: Logger,
    private val deviceConfiguration: CarPlatformConfiguration,
    private val blockingWriter : BlockingJSerialCommsWriter,
    private val nonBlockingWriter : NonBlockingJSerialCommsWriter,
    private val blockingReader : BlockingJSerialCommsReader,
    private val serialPortProvider: JSerialCommsSerialPortProvider,
    private val lineMonitor: IBusLineMonitor,
) : SerialPortWriter, SerialPortReader {

    private val rawWriter : SerialPortWriter
        get() = when (deviceConfiguration.serialPortWriteMode) {
            CarPlatformConfiguration.SerialPortWriteMode.BLOCKING -> blockingWriter
            CarPlatformConfiguration.SerialPortWriteMode.NON_BLOCKING -> nonBlockingWriter
        }

    /** On the car bus itself (a USB IBus interface), or on the Pico's point-to-point link. This can't
     *  be told from the baud rate: the Pico's CAR_WITH_PI firmware also runs its Pi link at 9600 8E1. */
    private val transmitter = IBusTransmitter(
        directOnBus = deviceConfiguration.iBusConnection == CarPlatformConfiguration.IBusConnection.DIRECT_BUS,
        monitor = lineMonitor,
        bytesAvailable = { serialPortProvider.serialPort.bytesAvailable() },
        write = { rawWriter.writeRawBytes(it) },
    )

    // Called by SerialPublisherService only, one packet at a time.
    override suspend fun writeRawBytes(bytes: ByteArray) {
        if (deviceConfiguration.serialPortWriteMode == CarPlatformConfiguration.SerialPortWriteMode.BLOCKING) {
            //Experimentally found to give the BMBT enough time to respond to long packets. (Was in
            //BlockingJSerialCommsWriter; it has to come before the transmitter's wait for an idle bus.)
            delay((bytes.size * 8).toLong())
        }
        when (transmitter.send(bytes)) {
            IBusTransmitter.Outcome.SENT -> Unit
            IBusTransmitter.Outcome.DROPPED_BUS_BUSY -> logger.w(TAG,
                "Dropped packet: the bus never went idle (${transmitter.droppedBusBusy} so far)")
            IBusTransmitter.Outcome.DROPPED_COLLISIONS -> logger.w(TAG,
                "Dropped packet after ${IBusTransmitter.MAX_ATTEMPTS} collisions " +
                        "(${transmitter.droppedAfterRetries} dropped, ${transmitter.collisions} collisions so far)")
        }
    }

    private companion object {
        const val TAG = "JSerialCommsAdapter"
    }

    override fun readMessages(): Flow<IBusMessage> {
        return when (deviceConfiguration.serialPortReadMode) {
            CarPlatformConfiguration.SerialPortReadMode.BLOCKING -> blockingReader.readMessages()
            else -> error("Not supported : ${deviceConfiguration.serialPortReadMode.name}")
        }
    }
}

@ExperimentalCoroutinesApi
@ConfiguredCarScope
class BlockingJSerialCommsReader @Inject constructor(
    private val logger: Logger,
    private val serialPortProvider: JSerialCommsSerialPortProvider,
    private val lineMonitor: IBusLineMonitor,
    private val coroutineScope: CoroutineScope,
) : SerialPortReader{

    private companion object {
        const val TAG = "SerialReader"
        /** Nothing to read: wait this long before asking again. At 9600 baud that's under 2 bytes. */
        const val IDLE_POLL_MS = 2L
        const val READ_CHUNK = 512
    }

    // Its own thread, made here rather than injected: Dagger ignores Kotlin
    // default arguments, so a `readerDispatcher: CoroutineDispatcher = ...`
    // parameter got the app's Dispatchers.IO, and this loop's yield() then
    // spun across the shared pool (~3 cores and constant allocation, idle).
    private val readerDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "ibus-serial-reader").apply { isDaemon = true }
    }.asCoroutineDispatcher()

    // The provider's current port: it's replaced when reopened.
    private val port get() = serialPortProvider.serialPort

    override fun readMessages(): Flow<IBusMessage> {
        return rawSerialPackets.consumeAsFlow()
            .flowOn(Dispatchers.IO)
            .mapNotNull { it.toIbusMessage() }
            .onStart { setupJSerialComm() }
            .onCompletion {
                readerJob?.cancel(ForegroundPlatform.PlatformShutdownCancellationException())
                port.closePort()
            }
    }

    private val rawSerialPackets = Channel<UByteArray>()

    private val packetizer = IBusPacketizer()

    private var readerJob : Job? = null

    private fun setupJSerialComm() {
        logger.v(TAG, "Setting up the serial read loop.")

        readerJob = coroutineScope.launch(readerDispatcher) {
            val chunk = ByteArray(READ_CHUNK)
            var reportedDropped = 0L
            while (true) {
                val available = port.bytesAvailable()
                if (available == -1) {
                    // Not open (yet, or any more: unplugged, a USB glitch,
                    // another process had it): a new port object, opened.
                    logger.w(TAG, "Port not open")
                    serialPortProvider.reopen().let { logger.d(TAG, "Port opening was successful: ${it.isOpen}") }
                    delay(500)
                    continue
                }
                if (available == 0) {
                    delay(IDLE_POLL_MS)
                    continue
                }

                val read = port.readBytes(chunk, minOf(available, chunk.size))
                if (read <= 0) continue
                lineMonitor.onBytesReceived(chunk.copyOf(read)) //bus activity + echo check for IBusTransmitter

                for (packet in packetizer.add(chunk, read)) {
                    rawSerialPackets.send(packet)
                }
                if (packetizer.droppedBytes - reportedDropped >= 1000) {
                    // E.g. the wrong SerialPortBaudRate for the Pico's bus topology.
                    logger.w(TAG, "Dropped ${packetizer.droppedBytes} bytes that weren't IBus packets so far " +
                            "(line noise, or the wrong SerialPortBaudRate for the link?)")
                    reportedDropped = packetizer.droppedBytes
                }
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

    /**
     * The port in use. Ask for it on every use rather than keeping it:
     * [reopen] replaces it.
     */
    @Volatile
    var serialPort : SerialPort = setupSerialPort()
        private set

    /**
     * Closes the port in use and opens a new one (a jSerialComm SerialPort
     * that lost its device, e.g. when the USB adapter was unplugged or
     * another process held it, doesn't reopen). Returns it; check isOpen.
     */
    @Synchronized
    fun reopen() : SerialPort {
        serialPort.closePort()
        serialPort = setupSerialPort()
        return serialPort
    }

    /** A new port for the configured device, set up and opened (if it can be). */
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
    private val serialPortProvider: JSerialCommsSerialPortProvider
) : SerialPortWriter {

    private val port get() = serialPortProvider.serialPort

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
    private val serialPortProvider: JSerialCommsSerialPortProvider,
    private val coroutineScope: CoroutineScope
) : SerialPortWriter {

    private val serialOutCoroutineDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    private val port get() = serialPortProvider.serialPort

    override suspend fun writeRawBytes(bytes: ByteArray) {
        withContext(coroutineScope.coroutineContext + serialOutCoroutineDispatcher) {
            //(The bytes.size * 8 ms delay that was here is now in JSerialCommsAdapter, before the
            //IBusTransmitter's wait for an idle bus.)
            port.writeBytes(bytes, bytes.size)

            logger.d("TAG", "Awaiting write: ${port.bytesAwaitingWrite()}")
        }
    }
}