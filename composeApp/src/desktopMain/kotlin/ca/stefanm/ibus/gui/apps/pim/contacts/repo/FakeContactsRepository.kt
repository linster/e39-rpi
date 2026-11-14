package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.text.Normalizer
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * Sample contacts in memory, for building screens without the PIM bridge
 * (E39Config.PimConfig.Contacts.useFakeData). Two address books; search and
 * caller lookup work like the bridge's (phone numbers compared by digits:
 * the same 10 = exact, the same last 7 = weak). Pictures are drawn
 * initials.
 */
@ApplicationScope
class FakeContactsRepository @Inject constructor() : ContactsRepository {

    private val books = listOf(
        AddressBook("b1", "fake_home", "Home server", "Personal", readOnly = false, contactCount = 0),
        AddressBook("b2", "fake_google", "Google", "Contacts", readOnly = false, contactCount = 0),
    )

    private val all = MutableStateFlow(sampleContacts())

    override fun addressBooks(): Flow<List<AddressBook>> = all.map { contacts ->
        books.map { b -> b.copy(contactCount = contacts.count { it.summary.addressBookId == b.id }.toLong()) }
    }

    override fun contacts(limit: Int, addressBookId: String?): Flow<ContactList> {
        require(limit in 1..ContactsRepository.MAX_CONTACTS) { "limit $limit not in 1..${ContactsRepository.MAX_CONTACTS}" }
        return all.map { contacts ->
            val matching = sorted(contacts).filter { addressBookId == null || it.summary.addressBookId == addressBookId }
            ContactList(matching.take(limit).map { it.summary }, matching.size.toLong())
        }
    }

    override suspend fun getContact(contactId: String): Result<Contact> = attempt { find(contactId) }

    override suspend fun search(text: String, addressBookId: String?, limit: Int): Result<ContactList> = attempt {
        require(text.isNotBlank()) { "Nothing to search for" }
        val folded = fold(text.trim())
        val digits = if (text.all { it.isDigit() || it in " ()+.-" }) digitsOf(text) else ""
        val hits = sorted(all.value)
            .filter { addressBookId == null || it.summary.addressBookId == addressBookId }
            .filter { c ->
                val s = c.summary
                (listOf(s.displayName, s.givenName, s.familyName, s.organization, c.nickname) + c.emails.map { it.address })
                    .any { fold(it).contains(folded) } ||
                    (digits.length >= 3 && c.phones.any { digitsOf(it.number).contains(digits) })
            }
        ContactList(hits.take(limit).map { it.summary }, hits.size.toLong())
    }

    override suspend fun findByPhoneNumber(number: String): Result<List<PhoneMatch>> = attempt {
        require(number.any { it.isDigit() }) { "Not a phone number: $number" }
        val wanted = digitsOf(number)
        sorted(all.value).mapNotNull { c ->
            val matches = c.phones.mapNotNull { p ->
                val have = digitsOf(p.number)
                when {
                    wanted.length >= 10 && have.length >= 10 && wanted.takeLast(10) == have.takeLast(10) -> PhoneMatch(c.summary, p, exact = true)
                    wanted.length >= 7 && have.length >= 7 && wanted.takeLast(7) == have.takeLast(7) -> PhoneMatch(c.summary, p, exact = false)
                    else -> null
                }
            }
            matches.firstOrNull { it.exact } ?: matches.firstOrNull()
        }.sortedByDescending { it.exact }
    }

    override suspend fun getPhoto(contactId: String, maxSize: Int): Result<ContactPhoto> = attempt {
        require(maxSize in 16..1024) { "maxSize $maxSize not in 16..1024" }
        val contact = find(contactId)
        if (!contact.summary.hasPhoto) throw PimBackendException.NotFound("${contact.summary.displayName} has no picture", null)
        val image = BufferedImage(maxSize, maxSize, BufferedImage.TYPE_INT_RGB)
        with(image.createGraphics()) {
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            color = Color.getHSBColor((contact.summary.sortKey.hashCode() and 0xff) / 255f, 0.5f, 0.7f)
            fillRect(0, 0, maxSize, maxSize)
            color = Color.WHITE
            font = Font(Font.SANS_SERIF, Font.BOLD, maxSize / 2)
            val initials = (contact.summary.givenName.take(1) + contact.summary.familyName.take(1)).ifEmpty { contact.summary.displayName.take(1) }
            val metrics = fontMetrics
            drawString(initials, (maxSize - metrics.stringWidth(initials)) / 2, (maxSize - metrics.height) / 2 + metrics.ascent)
            dispose()
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", out)
        ContactPhoto(out.toByteArray(), maxSize, maxSize)
    }

    private fun find(contactId: String): Contact =
        all.value.firstOrNull { it.summary.id == contactId } ?: throw PimBackendException.NotFound("No contact $contactId", null)

    private fun sorted(contacts: List<Contact>) =
        contacts.sortedWith(compareBy({ it.summary.sortKey }, { it.summary.displayName }, { it.summary.id }))

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }

