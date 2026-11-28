package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Address
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Candidate
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Precision
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import javax.inject.Inject

/**
 * Geocoding from a few sample places, for working on the screens and for the
 * UI tests without a maps backend (E39Config.MapConfig.Geocode.useFakeData).
 *
 * - The sample contact Anna Kowalski's "123 Main St, Regina": the address
 *   ([Precision.ADDRESS]), one candidate.
 * - "Main St" without a town: Main Street in Regina and in Ottawa ([Precision.STREET]).
 * - A known street with an unknown house number: the street.
 * - Only a town ("Ottawa", "Kanata", "Regina"): the town ([Precision.PLACE]);
 *   only a postal code ("S4P 1A1"): its centre ([Precision.POSTCODE]).
 * - Anything else: no candidates.
 *
 * [noGeocodingData] makes it fail as a map without geocoding data does.
 */
class FakeGeocodeRepository @Inject constructor() : GeocodeRepository {

    @Volatile
    var noGeocodingData = false

    private val places = listOf(
        Candidate("123 Main Street, Regina", LatLng(50.4501, -104.6178), Precision.ADDRESS, 1.0,
            "Main Street", "123", "Regina", "S4P 1A1"),
        Candidate("Main Street, Regina", LatLng(50.4470, -104.6180), Precision.STREET, 0.6,
            "Main Street", "", "Regina", ""),
        Candidate("Main Street, Ottawa", LatLng(45.4086, -75.6795), Precision.STREET, 0.6,
            "Main Street", "", "Ottawa", ""),
        Candidate("110 Bank Street, Ottawa", LatLng(45.4210, -75.6993), Precision.ADDRESS, 1.0,
            "Bank Street", "110", "Ottawa", "K1P 5N7"),
        Candidate("Bank Street, Ottawa", LatLng(45.4100, -75.6930), Precision.STREET, 0.6,
            "Bank Street", "", "Ottawa", ""),
        Candidate("Regina", LatLng(50.4452, -104.6189), Precision.PLACE, 0.4, "", "", "Regina", ""),
        Candidate("Ottawa", LatLng(45.4215, -75.6972), Precision.PLACE, 0.4, "", "", "Ottawa", ""),
        Candidate("Kanata", LatLng(45.3088, -75.8987), Precision.PLACE, 0.4, "", "", "Kanata", ""),
        Candidate("S4P 1A1", LatLng(50.4495, -104.6150), Precision.POSTCODE, 0.3, "", "", "Regina", "S4P 1A1"),
    )

    override suspend fun geocode(query: String, address: Address?, near: LatLng?, limit: Int): Result<List<Candidate>> =
        runCatching {
            if (noGeocodingData) throw GeocodeRepository.NoGeocodingData("this map has no geocoding data (sample)")
            val asked = address?.takeUnless { it.isEmpty } ?: parse(query)
            if (asked.isEmpty) throw MapsBackendException.InvalidArgument("give a query or an address", null)
            matches(asked)
                .sortedWith(compareByDescending<Candidate> { it.score }
                    .thenBy { c -> near?.let { LatLngTool.distance(it, c.location, LengthUnit.METER) } ?: 0.0 })
                .take(if (limit <= 0) GeocodeRepository.DEFAULT_LIMIT else limit)
        }

    private fun matches(a: Address): List<Candidate> {
        val (number, street) = splitNumber(a.houseNumber, a.street)
        val locality = a.locality.trim()
        fun sameLocality(c: Candidate) = locality.isEmpty() || c.locality.equals(locality, ignoreCase = true)
        return when {
            street.isNotEmpty() -> {
                val onStreet = places.filter {
                    (it.precision == Precision.ADDRESS || it.precision == Precision.STREET) &&
                            normalizeStreet(it.street) == street && sameLocality(it)
                }
                onStreet.filter { it.precision == Precision.ADDRESS && it.houseNumber == number }
                    .ifEmpty { onStreet.filter { it.precision == Precision.STREET } }
            }
            locality.isNotEmpty() -> places.filter { it.precision == Precision.PLACE && sameLocality(it) }
            a.postalCode.isNotBlank() -> places.filter {
                it.precision == Precision.POSTCODE && it.postalCode.replace(" ", "")
                    .equals(a.postalCode.replace(" ", ""), ignoreCase = true)
            }
            else -> emptyList()
        }
    }

    /** "123 Main St, Regina" -> street "123 Main St", locality "Regina"; "K1P 1J1" alone is a postal code. */
    private fun parse(query: String): Address {
        val parts = query.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size == 1 && POSTAL_CODE.matches(parts[0])) return Address(postalCode = parts[0])
        if (parts.size == 1 && parts[0].none { it.isDigit() } && places.any { it.locality.equals(parts[0], true) }) {
            return Address(locality = parts[0])
        }
        return Address(street = parts.getOrElse(0) { "" }, locality = parts.getOrElse(1) { "" })
    }

    private fun splitNumber(houseNumber: String, street: String): Pair<String, String> {
        val m = LEADING_NUMBER.find(street.trim())
        val number = houseNumber.trim().ifEmpty { m?.groupValues?.get(1) ?: "" }
        val name = if (m != null) street.trim().substring(m.range.last + 1) else street
        return number to normalizeStreet(name)
    }

    private fun normalizeStreet(street: String): String =
        street.lowercase().replace(".", "").split(Regex("\\s+")).filter { it.isNotEmpty() }
            .joinToString(" ") { ABBREVIATIONS[it] ?: it }

    private companion object {
        val LEADING_NUMBER = Regex("^(\\d+[a-zA-Z]?)\\s+")
        val POSTAL_CODE = Regex("^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$")
        val ABBREVIATIONS = mapOf("st" to "street", "ave" to "avenue", "rd" to "road", "dr" to "drive", "blvd" to "boulevard")
    }
}
