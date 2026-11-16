package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** IBusTransmitter with a fake clock (sleeping advances it) and a fake port. */
class IBusTransmitterTest {

    private val packet = byteArrayOf(0x3B, 0x05, 0x68, 0x01, 0x02, 0x55)

    private class Rig(directOnBus: Boolean) {
        var nowNanos = 1_000_000_000_000L
        /** Another module is talking (a byte arrives every 1 ms) until this time. */
        var busyUntilNanos = Long.MIN_VALUE
        /** What the port echoes for write number n (0-based); default: exactly what was written. */
        var echoFor: (Int, ByteArray) -> ByteArray = { _, written -> written }
        val writes = mutableListOf<Pair<Long, ByteArray>>()

        val monitor = IBusLineMonitor { nowNanos }
        val transmitter = IBusTransmitter(
            directOnBus = directOnBus,
            monitor = monitor,
            bytesAvailable = { 0 },
            write = { bytes ->
                writes += nowNanos to bytes
                if (directOnBus) monitor.onBytesReceived(echoFor(writes.size - 1, bytes))
            },
            nanoTime = { nowNanos },
            sleepMillis = { ms ->
                repeat(ms.toInt()) {
                    nowNanos += 1_000_000
                    if (nowNanos < busyUntilNanos) monitor.onBytesReceived(byteArrayOf(0x55))
                }
            },
            random = Random(1),
        )
    }

    @Test
    fun `direct on the bus, clean echo - sent once`() = runBlocking {
        val rig = Rig(directOnBus = true)
        assertEquals(IBusTransmitter.Outcome.SENT, rig.transmitter.send(packet))
        assertEquals(1, rig.writes.size)
        assertEquals(0L, rig.transmitter.collisions)
    }

    @Test
    fun `waits for the bus to be idle for 1_5 ms before sending`() = runBlocking {
        val rig = Rig(directOnBus = true)
        val start = rig.nowNanos
        rig.busyUntilNanos = start + 5_000_000
        rig.monitor.onBytesReceived(byteArrayOf(0x55)) // traffic right now
        assertEquals(IBusTransmitter.Outcome.SENT, rig.transmitter.send(packet))
        val sentAt = rig.writes.single().first
        assertTrue("sent during traffic at ${(sentAt - start) / 1e6} ms", sentAt >= rig.busyUntilNanos)
        assertTrue(sentAt - rig.monitor.lastByteReceivedNanos >= 0) // the echo is the last byte now
        assertEquals(1L, rig.transmitter.busyDeferrals)
    }

    @Test
    fun `a collision is detected from the echo and the packet resent after a backoff`() = runBlocking {
        val rig = Rig(directOnBus = true)
        rig.echoFor = { n, written -> if (n == 0) written.copyOf().also { it[2] = 0x7F } else written }
        assertEquals(IBusTransmitter.Outcome.SENT, rig.transmitter.send(packet))
        assertEquals(2, rig.writes.size)
        assertEquals(1L, rig.transmitter.collisions)
        val gapMs = (rig.writes[1].first - rig.writes[0].first) / 1_000_000
        assertTrue("resent after only $gapMs ms", gapMs >= IBusTransmitter.BACKOFF_MIN_MS)
    }

    @Test
    fun `a missing echo counts as a collision`() = runBlocking {
        val rig = Rig(directOnBus = true)
        rig.echoFor = { n, written -> if (n == 0) byteArrayOf() else written }
        assertEquals(IBusTransmitter.Outcome.SENT, rig.transmitter.send(packet))
        assertEquals(1L, rig.transmitter.collisions)
    }

    @Test
    fun `gives up after MAX_ATTEMPTS collisions`() = runBlocking {
        val rig = Rig(directOnBus = true)
        rig.echoFor = { _, written -> written.copyOf().also { it[0] = 0 } }
        assertEquals(IBusTransmitter.Outcome.DROPPED_COLLISIONS, rig.transmitter.send(packet))
        assertEquals(IBusTransmitter.MAX_ATTEMPTS, rig.writes.size)
        assertEquals(1L, rig.transmitter.droppedAfterRetries)
    }

    @Test
    fun `gives up if the bus never goes idle`() = runBlocking {
        val rig = Rig(directOnBus = true)
        rig.busyUntilNanos = Long.MAX_VALUE
        rig.monitor.onBytesReceived(byteArrayOf(0x55))
        assertEquals(IBusTransmitter.Outcome.DROPPED_BUS_BUSY, rig.transmitter.send(packet))
        assertEquals(0, rig.writes.size)
    }

    @Test
    fun `keeps at least 10 ms between its own packets`() = runBlocking {
        for (direct in listOf(true, false)) {
            val rig = Rig(directOnBus = direct)
            repeat(3) { rig.transmitter.send(packet) }
            rig.writes.zipWithNext().forEach { (a, b) ->
                assertTrue("direct=$direct: ${(b.first - a.first) / 1e6} ms apart",
                    b.first - a.first >= IBusTransmitter.MIN_GAP_NANOS)
            }
        }
    }

    @Test
    fun `through the Pico - no echo needed, no idle wait`() = runBlocking {
        val rig = Rig(directOnBus = false)
        rig.busyUntilNanos = Long.MAX_VALUE // irrelevant on a point-to-point link
        rig.monitor.onBytesReceived(byteArrayOf(0x55))
        assertEquals(IBusTransmitter.Outcome.SENT, rig.transmitter.send(packet))
        assertEquals(1, rig.writes.size)
        assertEquals(0L, rig.transmitter.busyDeferrals)
    }
}
