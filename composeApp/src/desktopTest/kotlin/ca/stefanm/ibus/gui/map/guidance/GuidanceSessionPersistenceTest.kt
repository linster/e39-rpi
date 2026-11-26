package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import com.javadocmd.simplelatlng.LatLng
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A session saved and read back (what GuidanceSessionStorage's flow emits on
 * every save, and what the HMI loads at start) keeps its state, points and route.
 */
class GuidanceSessionPersistenceTest {

    private val start = LatLng(45.4215, -75.6972)
    private val end = LatLng(44.9007, -76.2482)
    private val path = listOf(start, LatLng(45.2, -75.9), end)

    private fun roundTrip(session: GuidanceSession) =
        GuidanceSession(PersistedGuidanceSession.fromSession(session))

    private fun calculated() = GuidanceSession().apply {
        startPoint = start
        endPoint = end
        route = path
    }

    private fun assertSame(expected: GuidanceSession, actual: GuidanceSession) {
        assertEquals(expected.sessionState, actual.sessionState)
        assertEquals(expected.startPoint, actual.startPoint)
        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.route, actual.route)
    }

    @Test
    fun routeCalculatedKeepsItsRoute() {
        val session = calculated()
        assertEquals(GuidanceSession.SessionState.ROUTE_CALCULATED, session.sessionState)
        assertSame(session, roundTrip(session))
    }

    @Test
    fun inGuidanceKeepsItsRoute() {
        val session = calculated().apply { startGuidance() }
        assertSame(session, roundTrip(session))
    }

    @Test
    fun terminatedKeepsItsRoute() {
        val session = calculated().apply { startGuidance(); terminateGuidance() }
        assertSame(session, roundTrip(session))
    }

    @Test
    fun readyToCalculateHasNoRoute() {
        val session = GuidanceSession().apply { startPoint = start; endPoint = end }
        val back = roundTrip(session)
        assertEquals(GuidanceSession.SessionState.READY_TO_CALCULATE, back.sessionState)
        assertNull(back.route)
    }

    private val maneuvers = listOf(
        RemoteRouteRepository.Maneuver(
            kind = RemoteRouteRepository.ManeuverKind.START,
            instruction = "Drive west on Wellington Street.",
            verbalPreTransition = "Drive west on Wellington Street.",
            streetNames = listOf("Wellington Street"),
            lengthMeters = 1200.0,
            durationSeconds = 90.0,
            location = start,
            triggerRadiusMeters = 100.0,
            pathIndexRange = 0..1,
            roundaboutExit = 0,
        ),
        RemoteRouteRepository.Maneuver(
            kind = RemoteRouteRepository.ManeuverKind.DESTINATION,
            instruction = "You have arrived.",
            verbalPreTransition = "",
            streetNames = emptyList(),
            lengthMeters = 0.0,
            durationSeconds = 0.0,
            location = end,
            triggerRadiusMeters = 50.0,
            pathIndexRange = 2..2,
            roundaboutExit = 0,
        ),
    )

    private fun inGuidanceWithManeuvers() = calculated().apply {
        maneuvers = this@GuidanceSessionPersistenceTest.maneuvers
        startGuidance()
    }

    @Test
    fun maneuversAreKept() {
        val back = roundTrip(inGuidanceWithManeuvers())
        assertEquals(maneuvers, back.maneuvers)
        assertEquals(GuidanceInstruction.fromRoute(maneuvers), GuidanceInstruction.fromRoute(back.maneuvers))
    }

    /** Through konf's HOCON, as config.conf is written and read. */
    private fun hocon(session: PersistedGuidanceSession): String =
        Config { addSpec(E39Config.GuidanceConfig) }
            .apply { this[E39Config.GuidanceConfig.currentSession] = session }
            .toHocon.toText()

    private fun fromHocon(text: String): GuidanceSession =
        Config { addSpec(E39Config.GuidanceConfig) }.from.hocon.string(text)
            .let { GuidanceSession(it[E39Config.GuidanceConfig.currentSession]) }

    @Test
    fun maneuversAreKeptInTheConfigFile() {
        val session = inGuidanceWithManeuvers()
        val back = fromHocon(hocon(PersistedGuidanceSession.fromSession(session)))
        assertSame(session, back)
        assertEquals(maneuvers, back.maneuvers)
    }

    @Test
    fun aSessionSavedBeforeManeuversLoadsWithNone() {
        val text = hocon(PersistedGuidanceSession.fromSession(calculated().apply { startGuidance() }))
        val withoutManeuvers = text.lines().filterNot { it.trim().startsWith("maneuvers") }.joinToString("\n")
        assertNotEquals(text, withoutManeuvers)

        val back = fromHocon(withoutManeuvers)
        assertEquals(GuidanceSession.SessionState.IN_GUIDANCE, back.sessionState)
        assertEquals(path, back.route)
        assertEquals(emptyList<RemoteRouteRepository.Maneuver>(), back.maneuvers)
    }

    @Test
    fun emptySessionStaysEmpty() {
        val back = roundTrip(GuidanceSession())
        assertEquals(GuidanceSession.SessionState.SETTING_UP, back.sessionState)
        assertNull(back.startPoint)
        assertNull(back.endPoint)
        assertNull(back.route)
    }
}
