package ca.stefanm.ibus.lib.messages

import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage.Companion.toIbusMessage
import okio.Buffer
import org.junit.Assert
import org.junit.Test

internal class IBusMessageTest {


    @Test
    fun `index refresh message is correct`() {
        //This should equal <68 06 3B> A5 60 01 00 91

        val syntheticMessage = IBusMessage(
            sourceDevice = IBusDevice.RADIO,
            destinationDevice = IBusDevice.NAV_VIDEOMODULE,
            data = with(Buffer()) {
                writeByte(0xA5)
                writeByte(0x60)
                writeByte(0x01)
                writeByte(0x00)
            }.readByteArray().toUByteArray()
        ).toWireBytes()

        val expectedMessage = with(Buffer()) {
            writeByte(0x68)
            writeByte(0x06)
            writeByte(0x3B)
            writeByte(0xA5)
            writeByte(0x60)
            writeByte(0x01)
            writeByte(0x00)
            writeByte(0x91)
        }.readByteArray()

        Assert.assertArrayEquals(expectedMessage, syntheticMessage)
    }

    @Test
    fun `index refresh message should decode`() {
        val givenMessage = with(Buffer()) {
            writeByte(0x68)
            writeByte(0x06)
            writeByte(0x3B)
            writeByte(0xA5)
            writeByte(0x60)
            writeByte(0x01)
            writeByte(0x00)
            writeByte(0x91)
        }.readByteArray().toUByteArray().toIbusMessage()

        val expectedMessage = IBusMessage(
            sourceDevice = IBusDevice.RADIO,
            destinationDevice = IBusDevice.NAV_VIDEOMODULE,
            data = with(Buffer()) {
                writeByte(0xA5)
                writeByte(0x60)
                writeByte(0x01)
                writeByte(0x00)
            }.readByteArray().toUByteArray()
        )

        Assert.assertEquals(expectedMessage.sourceDevice, givenMessage!!.sourceDevice)
        Assert.assertEquals(expectedMessage.destinationDevice, givenMessage.destinationDevice)
        Assert.assertArrayEquals(expectedMessage.data.toByteArray(), givenMessage.data.toByteArray())
    }
}