    private fun digitsOf(text: String) = text.filter { it.isDigit() }

    private fun fold(text: String) =
        Normalizer.normalize(text, Normalizer.Form.NFKD).replace(Regex("\\p{Mn}"), "").lowercase()

    private fun sampleContacts(): List<Contact> {
        var id = 0
        fun contact(
            book: String, given: String, family: String, organization: String = "",
            phones: List<Pair<String, PhoneNumber.Type>> = emptyList(), email: String = "",
            address: PostalAddress? = null, photo: Boolean = false, nickname: String = "",
            title: String = "", birthday: LocalDate? = null, note: String = "",
        ): Contact {
            val name = "$given $family".trim().ifEmpty { organization.ifEmpty { email } }
            val sortKey = fold(if (family.isEmpty()) name else "$family $given").uppercase().trim()
            val phoneNumbers = phones.mapIndexed { i, (number, type) ->
                val digits = digitsOf(number)
                val e164 = when {
                    number.startsWith("+") -> "+$digits"
                    digits.length == 10 -> "+1$digits"
                    digits.length == 11 && digits.startsWith("1") -> "+$digits"
                    else -> ""
                }
                PhoneNumber(number, e164, type, "", preferred = i == 0)
            }
            return Contact(
                summary = ContactSummary(
                    id = "c${++id}", addressBookId = book, displayName = name, sortKey = sortKey,
                    givenName = given, familyName = family, organization = organization,
                    primaryPhone = phoneNumbers.firstOrNull()?.number ?: "", primaryEmail = email, hasPhoto = photo,
                ),
                nickname = nickname, title = title, phones = phoneNumbers,
                emails = if (email.isEmpty()) emptyList() else listOf(EmailAddress(email, EmailAddress.Type.HOME, true)),
                addresses = listOfNotNull(address), birthday = birthday, note = note, urls = emptyList(),
            )
        }
        fun home(street: String, city: String, region: String, postal: String) =
            PostalAddress(PostalAddress.Type.HOME, street, city, region, postal, "Canada", "$street, $city $region $postal, Canada", "")

        return listOf(
            contact("b1", "Anna", "Kowalski", phones = listOf("+1 306 555 0123" to PhoneNumber.Type.MOBILE, "(306) 555-0177" to PhoneNumber.Type.HOME),
                email = "anna@example.com", address = home("123 Main St", "Regina", "SK", "S4P 1A1"), photo = true, nickname = "Annie",
                birthday = LocalDate(1990, 5, 17)),
            contact("b1", "René", "Lévesque", organization = "Garage Lévesque", phones = listOf("306-555-0188" to PhoneNumber.Type.WORK),
                email = "rene@example.fr", title = "Mechanic"),
            contact("b1", "", "", organization = "Prairie Euro Garage", phones = listOf("555-0199" to PhoneNumber.Type.WORK)),
            contact("b1", "Zoë", "Adams", phones = listOf("+44 7700 900123" to PhoneNumber.Type.MOBILE), email = "zoe@example.co.uk", photo = true),
            contact("b2", "Bob", "Ng", phones = listOf("3065550123" to PhoneNumber.Type.MOBILE, "306 555 0100" to PhoneNumber.Type.FAX),
                note = "Shares a phone with Anna."),
            contact("b2", "Dana", "Whitford", organization = "Work", phones = listOf("+1 403 555 0142" to PhoneNumber.Type.WORK),
                email = "dana@work.example.com", title = "Manager", photo = true),
            contact("b2", "Carl", "Carlson", phones = listOf("+1 780 555 0161" to PhoneNumber.Type.MOBILE),
                address = home("9 Jasper Ave", "Edmonton", "AB", "T5J 0A1")),
            contact("b2", "", "", email = "someone@example.org"),
        )
    }
}
