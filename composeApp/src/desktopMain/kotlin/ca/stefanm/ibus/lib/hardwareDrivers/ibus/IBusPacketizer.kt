package ca.stefanm.ibus.lib.hardwareDrivers.ibus

import okio.Buffer

/**
 * Splits the bytes read from the IBus (or the Pico's link) into packets:
 * source, length, destination, data..., XOR checksum, where length counts
 * destination + data + checksum.
 *
 * Resyncs on anything that isn't a packet: a length below [MIN_LENGTH] or
 * above [MAX_LENGTH], or a bad checksum, drops one byte and tries again from
 * the next, so line noise (e.g. a link at the wrong baud rate, which reads as
 * zero bytes) never becomes messages and a real packet after it is still
 * found. Bytes of a packet still arriving stay in the buffer for the next call.
 */
class IBusPacketizer {

    companion object {
        /** Destination + checksum: a packet with no data. */
        const val MIN_LENGTH = 2
        /** The longest packet the HMI expects (IBus packets are well under this; Pico log lines too). */
        const val MAX_LENGTH = 128
    }

    private val buffer = Buffer()

    /** Bytes dropped while resyncing, since this packetizer was made. */
    var droppedBytes = 0L
        private set

    /** Bytes waiting for the rest of their packet. */
    val pending: Long get() = buffer.size

    /** Adds [count] bytes from [bytes] and returns the complete, valid packets now in the buffer. */
    fun add(bytes: ByteArray, count: Int = bytes.size): List<UByteArray> {
        if (count > 0) buffer.write(bytes, 0, count)
        return drain()
    }

    private fun drain(): List<UByteArray> {
        val packets = ArrayList<UByteArray>(2)
        while (buffer.size >= 2) {
            val length = buffer[1].toInt() and 0xFF
            if (length < MIN_LENGTH || length > MAX_LENGTH) {
                skip()
                continue
            }
            val total = length + 2L
            if (buffer.size < total) break          // the rest hasn't arrived yet
            var xor = 0
            for (i in 0 until total - 1) xor = xor xor (buffer[i].toInt() and 0xFF)
            if (xor != (buffer[total - 1].toInt() and 0xFF)) {
                skip()
                continue
            }
            packets.add(buffer.readByteArray(total).toUByteArray())
        }
        return packets
    }

    private fun skip() {
        buffer.skip(1)
        droppedBytes++
    }
}
