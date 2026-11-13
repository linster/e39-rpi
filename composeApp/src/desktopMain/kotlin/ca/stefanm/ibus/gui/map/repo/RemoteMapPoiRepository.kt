package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.PoiSource
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.places.FoundMapPoi
import ca.stefanm.ibus.gui.map.places.PlacesQuery
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toMapPoi
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toPb
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Places from the map data (map POIs: fuel, restaurants, ...), found by the
 * maps backend through [PoiGrpcClient] (SearchPois with source MAP). Read
 * only: the user's own POIs are [RemotePoiRepository]'s.
 *
 * [search] looks near the query's centre, widening the radius
 * ([RADII_METERS]) until it finds [ENOUGH] places, so a rare category (ski
 * hills) still finds some and a common one (restaurants) stays quick. It
 * re-searches when the backend settings change and retries every
 * [RETRY_INTERVAL] while the backend is unreachable.
 */
@ApplicationScope
class RemoteMapPoiRepository @Inject constructor(
    private val client: PoiGrpcClient,
    private val backendChannel: MapsBackendChannel,
    private val settingsRepository: MapsBackendSettingsRepository,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "RemoteMapPoiRepository"
        val RETRY_INTERVAL: Duration = 10.seconds
        val RADII_METERS = listOf(25_000.0, 100_000.0, 400_000.0)
        const val ENOUGH = 10
        const val LIMIT = 50
    }

    sealed interface SearchState {
        data object Searching : SearchState
        /** Nearest first; within [radiusMeters] of the centre. */
        data class Found(val places: List<FoundMapPoi>, val radiusMeters: Double) : SearchState
        /** The backend isn't reachable (retrying) or refused the search. */
        data class Failed(val reason: String, val retrying: Boolean) : SearchState
    }

    /** Remember the flow in composables: each collection searches. */
    fun search(query: PlacesQuery): Flow<SearchState> = channelFlow {
        val reloads = Channel<Unit>(Channel.CONFLATED)
        launch { settingsRepository.settings.drop(1).collect { reloads.send(Unit) } }
        reloads.send(Unit)

        var retry: Job? = null
        for (reload in reloads) {
            retry?.cancel()
            try {
                send(searchWidening(query))
            } catch (e: MapsBackendException) {
                val retrying = e is MapsBackendException.Unavailable
                logger.w(TAG, "Places (${query.title}): ${e.message}")
                send(SearchState.Failed(e.message ?: e.javaClass.simpleName, retrying))
                if (retrying) {
                    retry = launch {
                        delay(RETRY_INTERVAL)
                        backendChannel.resetConnectBackoff()
                        reloads.send(Unit)
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun searchWidening(query: PlacesQuery): SearchState.Found {
        var found = SearchState.Found(emptyList(), 0.0)
        for (radius in RADII_METERS) {
            val places = client.searchPois(
                near = query.center.toPb(),
                radiusMeters = radius,
                nameContains = query.nameContains.trim(),
                limit = LIMIT,
                sources = listOf(PoiSource.POI_SOURCE_MAP),
                categories = query.categories.map { it.pb },
            ).map { FoundMapPoi(it.poi.toMapPoi(), it.distanceMeters) }
            found = SearchState.Found(places, radius)
            if (places.size >= ENOUGH) break
        }
        return found
    }
}
