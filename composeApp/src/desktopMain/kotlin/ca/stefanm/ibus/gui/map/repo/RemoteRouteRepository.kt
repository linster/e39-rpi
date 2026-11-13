package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.ManeuverType
import ca.stefanm.e39maps.v1.Units
import ca.stefanm.e39maps.v1.computeRouteRequest
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toLatLng
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toPb
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject
import ca.stefanm.e39maps.v1.Maneuver as PbManeuver

/**
 * Turn-by-turn routes from the maps backend (Valhalla on the device, once
 * it's there; until then every call fails with
 * [MapsBackendException.Unimplemented]).
 */
class RemoteRouteRepository @Inject constructor(
    private val client: RouteGrpcClient,
) {
    data class ComputedRoute(
        /** The whole route, origin to destination (for the map's Route.path). */
        val path: List<LatLng>,
        val lengthMeters: Double,
        val durationSeconds: Double,
        val maneuvers: List<Maneuver>,
    )

    data class Maneuver(
        val kind: ManeuverKind,
        /** e.g. "Turn left onto Main Street." */
        val instruction: String,
        /** What to say ahead of the maneuver. */
        val verbalPreTransition: String,
        val streetNames: List<String>,
        /** Of the stretch after this maneuver. */
        val lengthMeters: Double,
        val durationSeconds: Double,
        /** Where the maneuver happens. */
        val location: LatLng?,
        /**
         * Show/announce the maneuver once the car is this close to [location]
         * (straight line): ~15 s at the approach speed, 30-1000 m.
         */
        val triggerRadiusMeters: Double,
        val pathIndexRange: IntRange,
        /** Roundabouts: which exit (1 = first); 0 otherwise. */
        val roundaboutExit: Int,
    )

    /** Valhalla's maneuver types: the proto's ManeuverType without its prefix. */
    enum class ManeuverKind {
        UNSPECIFIED, NONE, START, START_RIGHT, START_LEFT, DESTINATION, DESTINATION_RIGHT,
        DESTINATION_LEFT, BECOMES, CONTINUE, SLIGHT_RIGHT, RIGHT, SHARP_RIGHT, UTURN_RIGHT,
        UTURN_LEFT, SHARP_LEFT, LEFT, SLIGHT_LEFT, RAMP_STRAIGHT, RAMP_RIGHT, RAMP_LEFT,
        EXIT_RIGHT, EXIT_LEFT, STAY_STRAIGHT, STAY_RIGHT, STAY_LEFT, MERGE, ROUNDABOUT_ENTER,
        ROUNDABOUT_EXIT, FERRY_ENTER, FERRY_EXIT, MERGE_RIGHT, MERGE_LEFT;

        companion object {
            fun from(type: ManeuverType): ManeuverKind =
                entries.firstOrNull { "MANEUVER_TYPE_${it.name}" == type.name } ?: UNSPECIFIED
        }
    }

    /**
     * A car route from [origin] to [destination] through [via], with
     * instruction texts in [language] (BCP 47, e.g. "en-US") and kilometers
     * or miles ([metricUnits]).
     */
    suspend fun computeRoute(
        origin: LatLng,
        destination: LatLng,
        via: List<LatLng> = emptyList(),
        metricUnits: Boolean = true,
        language: String = "en-US",
    ): Result<ComputedRoute> = runCatching {
        val route = client.computeRoute(computeRouteRequest {
            this.origin = origin.toPb()
            this.destination = destination.toPb()
            this.via.addAll(via.map { it.toPb() })
            units = if (metricUnits) Units.UNITS_KILOMETERS else Units.UNITS_MILES
            this.language = language
        })
        val path = route.shapeList.map { it.toLatLng() }
        ComputedRoute(
            path = path,
            lengthMeters = route.lengthMeters,
            durationSeconds = route.durationSeconds,
            maneuvers = route.maneuversList.map { it.toManeuver(path) },
        )
    }

    private fun PbManeuver.toManeuver(path: List<LatLng>) = Maneuver(
        kind = ManeuverKind.from(type),
        instruction = instruction,
        verbalPreTransition = verbalPreTransition,
        streetNames = streetNamesList,
        lengthMeters = lengthMeters,
        durationSeconds = durationSeconds,
        // Older backends send no location: fall back to the shape index.
        location = if (hasLocation()) location.toLatLng() else path.getOrNull(beginShapeIndex),
        triggerRadiusMeters = triggerRadiusMeters,
        pathIndexRange = beginShapeIndex..endShapeIndex,
        roundaboutExit = roundaboutExitCount,
    )
}
