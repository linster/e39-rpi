package ca.stefanm.ibus.lib.hardwareDrivers

import ca.stefanm.ibus.lib.hardwareDrivers.ibus.TelephoneLedManager
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.toByte
import org.junit.Assert
import org.junit.Test

@ExperimentalStdlibApi
internal class TelephoneLedManagerTest {

    @Test
    fun `red on`() {
        Assert.assertEquals("1",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.RED, TelephoneLedManager.LedState.ON))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `red blink`() {
        Assert.assertEquals("10",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.RED, TelephoneLedManager.LedState.BLINK))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `orange on`() {
        Assert.assertEquals("100",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.ORANGE, TelephoneLedManager.LedState.ON))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `orange blink`() {
        Assert.assertEquals("1000",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.ORANGE, TelephoneLedManager.LedState.BLINK))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `green on`() {
        Assert.assertEquals("10000",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.GREEN, TelephoneLedManager.LedState.ON))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `green blink`() {
        Assert.assertEquals("100000",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.GREEN, TelephoneLedManager.LedState.BLINK))
                .toByte()
                .toString(2)
        )
    }

    @Test
    fun `all blink`() {
        Assert.assertEquals("101010",
            arrayOf(
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.RED, TelephoneLedManager.LedState.BLINK),
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.ORANGE, TelephoneLedManager.LedState.BLINK),
                TelephoneLedManager.LedStatus(
                    TelephoneLedManager.Led.GREEN, TelephoneLedManager.LedState.BLINK)
            )
                .toByte()
                .toString(2)
        )
    }
}