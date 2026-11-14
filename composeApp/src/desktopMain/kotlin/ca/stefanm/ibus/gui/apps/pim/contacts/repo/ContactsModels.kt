package ca.stefanm.ibus.gui.pim.contacts.repo

import kotlinx.datetime.LocalDate

data class AddressBook(
    val id: String,
    val accountId: String,
    val accountName: String,
    val name: String,
    val readOnly: Boolean,
    val contactCount: Long,
)

/** What lists show. */
data class ContactSummary(
    val id: String,
    val addressBookId: String,
    /** "Anna Kowalski", else the organization, nickname, e-mail or number. */
    val displayName: String,
    /** Family name first, upper case, no accents ("KOWALSKI ANNA"): for sorting and A–Z jumps. */
    val sortKey: String,
    val givenName: String,
    val familyName: String,
    val organization: String,
    /** The preferred (else first) number and address; empty if none. */
    val primaryPhone: String,
    val primaryEmail: String,
    val hasPhoto: Boolean,
    /** Changes with every change; give it back when editing (1 or more). */
    val revision: Long = 0,
) {
    /** The letter for A–Z jumps ('#' for anything that isn't a letter). */
    val initial: Char get() = sortKey.firstOrNull()?.takeIf { it.isLetter() } ?: '#'
}

data class PhoneNumber(
    /** As stored ("(306) 555-0123"). */
    val number: String,
    /** "+13065550123", or empty if it couldn't be worked out (no area code). */
    val e164: String,
    val type: Type,
    val label: String,
    val preferred: Boolean,
) {
    enum class Type { MOBILE, HOME, WORK, MAIN, FAX, PAGER, CAR, OTHER }

    /** Where to dial: E.164 if known, else as stored. */
    val dialable: String get() = e164.ifEmpty { number }
}

data class EmailAddress(val address: String, val type: Type, val preferred: Boolean) {
    enum class Type { HOME, WORK, OTHER }
}

data class PostalAddress(
    val type: Type,
    val street: String,
    val locality: String,
    val region: String,
    val postalCode: String,
    val country: String,
    /** One line, for showing and geocoding. */
    val formatted: String,
    val label: String,
) {
    enum class Type { HOME, WORK, OTHER }
}

data class Contact(
    val summary: ContactSummary,
    val nickname: String,
    val title: String,
    val phones: List<PhoneNumber>,
    val emails: List<EmailAddress>,
    val addresses: List<PostalAddress>,
    /** Null if unknown; year 0 if only the day is known (LocalDate can't say "no year"). */
    val birthday: LocalDate?,
    val note: String,
    val urls: List<String>,
)

/**
 * What can be written: a new contact, or the [ContactField]s of an edit.
 * Phone numbers as entered (their e164 is ignored); addresses' formatted is
 * made from the parts.
 */
data class ContactInput(
    val givenName: String = "",
    val familyName: String = "",
    val organization: String = "",
    val nickname: String = "",
    val title: String = "",
    val phones: List<PhoneNumber> = emptyList(),
    val emails: List<EmailAddress> = emptyList(),
    val addresses: List<PostalAddress> = emptyList(),
    val birthday: LocalDate? = null,
    val note: String = "",
    val urls: List<String> = emptyList(),
) {
    companion object {
        /** An edit form starting from what a contact has. */
        fun from(contact: Contact) = ContactInput(
            givenName = contact.summary.givenName,
            familyName = contact.summary.familyName,
            organization = contact.summary.organization,
            nickname = contact.nickname,
            title = contact.title,
            phones = contact.phones,
            emails = contact.emails,
            addresses = contact.addresses,
            birthday = contact.birthday,
            note = contact.note,
            urls = contact.urls,
        )

        /** "Save this caller": a contact with just a number. */
        fun forNumber(number: String, type: PhoneNumber.Type = PhoneNumber.Type.MOBILE, givenName: String = "", familyName: String = "") =
            ContactInput(givenName = givenName, familyName = familyName, phones = listOf(PhoneNumber(number, "", type, "", preferred = true)))
    }
}

/** The fields of an edit ([ContactsRepository.updateContact]); the protobuf field names. */
enum class ContactField(val path: String) {
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    ORGANIZATION("organization"),
    NICKNAME("nickname"),
    TITLE("title"),
    PHONES("phones"),
    EMAILS("emails"),
    ADDRESSES("addresses"),
    BIRTHDAY("birthday"),
    NOTE("note"),
    URLS("urls"),
}

/** A page or search result: [contacts] out of [totalCount] that match. */
data class ContactList(
    val contacts: List<ContactSummary>,
    val totalCount: Long,
) {
    val hasMore: Boolean get() = contacts.size < totalCount
}

/** Who has a phone number (caller names). */
data class PhoneMatch(
    val contact: ContactSummary,
    /** The contact's number that matched. */
    val phone: PhoneNumber,
    /** The same full number; false for weaker matches (a number stored without its area code). */
    val exact: Boolean,
)

/** A contact's picture, JPEG. */
class ContactPhoto(val jpeg: ByteArray, val width: Int, val height: Int)
