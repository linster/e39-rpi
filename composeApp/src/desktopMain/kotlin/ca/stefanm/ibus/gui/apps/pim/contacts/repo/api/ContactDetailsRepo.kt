package ca.stefanm.ibus.gui.pim.contacts.repo.api

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.Contact
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneNumber
import ca.stefanm.ibus.gui.pim.contacts.repo.PostalAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/** A line of a contact's details: [label] ("Mobile ★", "E-mail") and [value]. */
data class DetailRow(
    val kind: Kind,
    val label: String,
    val value: String,
    /** For e-mail rows: the address (to write to). */
    val email: EmailAddress? = null,
    /** For address rows: the address (to navigate to). */
    val address: PostalAddress? = null,
) {
    enum class Kind { PHONE, EMAIL, ADDRESS, BIRTHDAY, ORGANIZATION, NICKNAME, NOTE, URL }
}

/** A contact as the details screen shows it. */
data class ContactDetails(
    val contact: Contact,
    /** Title and organization, e.g. "Mechanic, Garage Lévesque". */
    val subtitle: String,
    val rows: List<DetailRow>,
    val addressBook: AddressBook?,
) {
    val name: String get() = contact.summary.displayName.ifBlank { "(no name)" }
}

/** One contact's details, picture, and what can be done with them. */
@ApplicationScope
class ContactDetailsRepo @Inject constructor(
    private val contacts: ContactsRepository,
) {
    suspend fun open(contactId: String): Result<ContactDetails> = contacts.getContact(contactId).map { c ->
        val book = contacts.addressBooks().first().firstOrNull { it.id == c.summary.addressBookId }
        ContactDetails(c, subtitleOf(c), rowsOf(c), book)
    }

    /** The picture (JPEG, scaled to fit [maxSize]), or null if there's none. */
    suspend fun photo(details: ContactDetails, maxSize: Int = 160): ByteArray? =
        if (!details.contact.summary.hasPhoto) null else contacts.getPhoto(details.contact.summary.id, maxSize).getOrNull()?.jpeg

    /** Whom "Write Mail" addresses. */
    fun mailRecipient(details: ContactDetails, email: EmailAddress) = MailAddress(details.contact.summary.displayName, email.address)

    companion object {
        private val MONTHS = listOf("January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December")

        fun subtitleOf(c: Contact): String {
            val org = c.summary.organization.takeIf { it.isNotBlank() && it != c.summary.displayName }
            return listOfNotNull(c.title.takeIf { it.isNotBlank() }, org).joinToString(", ")
        }

        fun phoneLabel(p: PhoneNumber): String = (p.label.ifBlank {
            when (p.type) {
                PhoneNumber.Type.MOBILE -> "Mobile"
                PhoneNumber.Type.HOME -> "Home"
                PhoneNumber.Type.WORK -> "Work"
                PhoneNumber.Type.MAIN -> "Main"
                PhoneNumber.Type.FAX -> "Fax"
                PhoneNumber.Type.PAGER -> "Pager"
                PhoneNumber.Type.CAR -> "Car"
                PhoneNumber.Type.OTHER -> "Phone"
            }
        }) + if (p.preferred) " ★" else ""

        fun emailLabel(e: EmailAddress): String = when (e.type) {
            EmailAddress.Type.HOME -> "E-mail (home)"
            EmailAddress.Type.WORK -> "E-mail (work)"
            EmailAddress.Type.OTHER -> "E-mail"
        } + if (e.preferred) " ★" else ""

        fun addressLabel(a: PostalAddress): String = a.label.ifBlank {
            when (a.type) {
                PostalAddress.Type.HOME -> "Home address"
                PostalAddress.Type.WORK -> "Work address"
                PostalAddress.Type.OTHER -> "Address"
            }
        }

        /** "123 Main St\nRegina SK S4P 1A1\nCanada" (the parts, else the formatted text). */
        fun addressText(a: PostalAddress): String {
            val city = listOf(a.locality, a.region, a.postalCode).filter { it.isNotBlank() }.joinToString(" ")
            val lines = listOf(a.street, city, a.country).filter { it.isNotBlank() }
            return if (lines.isEmpty()) a.formatted else lines.joinToString("\n")
        }

        fun birthdayText(d: LocalDate): String = "${MONTHS[d.month.ordinal]} ${d.day}, ${d.year}"

        fun rowsOf(c: Contact): List<DetailRow> = buildList {
            c.phones.forEach { add(DetailRow(DetailRow.Kind.PHONE, phoneLabel(it), it.number)) }
            c.emails.forEach { add(DetailRow(DetailRow.Kind.EMAIL, emailLabel(it), it.address, email = it)) }
            c.addresses.forEach { add(DetailRow(DetailRow.Kind.ADDRESS, addressLabel(it), addressText(it), address = it)) }
            c.birthday?.let { add(DetailRow(DetailRow.Kind.BIRTHDAY, "Birthday", birthdayText(it))) }
            if (c.nickname.isNotBlank()) add(DetailRow(DetailRow.Kind.NICKNAME, "Nickname", c.nickname))
            c.urls.forEach { add(DetailRow(DetailRow.Kind.URL, "Web", it)) }
            if (c.note.isNotBlank()) add(DetailRow(DetailRow.Kind.NOTE, "Note", c.note))
        }
    }
}
