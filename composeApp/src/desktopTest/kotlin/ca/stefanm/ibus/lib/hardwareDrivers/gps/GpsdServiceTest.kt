package ca.stefanm.ibus.lib.hardwareDrivers.gps

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

/**
 * GpsdService against a fake gpsd: a local ServerSocket that reads the WATCH and sends
 * canned lines. Real time and sockets, with short stale and backoff times.
 */
class GpsdServiceTest {

    private val logger = object : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val server = ServerSocket(0)
    private val clients = mutableListOf<Socket>()

    private val service = GpsdService(
        host = "127.0.0.1",
        port = server.localPort,
        staleAfter = 400.milliseconds,
        logger = logger,
        coroutineScope = scope,
        dispatcher = Dispatchers.IO,
        minBackoff = 50.milliseconds,
        maxBackoff = 200.milliseconds,
        stateCheckInterval = 50.milliseconds,
    )

    @After
    fun tearDown() {
        service.onShutdown()
        scope.cancel()
        clients.forEach { it.close() }
        server.close()
    }

    /** Accepts the service's connection; returns its WATCH line and a writer to send gpsd lines. */
    private fun accept(): Pair<String?, PrintWriter> {
        server.soTimeout = 5000
        val client = server.accept().also { clients += it }
        client.soTimeout = 5000
        val watch = BufferedReader(client.getInputStream().reader()).readLine()
        val out = PrintWriter(client.getOutputStream().writer(), true)
        out.println("""{"class":"VERSION","release":"3.25","rev":"3.25","proto_major":3,"proto_minor":15}""")
        return watch to out
    }

    private fun waitFor(what: String, timeoutMs: Long = 3000, check: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
        while (!check()) {
            if (System.nanoTime() > deadline) throw AssertionError("Timed out waiting for $what; state ${service.state.value}")
            Thread.sleep(10)
        }
    }

    private fun tpv(lat: Double, lon: Double, mode: Int = 3) =
        """{"class":"TPV","device":"/dev/ttyACM0","mode":$mode,"lat":$lat,"lon":$lon,"speed":10.0,"track":90.0,"eph":5.0}"""

    @Test
    fun fixThenStaleThenNoFix() {
        service.onCreate()
        val (watch, gpsd) = accept()
        assertEquals(GpsdMessage.WATCH.trimEnd('\n'), watch)

        gpsd.println("""{"class":"DEVICES","devices":[]}""")
        waitFor("no receiver") { service.state.value == GpsState.NO_RECEIVER }

        gpsd.println("""{"class":"DEVICES","devices":[{"class":"DEVICE","path":"/dev/ttyACM0","activated":"2026-11-23T20:00:00.000Z"}]}""")
        waitFor("searching") { service.state.value == GpsState.SEARCHING }
        assertEquals(setOf("/dev/ttyACM0"), service.devices.value)

        gpsd.println(tpv(45.347, -75.919))
        waitFor("a fix") { service.state.value == GpsState.FIX }
        val fix = service.fix.value!!
        assertEquals(45.347, fix.latLng.latitude, 1e-9)
        assertEquals(-75.919, fix.latLng.longitude, 1e-9)
        assertEquals(5.0, fix.accuracyM!!, 1e-9)

        gpsd.println("""{"class":"SKY","nSat":9,"uSat":6}""")
        waitFor("satellites") { service.satellites.value == GpsSatellites(used = 6, visible = 9) }

        // Nothing more from gpsd: stale, keeping the last position.
        waitFor("stale") { service.state.value == GpsState.STALE }
        assertEquals(45.347, service.fix.value!!.latLng.latitude, 1e-9)

        // A new fix is current again; gpsd saying "no fix" makes it stale at once.
        gpsd.println(tpv(45.35, -75.92))
        waitFor("a fix again") { service.state.value == GpsState.FIX }
        assertEquals(45.35, service.fix.value!!.latLng.latitude, 1e-9)
        gpsd.println("""{"class":"TPV","device":"/dev/ttyACM0","mode":1}""")
        waitFor("stale after no fix", timeoutMs = 300) { service.state.value == GpsState.STALE }
        assertEquals(45.35, service.fix.value!!.latLng.latitude, 1e-9)

        // Receiver unplugged.
        gpsd.println("""{"class":"DEVICE","path":"/dev/ttyACM0","activated":0}""")
        waitFor("no receiver after unplug") { service.state.value == GpsState.NO_RECEIVER }
    }

    @Test
    fun noGpsdThenGpsdAppearsAndReconnects() {
        // Nothing listening yet: refuse connections by not accepting until later.
        val port = server.localPort
        server.close()
        val service = GpsdService(
            host = "127.0.0.1", port = port, staleAfter = 400.milliseconds, logger = logger,
            coroutineScope = scope, dispatcher = Dispatchers.IO,
            minBackoff = 50.milliseconds, maxBackoff = 200.milliseconds, stateCheckInterval = 50.milliseconds,
        )
        try {
            service.onCreate()
            Thread.sleep(300)
            assertEquals(GpsState.NO_GPSD, service.state.value)

            // gpsd starts (socket activation): the service connects within its backoff.
            val gpsdServer = ServerSocket(port)
            try {
                gpsdServer.soTimeout = 5000
                val first = gpsdServer.accept()
                PrintWriter(first.getOutputStream().writer(), true).println(tpv(45.0, -75.0))
                waitFor("a fix from the new gpsd") {
                    service.state.value == GpsState.FIX
                }

                // gpsd goes away: no gpsd, last position kept; then it reconnects.
                first.close()
                waitFor("no gpsd") { service.state.value == GpsState.NO_GPSD }
                assertEquals(45.0, service.fix.value!!.latLng.latitude, 1e-9)
                assertTrue(service.devices.value.isEmpty())

                val second = gpsdServer.accept()
                PrintWriter(second.getOutputStream().writer(), true).println(tpv(46.0, -76.0))
                waitFor("a fix after reconnecting") {
                    service.state.value == GpsState.FIX && service.fix.value?.latLng?.latitude == 46.0
                }
                second.close()
            } finally {
                gpsdServer.close()
            }
        } finally {
            service.onShutdown()
        }
    }

    @Test
    fun shutdownStops() {
        service.onCreate()
        val (_, gpsd) = accept()
        gpsd.println(tpv(45.0, -75.0))
        waitFor("a fix") { service.state.value == GpsState.FIX }
        service.onShutdown()
        assertEquals(GpsState.NO_GPSD, service.state.value)
        // The connection is closed: gpsd's next read sees the end.
        val client = clients.single()
        assertEquals(-1, client.getInputStream().read())
    }
}
