package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalUnsignedTypes::class)
class IBusPacketizerTest {

    /** A packet: source, length, destination, data, XOR of all of those. */
    private fun packet(src: Int, dst: Int, vararg data: Int): ByteArray {
        val body = intArrayOf(src, data.size + 2, dst, *data)
        val xor = body.fold(0) { acc, b -> acc xor b }
        return (body + xor).map { it.toByte() }.toByteArray()
    }

    private fun List<UByteArray>.hex() = map { p -> p.joinToString(" ") { it.toString(16).padStart(2, '0') } }

    // The BMBT knob turned right one click: F0 04 3B 49 81 xx.
    private val knob = packet(0xF0, 0x3B, 0x49, 0x81)
    // A Pi -> Pico ConfigStatusRequest (protobuf 08 03).
    private val configRequest = packet(0x81, 0x31, 0x08, 0x03)

    @Test
    fun wholePackets() {
        val p = IBusPacketizer()
        val out = p.add(knob + configRequest)
        assertEquals(listOf(knob, configRequest).map { it.toUByteArray() }.hex(), out.hex())
        assertEquals(0L, p.pending)
        assertEquals(0L, p.droppedBytes)
    }

    @Test
    fun aPacketSplitAcrossReads() {
        val p = IBusPacketizer()
        assertEquals(0, p.add(knob.copyOfRange(0, 3)).size)
        assertEquals(3L, p.pending)
        assertEquals(listOf(knob.toUByteArray()).hex(), p.add(knob.copyOfRange(3, knob.size)).hex())
    }

    @Test
    fun zeroBytesAreNotMessages() {
        // The Pico's 9600 8E1 link read at 115200 8N1 on the first Pi image: only 0x00.
        val p = IBusPacketizer()
        assertEquals(0, p.add(ByteArray(2000)).size)
        assertEquals(1999L, p.droppedBytes)        // one byte waits (a length needs two)
    }

    @Test
    fun resyncsAfterNoiseAndBadChecksums() {
        val corrupt = knob.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 0x55).toByte() }
        val p = IBusPacketizer()
        // Noise, a corrupted packet, then real traffic: every real packet comes out, in order (a byte
        // inside the corrupted one can read as a length and hold them until it can be checked).
        val traffic = configRequest + byteArrayOf(0x00) + (1..30).flatMap { knob.toList() }.toByteArray()
        val out = p.add(byteArrayOf(0, 0, 0x01, 0x01) + corrupt) + p.add(traffic)
        assertEquals((listOf(configRequest) + List(30) { knob }).map { it.toUByteArray() }.hex(), out.hex())
        assertEquals(0L, p.pending)
    }

    @Test
    fun aFalseLongLengthWaitsUntilItCanBeChecked() {
        // Noise that reads as a plausible length (0x7F = 127) holds the packets after it until 129
        // bytes are there to check its checksum (~130 ms at 9600 baud); then they're found.
        val p = IBusPacketizer()
        assertEquals(0, p.add(byteArrayOf(0x00, 0x7F) + knob).size)
        val filler = (1..30).flatMap { configRequest.toList() }.toByteArray()   // real traffic keeps coming
        val out = p.add(filler)
        assertEquals(knob.toUByteArray().toList(), out.first().toList())
        assertEquals(31, out.size)
    }

    @Test
    fun tooLongIsNoise() {
        val p = IBusPacketizer()
        // A "length" of 0xFF can't be a packet: skipped, the real one after it found.
        assertEquals(listOf(knob.toUByteArray()).hex(), p.add(byteArrayOf(0x50, 0xFF.toByte()) + knob).hex())
    }
}
