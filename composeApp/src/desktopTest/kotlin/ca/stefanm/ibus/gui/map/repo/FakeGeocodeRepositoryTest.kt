package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Address
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository.Precision
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The sample answers the screens and UI tests rely on. */
class FakeGeocodeRepositoryTest {

    private val fake = FakeGeocodeRepository()

    private fun labels(query: String = "", address: Address? = null, near: LatLng? = null) =
        runBlocking { fake.geocode(query, address, near).getOrThrow() }.map { it.label to it.precision }

    /** As FakeContactsRepository has Anna Kowalski's address. */
    private val anna = Address("123 Main St", "", "Regina", "SK", "S4P 1A1", "Canada")

    @Test
    fun annasAddressIsOneExactCandidate() {
        assertEquals(listOf("123 Main Street, Regina" to Precision.ADDRESS), labels(address = anna))
        assertEquals(listOf("123 Main Street, Regina" to Precision.ADDRESS), labels(query = "123 Main St, Regina"))
    }

    @Test
    fun aStreetInTwoTownsIsTwoStreets() {
        val streets = labels(address = Address(street = "Main St"))
        assertEquals(setOf("Main Street, Regina" to Precision.STREET, "Main Street, Ottawa" to Precision.STREET), streets.toSet())
        // The nearer first.
        assertEquals("Main Street, Ottawa", labels(address = Address(street = "Main St"), near = LatLng(45.3, -75.9))[0].first)
        assertEquals("Main Street, Regina", labels(address = Address(street = "Main St"), near = LatLng(50.4, -104.6))[0].first)
    }

    @Test
    fun anUnknownHouseNumberIsTheStreet() {
        assertEquals(listOf("Bank Street, Ottawa" to Precision.STREET),
            labels(address = Address(street = "999 Bank St.", locality = "Ottawa")))
    }

    @Test
    fun aTownOrAPostalCode() {
        assertEquals(listOf("Kanata" to Precision.PLACE), labels(address = Address(locality = "Kanata")))
        assertEquals(listOf("Ottawa" to Precision.PLACE), labels(query = "Ottawa"))
        assertEquals(listOf("S4P 1A1" to Precision.POSTCODE), labels(query = "s4p1a1"))
    }

    @Test
    fun anythingElseIsNoCandidates() {
        assertEquals(emptyList<Pair<String, Precision>>(), labels(query = "1 Nowhere Lane, Atlantis"))
    }

    @Test
    fun nothingToLookUpIsInvalidArgument() {
        val e = runBlocking { fake.geocode() }.exceptionOrNull()
        assertTrue(e.toString(), e is MapsBackendException.InvalidArgument)
    }

    @Test
    fun noGeocodingData() {
        fake.noGeocodingData = true
        val e = runBlocking { fake.geocode(address = anna) }.exceptionOrNull()
        assertTrue(e.toString(), e is GeocodeRepository.NoGeocodingData)
    }
}
