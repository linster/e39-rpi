package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.e39maps.v1.RouteServiceGrpcKt
import ca.stefanm.e39maps.v1.computeRouteRequest
import ca.stefanm.e39maps.v1.latLng
import ca.stefanm.ibus.gui.map.guidance.GuidanceInstructionTracker.Event
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import com.javadocmd.simplelatlng.LatLng
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * The tracker on real routes from a running maps backend (e.g. QEMU's
 * forwarded port), driven along each route's line in 10 m steps and in
 * 28 m steps (a GPS fix a second at 100 km/h):
 *
 *   E39_MAPS_BACKEND=localhost:50051 ./gradlew :composeApp:desktopTest --tests '*GuidanceRealRouteTest*'
 *
 * Skipped without E39_MAPS_BACKEND.
 */
internal class GuidanceRealRouteTest {

    private val routes = listOf(
        "Parliament Hill -> Kanata Centrum" to (LatLng(45.4236, -75.7009) to LatLng(45.3089, -75.9128)),
        "Westboro -> Rideau Centre" to (LatLng(45.3925, -75.7531) to LatLng(45.4255, -75.6920)),
        "Kanata_path" to (ca.stefanm.ibus.gui.map.Kanata_path.first() to ca.stefanm.ibus.gui.map.Kanata_path.last()),
    )

    @Test
    fun `every instruction is shown and passed in order, then the destination`() {
        val target = System.getenv("E39_MAPS_BACKEND")
        assumeTrue("E39_MAPS_BACKEND not set", target != null)
        val channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build()
        try {
            for ((name, ends) in routes) {
                val route = runBlocking {
                    RouteServiceGrpcKt.RouteServiceCoroutineStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS)
                        .computeRoute(computeRouteRequest {
                            origin = latLng { latitude = ends.first.latitude; longitude = ends.first.longitude }
                            destination = latLng { latitude = ends.second.latitude; longitude = ends.second.longitude }
                        })
                }
                val path = route.shapeList.map { LatLng(it.latitude, it.longitude) }
                val instructions = GuidanceInstruction.fromRoute(route.maneuversList.map {
                    RemoteRouteRepository.Maneuver(
                        RemoteRouteRepository.ManeuverKind.from(it.type), it.instruction, it.verbalPreTransition,
                        it.streetNamesList, it.lengthMeters, it.durationSeconds,
                        LatLng(it.location.latitude, it.location.longitude), it.triggerRadiusMeters,
                        it.beginShapeIndex..it.endShapeIndex, it.roundaboutExitCount,
                    )
                })
                for (step in listOf(10.0, 28.0)) {
                val tracker = GuidanceInstructionTracker(instructions)
                val line = RoutePath(path)
                val events = mutableListOf<Event>()
                var m = 0.0
                while (m <= line.lengthMeters + step) {
                    events += tracker.update(line.at(m))
                    m += step
                }
                val shown = events.filterIsInstance<Event.Approaching>().map { it.instruction.index }
                val passed = events.filter { it is Event.Passed || it is Event.Arrived }.map { it.instruction.index }
                println("$name, $step m steps: %.1f km, %d instructions; radii %s".format(
                    line.lengthMeters / 1000, instructions.size, instructions.map { it.triggerRadiusMeters.toInt() }))
                events.filter { it !is Event.Distance }.forEach { e ->
                    println("  ${e::class.simpleName} #${e.instruction.index} ${e.instruction.kind}: ${e.instruction.text}")
                }
                assertEquals("$name/$step: shown in order", instructions.map { it.index }, shown)
                assertEquals("$name/$step: passed in order", instructions.map { it.index }, passed)
                assertTrue("$name/$step: arrived", events.last() is Event.Arrived)
                assertTrue(tracker.isFinished)
                }
            }
        } finally {
            channel.shutdownNow()
        }
    }
}
