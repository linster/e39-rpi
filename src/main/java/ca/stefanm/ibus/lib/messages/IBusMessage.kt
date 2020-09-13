package ca.stefanm.ibus.lib.messages

import ca.stefanm.ibus.lib.bordmonitor.input.IBusDevice
import okio.Buffer


fun UByte.toDeviceIdString() : String = IBusDevice.values()
    .firstOrNull { it.deviceId == this.toInt() }?.name ?: this.toString(16).capitalize()

fun UByte.toDevice() : IBusDevice? =
    IBusDevice.values().firstOrNull { it.deviceId.toUByte() == this }

open class IBusMessage(
    val sourceDevice: IBusDevice,
    val destinationDevice: IBusDevice,
    val data: UByteArray
) {

    companion object {
        fun UByteArray.toIbusMessage() : IBusMessage? {
            val buffer = Buffer().write(this.toByteArray())

            val sourceDeviceRaw = buffer.readByte().toUByte()
            val packetLength = buffer.readByte().toUByte().toInt()
            val destDeviceRaw = buffer.readByte().toUByte()

            val data = if (packetLength <= 2) {
                ubyteArrayOf()
            } else {
                // subtract 2 because length includes checksum and dest address
                buffer.readByteArray(packetLength.toLong() - 2).toUByteArray()
            }

            if (sourceDeviceRaw.toDevice() == null || destDeviceRaw.toDevice() == null) {
                return null
            }

            return IBusMessage(
                sourceDevice = sourceDeviceRaw.toDevice()!!,
                destinationDevice = destDeviceRaw.toDevice()!!,
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
            writeBytes(*data.toByteArray())
            //Xor checksum byte goes here.
            var checksum = 0x00
            this.clone().readByteArray().forEach { byte -> checksum = checksum xor byte.toInt() }
            writeByte(checksum)
        }.readByteArray()
    }

}
