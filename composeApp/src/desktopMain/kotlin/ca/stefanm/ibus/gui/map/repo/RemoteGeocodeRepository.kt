package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.e39maps.v1.GeocodeCandidate
import ca.stefanm.e39maps.v1.GeocodePrecision
import ca.stefanm.e39maps.v1.geocodeRequest
import ca.stefanm.e39maps.v1.structuredAddress
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Address
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Candidate
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Precision
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toLatLng
import ca.stefanm.ibus.gui.map.repo.PoiMapper.toPb
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject

/** Geocoding by the maps backend, from the installed map's data (offline). */
class RemoteGeocodeRepository @Inject constructor(
    private val client: GeocodeGrpcClient,
) : GeocodeRepository {

    override suspend fun geocode(query: String, address: Address?, near: LatLng?, limit: Int): Result<List<Candidate>> =
        runCatching {
            client.geocode(geocodeRequest {
                this.query = query
                if (address != null) this.address = address.toPb()
                if (near != null) this.near = near.toPb()
                this.limit = limit
            }).candidatesList.map { it.toCandidate() }
        }

    internal companion object {
        fun Address.toPb() = structuredAddress {
            street = this@toPb.street
            houseNumber = this@toPb.houseNumber
            locality = this@toPb.locality
            region = this@toPb.region
            postalCode = this@toPb.postalCode
            country = this@toPb.country
        }

        fun GeocodeCandidate.toCandidate() = Candidate(
            label = label,
            location = location.toLatLng(),
            precision = precision.toPrecision(),
            score = score,
            street = street,
            houseNumber = houseNumber,
            locality = locality,
            postalCode = postalCode,
        )

        fun GeocodePrecision.toPrecision(): Precision = when (this) {
            GeocodePrecision.GEOCODE_PRECISION_ADDRESS -> Precision.ADDRESS
            GeocodePrecision.GEOCODE_PRECISION_INTERPOLATED -> Precision.INTERPOLATED
            GeocodePrecision.GEOCODE_PRECISION_STREET -> Precision.STREET
            GeocodePrecision.GEOCODE_PRECISION_PLACE -> Precision.PLACE
            GeocodePrecision.GEOCODE_PRECISION_POSTCODE -> Precision.POSTCODE
            GeocodePrecision.GEOCODE_PRECISION_UNSPECIFIED, GeocodePrecision.UNRECOGNIZED -> Precision.UNKNOWN
        }
    }
}
