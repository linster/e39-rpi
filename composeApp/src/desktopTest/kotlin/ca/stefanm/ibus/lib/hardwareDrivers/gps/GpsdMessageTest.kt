package ca.stefanm.ibus.lib.hardwareDrivers.gps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Lines as gpsd sends them (https://gpsd.io/gpsd_json.html), and broken ones. */
class GpsdMessageTest {

    @Test
    fun version() {
        val m = GpsdMessage.parse("""{"class":"VERSION","release":"3.25","rev":"3.25","proto_major":3,"proto_minor":15}""")
        assertEquals(GpsdMessage.Version("3.25"), m)
    }

    @Test
    fun devices() {
        val m = GpsdMessage.parse(
            """{"class":"DEVICES","devices":[{"class":"DEVICE","path":"/dev/ttyACM0","driver":"u-blox",""" +
                    """"activated":"2026-11-23T20:00:00.000Z","flags":1,"native":1,"bps":9600}]}"""
        )
        assertEquals(GpsdMessage.Devices(listOf("/dev/ttyACM0")), m)
        assertEquals(GpsdMessage.Devices(emptyList()), GpsdMessage.parse("""{"class":"DEVICES","devices":[]}"""))
    }

    @Test
    fun deviceCameAndWent() {
        assertEquals(
            GpsdMessage.Device("/dev/ttyUSB1", true),
            GpsdMessage.parse("""{"class":"DEVICE","path":"/dev/ttyUSB1","activated":"2026-11-23T20:00:00.000Z"}""")
        )
        // Unplugged: activated 0 (a number), or a number in old gpsd.
        assertEquals(
            GpsdMessage.Device("/dev/ttyUSB1", false),
            GpsdMessage.parse("""{"class":"DEVICE","path":"/dev/ttyUSB1","activated":0}""")
        )
        assertEquals(
            GpsdMessage.Device("/dev/ttyUSB1", true),
            GpsdMessage.parse("""{"class":"DEVICE","path":"/dev/ttyUSB1","activated":1700000000.5}""")
        )
    }

    @Test
    fun tpv3dFix() {
        val m = GpsdMessage.parse(
            """{"class":"TPV","device":"/dev/ttyACM0","mode":3,"time":"2026-11-23T20:01:02.000Z",""" +
                    """"ept":0.005,"lat":45.347,"lon":-75.919,"altHAE":95.3,"epx":4.1,"epy":5.6,"epv":9.2,""" +
                    """"track":271.5,"speed":13.9,"climb":0.1,"eps":11.2,"eph":6.3}"""
        ) as GpsdMessage.Tpv
        val fix = m.toFix()!!
        assertEquals(45.347, fix.latLng.latitude, 1e-9)
        assertEquals(-75.919, fix.latLng.longitude, 1e-9)
        assertEquals(GpsFix.Mode.THREE_D, fix.mode)
        assertEquals(13.9, fix.speedMps!!, 1e-9)
        assertEquals(271.5, fix.trackDeg!!, 1e-9)
        assertEquals(Instant.parse("2026-11-23T20:01:02Z"), fix.time)
        assertEquals(6.3, fix.accuracyM!!, 1e-9)
    }

    @Test
    fun tpv2dFixWithoutEph() {
        val m = GpsdMessage.parse("""{"class":"TPV","mode":2,"lat":45.0,"lon":-75.0,"epx":3.0,"epy":7.5}""") as GpsdMessage.Tpv
        val fix = m.toFix()!!
        assertEquals(GpsFix.Mode.TWO_D, fix.mode)
        assertEquals(7.5, fix.accuracyM!!, 1e-9)
        assertNull(fix.speedMps)
        assertNull(fix.trackDeg)
        assertNull(fix.time)
        assertNull(m.device)
    }

    @Test
    fun tpvWithoutFix() {
        // Mode 1 (no fix), 0 (unknown) or missing, and a fix mode without a position: no fix.
        listOf(
            """{"class":"TPV","device":"/dev/ttyACM0","mode":1,"time":"2026-11-23T20:01:02.000Z"}""",
            """{"class":"TPV","mode":0}""",
            """{"class":"TPV"}""",
            """{"class":"TPV","mode":3,"lat":45.0}""",
            """{"class":"TPV","mode":3,"lat":95.0,"lon":-75.0}""",
        ).forEach { line ->
            val m = GpsdMessage.parse(line) as GpsdMessage.Tpv
            assertNull(line, m.toFix())
        }
        assertEquals(1, (GpsdMessage.parse("""{"class":"TPV","mode":1}""") as GpsdMessage.Tpv).mode)
        assertEquals(0, (GpsdMessage.parse("""{"class":"TPV"}""") as GpsdMessage.Tpv).mode)
    }

    @Test
    fun tpvBadTimeIsIgnored() {
        val m = GpsdMessage.parse("""{"class":"TPV","mode":2,"lat":45.0,"lon":-75.0,"time":"yesterday"}""") as GpsdMessage.Tpv
        assertNull(m.time)
        assertTrue(m.toFix() != null)
    }

    @Test
    fun skyFromSatellites() {
        val m = GpsdMessage.parse(
            """{"class":"SKY","device":"/dev/ttyACM0","hdop":1.2,"satellites":[""" +
                    """{"PRN":10,"el":45,"az":196,"ss":34,"used":true},""" +
                    """{"PRN":29,"el":67,"az":268,"ss":40,"used":true},""" +
                    """{"PRN":28,"el":7,"az":86,"ss":0,"used":false}]}"""
        )
        assertEquals(GpsdMessage.Sky(GpsSatellites(used = 2, visible = 3)), m)
    }

    @Test
    fun skyCounts() {
        // Newer gpsd: nSat / uSat.
        assertEquals(
            GpsdMessage.Sky(GpsSatellites(used = 7, visible = 12)),
            GpsdMessage.parse("""{"class":"SKY","nSat":12,"uSat":7,"satellites":[]}""")
        )
        // Only DOPs: nothing about satellites.
        assertEquals(GpsdMessage.Other("SKY"), GpsdMessage.parse("""{"class":"SKY","hdop":1.1,"vdop":2.0}"""))
    }

    @Test
    fun otherAndBroken() {
        assertEquals(
            GpsdMessage.Other("WATCH"),
            GpsdMessage.parse("""{"class":"WATCH","enable":true,"json":true,"nmea":false}""")
        )
        assertEquals(GpsdMessage.Other(null), GpsdMessage.parse("""{"lat":1}"""))
        assertNull(GpsdMessage.parse("not json {"))
        assertNull(GpsdMessage.parse("[1,2]"))
        assertNull(GpsdMessage.parse(""))
        // A DEVICE without a path says nothing.
        assertEquals(GpsdMessage.Other("DEVICE"), GpsdMessage.parse("""{"class":"DEVICE","activated":0}"""))
    }

    @Test
    fun watchCommand() {
        assertTrue(GpsdMessage.WATCH.startsWith("?WATCH={"))
        assertTrue(GpsdMessage.WATCH.endsWith(";\n"))
        assertFalse(GpsdMessage.WATCH.contains("nmea"))
    }
}
