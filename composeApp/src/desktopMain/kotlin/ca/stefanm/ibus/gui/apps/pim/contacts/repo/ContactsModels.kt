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
