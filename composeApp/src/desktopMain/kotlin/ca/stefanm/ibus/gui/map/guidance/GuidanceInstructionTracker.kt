package ca.stefanm.ibus.gui.map.guidance

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit

/**
 * Follows the car along a route's instructions, from positions (GPS, or
 * fake ones from the guidance debug window), and says what to show:
 *
 * - [Event.Approaching]: the car came within the next instruction's trigger
 *   radius: show it.
 * - [Event.Distance]: the car is closer to (or further from) the shown
 *   instruction: update its distance.
 * - [Event.Passed]: the car made the maneuver (got within [AT_METERS] of it
 *   and is moving away again or is closer to the one after it; or, without
 *   ever getting that close, is near the one after it) or it was dismissed:
 *   hide it. The next instruction is then the one after.
 * - [Event.Arrived]: the car reached the destination.
 *
 * Not thread-safe: feed it from one coroutine.
 */
class GuidanceInstructionTracker(val instructions: List<GuidanceInstruction>) {

    companion object {
        /** Within this of a maneuver's location the car is "at" it. */
        const val AT_METERS = 35.0
        /** Moving this much further away again after being at it: passed. */
        const val LEAVING_METERS = 15.0
        /** Within this of the destination: arrived. */
        const val ARRIVED_METERS = 25.0
        /** Distance updates only for changes of at least this much. */
        const val DISTANCE_STEP_METERS = 10.0

        fun meters(a: LatLng, b: LatLng): Double = LatLngTool.distance(a, b, LengthUnit.METER)
    }

    sealed interface Event {
        val instruction: GuidanceInstruction

        data class Approaching(override val instruction: GuidanceInstruction, val distanceMeters: Double) : Event
        data class Distance(override val instruction: GuidanceInstruction, val distanceMeters: Double) : Event
        data class Passed(override val instruction: GuidanceInstruction, val dismissed: Boolean) : Event
        data class Arrived(override val instruction: GuidanceInstruction) : Event
    }

    /** The index of the next instruction not yet passed; [instructions].size when done. */
    var nextIndex: Int = 0
        private set

    /** The instruction being shown (approached, not yet passed), if any. */
    var shown: GuidanceInstruction? = null
        private set

    val next: GuidanceInstruction? get() = instructions.getOrNull(nextIndex)
    val isFinished: Boolean get() = nextIndex >= instructions.size

    private var closestMeters = Double.MAX_VALUE
    private var lastReportedMeters: Double? = null

    /** What the car being at [position] changes. */
    fun update(position: LatLng): List<Event> {
        val events = mutableListOf<Event>()
        // A position can pass several short maneuvers at once (e.g. a
        // sparse GPS fix); each is handled in turn.
        while (true) {
            val instruction = next ?: return events
            val meters = meters(position, instruction.location)

            if (shown == null) {
                if (meters > instruction.triggerRadiusMeters && !nearFollowing(position, instruction)) {
                    return events
                }
                shown = instruction
                closestMeters = meters
                lastReportedMeters = meters
                events += Event.Approaching(instruction, meters)
            }

            if (instruction.isDestination) {
                if (meters <= ARRIVED_METERS) {
                    events += Event.Arrived(instruction)
                    advance()
                } else {
                    reportDistance(instruction, meters, events)
                }
                return events
            }

            closestMeters = minOf(closestMeters, meters)
            // Been at it, and now moving away, or already closer to the next
            // one (maneuvers a few meters apart, e.g. a turn just before the
            // destination: there's no room to move away first).
            val following = instructions.getOrNull(nextIndex + 1)
            val closerToFollowing = following != null && meters(position, following.location) < meters
            val madeIt = closestMeters <= AT_METERS && (meters >= closestMeters + LEAVING_METERS || closerToFollowing)
            if (madeIt || nearFollowing(position, instruction) && meters > AT_METERS) {
                events += Event.Passed(instruction, dismissed = false)
                advance()
                continue   // maybe already within the next one's radius
            }
            reportDistance(instruction, meters, events)
            return events
        }
    }

    /** The user (or the debug window) dismissed the shown/next instruction. */
    fun dismiss(): List<Event> {
        val instruction = next ?: return emptyList()
        advance()
        return listOf(Event.Passed(instruction, dismissed = true))
    }

    /**
     * Close to the instruction after [instruction] (the next one), and closer
     * to it than to [instruction]: [instruction] was passed unnoticed (e.g.
     * a GPS gap).
     */
    private fun nearFollowing(position: LatLng, instruction: GuidanceInstruction): Boolean {
        val following = instructions.getOrNull(nextIndex + 1) ?: return false
        return meters(position, following.location) <= minOf(following.triggerRadiusMeters, AT_METERS * 2) &&
                meters(position, following.location) < meters(position, instruction.location)
    }

    private fun reportDistance(instruction: GuidanceInstruction, meters: Double, events: MutableList<Event>) {
        val last = lastReportedMeters
        if (last == null || kotlin.math.abs(last - meters) >= DISTANCE_STEP_METERS) {
            lastReportedMeters = meters
            events += Event.Distance(instruction, meters)
        }
    }

    private fun advance() {
        nextIndex++
        shown = null
        closestMeters = Double.MAX_VALUE
        lastReportedMeters = null
    }
}
