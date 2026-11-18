package ca.stefanm.ibus.configuration

import ca.stefanm.ibus.configuration.CarPlatformConfiguration.IBusConnection
import ca.stefanm.ibus.configuration.CarPlatformConfiguration.MachineType
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

internal class MachineTypeConfigTest {

    private fun config(hocon: String): Config =
        Config { addSpec(E39Config) }.from.hocon.string(hocon)

    private fun platform(hocon: String) =
        E39Config.CarPlatformConfigSpec.toCarPlatformConfiguration(config(hocon))

    @Test
    fun `machine type defaults to E39`() {
        assertEquals(MachineType.E39, platform("").machineType)
    }

    @Test
    fun `machine type is read from the config`() {
        assertEquals(
            MachineType.VOLVO_P2,
            platform("e39Config.carPlatformConfig.MachineType = VOLVO_P2").machineType
        )
        assertEquals(
            MachineType.CARDPUTER_ZERO,
            platform("e39Config.carPlatformConfig.MachineType = \"CARDPUTER_ZERO\"").machineType
        )
    }

    @Test
    fun `a Pico only on an E39 talking to one`() {
        assertTrue(platform("").hasPico)
        assertFalse(platform("e39Config.carPlatformConfig.IBusConnection = DIRECT_BUS").hasPico)
        assertFalse(platform("e39Config.carPlatformConfig.MachineType = VOLVO_P2").hasPico)
        assertEquals(IBusConnection.PICO, platform("e39Config.carPlatformConfig.MachineType = VOLVO_P2").iBusConnection)
    }

    @Test
    fun `service groups for missing hardware are unavailable`() {
        assertEquals(emptySet<String>(), MachineType.E39.unavailableServiceGroups)
        assertTrue("SerialInterface" in MachineType.VOLVO_P2.unavailableServiceGroups)
        assertTrue("PicoToPiParsers" in MachineType.VOLVO_P2.unavailableServiceGroups)
        assertFalse("Pim" in MachineType.VOLVO_P2.unavailableServiceGroups)
        assertTrue("Pim" in MachineType.CARDPUTER_ZERO.unavailableServiceGroups)
        assertFalse("Bluetooth" in MachineType.CARDPUTER_ZERO.unavailableServiceGroups)
    }

    @Test
    fun `kiosk windows default to the old isPi`() {
        assertFalse(E39Config.WindowManagerConfig.isKiosk(config("")))
        assertTrue(E39Config.WindowManagerConfig.isKiosk(config("e39Config.carPlatformConfig.isPi = true")))
    }

    @Test
    fun `KioskWindows wins over the old isPi`() {
        assertFalse(E39Config.WindowManagerConfig.isKiosk(config(
            "e39Config { carPlatformConfig.isPi = true, windowManagerConfig.KioskWindows = false }"
        )))
        assertTrue(E39Config.WindowManagerConfig.isKiosk(config(
            "e39Config.windowManagerConfig.KioskWindows = true"
        )))
    }

    @Test
    fun `a saved config reads back the same`() {
        // The HMI writes the whole config back when a setting changes,
        // including KioskWindows while it's unset.
        val file = File(Files.createTempDirectory("e39-config").toFile(), "config.conf")
        val before = config("e39Config.carPlatformConfig { isPi = true, MachineType = VOLVO_P2 }")
        before.toHocon.toFile(file)

        val after = Config { addSpec(E39Config) }.from.hocon.file(file)
        assertTrue(E39Config.WindowManagerConfig.isKiosk(after))
        assertEquals(MachineType.VOLVO_P2, after[E39Config.CarPlatformConfigSpec._machineType])
    }
}
