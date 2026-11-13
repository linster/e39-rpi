package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository.ManeuverKind
import com.javadocmd.simplelatlng.LatLng

/**
 * One turn-by-turn instruction of a route: a maneuver from the maps
 * backend's RouteService (Valhalla's, e39maps.v1.Maneuver), as the
 * instruction consumers (notification, side panel, guidance screens) get it.
 */
data class GuidanceInstruction(
    /** Its place in the route's maneuvers (0 = the start). */
    val index: Int,
    /** How many maneuvers the route has. */
    val count: Int,
    val kind: ManeuverKind,
    /** e.g. "Turn left onto Main Street." */
    val text: String,
    /** What to say ahead of the maneuver. */
    val verbalPreTransition: String,
    val streetNames: List<String>,
    /** Where the user makes the maneuver. */
    val location: LatLng,
    /**
     * Show the instruction once the car is this close to [location]
     * (straight line): about 15 s at the approach speed, 30-1000 m.
     */
    val triggerRadiusMeters: Double,
    /** Of the stretch after this maneuver (to the next one). */
    val lengthAfterMeters: Double,
    val durationAfterSeconds: Double,
    /** Roundabouts: which exit (1 = first); 0 otherwise. */
    val roundaboutExit: Int,
) {
    val isDestination: Boolean
        get() = kind in setOf(ManeuverKind.DESTINATION, ManeuverKind.DESTINATION_LEFT, ManeuverKind.DESTINATION_RIGHT)

    /** The street names joined ("Main Street / Highway 7"); empty without. */
    val streets: String
        get() = streetNames.joinToString(" / ")

    companion object {
        /** The route's maneuvers as instructions; ones without a location are left out. */
        fun fromRoute(maneuvers: List<RemoteRouteRepository.Maneuver>): List<GuidanceInstruction> =
            maneuvers.mapIndexedNotNull { index, m ->
                m.location?.let { location ->
                    GuidanceInstruction(
                        index = index,
                        count = maneuvers.size,
                        kind = m.kind,
                        text = m.instruction,
                        verbalPreTransition = m.verbalPreTransition,
                        streetNames = m.streetNames,
                        location = location,
                        triggerRadiusMeters = m.triggerRadiusMeters,
                        lengthAfterMeters = m.lengthMeters,
                        durationAfterSeconds = m.durationSeconds,
                        roundaboutExit = m.roundaboutExit,
                    )
                }
            }
    }
}
