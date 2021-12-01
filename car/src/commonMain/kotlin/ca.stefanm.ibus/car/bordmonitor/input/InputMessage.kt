package ca.stefanm.ibus.car.bordmonitor.input

import ca.stefanm.ibus.car.conduit.IBusDevice


sealed class KeyInputMessage(
    val sourceDevice: IBusDevice = IBusDevice.BOARDMONITOR_BUTTONS,
    val destinationDevice: IBusDevice = IBusDevice.RADIO,
    val pushData : List<Short>,
    val longPushData : List<Short>,
    val releaseData : List<Short>
){

    object SteeringWheelPreviousTrack : KeyInputMessage(
        sourceDevice = IBusDevice.MFL,
        destinationDevice = IBusDevice.RADIO,
        pushData = listOf(0x3B, 0x08), longPushData = listOf(0x3B, 0x08), releaseData = listOf(0x3B, 0x28)
    )

    object SteeringWheelNextTrack : KeyInputMessage(
        sourceDevice = IBusDevice.MFL,
        destinationDevice = IBusDevice.RADIO,
        pushData = listOf(0x3B, 0x01), longPushData = listOf(0x3B, 0x01), releaseData = listOf(0x3B, 0x21)
    )

    object RadioPreviousTrack : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        pushData = listOf(0x48, 0x10), longPushData = listOf(0x48, 0x50), releaseData = listOf(0x48, 0x90)
    )

    object RadioNextTrack : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        pushData = listOf(0x48, 0x00), longPushData = listOf(0x48, 0x40), releaseData = listOf(0x48, 0x80)
    )

    /* The thing with the tape icon? */
    object RadioMenu : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        pushData = listOf(0x48, 0x30), longPushData = listOf(0x48, 0x70), releaseData = listOf(0x48, 0xB0)
    )

    /* The thing with the tape icon?? */
    object RadioTP : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        pushData = listOf(0x48, 0x32), longPushData = listOf(0x48, 0x72), releaseData = listOf(0x48, 0xB2)
    )

    object BMBTPhoneButton : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        destinationDevice = IBusDevice.BROADCAST,
        pushData = listOf(0x48, 0x08), longPushData = listOf(0x48, 0x48), releaseData = listOf(0x48, 0x88)
    )

    /* Menu key */
    object BMBTMenuButton : KeyInputMessage(
        sourceDevice = IBusDevice.BOARDMONITOR_BUTTONS,
        destinationDevice = IBusDevice.BROADCAST,
        pushData = listOf(0x48, 0x34), longPushData = listOf(0x48, 0x74), releaseData = listOf(0x48, 0xB4)
    )
}

sealed class KnobInputMessage(
    val sourceDevice: IBusDevice,
    val destinationDevice: IBusDevice,
    val pushData : List<Short>,
    val longPushData : List<Short>,
    val releaseData : List<Short>
) {

    val turnDirection : Direction?
        get() = null

    enum class Direction { LEFT, RIGHT }

}