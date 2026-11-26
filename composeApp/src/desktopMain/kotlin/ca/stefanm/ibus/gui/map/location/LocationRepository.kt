package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.hardwareDrivers.gps.FakeLocationService
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsdService
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

/**
 * Where the car is, for the map and guidance. The one place the position is
 * kept: the location services (platform service groups "GpsLocationProviders":
 * GpsdService, and "GpsLocationProvidersDebug": FakeLocationService) report into
 * it, and everything else reads it. The last report wins.
 *
 * App-wide: the platform's services get this same instance (their component
 * is made from the app's), and it outlives them across platform restarts.
 */
@ApplicationScope
class LocationRepository @Inject constructor(
    private val logger: Logger,
    private val configurablePlatform: ConfigurablePlatform
) {
    companion object {
        const val TAG = "LocationRepository"
    }

    private val lock = Any()

    private val _fix = MutableStateFlow<GpsFix?>(null)
    /** The last fix: current while [state] is [GpsState.FIX], else the last known position (or null). */
    val fix: StateFlow<GpsFix?> = _fix.asStateFlow()

    private val _state = MutableStateFlow(GpsState.NO_GPSD)
    /** For a reception indicator: no source, no receiver, searching, fix, stale. */
    val state: StateFlow<GpsState> = _state.asStateFlow()

    private val _satellites = MutableStateFlow<GpsSatellites?>(null)
    /** Satellites used and visible, when the source knows. */
    val satellites: StateFlow<GpsSatellites?> = _satellites.asStateFlow()

    private val _source = MutableStateFlow<String?>(null)
    /** The service that reported last (e.g. "GpsdService"); null when none is running. */
    val source: StateFlow<String?> = _source.asStateFlow()

    private val _position = MutableStateFlow<LatLng?>(null)

    fun getPosition() : Flow<LatLng?> {
        return fix.map { it?.latLng }
    }
    fun getPositionStateFlow() : StateFlow<LatLng?> {
        return _position
    }

    // --- For the location services ---------------------------------------

    fun reportFix(source: String, fix: GpsFix) = synchronized(lock) {
        _source.value = source
        _fix.value = fix
        _position.value = fix.latLng
    }

    fun reportState(source: String, state: GpsState) = synchronized(lock) {
        _source.value = source
        val old = _state.value
        if (old != state) {
            _state.value = state
            logger.i(TAG, "$source: $old -> $state")
        }
    }

    fun reportSatellites(source: String, satellites: GpsSatellites?) = synchronized(lock) {
        _source.value = source
        _satellites.value = satellites
    }

    /**
     * [source] stopped. If it was the one reporting, there's no position
     * source now: [GpsState.NO_GPSD], no satellites. The last fix is kept as
     * the last known position.
     */
    fun sourceStopped(source: String) = synchronized(lock) {
        if (_source.value != source) return@synchronized
        _source.value = null
        _satellites.value = null
        if (_state.value != GpsState.NO_GPSD) {
            logger.i(TAG, "$source stopped: ${_state.value} -> ${GpsState.NO_GPSD}")
            _state.value = GpsState.NO_GPSD
        }
    }


    fun anyLocationServiceRunning() : Flow<Boolean> {
        val serviceNames = listOf(
            FakeLocationService.NAME,
            GpsdService.NAME
        )
        return serviceNames
            .map { isLocationServiceRunning(it) }
            .let { combine(it) { it.any()} }
    }

    private fun isLocationServiceRunning (name : String) : Flow<Boolean> {
        return configurablePlatform.servicesRunning
            .transformLatest {
                if (it.isEmpty()) {
                    emit(false)
                } else {
                    val anyRunning = it
                        .flatMap { group -> group.children }
                        .filter { service -> service.name == name }
                        .map { it.runStatus }
                        .merge()
                        .map { it == PlatformService.RunStatus.RUNNING }
                    emitAll(anyRunning)
                }
            }
    }
}
