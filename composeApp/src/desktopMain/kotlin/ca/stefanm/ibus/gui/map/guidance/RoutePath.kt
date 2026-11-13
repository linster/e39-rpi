package ca.stefanm.ibus.gui.map.guidance

import com.javadocmd.simplelatlng.LatLng

/**
 * A route's line measured along its length, for fake GPS positions (the
 * guidance debug window): where the car is after driving some distance, or
 * some distance before/after a point of the line.
 */
class RoutePath(val points: List<LatLng>) {

    /** Meters from the start to each point. */
    val distances: List<Double> = points.indices.runningFold(0.0) { total, i ->
        if (i == 0) total else total + GuidanceInstructionTracker.meters(points[i - 1], points[i])
    }.drop(1)

    val lengthMeters: Double get() = distances.lastOrNull() ?: 0.0

    /** The point [meters] along the line (clamped to its ends), interpolated between points. */
    fun at(meters: Double): LatLng {
        require(points.isNotEmpty()) { "an empty route" }
        if (meters <= 0.0 || points.size == 1) return points.first()
        if (meters >= lengthMeters) return points.last()
        var i = distances.indexOfFirst { it >= meters }
        if (i <= 0) i = 1
        val segment = distances[i] - distances[i - 1]
        val f = if (segment > 0) (meters - distances[i - 1]) / segment else 0.0
        val a = points[i - 1]
        val b = points[i]
        return LatLng(a.latitude + (b.latitude - a.latitude) * f, a.longitude + (b.longitude - a.longitude) * f)
    }

    /** Meters along the line of point [index]. */
    fun metersAt(index: Int): Double = distances[index.coerceIn(points.indices)]
}
