package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.ComputeRouteRequest
import ca.stefanm.e39maps.v1.Route
import ca.stefanm.e39maps.v1.RouteServiceGrpcKt
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for routing: e39maps.v1.RouteService. The backend
 * answers UNIMPLEMENTED ([MapsBackendException.Unimplemented]) until it has
 * Valhalla. [RemoteRouteRepository] builds on it with the HMI's types.
 */
class RouteGrpcClient @Inject constructor(
    private val backendChannel: MapsBackendChannel,
) {
    companion object {
        /** Routing a long trip on a Pi takes longer than a POI lookup. */
        const val ROUTE_TIMEOUT_SECONDS = 30L
    }

    suspend fun computeRoute(request: ComputeRouteRequest): Route = backendCall {
        RouteServiceGrpcKt.RouteServiceCoroutineStub(backendChannel.channel())
            .withDeadlineAfter(ROUTE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .computeRoute(request)
    }
}
