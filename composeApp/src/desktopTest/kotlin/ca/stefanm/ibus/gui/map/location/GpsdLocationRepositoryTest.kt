package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class GpsdLocationRepositoryTest {

    private val logger = object : Logger {
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, e: Throwable?) {}
    }

    @Test
    fun noPlatformNoServiceNoPosition() {
        // Never run: no platform services.
        val platform = ConfigurablePlatform(Provider { error("not used before run()") }, Provider { error("not used before run()") })
        platform.stop()
        val repository = GpsdLocationRepository(platform, logger)

        assertNull(repository.findGpsdService())
        assertNull(repository.fix.value)
        assertEquals(GpsState.NO_GPSD, repository.state.value)
        assertNull(repository.satellites.value)
    }
}
