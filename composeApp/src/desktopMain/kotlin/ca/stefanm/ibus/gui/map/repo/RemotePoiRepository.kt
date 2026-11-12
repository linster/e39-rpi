package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.poi.PoiRepository.Poi
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toPb
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toPoi
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The user's POIs, stored by the maps backend (e39-rpi-maps) and reached
 * through [PoiGrpcClient]: the HMI keeps no POI database of its own.
 *
 * [pois] is a cached copy of the backend's list, so screens can read it
 * synchronously. It's refreshed when the backend settings change, after every
 * change made through this repository, and every [REFRESH_INTERVAL] (the
 * backend has no change stream yet, and POIs can also be edited in its Django
 * admin). While the backend is unreachable the last list stays and
 * [connectionState] says why.
 */
@ApplicationScope
class RemotePoiRepository @Inject constructor(
    private val client: PoiGrpcClient,
    private val backendChannel: MapsBackendChannel,
    private val settingsRepository: MapsBackendSettingsRepository,
    coroutineScope: CoroutineScope,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "RemotePoiRepository"
        val REFRESH_INTERVAL: Duration = 30.seconds
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    sealed interface ConnectionState {
        data object Connecting : ConnectionState
        data class Connected(val target: String) : ConnectionState
        data class Unavailable(val target: String, val reason: String) : ConnectionState
    }

    data class SearchResult(val poi: Poi, val distanceMeters: Double)

    private val _pois = MutableStateFlow<List<Poi>>(emptyList())
    /** All of the user's POIs, by name. */
    val pois: StateFlow<List<Poi>> = _pois.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val refreshRequests = Channel<Unit>(Channel.CONFLATED)

    init {
        coroutineScope.launch(Dispatchers.IO) {
            settingsRepository.settings.collectLatest { settings ->
                _connectionState.value = ConnectionState.Connecting
                while (true) {
                    val ok = fetch(settings.target)
                    // Wake up early for refresh(); otherwise poll.
                    withTimeoutOrNull(if (ok) REFRESH_INTERVAL else RETRY_INTERVAL) {
                        refreshRequests.receive()
                    }
                }
            }
        }
    }

    private suspend fun fetch(target: String): Boolean = try {
        _pois.value = client.listPois().map { it.toPoi() }
        if (_connectionState.value !is ConnectionState.Connected) {
            logger.i(TAG, "Maps backend at $target: ${_pois.value.size} POIs")
        }
        _connectionState.value = ConnectionState.Connected(target)
        true
    } catch (e: MapsBackendException) {
        val state = ConnectionState.Unavailable(target, e.message ?: e.javaClass.simpleName)
        if (_connectionState.value != state) {
            logger.w(TAG, "Maps backend at $target: ${state.reason}")
        }
        _connectionState.value = state
        backendChannel.resetConnectBackoff()
        false
    }

    /** Reload [pois] from the backend now (in the background). */
    fun refresh() {
        refreshRequests.trySend(Unit)
    }

    private suspend fun <T> changing(block: suspend () -> T): Result<T> =
        runCatching { block() }
            .onFailure {
                // Expected failures (backend down, rejected input) are the
                // caller's to show; only log a stack trace for the unexpected.
                if (it is MapsBackendException.Failed || it !is MapsBackendException) {
                    logger.e(TAG, "POI change failed: ${it.message}", it)
                } else {
                    logger.w(TAG, "POI change failed: ${it.message}")
                }
            }
            .also { refresh() }

    /** Saves a new POI ([Poi.id] is ignored); returns it with its id. */
    suspend fun create(poi: Poi): Result<Poi> = changing {
        client.createPoi(poi.toPb()).toPoi()
    }

    /** Replaces the editable fields of the POI with [Poi.id]. */
    suspend fun update(poi: Poi): Result<Poi> = changing {
        client.updatePoi(poi.requireId().toPb()).toPoi()
    }

    /** Creates the POI if it has no id yet, otherwise updates it. */
    suspend fun save(poi: Poi): Result<Poi> = if (poi.id == null) create(poi) else update(poi)

    suspend fun setVisible(poi: Poi, visible: Boolean): Result<Poi> = changing {
        client.updatePoi(poi.requireId().copy(isVisible = visible).toPb(), setOf(PoiGrpcClient.Field.VISIBLE)).toPoi()
    }

    /** Shows or hides every POI on the map. */
    suspend fun setAllVisible(visible: Boolean): Result<Unit> = changing {
        client.listPois().filter { it.visible != visible }.forEach { pb ->
            client.updatePoi(pb.toBuilder().setVisible(visible).build(), setOf(PoiGrpcClient.Field.VISIBLE))
        }
    }

    suspend fun delete(poi: Poi): Result<Unit> = changing {
        client.deletePoi(poi.requireId().id!!)
    }

    suspend fun get(id: Long): Result<Poi> = runCatching { client.getPoi(id).toPoi() }

    /** POIs within [radiusMeters] of [center], nearest first. */
    suspend fun searchNear(
        center: LatLng,
        radiusMeters: Double,
        nameContains: String = "",
        visibleOnly: Boolean = false,
        limit: Int = 0,
    ): Result<List<SearchResult>> = runCatching {
        client.searchPois(
            near = center.toPb(), radiusMeters = radiusMeters,
            nameContains = nameContains, visibleOnly = visibleOnly, limit = limit,
        ).map { SearchResult(it.poi.toPoi(), it.distanceMeters) }
    }

    /** POIs whose name contains [nameContains] (case-insensitive), by name. */
    suspend fun searchByName(nameContains: String, visibleOnly: Boolean = false, limit: Int = 0): Result<List<Poi>> =
        runCatching {
            client.searchPois(nameContains = nameContains, visibleOnly = visibleOnly, limit = limit)
                .map { it.poi.toPoi() }
        }

    /** POIs inside the box from [southWest] to [northEast], by name. */
    suspend fun searchInArea(southWest: LatLng, northEast: LatLng, visibleOnly: Boolean = false, limit: Int = 0) =
        runCatching {
            client.searchPois(
                boundingBox = ca.stefanm.e39maps.v1.boundingBox {
                    this.southWest = southWest.toPb()
                    this.northEast = northEast.toPb()
                },
                visibleOnly = visibleOnly, limit = limit,
            ).map { it.poi.toPoi() }
        }

    private fun Poi.requireId(): Poi {
        if (id == null) throw MapsBackendException.InvalidArgument("POI '$name' isn't saved yet (no id)", null)
        return this
    }
}
