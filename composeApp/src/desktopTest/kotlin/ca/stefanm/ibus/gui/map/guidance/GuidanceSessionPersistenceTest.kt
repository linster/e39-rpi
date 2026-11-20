package ca.stefanm.ibus.gui.map.guidance

import com.javadocmd.simplelatlng.LatLng
import org.junit.Assert.assertEquals
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

    @Test
    fun emptySessionStaysEmpty() {
        val back = roundTrip(GuidanceSession())
        assertEquals(GuidanceSession.SessionState.SETTING_UP, back.sessionState)
        assertNull(back.startPoint)
        assertNull(back.endPoint)
        assertNull(back.route)
    }
}
