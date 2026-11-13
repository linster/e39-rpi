package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.BoundingBox
import ca.stefanm.e39maps.v1.PoiCategory
import ca.stefanm.e39maps.v1.PoiSource
import ca.stefanm.e39maps.v1.PoiServiceGrpcKt
import ca.stefanm.e39maps.v1.PoiResult
import ca.stefanm.e39maps.v1.SearchPoisRequestKt
import ca.stefanm.e39maps.v1.createPoiRequest
import ca.stefanm.e39maps.v1.deletePoiRequest
import ca.stefanm.e39maps.v1.getPoiRequest
import ca.stefanm.e39maps.v1.listPoisRequest
import ca.stefanm.e39maps.v1.searchPoisRequest
import ca.stefanm.e39maps.v1.updatePoiRequest
import com.google.protobuf.fieldMask
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import ca.stefanm.e39maps.v1.LatLng as PbLatLng
import ca.stefanm.e39maps.v1.Poi as PbPoi

/**
 * The transport layer for POIs: e39maps.v1.PoiService calls, with protobuf
 * messages in and out. [RemotePoiRepository] builds on it with the HMI's
 * types. Every call throws [MapsBackendException] on failure.
 */
class PoiGrpcClient @Inject constructor(
    private val backendChannel: MapsBackendChannel,
) {
    /** Fields UpdatePoi may change (the proto's update_mask paths). */
    enum class Field(val path: String) { NAME("name"), LOCATION("location"), ICON("icon"), VISIBLE("visible") }

    private fun stub() = PoiServiceGrpcKt.PoiServiceCoroutineStub(backendChannel.channel())
        .withDeadlineAfter(MapsBackendChannel.CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    suspend fun listPois(visibleOnly: Boolean = false): List<PbPoi> = backendCall {
        stub().listPois(listPoisRequest { this.visibleOnly = visibleOnly }).poisList
    }

    suspend fun getPoi(id: Long): PbPoi = backendCall {
        stub().getPoi(getPoiRequest { this.id = id })
    }

    suspend fun createPoi(poi: PbPoi): PbPoi = backendCall {
        stub().createPoi(createPoiRequest { this.poi = poi })
    }

    /** Changes [fields] of the POI with poi.id; all editable fields if empty. */
    suspend fun updatePoi(poi: PbPoi, fields: Set<Field> = emptySet()): PbPoi = backendCall {
        stub().updatePoi(updatePoiRequest {
            this.poi = poi
            if (fields.isNotEmpty()) {
                updateMask = fieldMask { paths.addAll(fields.map { it.path }) }
            }
        })
    }

    suspend fun deletePoi(id: Long) {
        backendCall { stub().deletePoi(deletePoiRequest { this.id = id }) }
    }

    /**
     * Nearest first within [radiusMeters] of [near], or by name inside
     * [boundingBox], or all; optionally matching part of the name. [sources]:
     * the user's POIs (the backend's default when empty) and/or the map's
     * (OpenStreetMap, read-only); [categories] only finds map POIs.
     */
    suspend fun searchPois(
        near: PbLatLng? = null,
        radiusMeters: Double = 0.0,
        boundingBox: BoundingBox? = null,
        nameContains: String = "",
        visibleOnly: Boolean = false,
        limit: Int = 0,
        sources: Collection<PoiSource> = emptyList(),
        categories: Collection<PoiCategory> = emptyList(),
    ): List<PoiResult> {
        require(near == null || boundingBox == null) { "near or boundingBox, not both" }
        return backendCall {
            stub().searchPois(searchPoisRequest {
            if (near != null) {
                this.near = SearchPoisRequestKt.near {
                    center = near
                    this.radiusMeters = radiusMeters
                }
            }
            if (boundingBox != null) this.boundingBox = boundingBox
            this.nameContains = nameContains
            this.visibleOnly = visibleOnly
            this.limit = limit
            this.sources.addAll(sources)
            this.categories.addAll(categories)
        }).resultsList
        }
    }
}
