package ca.stefanm.ibus.lib.bordmonitor.menu.painter

import ca.stefanm.ibus.lib.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import okio.Buffer

//TODO move all the paint messages into here out of the screen painter.
//TODO make them all subclass IbusMessage.

interface LengthConstraintValidator {
    fun isLabelLengthValid() : Boolean
}

class Title0Message(
    val label : String,
    val attribute : ScreenPainter.TitleTextAttribute = ScreenPainter.TitleTextAttribute.NORMAL,
    val lengthConstraints: TextLengthConstraints
) : IBusMessage(
    sourceDevice = IBusDevice.RADIO,
    destinationDevice = IBusDevice.NAV_VIDEOMODULE,
    data = with(Buffer()) {
        writeByte(0x23)
        writeByte(0x62)
        writeByte(0x30)
        appendString(label)
    }.readByteArray().toUByteArray()
), LengthConstraintValidator {
    override fun isLabelLengthValid()
            = label.length <= lengthConstraints.AREA_0
}

class TitleNMessage(
    private val label: String,
    private val n : Int,
    private val lengthConstraints: TextLengthConstraints
) : IBusMessage(
    sourceDevice = IBusDevice.RADIO,
    destinationDevice = IBusDevice.NAV_VIDEOMODULE,
    data = with(Buffer()) {
        writeByte(0xA5)
        writeByte(0x62)
        writeByte(0x01)
        writeByte(when (n){
            1 -> 0x01
            2 -> 0x02
            3 -> 0x03
            4 -> 0x04
            5 -> 0x05
            6 -> 0x06
            7 -> 0x07
            else -> throw IllegalArgumentException("Invalid title")
        })
        appendString(label)
    }.readByteArray().toUByteArray()
), LengthConstraintValidator {
    override fun isLabelLengthValid()
            = label.length <= lengthConstraints.getAllowedLength(n)
}

class IndexMessage(
    private val label: String,
    private val n : Int,
    private val lengthConstraints: TextLengthConstraints
) : IBusMessage(
    sourceDevice = IBusDevice.RADIO,
    destinationDevice = IBusDevice.NAV_VIDEOMODULE,
    data = with(Buffer()) {
        writeByte(0x21)
        writeByte(0x60)
        writeByte(0x00)
        writeByte(mapOf( //FieldIndex
            0 to 0x40, 5 to 0x45,
            1 to 0x41, 6 to 0x46,
            2 to 0x42, 7 to 0x47 /* possibly 0x07?? */,
            /* Below are shown only in full screen moce */
            3 to 0x43, 8 to 0x48,
            4 to 0x44, 9 to 0x49
        )[n] ?: throw IllegalArgumentException("Invalid n selected"))
        appendString(label)
    }.readByteArray().toUByteArray()
), LengthConstraintValidator {
    override fun isLabelLengthValid()
            = label.length <= lengthConstraints.INDEX_0_9
}

object IndexRefreshMessage : IBusMessage(
    sourceDevice = IBusDevice.RADIO,
    destinationDevice = IBusDevice.NAV_VIDEOMODULE,
    data = with(Buffer()) {
        writeByte(0xA5)
        writeByte(0x60)
        writeByte(0x01)
        writeByte(0x00)
        writeByte(0x09)
    }.readByteArray().toUByteArray()
)