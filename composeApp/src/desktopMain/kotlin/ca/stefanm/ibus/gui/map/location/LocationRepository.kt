package ca.stefanm.ibus.gui.map.location

import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsFix
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import kotlinx.coroutines.flow.StateFlow

/**
 * Where the car is, for the map and guidance. App-wide: it outlives the car
 * platform's services (a platform restart makes new ones), so screens use
 * this rather than a service.
 */
interface LocationRepository {
    /** The last fix: current while [state] is [GpsState.FIX], else the last known position (or null). */
    val fix: StateFlow<GpsFix?>

    /** For a reception indicator: no gpsd, no receiver, searching, fix, stale. */
    val state: StateFlow<GpsState>

    /** Satellites used and visible, when known. */
    val satellites: StateFlow<GpsSatellites?>
}
