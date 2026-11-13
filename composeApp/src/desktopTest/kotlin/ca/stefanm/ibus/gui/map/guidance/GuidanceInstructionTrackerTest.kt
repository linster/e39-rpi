package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.map.guidance.GuidanceInstructionTracker.Event
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository.ManeuverKind
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GuidanceInstructionTrackerTest {

    // An L-shaped route: 600 m north from the start, then 400 m east.
    private val start = LatLng(45.30, -75.90)
    private val corner = LatLngTool.travel(start, LatLngTool.Bearing.NORTH, 600.0, LengthUnit.METER)
    private val end = LatLngTool.travel(corner, LatLngTool.Bearing.EAST, 400.0, LengthUnit.METER)
    private val path = RoutePath((0..60).map { LatLngTool.travel(start, 0.0, it * 10.0, LengthUnit.METER) } +
            (1..40).map { LatLngTool.travel(corner, 90.0, it * 10.0, LengthUnit.METER) })

    private fun maneuver(kind: ManeuverKind, at: LatLng, radius: Double, text: String, index: Int) =
        RemoteRouteRepository.Maneuver(kind, text, "", listOf("Street"), 0.0, 0.0, at, radius, index..index, 0)

    private val instructions = GuidanceInstruction.fromRoute(listOf(
        maneuver(ManeuverKind.START, start, 30.0, "Head north.", 0),
        maneuver(ManeuverKind.RIGHT, corner, 200.0, "Turn right.", 60),
        maneuver(ManeuverKind.DESTINATION, end, 100.0, "You have arrived.", 100),
    ))

    /** Drive along the route in [step]-meter steps; all the events. */
    private fun drive(tracker: GuidanceInstructionTracker, from: Double, to: Double, step: Double = 10.0): List<Event> {
        val events = mutableListOf<Event>()
        var m = from
        while (m <= to) {
            events += tracker.update(path.at(m))
            m += step
        }
        return events
    }

    private fun summary(events: List<Event>) = events.filter { it !is Event.Distance }.map {
        "${it::class.simpleName} ${it.instruction.kind}"
    }

    @Test
    fun `driving the route shows each instruction in its radius and hides it once passed`() {
        val tracker = GuidanceInstructionTracker(instructions)
        val events = drive(tracker, 0.0, path.lengthMeters)
        assertEquals(
            listOf("Approaching START", "Passed START", "Approaching RIGHT", "Passed RIGHT",
                "Approaching DESTINATION", "Arrived DESTINATION"),
            summary(events)
        )
        assertTrue(tracker.isFinished)
        // The turn shows up at its 200 m radius, not before.
        val approach = events.filterIsInstance<Event.Approaching>().first { it.instruction.kind == ManeuverKind.RIGHT }
        assertTrue(approach.distanceMeters in 190.0..200.0)
    }

    @Test
    fun `distance updates come in steps while approaching`() {
        val tracker = GuidanceInstructionTracker(instructions)
        drive(tracker, 0.0, 100.0)   // start passed
        val events = drive(tracker, 400.0, 560.0, step = 5.0)
        val distances = events.filterIsInstance<Event.Distance>().map { it.distanceMeters }
        assertTrue(distances.size in 10..20)
        assertTrue(distances.zipWithNext().all { (a, b) -> a - b >= GuidanceInstructionTracker.DISTANCE_STEP_METERS - 0.01 })
    }

    @Test
    fun `a dismissed instruction goes away and the next one follows`() {
        val tracker = GuidanceInstructionTracker(instructions)
        drive(tracker, 0.0, 100.0)
        drive(tracker, 450.0, 450.0)
        assertEquals(ManeuverKind.RIGHT, tracker.shown?.kind)
        assertEquals(listOf("Passed RIGHT"), summary(tracker.dismiss()))
        assertEquals(ManeuverKind.DESTINATION, tracker.next?.kind)
        assertEquals(null, tracker.shown)
    }

    @Test
    fun `a gap in positions past a maneuver catches up with the next one`() {
        val tracker = GuidanceInstructionTracker(instructions)
        drive(tracker, 0.0, 100.0)
        drive(tracker, 500.0, 500.0)            // the turn is shown
        // Next fix: far along the second leg, near the destination.
        val events = tracker.update(path.at(path.lengthMeters - 30))
        assertEquals(listOf("Passed RIGHT", "Approaching DESTINATION"), summary(events))
    }

    @Test
    fun `route path positions`() {
        assertEquals(1000.0, path.lengthMeters, 1.0)
        assertEquals(0.0, LatLngTool.distance(path.at(600.0), corner, LengthUnit.METER), 1.0)
        assertEquals(0.0, LatLngTool.distance(path.at(5000.0), path.points.last(), LengthUnit.METER), 0.01)
        assertEquals(25.0, LatLngTool.distance(path.at(25.0), start, LengthUnit.METER), 0.5)
    }
}
