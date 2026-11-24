package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsdService
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * [LocationRepository] from the car platform's [GpsdService] (platform service
 * group "GpsLocationProviders").
 *
 * Not fed from the service yet: [fix], [state] and [satellites] stay at no
 * gpsd. [findGpsdService] finds the service.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ApplicationScope
class GpsdLocationRepository @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger: Logger,
) : LocationRepository {

    companion object {
        const val TAG = "GpsdLocationRepository"
    }

    private val _fix = MutableStateFlow<GpsFix?>(null)
    override val fix: StateFlow<GpsFix?> = _fix.asStateFlow()

    private val _state = MutableStateFlow(GpsState.NO_GPSD)
    override val state: StateFlow<GpsState> = _state.asStateFlow()

    private val _satellites = MutableStateFlow<GpsSatellites?>(null)
    override val satellites: StateFlow<GpsSatellites?> = _satellites.asStateFlow()

    /**
     * The running platform's [GpsdService], or null: the platform isn't
     * running, or has no such service. Each platform start makes a new one,
     * so look it up again after a restart rather than keeping it.
     */
    fun findGpsdService(): GpsdService? {
        val platformService = try {
            configurablePlatform.findServiceByName(GpsdService.NAME)
        } catch (e: NoSuchElementException) {
            // The platform's service list doesn't have it (findService uses first {}).
            logger.w(TAG, "The car platform has no ${GpsdService.NAME}")
            return null
        }
        return platformService?.baseService as? GpsdService
    }
}
