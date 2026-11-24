package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.lib.hardwareDrivers.gps.FakeLocationService
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Through the real Dagger graph: a location service in the car platform's component
 * (FakeLocationService) -> the one LocationRepository -> the GUI's GuidanceService.
 * The car component is made from the GUI's ApplicationComponent, as ConfigurablePlatform
 * does, so both sides share the @ApplicationScope objects.
 */
class LocationFeedsGuidanceTest {

    private fun waitFor(what: String, timeoutMs: Long = 3000, check: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!check()) {
            if (System.currentTimeMillis() > deadline) throw AssertionError("Timed out waiting for $what")
            Thread.sleep(10)
        }
    }

    @Test
    fun fakeLocationReachesGuidance() {
        val gui = DaggerApplicationComponent.builder().build()   // GuiMain
        val guidance = gui.guidanceService()                       // what the map and screens get
        val repository = gui.locationRepository()

        val car = gui.configuredCarComponent(ConfiguredCarModule(LaptopDeviceConfiguration()))
        val fake = car.discoveredServiceFakeLocationService()
        fake.onCreate()
        try {
            fake.setLocation(LatLng(45.3470, -75.9190))

            assertEquals(45.3470, repository.fix.value!!.latLng.latitude, 1e-9)
            assertEquals(GpsState.FIX, repository.state.value)
            assertEquals(FakeLocationService.NAME, repository.source.value)

            waitFor("GuidanceService to get the first location") { guidance.position.value?.latitude == 45.3470 }
            assertEquals(-75.9190, guidance.position.value!!.longitude, 1e-9)

            fake.setLocation(LatLng(45.3500, -75.9200))
            waitFor("GuidanceService to get the second location") { guidance.position.value?.latitude == 45.3500 }
            assertEquals(-75.9200, guidance.position.value!!.longitude, 1e-9)
        } finally {
            fake.onShutdown()
        }
    }
}
