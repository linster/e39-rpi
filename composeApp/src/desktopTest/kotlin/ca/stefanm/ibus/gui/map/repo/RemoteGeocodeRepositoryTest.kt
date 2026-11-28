package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.GeocodePrecision
import ca.stefanm.e39maps.v1.GeocodeRequest
import ca.stefanm.e39maps.v1.GeocodeResponse
import ca.stefanm.e39maps.v1.GeocodeServiceGrpcKt
import ca.stefanm.e39maps.v1.geocodeCandidate
import ca.stefanm.e39maps.v1.geocodeResponse
import ca.stefanm.e39maps.v1.latLng
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Address
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Precision
import com.javadocmd.simplelatlng.LatLng
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket

/** [RemoteGeocodeRepository] against a GeocodeService on a local port. */
class RemoteGeocodeRepositoryTest {

    private var requests = mutableListOf<GeocodeRequest>()
    private var answer: (GeocodeRequest) -> GeocodeResponse = { geocodeResponse { } }

    private lateinit var server: Server
    private lateinit var channel: ManagedChannel
    private lateinit var repository: RemoteGeocodeRepository

    @Before
    fun start() {
        server = ServerBuilder.forPort(0).addService(object : GeocodeServiceGrpcKt.GeocodeServiceCoroutineImplBase() {
            override suspend fun geocode(request: GeocodeRequest): GeocodeResponse {
                requests += request
                return answer(request)
            }
        }).build().start()
        channel = ManagedChannelBuilder.forAddress("localhost", server.port).usePlaintext().build()
        repository = RemoteGeocodeRepository(GeocodeGrpcClient { channel })
    }

    @After
    fun stop() {
        channel.shutdownNow()
        server.shutdownNow()
    }

    @Test
    fun sendsTheAddressPartsAndPosition() = runBlocking {
        repository.geocode(
            query = "123 Main St, Regina",
            address = Address("123 Main St", "", "Regina", "SK", "S4P 1A1", "Canada"),
            near = LatLng(50.0, -104.0),
            limit = 3,
        ).getOrThrow()

        val r = requests.single()
        assertEquals("123 Main St, Regina", r.query)
        assertEquals("123 Main St", r.address.street)
        assertEquals("", r.address.houseNumber)
        assertEquals("Regina", r.address.locality)
        assertEquals("SK", r.address.region)
        assertEquals("S4P 1A1", r.address.postalCode)
        assertEquals("Canada", r.address.country)
        assertTrue(r.hasNear())
        assertEquals(50.0, r.near.latitude, 1e-9)
        assertEquals(-104.0, r.near.longitude, 1e-9)
        assertEquals(3, r.limit)
    }

    @Test
    fun noAddressOrPositionSendsNone() = runBlocking {
        repository.geocode(query = "K1P 1J1").getOrThrow()
        val r = requests.single()
        assertFalse(r.hasAddress())
        assertFalse(r.hasNear())
        assertEquals(GeocodeRepository.DEFAULT_LIMIT, r.limit)
    }

    @Test
    fun mapsTheCandidatesInOrder() = runBlocking {
        answer = {
            geocodeResponse {
                candidates += geocodeCandidate {
                    label = "110 Bank Street, Ottawa"
                    location = latLng { latitude = 45.421; longitude = -75.6993 }
                    precision = GeocodePrecision.GEOCODE_PRECISION_ADDRESS
                    score = 9.5
                    street = "Bank Street"; houseNumber = "110"; locality = "Ottawa"; postalCode = "K1P 5N7"
                }
                candidates += geocodeCandidate {
                    label = "Bank Street, Ottawa"
                    location = latLng { latitude = 45.41; longitude = -75.693 }
                    precision = GeocodePrecision.GEOCODE_PRECISION_STREET
                    score = 4.0
                }
                candidates += geocodeCandidate {
                    label = "Somewhere"
                    // A kind from a newer backend.
                    precisionValue = 99
                }
            }
        }
        val c = repository.geocode(query = "110 Bank").getOrThrow()

        assertEquals(listOf("110 Bank Street, Ottawa", "Bank Street, Ottawa", "Somewhere"), c.map { it.label })
        assertEquals(LatLng(45.421, -75.6993), c[0].location)
        assertEquals(Precision.ADDRESS, c[0].precision)
        assertEquals(9.5, c[0].score, 1e-9)
        assertEquals(listOf("Bank Street", "110", "Ottawa", "K1P 5N7"),
            c[0].let { listOf(it.street, it.houseNumber, it.locality, it.postalCode) })
        assertEquals(Precision.STREET, c[1].precision)
        assertEquals(Precision.UNKNOWN, c[2].precision)
    }

    @Test
    fun everyPrecision() {
        val expected = mapOf(
            GeocodePrecision.GEOCODE_PRECISION_ADDRESS to Precision.ADDRESS,
            GeocodePrecision.GEOCODE_PRECISION_INTERPOLATED to Precision.INTERPOLATED,
            GeocodePrecision.GEOCODE_PRECISION_STREET to Precision.STREET,
            GeocodePrecision.GEOCODE_PRECISION_PLACE to Precision.PLACE,
            GeocodePrecision.GEOCODE_PRECISION_POSTCODE to Precision.POSTCODE,
            GeocodePrecision.GEOCODE_PRECISION_UNSPECIFIED to Precision.UNKNOWN,
        )
        with(RemoteGeocodeRepository) {
            GeocodePrecision.entries.filter { it != GeocodePrecision.UNRECOGNIZED }.forEach {
                assertEquals(it.name, expected.getValue(it), it.toPrecision())
            }
        }
        assertEquals(listOf(Precision.ADDRESS, Precision.INTERPOLATED), Precision.entries.filter { it.isExact })
    }

    @Test
    fun noMatchIsAnEmptyList() = runBlocking {
        assertEquals(emptyList<GeocodeRepository.Candidate>(), repository.geocode(query = "1 Nowhere Lane").getOrThrow())
    }

    @Test
    fun theBackendsUnavailableIsNoGeocodingData() = runBlocking {
        answer = { throw StatusException(Status.UNAVAILABLE.withDescription("this map has no geocoding data")) }
        val e = repository.geocode(query = "110 Bank St").exceptionOrNull()
        assertTrue(e.toString(), e is GeocodeRepository.NoGeocodingData)
        assertEquals("this map has no geocoding data", e!!.message)
    }

    @Test
    fun anEmptyRequestIsInvalidArgument() = runBlocking {
        answer = { throw StatusException(Status.INVALID_ARGUMENT.withDescription("give a query or an address")) }
        val e = repository.geocode().exceptionOrNull()
        assertTrue(e.toString(), e is MapsBackendException.InvalidArgument)
    }

    @Test
    fun noBackendIsUnavailableNotNoGeocodingData() = runBlocking {
        val closedPort = ServerSocket(0).use { it.localPort }
        val nowhere = ManagedChannelBuilder.forAddress("localhost", closedPort).usePlaintext().build()
        try {
            val e = RemoteGeocodeRepository(GeocodeGrpcClient { nowhere }).geocode(query = "x").exceptionOrNull()
            assertTrue(e.toString(), e is MapsBackendException.Unavailable)
        } finally {
            nowhere.shutdownNow()
        }
    }
}
