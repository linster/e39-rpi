package ca.stefanm.ibus.lib.hardwareDrivers.gps

import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeLocationServiceTest {

    private val logger = object : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    private val repository = LocationRepository(logger)
    private val service = FakeLocationService(repository, logger)

    @Test
    fun locationGoesIntoTheRepository() {
        service.onCreate()
        service.setLocation(LatLng(45.347, -75.919), speedMps = 12.5, trackDeg = 90.0)

        val fix = repository.fix.value!!
        assertEquals(45.347, fix.latLng.latitude, 1e-9)
        assertEquals(-75.919, fix.latLng.longitude, 1e-9)
        assertEquals(GpsFix.Mode.THREE_D, fix.mode)
        assertEquals(12.5, fix.speedMps!!, 1e-9)
        assertEquals(90.0, fix.trackDeg!!, 1e-9)
        assertEquals(GpsState.FIX, repository.state.value)
        assertEquals(FakeLocationService.NAME, repository.source.value)

        // A new location replaces it.
        service.setLocation(LatLng(45.35, -75.92))
        assertEquals(45.35, repository.fix.value!!.latLng.latitude, 1e-9)
        assertNull(repository.fix.value!!.speedMps)
    }

    @Test
    fun ignoredWhileNotRunning() {
        service.setLocation(LatLng(45.0, -75.0))
        assertNull(repository.fix.value)
        assertEquals(GpsState.NO_GPSD, repository.state.value)

        service.onCreate()
        service.setLocation(LatLng(45.0, -75.0))
        service.onShutdown()
        service.setLocation(LatLng(46.0, -76.0))
        // Stopped: no source, last position kept.
        assertEquals(GpsState.NO_GPSD, repository.state.value)
        assertNull(repository.source.value)
        assertEquals(45.0, repository.fix.value!!.latLng.latitude, 1e-9)
    }
}
