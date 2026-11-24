package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationRepositoryTest {

    private val logger = object : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    private fun fix(lat: Double) = GpsFix(LatLng(lat, -75.0), GpsFix.Mode.THREE_D, null, null, null, null)

    @Test
    fun startsWithNothing() {
        val repository = LocationRepository(logger)
        assertNull(repository.fix.value)
        assertEquals(GpsState.NO_GPSD, repository.state.value)
        assertNull(repository.satellites.value)
        assertNull(repository.source.value)
    }

    @Test
    fun reportsFromAService() {
        val repository = LocationRepository(logger)
        repository.reportState("A", GpsState.FIX)
        repository.reportFix("A", fix(45.0))
        repository.reportSatellites("A", GpsSatellites(used = 5, visible = 8))
        assertEquals(GpsState.FIX, repository.state.value)
        assertEquals(45.0, repository.fix.value!!.latLng.latitude, 1e-9)
        assertEquals(GpsSatellites(5, 8), repository.satellites.value)
        assertEquals("A", repository.source.value)
    }

    @Test
    fun stoppingKeepsTheLastPosition() {
        val repository = LocationRepository(logger)
        repository.reportState("A", GpsState.FIX)
        repository.reportFix("A", fix(45.0))
        repository.reportSatellites("A", GpsSatellites(used = 5, visible = 8))
        repository.sourceStopped("A")
        assertEquals(GpsState.NO_GPSD, repository.state.value)
        assertNull(repository.satellites.value)
        assertNull(repository.source.value)
        assertEquals(45.0, repository.fix.value!!.latLng.latitude, 1e-9)
    }

    @Test
    fun anotherServiceStoppingChangesNothing() {
        // B reported last: A stopping (e.g. a service that was also running) leaves B's position.
        val repository = LocationRepository(logger)
        repository.reportState("A", GpsState.SEARCHING)
        repository.reportState("B", GpsState.FIX)
        repository.reportFix("B", fix(46.0))
        repository.sourceStopped("A")
        assertEquals(GpsState.FIX, repository.state.value)
        assertEquals("B", repository.source.value)
        assertEquals(46.0, repository.fix.value!!.latLng.latitude, 1e-9)
    }
}
