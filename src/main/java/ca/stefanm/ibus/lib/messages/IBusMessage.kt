package ca.stefanm.ibus.lib.messages

import ca.stefanm.ibus.lib.bordmonitor.input.IBusDevice
import okio.Buffer

open class IBusMessage(
    val sourceDevice: IBusDevice,
    val destinationDevice: IBusDevice,
    val data: ByteArray
) {

    companion object {
        fun ByteArray.toIbusMessage() : IBusMessage? {
            val buffer = Buffer().write(this)

            val sourceDeviceRaw = buffer.readByte()
            val packetLength = buffer.readByte()
            val destDeviceRaw = buffer.readByte()
            // subtract 2 because length includes checksum and dest address
            val data = buffer.readByteArray(packetLength.toLong() - 2)

            var checksum = 0x00
            forEach { byte -> checksum = checksum xor byte.toInt() }
            //https://github.com/tedsalmon/DroidIBus/blob/master/app/src/main/java/com/ibus/droidibus/ibus/IBusMessageService.java#L242
            if (checksum.toByte() != 0x00.toByte()) {
                return null
            }

            return IBusMessage(
                sourceDevice = IBusDevice.values().first { it.deviceId == sourceDeviceRaw.toInt() },
                destinationDevice = IBusDevice.values().first { it.deviceId == destDeviceRaw.toInt() },
                data = data
            )
        }
    }

    private fun Buffer.writeBytes(vararg bytes : Byte) : Buffer {
        for (b in bytes) { this.writeByte(b.toInt()) }
        return this
    }

    fun toWireBytes() : ByteArray {
        return with(Buffer()) {
            writeByte(sourceDevice.deviceId)
            //Length goes here
            //Length is everything past source + length field.
            val length = 1 /* Dest is 1 byte */ + data.size + 1 /* Checksum */
            writeByte(length)

            writeByte(destinationDevice.deviceId)
            writeBytes(*data)
            //Xor checksum byte goes here.
            var checksum = 0x00
            this.clone().readByteArray().forEach { byte -> checksum = checksum xor byte.toInt() }
            writeByte(checksum)
        }.readByteArray()
    }

}
