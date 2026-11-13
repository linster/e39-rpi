package ca.stefanm.ibus.configuration

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.hocon
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

internal class VersionFileTest {

    @Test
    fun `setting one version keeps what others wrote meanwhile`() {
        val file = File(Files.createTempDirectory("e39-version").toFile(), "version.conf")
        file.writeText("hmiVersion {\n  hmiHash = \"aaa1111\"\n  mapVersion = \"map-20261113T040254Z\"\n}\n")
        // What the HMI read at start-up...
        val atStartup = Config { addSpec(HmiVersion) }.from.hocon.file(file)
        // ...then the map installer and a deploy changed the file.
        file.writeText("hmiVersion {\n  hmiHash = \"bbb2222\"\n  mapVersion = \"map-ottawa-20261113T044215Z\"\n}\n")

        // The Pico reports its firmware.
        setOnFile(file, HmiVersion, HmiVersion.fwHash, "fw123")

        val now = Config { addSpec(HmiVersion) }.from.hocon.file(file)
        assertEquals("fw123", now[HmiVersion.fwHash])
        assertEquals("map-ottawa-20261113T044215Z", now[HmiVersion.mapVersion])
        assertEquals("bbb2222", now[HmiVersion.hmiHash])
        assertEquals("map-20261113T040254Z", atStartup[HmiVersion.mapVersion])
    }

    @Test
    fun `a missing file is created`() {
        val file = File(Files.createTempDirectory("e39-version").toFile(), "version.conf")
        setOnFile(file, HmiVersion, HmiVersion.fwHash, "fw123")
        assertEquals("fw123", Config { addSpec(HmiVersion) }.from.hocon.file(file)[HmiVersion.fwHash])
    }
}
