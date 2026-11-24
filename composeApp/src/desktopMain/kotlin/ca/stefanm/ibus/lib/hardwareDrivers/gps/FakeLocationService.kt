package ca.stefanm.ibus.lib.hardwareDrivers.gps

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.GpsLocationProvidersServiceGroup
import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import java.time.Instant
import javax.inject.Inject

/**
 * A location service without a receiver, for the laptop and tests: whatever
 * location it's given ([setLocation]) goes into the [LocationRepository] as
 * a current fix. Only while it runs (between onCreate and onShutdown).
 */
@PlatformServiceInfo(
    name = FakeLocationService.NAME,
    description = "Puts a location it's given (setLocation) into the LocationRepository, as a fix. " +
            "For the laptop and tests: no receiver."
)
@GpsLocationProvidersServiceGroup
@ConfiguredCarScope
class FakeLocationService @Inject constructor(
    private val locationRepository: LocationRepository,
    private val logger: Logger,
) : Service {

    companion object {
        const val TAG = "FakeLocationService"
        /** Its name in the car platform's service list, and as a [LocationRepository] source. */
        const val NAME = "FakeLocationService"
    }

    private val lock = Any()
    private var running = false

    override fun onCreate() {
        synchronized(lock) { running = true }
    }

    override fun onShutdown() {
        synchronized(lock) { running = false }
        locationRepository.sourceStopped(NAME)
    }

    /**
     * The car is at [latLng] (optionally moving at [speedMps] towards
     * [trackDeg]): reported to the [LocationRepository] as a current 3D fix.
     * Ignored while the service isn't running. Whether it was reported.
     */
    fun setLocation(latLng: LatLng, speedMps: Double? = null, trackDeg: Double? = null): Boolean {
        synchronized(lock) {
            if (!running) {
                logger.w(TAG, "Not running: ignoring location $latLng")
                return false
            }
            locationRepository.reportFix(
                NAME,
                GpsFix(
                    latLng = latLng,
                    mode = GpsFix.Mode.THREE_D,
                    speedMps = speedMps,
                    trackDeg = trackDeg,
                    time = Instant.now(),
                    accuracyM = null,
                )
            )
            locationRepository.reportState(NAME, GpsState.FIX)
            return true
        }
    }
}
