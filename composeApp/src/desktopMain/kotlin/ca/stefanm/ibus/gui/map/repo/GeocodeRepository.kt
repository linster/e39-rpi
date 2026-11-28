package ca.stefanm.ibus.gui.map.repo

import com.javadocmd.simplelatlng.LatLng

/**
 * An address to positions on the map (#59): for navigating to a contact's
 * address, and later a typed destination or a calendar event's location.
 *
 * [RemoteGeocodeRepository] asks the maps backend (e39maps.v1.GeocodeService,
 * offline, from the installed map); [FakeGeocodeRepository] answers from a few
 * sample places. [GeocodeModule] picks one (E39Config.MapConfig.Geocode.useFakeData).
 */
interface GeocodeRepository {

    /** An address in parts, as a vCard ADR (a contact's postal address) has them. All optional. */
    data class Address(
        /** "123 Main St"; the house number may be here or in [houseNumber]. */
        val street: String = "",
        val houseNumber: String = "",
        /** City, town or village. */
        val locality: String = "",
        /** Province or state ("ON"). */
        val region: String = "",
        val postalCode: String = "",
        val country: String = "",
    ) {
        val isEmpty: Boolean get() = listOf(street, houseNumber, locality, region, postalCode, country).all { it.isBlank() }
    }

    /** How exactly a [Candidate] is the address asked for, most exact first. */
    enum class Precision {
        /** The address itself (an address point or building). */
        ADDRESS,
        /** Between the street's known house numbers. */
        INTERPOLATED,
        /** The street; not the house. */
        STREET,
        /** A city, town, village or neighbourhood. */
        PLACE,
        /** The centre of a postal code's addresses. */
        POSTCODE,
        /** A newer backend's kind this HMI doesn't know. */
        UNKNOWN;

        /** The address itself, near enough to drive to without asking. */
        val isExact: Boolean get() = this == ADDRESS || this == INTERPOLATED
    }

    data class Candidate(
        /** For showing, e.g. "123 Bank Street, Ottawa". */
        val label: String,
        val location: LatLng,
        val precision: Precision,
        /** Higher is better; only for comparing the candidates of one answer. */
        val score: Double,
        val street: String,
        val houseNumber: String,
        val locality: String,
        /** Formatted ("K1S 3V8"), when known. */
        val postalCode: String,
    )

    /**
     * The installed map can't look addresses up: it has no geocoding data (built
     * before e39-rpi-maps#6) or there's no map. Not the same as the backend being
     * unreachable ([MapsBackendException.Unavailable]).
     */
    class NoGeocodingData(message: String) : Exception(message)

    /**
     * Candidates for an address, best first, from free text ([query]), its parts
     * ([address]; they win), or both. [near]: prefer candidates near here (the
     * car's position). No match is an empty list.
     *
     * Fails with [NoGeocodingData], or a [MapsBackendException]: Unavailable (not
     * reachable), InvalidArgument (nothing to look up), ...
     */
    suspend fun geocode(
        query: String = "",
        address: Address? = null,
        near: LatLng? = null,
        limit: Int = DEFAULT_LIMIT,
    ): Result<List<Candidate>>

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
