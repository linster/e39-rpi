package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import ca.stefanm.ibus.lib.logging.StdOutLogger
import com.javadocmd.simplelatlng.LatLng


//A session that describes the guidance we're doing
class GuidanceSession {
    //We can be in a session while we're fetching the route for it
    //Navigaing means we're actually driving the route.
    //Setting up means we're building up the info needed for the session to start.

    var sessionState : SessionState = SessionState.SETTING_UP
        private set

    enum class SessionState {
        SETTING_UP,
        READY_TO_CALCULATE, //We're set up enough to calculate the route
        ROUTE_CALCULATED, //We have the route
        IN_GUIDANCE, //The user is in a guidance session.
        TERMINATED
    }

    var startPoint : LatLng? = null
        set(value) {
            if (sessionState == SessionState.SETTING_UP) {
                field = value
                updateStateWhenPointsSet()
            }
        }

    var endPoint : LatLng? = null
        set(value) {
            if (sessionState == SessionState.SETTING_UP) {
                field = value
                updateStateWhenPointsSet()
            }
        }


    private fun updateStateWhenPointsSet() {
        if (startPoint != null && endPoint != null && sessionState == SessionState.SETTING_UP) {
            sessionState = SessionState.READY_TO_CALCULATE
        }
    }

    var route : List<LatLng>? = null
        set(value) {
            if (sessionState == SessionState.READY_TO_CALCULATE) {
                field = value
                sessionState = SessionState.ROUTE_CALCULATED
            } else {
                if (route != null) {
                    //TODO claude did a shitty job of this.
                    StdOutLogger().d("GuidanceSession", "Incorrect state ${sessionState} for setting route $route.")
                }
            }
        }

    /** The route's maneuvers (the maps backend's), kept with the route so guidance survives a restart. */
    var maneuvers : List<RemoteRouteRepository.Maneuver> = emptyList()

    fun startGuidance() {
        sessionState = SessionState.IN_GUIDANCE
    }

    fun terminateGuidance() {
        sessionState = SessionState.TERMINATED
    }

    fun reset() {
        sessionState = SessionState.SETTING_UP
        startPoint = null
        endPoint = null
    }

    override fun toString(): String {
        return "GuidanceSession(sessionState=$sessionState, startPoint=$startPoint, endPoint=$endPoint, route=$route, maneuvers=${maneuvers.size})"
    }

    constructor()

    constructor(session: PersistedGuidanceSession) {
        startPoint = if (session.startIsNull) null else session.start?.let { LatLng(it.first, it.second) }
        endPoint = if (session.endIsNull) null else session.end?.let { LatLng(it.first, it.second) }
        // The route setter only takes a route in READY_TO_CALCULATE (both
        // points set, as above), so the saved state is restored after it.
        if (!session.routeIsNull) {
            route = session.route?.map { LatLng(it.first, it.second) } ?: listOf()
        }
        maneuvers = session.maneuvers.map { it.toManeuver() }
        sessionState = SessionState.valueOf(session.sessionState)
    }

    companion object {
        val DEFAULT = GuidanceSession(PersistedGuidanceSession.DEFAULT)
    }
}

data class PersistedGuidanceSession(
    val sessionState: String,
    val start : Pair<Double, Double>?,
    val startIsNull : Boolean,
    val end : Pair<Double, Double>?,
    val endIsNull : Boolean,
    val route : List<Pair<Double, Double>>?,
    val routeIsNull : Boolean,
    /** Defaulted: a session saved before maneuvers were kept loads with none. */
    val maneuvers : List<PersistedManeuver> = emptyList(),
) {
    companion object {

        val DEFAULT = PersistedGuidanceSession(
            sessionState = GuidanceSession.SessionState.SETTING_UP.name,
            start = Pair(0.0, 0.0),
            startIsNull = true,
            end = Pair(0.0, 0.0),
            endIsNull = true,
            route = listOf(),
            routeIsNull = true
        )

        fun fromSession(guidanceSession: GuidanceSession) : PersistedGuidanceSession {
            return PersistedGuidanceSession(
                sessionState = guidanceSession.sessionState.name,
                start = guidanceSession.startPoint?.let { it.latitude to it.longitude } ?: Pair(0.0, 0.0),
                startIsNull = guidanceSession.startPoint == null,
                end = guidanceSession.endPoint?.let { it.latitude to it.longitude } ?: Pair(0.0, 0.0),
                endIsNull = guidanceSession.endPoint == null,
                route = guidanceSession.route?.map { it.latitude to it.longitude } ?: listOf(),
                routeIsNull = guidanceSession.route == null,
                maneuvers = guidanceSession.maneuvers.map { PersistedManeuver.from(it) },
            )
        }
    }
}

/** A Maneuver as the config file keeps it: points as pairs, the path range as two ints. */
data class PersistedManeuver(
    val kind: String,
    val instruction: String,
    val verbalPreTransition: String,
    val streetNames: List<String>,
    val lengthMeters: Double,
    val durationSeconds: Double,
    val location: Pair<Double, Double>?,
    val triggerRadiusMeters: Double,
    val pathIndexFirst: Int,
    val pathIndexLast: Int,
    val roundaboutExit: Int,
) {
    fun toManeuver() = RemoteRouteRepository.Maneuver(
        kind = RemoteRouteRepository.ManeuverKind.entries.firstOrNull { it.name == kind }
            ?: RemoteRouteRepository.ManeuverKind.UNSPECIFIED,
        instruction = instruction,
        verbalPreTransition = verbalPreTransition,
        streetNames = streetNames,
        lengthMeters = lengthMeters,
        durationSeconds = durationSeconds,
        location = location?.let { LatLng(it.first, it.second) },
        triggerRadiusMeters = triggerRadiusMeters,
        pathIndexRange = pathIndexFirst..pathIndexLast,
        roundaboutExit = roundaboutExit,
    )

    companion object {
        fun from(m: RemoteRouteRepository.Maneuver) = PersistedManeuver(
            kind = m.kind.name,
            instruction = m.instruction,
            verbalPreTransition = m.verbalPreTransition,
            streetNames = m.streetNames,
            lengthMeters = m.lengthMeters,
            durationSeconds = m.durationSeconds,
            location = m.location?.let { it.latitude to it.longitude },
            triggerRadiusMeters = m.triggerRadiusMeters,
            pathIndexFirst = m.pathIndexRange.first,
            pathIndexLast = m.pathIndexRange.last,
            roundaboutExit = m.roundaboutExit,
        )
    }
}

