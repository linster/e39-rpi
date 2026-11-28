package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.GeocodeRequest
import ca.stefanm.e39maps.v1.GeocodeResponse
import ca.stefanm.e39maps.v1.GeocodeServiceGrpcKt
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for geocoding: e39maps.v1.GeocodeService.
 * [RemoteGeocodeRepository] builds on it with the HMI's types.
 */
class GeocodeGrpcClient internal constructor(
    private val channel: () -> Channel,
) {
    @Inject constructor(backendChannel: MapsBackendChannel) : this({ backendChannel.channel() })

    /**
     * @throws GeocodeRepository.NoGeocodingData when the backend answers
     *   UNAVAILABLE itself (the map can't geocode); an UNAVAILABLE from not
     *   reaching it is [MapsBackendException.Unavailable], as for every call.
     */
    suspend fun geocode(request: GeocodeRequest): GeocodeResponse = backendCall {
        try {
            GeocodeServiceGrpcKt.GeocodeServiceCoroutineStub(channel())
                .withDeadlineAfter(MapsBackendChannel.CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .geocode(request)
        } catch (e: StatusException) {
            // A status the backend sent has no cause; gRPC's own (connection
            // refused, reset, ...) carries the I/O error.
            if (e.status.code == Status.Code.UNAVAILABLE && e.status.cause == null) {
                throw GeocodeRepository.NoGeocodingData(e.status.description ?: "this map can't look up addresses")
            }
            throw e
        }
    }
}
