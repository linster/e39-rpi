package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import ca.stefanm.ibus.car.di.ConfiguredCarScope
import javax.inject.Inject
import kotlin.random.Random

/**
 * What the serial reader has seen on the line: when the last byte arrived, and (while a transmit is
 * being checked) the bytes that came back. Written by the reader thread, read by the writer.
 */
@ConfiguredCarScope
class IBusLineMonitor internal constructor(private val nanoTime: () -> Long) {

    @Inject constructor() : this(System::nanoTime)

    /** Starts a second in the past (System.nanoTime() can be negative, so not 0): the line starts idle. */
    @Volatile
    var lastByteReceivedNanos: Long = nanoTime() - 1_000_000_000L
        private set

    private val echoLock = Any()
    private var capturing = false
    private val echo = ArrayList<Byte>()

    /** Call from the reader for every chunk read from the port. */
    fun onBytesReceived(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        lastByteReceivedNanos = nanoTime()
        synchronized(echoLock) {
            if (capturing) bytes.forEach { echo.add(it) }
        }
    }

    fun startEchoCapture() = synchronized(echoLock) {
        echo.clear()
        capturing = true
    }

    fun echoSoFar(): ByteArray = synchronized(echoLock) { echo.toByteArray() }

    fun stopEchoCapture() = synchronized(echoLock) {
        capturing = false
        echo.clear()
    }
}

/**
 * Sends IBus packets with I/K-bus etiquette (linster/e39-rpi#53).
 *
 * On the car bus directly ([directOnBus]: setting IBusConnection = DIRECT_BUS, a USB IBus interface) the Pi is one of several
 * talkers on a single wire. Like the modules, it only starts after the bus has been idle for
 * [BUS_IDLE_NANOS], keeps [MIN_GAP_NANOS] between its own packets, and checks the echo (the
 * transceiver feeds the bus back to RX). On a mismatch (a collision) it backs off for a random 10-20 ms
 * and resends, up to [MAX_ATTEMPTS].
 *
 * Through the Pico (IBusConnection = PICO, the default) the link is point to point: no other talker and no echo, and
 * the Pico does the bus etiquette when it forwards to the car. Packets are only paced by
 * [MIN_GAP_NANOS], because the Pico can't send to the car faster than the bus allows and its queue
 * (32 packets) drops new packets when full.
 *
 * The same timings as the Pico's own transmitter (e39-rpi-fw#11), which on the bench took a busy bus
 * from 19% to 100% of packets arriving intact.
 */
class IBusTransmitter(
    private val directOnBus: Boolean,
    private val monitor: IBusLineMonitor,
    /** Bytes waiting in the port's receive buffer (not yet read by the reader). */
    private val bytesAvailable: () -> Int,
    private val write: suspend (ByteArray) -> Unit,
    private val nanoTime: () -> Long = System::nanoTime,
    private val sleepMillis: suspend (Long) -> Unit = { kotlinx.coroutines.delay(it) },
    private val random: Random = Random.Default,
) {
    companion object {
        const val BUS_IDLE_NANOS = 1_500_000L          // 1.5 ms of silence before transmitting
        const val MIN_GAP_NANOS = 10_000_000L          // between our own packets
        const val MAX_IDLE_WAIT_NANOS = 250_000_000L   // give up on a bus that never goes quiet
        const val MAX_ATTEMPTS = 4
        const val BACKOFF_MIN_MS = 10L
        const val BACKOFF_RANDOM_MS = 10              // + 0..10 ms, so two talkers don't retry in step
        /** 9600 8E1 = 11 bits a byte = 1.15 ms; plus USB adapter latency (FTDI's default timer is 16 ms). */
        private const val ECHO_NANOS_PER_BYTE = 1_200_000L
        private const val ECHO_SLACK_NANOS = 50_000_000L
    }

    enum class Outcome { SENT, DROPPED_BUS_BUSY, DROPPED_COLLISIONS }

    @Volatile var sent = 0L; private set
    @Volatile var collisions = 0L; private set
    @Volatile var busyDeferrals = 0L; private set
    @Volatile var droppedBusBusy = 0L; private set
    @Volatile var droppedAfterRetries = 0L; private set

    private var lastSendEndNanos: Long? = null

    suspend fun send(packet: ByteArray): Outcome {
        if (!directOnBus) {
            waitForGapAfterLastSend()
            write(packet)
            lastSendEndNanos = nanoTime()
            sent++
            return Outcome.SENT
        }

        repeat(MAX_ATTEMPTS) { attempt ->
            waitForGapAfterLastSend()
            if (!waitForIdleBus()) {
                droppedBusBusy++
                return Outcome.DROPPED_BUS_BUSY
            }
            monitor.startEchoCapture()
            try {
                write(packet)
                val echoed = awaitEcho(packet.size)
                lastSendEndNanos = nanoTime()
                if (echoed.size >= packet.size && echoed.copyOf(packet.size).contentEquals(packet)) {
                    sent++
                    return Outcome.SENT
                }
            } finally {
                monitor.stopEchoCapture()
            }
            collisions++
            if (attempt < MAX_ATTEMPTS - 1) {
                sleepMillis(BACKOFF_MIN_MS + random.nextInt(BACKOFF_RANDOM_MS + 1))
            }
        }
        droppedAfterRetries++
        return Outcome.DROPPED_COLLISIONS
    }

    private suspend fun waitForGapAfterLastSend() {
        val last = lastSendEndNanos ?: return
        val remaining = MIN_GAP_NANOS - (nanoTime() - last)
        if (remaining > 0) sleepMillis((remaining + 999_999) / 1_000_000)
    }

    /** True once the line has been quiet for [BUS_IDLE_NANOS]; false if it never was within [MAX_IDLE_WAIT_NANOS]. */
    private suspend fun waitForIdleBus(): Boolean {
        val start = nanoTime()
        var deferred = false
        while (true) {
            val now = nanoTime()
            if (bytesAvailable() <= 0 && now - monitor.lastByteReceivedNanos >= BUS_IDLE_NANOS) return true
            if (now - start >= MAX_IDLE_WAIT_NANOS) return false
            if (!deferred) { busyDeferrals++; deferred = true }
            sleepMillis(1)
        }
    }

    private suspend fun awaitEcho(size: Int): ByteArray {
        val deadline = nanoTime() + size * ECHO_NANOS_PER_BYTE + ECHO_SLACK_NANOS
        while (true) {
            val echoed = monitor.echoSoFar()
            if (echoed.size >= size || nanoTime() >= deadline) return echoed
            sleepMillis(1)
        }
    }
}
